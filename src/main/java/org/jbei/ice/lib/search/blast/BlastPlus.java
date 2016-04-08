package org.jbei.ice.lib.search.blast;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FeatureDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Feature;
import org.jbei.ice.storage.model.Sequence;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Blast Search functionality for BLAST+
 *
 * @author Hector Plahar
 */
public class BlastPlus {

    private static final String BLAST_DB_FOLDER = "blast";
    private static final String BLAST_DB_NAME = "ice";
    private static final String DELIMITER = ",";
    private static final String LOCK_FILE_NAME = "write.lock";
    private static final String AUTO_ANNOTATION_FOLDER_NAME = "auto-annotation";

    public static String runBlastQuery(String dbFolder, BlastQuery query, String... options) throws BlastException {
        try {
            String command = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + query.getBlastProgram().getName();
            String blastDb = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), dbFolder,
                    BLAST_DB_NAME).toString();
            if (!Files.exists(Paths.get(blastDb + ".nsq"))) {
                return "";
            }

            String[] blastCommand = new String[3 + options.length];
            blastCommand[0] = command;
            blastCommand[1] = "-db";
            blastCommand[2] = blastDb;
            System.arraycopy(options, 0, blastCommand, 3, options.length);

            Process process = Runtime.getRuntime().exec(blastCommand);
            ProcessResultReader reader = new ProcessResultReader(process.getInputStream(), "STD_OUT");
            ProcessResultReader error = new ProcessResultReader(process.getInputStream(), "STD_ERR");
            reader.start();
            BufferedWriter programInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            programInputWriter.write(query.getSequence());
            programInputWriter.flush();
            programInputWriter.close();
            process.getOutputStream().close();

