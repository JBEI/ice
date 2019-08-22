package org.jbei.ice.lib.search.blast;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
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
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.IndexBuildStatus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FeatureDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureDAO;
import org.jbei.ice.storage.model.Feature;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.jbei.ice.lib.utils.SequenceUtils.breakUpLines;

/**
 * Enables (command line) interaction with BLAST+
 * <p>
 * Current usage is for blast searches and auto-annotation support
 *
 * @author Hector Plahar
 */
public class BlastPlus implements Closeable {

    private static final String DELIMITER = ",";
    private static final String LOCK_FILE_NAME = "write.lock";
    private static final String NEW_INDEX_FILE_NAME = "bigfastafile.new";

    private final Path indexPath;
    private final String dbName;
    private final Path blastInstallDirectory;

    public BlastPlus() {
        this("blast", "ice");
    }

    /**
     * @param folderName folder location of the index folder
     * @param dbName     name of the blast database
     */
    public BlastPlus(String folderName, String dbName) {
        this.indexPath = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), folderName);
        this.dbName = dbName;
        this.blastInstallDirectory = Paths.get(Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR));
    }

    /**
     * Runs a blast query in the specified database folder
     * using the specified options
     *
     * @param query   wrapper around blast query including options such as blast type
     * @param options command line options for blast
     * @return results of the query run. An empty string is returned if the specified blast database does not exist
     * in the ice data directory
     * @throws BlastException on exception running blast on the command line
     */
    private String runBlastQuery(BlastQuery query, String... options) throws BlastException {
        if (query.getBlastProgram() == null)
            query.setBlastProgram(BlastProgram.BLAST_N);

        try {
            Path commandPath = Paths.get(Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR),
                    query.getBlastProgram().getName());
            String blastDb = Paths.get(this.indexPath.toString(), this.dbName).toString();
            if (!Files.exists(Paths.get(blastDb + ".nsq"))) {
                return "";
            }

            String[] blastCommand = new String[3 + options.length];
            blastCommand[0] = commandPath.toString();
            blastCommand[1] = "-db";
            blastCommand[2] = blastDb;
            System.arraycopy(options, 0, blastCommand, 3, options.length);

            Process process = Runtime.getRuntime().exec(blastCommand);
            ProcessResultReader reader = new ProcessResultReader(process.getInputStream());
            reader.start();
            BufferedWriter programInputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            programInputWriter.write(query.getSequence());
            programInputWriter.flush();
            programInputWriter.close();
            process.getOutputStream().close();

            // TODO this should go into the thread itself & have future wait on it
            final int exitValue = process.waitFor();
            switch (exitValue) {
                case 0:
                    return reader.toString();

                case 1:
                    Logger.error("Error in query sequence(s) or BLAST options");
                    break;

                case 2:
                    Logger.error("Error in BLAST database");
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
     * <li><code>qstart</code> - query match start index</li>
     * <li><code>qend</code> - query match end index</li>
     * <li><code>sstart</code> - subject match start index</li>
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
     * @throws BlastException if results of running blast is null
     */
    public HashMap<String, SearchResult> runBlast(BlastQuery query) throws BlastException {
        String result = runBlastQuery(query, "-perc_identity", "95", "-outfmt",
                "10 stitle qstart qend sstart send sstrand evalue bitscore score length nident");
        if (result == null)
            throw new BlastException("Exception running blast");
        return processBlastOutput(result, query.getSequence().length());
    }

    /**
     * Run a blast query against the sequence features blast database.
     *
     * @param query wrapper around sequence to blast
     * @return list of DNA features that match the query according to the parameters
     * @throws BlastException on null result or exception processing the result
     */
    public List<DNAFeature> runCheckFeatures(BlastQuery query) throws BlastException {   // todo add evalue
        String result = runBlastQuery(query, "-perc_identity", "100",
                "-outfmt", "10 stitle qstart qend sstart send sstrand");
        if (result == null)
            throw new BlastException("Exception running blast");
        return processFeaturesBlastOutput(result);
    }

    /**
     * Process the output of the blast run for features
     * into a list of feature objects
     * <br>
     * Expected format for the output (per line) is
     * <code>feature_id, label, type, qstart, qend, sstart, send, sstrand</code>
     * Therefore line[0] is feature_id, line[1] is label etc
     * <br>Since we are only interested in features that have a full match (covers entire feature) some matches are
     * manually eliminated. The results returned by blast can cover only a subset of the sequence. e.g.
     * given query = 'ATGC' and feature1 = 'ATG' and feature2 = 'TATGT', the query will return
     * 1,3,1,3 and 1,3,2,4.
     *
     * @param blastOutput blast program output
     * @return list of feature objects resulting from processing the blast output
     */
    private static List<DNAFeature> processFeaturesBlastOutput(String blastOutput) {
        List<DNAFeature> hashMap = new ArrayList<>();
        HashSet<String> duplicates = new HashSet<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                if (line.length != 9) {
                    continue;
                }

                long id = Long.decode(line[0]);
                String label = line[1];
                String type = line[2];
                int strand = Integer.decode(line[3]);
                int queryStart = Integer.decode(line[4]);
                int queryEnd = Integer.decode(line[5]);
                int subjectStart = Integer.decode(line[6]);
                int subjectEnd = Integer.decode(line[7]);
//                int strand = "plus".equalsIgnoreCase(line[7]) ? 1 : -1;

                if (!duplicates.add(label + ":" + queryStart + ":" + queryEnd + ":" + strand)) {
                    continue;
                }

                if (subjectStart != 1 && (queryEnd - queryStart) + 1 != subjectEnd)
                    continue;

                // check for full feature coverage
                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setId(id);
                dnaFeature.setName(label);
                dnaFeature.setType(type);
                DNAFeatureLocation location = new DNAFeatureLocation();
                location.setGenbankStart(queryStart);
                location.setEnd(queryEnd);
                dnaFeature.getLocations().add(location);
                dnaFeature.setStrand(strand);
                hashMap.add(dnaFeature);
            }

            return hashMap;
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Parses a blast output that represents a single hit
     *
     * @param line blast output for hit
     * @return object wrapper around details of the hit
     */
    private static SearchResult parseBlastOutputLine(String[] line) {
        try {
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
            searchResult.setScore(Float.valueOf(line[11].trim()));
            searchResult.setAlignment(line[13]);
            searchResult.setQueryLength(Integer.parseInt(line[12].trim()));
            searchResult.setNident(Integer.parseInt(line[13].trim()));
            return searchResult;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Processes the result of a blast search
     *
     * @param blastOutput result output from running blast on the command line
     * @param queryLength length of query sequence
     * @return mapping of entryId to search result object containing information about the blast search for that particular hit
     */
    private static LinkedHashMap<String, SearchResult> processBlastOutput(String blastOutput, int queryLength) {
        LinkedHashMap<String, SearchResult> hashMap = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();
            reader.close();

            for (String[] line : lines) {
                SearchResult info = parseBlastOutputLine(line);
                if (info == null)
                    continue;

                info.setQueryLength(queryLength);
                String idString = Long.toString(info.getEntryInfo().getId());
                // if there is an existing record for same entry with a lower relative score then replace
                hashMap.putIfAbsent(idString, info);
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        return hashMap;
    }

    /**
     * Checks if a database exists for blast searches exists by checking for the existence of
     * the blast database name (currently <code>ice</code>) with <code>.nsq</code> extension
     *
     * @return true is a blast database is found, false otherwise
     */
    private boolean blastDatabaseExists() {
        return Files.exists(Paths.get(indexPath.toString(), this.dbName + ".nsq"), LinkOption.NOFOLLOW_LINKS);
    }

    public void rebuildFeaturesBlastDatabase() throws IOException {
        File lockFile = Paths.get(blastInstallDirectory.toString(), LOCK_FILE_NAME).toFile();

        try {
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
                if (!lockFile.createNewFile()) {
                    Logger.warn("Could not create lock file for features blast rebuild");
                    return;
                }

                FileOutputStream fos = new FileOutputStream(lockFile);
                try (FileLock lock = fos.getChannel().tryLock()) {
                    if (lock == null)
                        return;
                    Logger.info("Rebuilding features blast database...");
                    rebuildSequenceDatabase(true);
                    Logger.info("Blast features database rebuild complete");
                }
            } catch (OverlappingFileLockException l) {
                Logger.warn("Could not obtain lock file for blast at " + blastInstallDirectory.toString());
            } catch (BlastException be) {
                FileUtils.deleteQuietly(lockFile);
                Logger.error(be);
            }
        } finally {
            FileUtils.deleteQuietly(lockFile);
        }
    }

    /**
     * Re-builds the blast database, using a lock file to prevent concurrent rebuilds.
     * The lock file has a "life-span" of 1 day after which it is deleted.
     * <p>
     * Also, a rebuild can be forced even if a lock file exists which is less than a day old
     *
     * @param force set to true to force a rebuild. Use with caution
     * @throws BlastException on exception rebuilding
     */
    public void rebuildDatabase(boolean force) throws BlastException {
        File lockFile = Paths.get(this.indexPath.toString(), LOCK_FILE_NAME).toFile();

        try {
            // check if db is locked
            if (lockFile.exists()) {
                if (lockFile.lastModified() <= (System.currentTimeMillis() - (1000 * 60 * 60 * 24))) {
                    if (!lockFile.delete()) {
                        Logger.warn("Could not delete outdated blast lockfile. Delete the following file manually: "
                                + lockFile.getAbsolutePath());
                        return;
                    }
                } else {
                    Logger.info("Blast db locked (lockfile - " + lockFile.getAbsolutePath() + "). Rebuild aborted!");
                    return;
                }
            }

            try {
                if (!Files.exists(this.indexPath)) {
                    Logger.info("Blast folder (" + this.indexPath.toString() + ") does not exist. Creating...");
                    try {
                        Files.createDirectories(this.indexPath);
                    } catch (Exception e) {
                        Logger.warn("Could not create blast folder. Create it manually or all blast runs will fail");
                        return;
                    }
                } else if (!Files.isDirectory(this.indexPath)) {
                    throw new IllegalArgumentException(this.indexPath.toString() + " is not a directory");
                }

                if (!force && blastDatabaseExists()) {
                    Logger.info("Blast database found in " + this.indexPath.toAbsolutePath().toString());
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
                    rebuildSequenceDatabase(false);
                    Logger.info("Blast database rebuild complete");
                }
            } catch (OverlappingFileLockException l) {
                Logger.warn("Could not obtain lock file for blast at " + indexPath.toString());
            } catch (IOException eio) {
                FileUtils.deleteQuietly(lockFile);
                throw new BlastException(eio);
            }
        } finally {
            FileUtils.deleteQuietly(lockFile);
        }
    }

    /**
     * Run the bl2seq program on multiple subjects.
     * <p>
     * This method requires disk space write temporary files. It tries to clean up after itself.
     *
     * @param query   reference sequence.
     * @param subject query sequence.
     * @return List of output string from bl2seq program.
     * @throws BlastException on exception running blast 2 seq
     */
    public List<Bl2SeqResult> runBlast2Seq(String query, String subject) throws BlastException {
        try {
            Path queryFilePath = Files.write(Files.createTempFile("query-", ".seq"), query.getBytes());
            Path subjectFilePath = Files.write(Files.createTempFile("subject-", ".seq"), subject.getBytes());

            String blastN = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + BlastProgram.BLAST_N.getName();

            String[] command = new String[]{
                    blastN, "-query", queryFilePath.toString(), "-subject", subjectFilePath.toString(),
                    "-outfmt", "10 score qstart qend qseq sstart send sseq sstrand"
            };

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            List<Bl2SeqResult> results = new ArrayList<>();
            while ((line = input.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length != 8) {
                    Logger.error("Invalid bl2seq result line obtained. skipping");
                    continue;
                }

                int score = Integer.decode(data[0]);
                int queryStart = Integer.decode(data[1]);
                int queryEnd = Integer.decode(data[2]);
                int subjectStart = Integer.decode(data[4]);
                int subjectEnd = Integer.decode(data[5]);
                int orientation = "plus".equalsIgnoreCase(data[7]) ? 0 : 1;
                Bl2SeqResult result = new Bl2SeqResult(score, queryStart, queryEnd, data[3], subjectStart, subjectEnd,
                        data[6], orientation);
                results.add(result);
            }

            input.close();
            Files.deleteIfExists(subjectFilePath);
            Files.deleteIfExists(queryFilePath);
            return results;
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    /**
     * Build the blast search or sequence database database.
     * <p>
     * <p/>First dump the sequences from the sql database into a fasta file, than create the blast
     * database by calling formatBlastDb.
     *
     * @param isFeatures determines which database to rebuild. True for sequence features database, false for
     *                   blast search database
     * @throws BlastException on exception rebuilding sequence db
     */
    private void rebuildSequenceDatabase(boolean isFeatures) throws BlastException {
        Path newFastaFile = Paths.get(this.indexPath.toString(), NEW_INDEX_FILE_NAME);

        // check if file exists
        if (Files.exists(newFastaFile)) {
            try {
                Files.delete(newFastaFile);
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

        formatBlastDb();
        try {
            Path fastaFile = Paths.get(this.indexPath.toString(), "bigfastafile");
            Files.move(newFastaFile, fastaFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            Logger.error(ioe);
        }
    }

    private void formatBlastDb() throws BlastException {
        ArrayList<String> commands = new ArrayList<>();
        String makeBlastDbCmd = this.blastInstallDirectory.toAbsolutePath().toString() + File.separator + "makeblastdb";
        commands.add(makeBlastDbCmd);
        commands.add("-dbtype nucl");
        commands.add("-in");
        commands.add("bigfastafile.new");
        commands.add("-logfile");
        commands.add(this.dbName + ".log");
        commands.add("-out");
        commands.add(this.dbName);
//        commands.add("-title");
//        commands.add("ICE Blast DB");
        String commandString = Utils.join(" ", commands);
        Logger.info("makeblastdb: " + commandString);

        Runtime runTime = Runtime.getRuntime();

        try {
            Process process = runTime.exec(commandString, new String[0], indexPath.toFile());
            InputStream blastOutputStream = process.getInputStream();
            InputStream blastErrorStream = process.getErrorStream();
            process.waitFor();
            String outputString = Utils.getString(blastOutputStream);
            blastOutputStream.close();

            Logger.debug("format output was: " + outputString);
            String errorString = Utils.getString(blastErrorStream);
            blastErrorStream.close();

            Logger.debug("format error was: " + errorString);
            process.destroy();
            if (errorString.length() > 0) {
                Logger.error(errorString);
                throw new IOException("Could not make blast db");
            }
        } catch (InterruptedException e) {
            throw new BlastException("Could not run makeblastdb [BlastDBPath is " + blastInstallDirectory.toString() + "]", e);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    public IndexBuildStatus getStatus() {
        Path path = Paths.get(indexPath.toString(), LOCK_FILE_NAME);

        if (Files.exists(path)) {
            try {
                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                long seconds = attributes.creationTime().to(TimeUnit.SECONDS);
                if ((Instant.now().getEpochSecond() - seconds) > 3 * 24 * 60) {
                    Files.delete(path);
                    return new IndexBuildStatus(0, 0);
                }
            } catch (IOException e) {
                // ok to ignore
            }
            return new IndexBuildStatus(0, 1);
        }
        return new IndexBuildStatus(0, 0);
    }

    /**
     * Retrieve all the sequences from the database, and writes it out to a fasta file on disk.
     *
     * @throws BlastException on {@link IOException}
     */
    private void writeBigFastaFile(BufferedWriter writer) throws BlastException {
        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        long count = sequenceDAO.getSequenceCount();
        if (count <= 0)
            return;

        int offset = 0;
        while (offset < count) {
            Sequence sequence = sequenceDAO.getSequence(offset++);
            if (sequence == null || sequence.getEntry() == null)
                continue;
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
                sequenceString = breakUpLines(symL.seqString() + symL.seqString());
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

    /**
     * Writes the fasta file (part of the blast database) that contains all the features that exists on this system.
     * This routine is expected to be called as part of the blast sequence feature database rebuild
     *
     * @param writer writer for fasta file
     * @throws BlastException on {@link IOException}
     */
    private void writeBigFastaFileForFeatures(BufferedWriter writer) throws BlastException {
        FeatureDAO featureDAO = DAOFactory.getFeatureDAO();
        SequenceFeatureDAO sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();

        long count = featureDAO.getFeatureCount(null);
        if (count <= 0)
            return;

        int offset = 0;
        while (offset < count) {
            List<Feature> features = featureDAO.getFeatures(offset++, 1, null);
            Feature feature = features.get(0);
            String featureName = feature.getName();
            if (featureName == null || featureName.trim().isEmpty())
                continue;

            if (feature.getCuration() != null && feature.getCuration().isExclude())
                continue;

            boolean hasNegativeStrand = false;
            boolean hasPositiveStrand = false;

            List<SequenceFeature> sequenceFeatures = sequenceFeatureDAO.getByFeature(feature);
            if (sequenceFeatures == null || sequenceFeatures.isEmpty()) {
                hasPositiveStrand = true;
            } else {
                for (SequenceFeature sequenceFeature : sequenceFeatures) {
                    if (sequenceFeature.getStrand() == 1) {
                        hasPositiveStrand = true;
                    } else if (sequenceFeature.getStrand() == -1) {
                        hasNegativeStrand = true;
                    }
                }
            }

            try {
                String sequenceString = feature.getSequence().trim();
                if (StringUtils.isEmpty(sequenceString))
                    continue;

                if (hasNegativeStrand) {
                    try {
                        SymbolList symbolList = DNATools.createDNA(sequenceString);
                        symbolList = DNATools.reverseComplement(symbolList);
                        writeSequenceString(feature, writer, symbolList.seqString(), -1);
                    } catch (IllegalSymbolException | IllegalAlphabetException e) {
                        Logger.warn(e.getMessage());
                        continue;
                    }
                }

                if (hasPositiveStrand) {
                    writeSequenceString(feature, writer, sequenceString, 1);
                }
            } catch (IOException e) {
                throw new BlastException(e);
            }
        }
    }

    private void writeSequenceString(Feature feature, BufferedWriter writer, String seq, int strand)
            throws IOException {
        String idString = ">"
                + feature.getId() + DELIMITER
                + feature.getName() + DELIMITER
                + feature.getGenbankType() + DELIMITER
                + strand;
        idString += "\n";
        writer.write(idString);
        writer.write(seq + "\n");
    }

    @Override
    public void close() {
        FileUtils.deleteQuietly(Paths.get(indexPath.toString(), File.separator, NEW_INDEX_FILE_NAME).toFile());
    }

    /**
     * Thread that reads the result of a command line process execution
     */
    static class ProcessResultReader extends Thread {
        final InputStream inputStream;
        final StringBuilder sb;

        ProcessResultReader(final InputStream is) {
            this.inputStream = is;
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