            //TODO this should go into the thread itself & have future wait on it
            final int exitValue = process.waitFor();
            switch (exitValue) {
                case 0:
                    return reader.toString();

                case 1:
                    Logger.error("Error in query sequence(s) or BLAST options: " + error.toString());
                    break;

                case 2:
                    Logger.error("Error in BLAST database: " + error.toString());
                    break;

                default:
                    Logger.error("Unknown exit value " + exitValue);
            }
            return null;
        } catch (Exception e) {
            Logger.error(e);
            throw new BlastException(e);
        }
    }

    /**
     * Run a blast query using the following output format options
     * <ul>
     * <li><code>stitle</code> - subject title</li>
     * <li><code>qstart</code> - query start</li>
     * <li><code>qend</code></li>
     * <li><code>sstart</code></li>
     * <li><code>send</code></li>
     * <li><code>sstrand</code></li>
     * <li><code>evalue</code></li>
     * <li><code>bitscore</code></li>
     * <li><code>length</code> - alignment length</li>
     * <li><code>nident</code> - number of identical matches</li>
     * </ul>
     *
     * @param query wrapper around blast query
     * @return map of unique entry identifier (whose sequence was a subject) to the search result hit details
     * @throws BlastException
     */
    public static HashMap<String, SearchResult> runBlast(BlastQuery query) throws BlastException {
        String result = runBlastQuery(BLAST_DB_FOLDER, query, "-perc_identity", "95", "-outfmt",
                "10 stitle qstart qend sstart send sstrand evalue bitscore score length nident");
        if (result == null)
            throw new BlastException("Exception running blast");
        return processBlastOutput(result, query.getSequence().length());
    }

    public static List<DNAFeature> runCheckFeatures(BlastQuery query) throws BlastException {
        String result = runBlastQuery(AUTO_ANNOTATION_FOLDER_NAME, query, "-perc_identity", "100",
                "-outfmt", "10 stitle qstart qend sstart send sstrand");
        if (result == null)
            throw new BlastException("Exception running blast");
        return processFeaturesBlastOutput(result);
    }

    public static List<DNAFeature> processFeaturesBlastOutput(String blastOutput) {
        List<DNAFeature> hashMap = new ArrayList<>();
        HashSet<String> duplicates = new HashSet<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                String type = line[2];
                String start = line[4];
                String end = line[5];
                if (!duplicates.add(type + ":" + start + ":" + end)) {
                    continue;
                }

                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setId(Long.decode(line[0]));
                dnaFeature.setName(line[1]);
                dnaFeature.setType(type);
                dnaFeature.setIdentifier(line[3]);
                DNAFeatureLocation location = new DNAFeatureLocation();
                location.setGenbankStart(Integer.decode(start));
                location.setEnd(Integer.decode(end));
                dnaFeature.getLocations().add(location);
                dnaFeature.setStrand("plus".equalsIgnoreCase(line[8]) ? 1 : -1);
                hashMap.add(dnaFeature);
            }

            return hashMap;
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }

    private static SearchResult parseBlastOutputLine(String[] line) {

        // extract part information
        PartData view = new PartData(EntryType.nameToType(line[1]));
        view.setId(Long.decode(line[0]));
        view.setName(line[2]);
        view.setPartId(line[3]);
        String summary = DAOFactory.getEntryDAO().getEntrySummary(view.getId());
        view.setShortDescription(summary);

        //search result object
        SearchResult searchResult = new SearchResult();
        searchResult.setEntryInfo(view);
        searchResult.seteValue(line[9]);
        searchResult.setScore(Float.valueOf(line[11]));
        searchResult.setAlignment(line[13]);
        searchResult.setQueryLength(Integer.valueOf(line[12]));
        searchResult.setNident(Integer.valueOf(line[13]));
        return searchResult;
    }

    private static LinkedHashMap<String, SearchResult> processBlastOutput(String blastOutput, int queryLength) {
        LinkedHashMap<String, SearchResult> hashMap = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();
            reader.close();

            for (String[] line : lines) {
                SearchResult info = parseBlastOutputLine(line);

                info.setQueryLength(queryLength);
                String idString = Long.toString(info.getEntryInfo().getId());
                SearchResult currentResult = hashMap.get(idString);
                // if there is an existing record for same entry with a lower relative score then replace
                if (currentResult == null)
                    hashMap.put(idString, info);
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        return hashMap;
    }

    private static boolean blastDatabaseExists() {
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        Path path = FileSystems.getDefault().getPath(dataDir, BLAST_DB_FOLDER, BLAST_DB_NAME + ".nsq");
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    // todo: does not delete for lock file to allow for 4 hours
    public static void rebuildFeaturesBlastDatabase(String featureFolder) throws IOException {
        String blastInstallDir = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR);
        if (StringUtils.isEmpty(blastInstallDir)) {
            Logger.warn("Blast install directory not available. Aborting blast rebuild");
            return;
        }
        Path blastDir = Paths.get(blastInstallDir);
        if (!Files.exists(blastDir))
            throw new IOException("Could not locate Blast installation in " + blastInstallDir);

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        final Path blastFolder = Paths.get(dataDir, featureFolder);
        File lockFile = Paths.get(blastFolder.toString(), LOCK_FILE_NAME).toFile();
        if (lockFile.exists()) {
            if (lockFile.lastModified() <= (System.currentTimeMillis() - (1000 * 60 * 60 * 24)))
                if (!lockFile.delete()) {
                    Logger.warn("Could not delete outdated features blast lockfile. Delete the following file manually: "
                            + lockFile.getAbsolutePath());
                } else {
                    Logger.info("Features blast db locked (lockfile - " + lockFile.getAbsolutePath() + "). Rebuild aborted!");
                    return;
                }
        }

        try {
            if (!Files.exists(blastFolder)) {
                Logger.info("Features blast folder (" + blastFolder.toString() + ") does not exist. Creating...");
                try {
                    Files.createDirectories(blastFolder);
                } catch (Exception e) {
                    Logger.warn("Could not create features blast folder. Create it manually or all blast runs will fail");
                    return;
                }
            }

            if (!lockFile.createNewFile()) {
                Logger.warn("Could not create lock file for features blast rebuild");
                return;
            }

            FileOutputStream fos = new FileOutputStream(lockFile);
            try (FileLock lock = fos.getChannel().tryLock()) {
                if (lock == null)
                    return;
                Logger.info("Rebuilding features blast database");
                rebuildSequenceDatabase(blastDir, blastFolder, true);
                Logger.info("Blast features database rebuild complete");
            }
        } catch (OverlappingFileLockException l) {
            Logger.warn("Could not obtain lock file for blast at " + blastFolder.toString());
        } catch (BlastException be) {
            FileUtils.deleteQuietly(lockFile);
            Logger.error(be);
        }
        FileUtils.deleteQuietly(lockFile);
    }

    public static void rebuildDatabase(boolean force) throws BlastException {
        String blastInstallDir = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR);
        if (StringUtils.isEmpty(blastInstallDir)) {
            Logger.warn("Blast install directory not available. Aborting blast rebuild");
            return;
        }

        Path blastDir = Paths.get(blastInstallDir);
        if (!Files.exists(blastDir))
            throw new BlastException("Could not locate Blast installation in " + blastInstallDir);

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        final Path blastFolder = Paths.get(dataDir, BLAST_DB_FOLDER);
        File lockFile = Paths.get(blastFolder.toString(), LOCK_FILE_NAME).toFile();
        if (lockFile.exists()) {
            if (lockFile.lastModified() <= (System.currentTimeMillis() - (1000 * 60 * 60 * 24)))
                if (!lockFile.delete()) {
                    Logger.warn("Could not delete outdated blast lockfile. Delete the following file manually: "
                            + lockFile.getAbsolutePath());
                } else {
                    Logger.info("Blast db locked (lockfile - " + lockFile.getAbsolutePath() + "). Rebuild aborted!");
                    return;
                }
        }

        try {
            if (!Files.exists(blastFolder)) {
                Logger.info("Blast folder (" + blastFolder.toString() + ") does not exist. Creating...");
                try {
                    Files.createDirectories(blastFolder);
                } catch (Exception e) {
                    Logger.warn("Could not create blast folder. Create it manually or all blast runs will fail");
                    return;
                }
            }

            if (!force && blastDatabaseExists()) {
                Logger.info("Blast database found in " + blastFolder.toAbsolutePath().toString());
                return;
            }

            if (!lockFile.createNewFile()) {
                Logger.warn("Could not create lock file for blast rebuild");
                return;
            }

            FileOutputStream fos = new FileOutputStream(lockFile);
            try (FileLock lock = fos.getChannel().tryLock()) {
                if (lock == null)
                    return;
                Logger.info("Rebuilding blast database");
                rebuildSequenceDatabase(blastDir, blastFolder, false);
                Logger.info("Blast database rebuild complete");
            }
        } catch (OverlappingFileLockException l) {
            Logger.warn("Could not obtain lock file for blast at " + blastFolder.toString());
        } catch (IOException eio) {
            FileUtils.deleteQuietly(lockFile);
            throw new BlastException(eio);
        }
        FileUtils.deleteQuietly(lockFile);
    }

    /**
     * Run the bl2seq program on multiple subjects.
     * <p>
     * This method requires disk space write temporary files. It tries to clean up after itself.
     *
     * @param query   reference sequence.
     * @param subject query sequence.
     * @return List of output string from bl2seq program.
     * @throws BlastException
     * @throws ProgramTookTooLongException
     */
    public static String runBlast2Seq(String query, String subject) throws BlastException, ProgramTookTooLongException {
        String result;
        try {
            Path queryFilePath = Files.write(Files.createTempFile("query-", ".seq"), query.getBytes());
            Path subjectFilePath = Files.write(Files.createTempFile("subject-", ".seq"), subject.getBytes());
            if (queryFilePath == null || subjectFilePath == null)
                throw new BlastException("Subject or query is null");

            StringBuilder command = new StringBuilder();
            String blastN = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + BlastProgram.BLAST_N.getName();
            command.append(blastN)
                    .append(" -query ")
                    .append(queryFilePath.toString())
                    .append(" -subject ")
                    .append(subjectFilePath.toString())
                    .append(" -dust no");

            Logger.info("Blast-2-seq query: " + command.toString());
            result = runSimpleExternalProgram(command.toString());
            Files.deleteIfExists(subjectFilePath);
            Files.deleteIfExists(queryFilePath);
        } catch (IOException e) {
            throw new BlastException(e);
        }

        return result;
    }

    /**
     * Schedule task to rebuild the blast index
     */
    public static void scheduleBlastIndexRebuildTask(boolean force) {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask(force);
        IceExecutorService.getInstance().runTask(task);
    }

    /**
     * Wrapper to run an external program, and collect its output.
     *
     * @param commandString command to run.
     * @return Output string from the program.
     * @throws BlastException
     */
    private static String runSimpleExternalProgram(String commandString) throws BlastException {
        StringBuilder output = new StringBuilder();

        try {
            Process p = Runtime.getRuntime().exec(commandString);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
            }

            input.close();
        } catch (Exception e) {
            throw new BlastException(e);
        }

        return output.toString();
    }

    /**
     * Build the blast database.
     * <p>
     * <p/>First dump the sequences from the sql database into a fasta file, than create the blast
     * database by calling formatBlastDb.
     *
     * @param blastInstall the installation directory path for blast
     * @param blastDb      folder location for the blast database
     * @throws BlastException
     */
    private static void rebuildSequenceDatabase(Path blastInstall, Path blastDb, boolean isFeatures) throws BlastException {

        Path newFastaFile = Paths.get(blastDb.toString(), "bigfastafile.new");

        // check if file exists
        if (Files.exists(newFastaFile)) {
            try {
                BasicFileAttributes attr = Files.readAttributes(newFastaFile, BasicFileAttributes.class);
                long hoursSinceCreation = attr.creationTime().to(TimeUnit.HOURS);
                if (hoursSinceCreation > 1)
                    Files.delete(newFastaFile);
                else
                    return;
            } catch (IOException ioe) {
                Logger.error(ioe);
                return;
            }
        }

        try (BufferedWriter write = Files.newBufferedWriter(newFastaFile, Charset.defaultCharset(),
                StandardOpenOption.CREATE_NEW)) {
            if (isFeatures)
                writeBigFastaFileForFeatures(write);
            else
                writeBigFastaFile(write);
        } catch (IOException ioe) {
            throw new BlastException(ioe);
        }

        formatBlastDb(blastDb, blastInstall);
        try {
            Path fastaFile = Paths.get(blastDb.toString(), "bigfastafile");
            Files.move(newFastaFile, fastaFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            Logger.error(ioe);
        }
    }

    private static void formatBlastDb(Path blastDb, Path blastInstall) throws BlastException {
        ArrayList<String> commands = new ArrayList<>();
        String makeBlastDbCmd = blastInstall.toAbsolutePath().toString() + File.separator + "makeblastdb";
        commands.add(makeBlastDbCmd);
        commands.add("-dbtype nucl");
        commands.add("-in");
        commands.add("bigfastafile.new");
        commands.add("-logfile");
        commands.add(BLAST_DB_NAME + ".log");
        commands.add("-out");
        commands.add(BLAST_DB_NAME);
//        commands.add("-title");
//        commands.add("ICE Blast DB");
        String commandString = Utils.join(" ", commands);
        Logger.info("makeblastdb: " + commandString);

        Runtime runTime = Runtime.getRuntime();

        try {
            Process process = runTime.exec(commandString, new String[0], blastDb.toFile());
            InputStream blastOutputStream = process.getInputStream();
            InputStream blastErrorStream = process.getErrorStream();

            process.waitFor();
            StringWriter writer = new StringWriter();
            IOUtils.copy(blastOutputStream, writer);
            blastOutputStream.close();
            String outputString = writer.toString();
            Logger.debug("format output was: " + outputString);
            writer = new StringWriter();
            IOUtils.copy(blastErrorStream, writer);
            String errorString = writer.toString();
            Logger.debug("format error was: " + errorString);
            process.destroy();
            if (errorString.length() > 0) {
                Logger.error(errorString);
                throw new IOException("Could not make blast db");
            }
        } catch (InterruptedException e) {
            throw new BlastException("Could not run makeblastdb [BlastDBPath is " + blastDb.toString() + "]", e);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    /**
     * Retrieve all the sequences from the database, and writes it out to a fasta file on disk.
     *
     * @throws BlastException
     */
    private static void writeBigFastaFile(BufferedWriter writer) throws BlastException {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        long count = sequenceDAO.getSequenceCount();
        if (count <= 0)
            return;

        int offset = 0;
        while (offset < count) {
            Sequence sequence = sequenceDAO.getSequence(offset++);
            long id = sequence.getEntry().getId();

            String sequenceString = "";
            String temp = sequence.getSequence();
//            int sequenceLength = 0;
            if (temp != null) {
                SymbolList symL;
                try {
                    symL = DNATools.createDNA(sequence.getSequence().trim());
                } catch (IllegalSymbolException e1) {
                    // maybe it's rna?
                    try {
                        symL = RNATools.createRNA(sequence.getSequence().trim());
                    } catch (IllegalSymbolException e2) {
                        // skip this sequence
                        Logger.debug("Invalid characters in sequence for " + sequence.getEntry().getId()
                                + ". Skipped for indexing");
                        Logger.debug(e2.toString());
                        continue;
                    }
                }

//                sequenceLength = symL.seqString().length();
                sequenceString = SequenceUtils.breakUpLines(symL.seqString() + symL.seqString());
            }

            if (sequenceString.length() > 0) {
                try {
                    String idString = ">" + id;
                    idString += DELIMITER + sequence.getEntry().getRecordType();
                    String name = sequence.getEntry().getName() == null ? "None" : sequence.getEntry().getName();
                    idString += DELIMITER + name;
                    String pNumber = sequence.getEntry().getPartNumber();
                    idString += DELIMITER + pNumber;
                    idString += "\n";
                    writer.write(idString);
                    writer.write(sequenceString + "\n");
                } catch (IOException e) {
                    throw new BlastException(e);
                }
            }
        }
    }

    private static void writeBigFastaFileForFeatures(BufferedWriter writer) throws BlastException {
        FeatureDAO featureDAO = DAOFactory.getFeatureDAO();
        long count = featureDAO.getFeatureCount();
        if (count <= 0)
            return;

        int offset = 0;
        while (offset < count) {
            List<Feature> features = featureDAO.getFeatures(offset++, 1);
            Feature feature = features.get(0);
            String sequenceString = feature.getSequence().trim();
            try {
                String idString = ">"
                        + feature.getId() + DELIMITER
                        + feature.getName() + DELIMITER
                        + feature.getGenbankType() + DELIMITER
                        + feature.getHash();
                idString += "\n";
                writer.write(idString);
                writer.write(sequenceString + "\n");
            } catch (IOException e) {
                throw new BlastException(e);
            }
        }
    }

    static class ProcessResultReader extends Thread {
        final InputStream inputStream;
        final String type;
        final StringBuilder sb;

        ProcessResultReader(final InputStream is, String type) {
            this.inputStream = is;
            this.type = type;
            this.sb = new StringBuilder();
        }

        public void run() {
            try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                final BufferedReader br = new BufferedReader(inputStreamReader);
                String line;
                while ((line = br.readLine()) != null) {
                    this.sb.append(line).append("\n");
                }
            } catch (final IOException ioe) {
                Logger.error(ioe.getMessage());
                throw new RuntimeException(ioe);
            }
        }

        @Override
        public String toString() {
            return this.sb.toString();
        }
    }
}
