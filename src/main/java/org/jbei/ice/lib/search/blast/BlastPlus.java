package org.jbei.ice.lib.search.blast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;

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

    public static HashMap<String, SearchResult> runBlast(BlastQuery query) throws BlastException {
        try {
            String command = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + query.getBlastProgram().getName();
            String blastDb = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), BLAST_DB_FOLDER,
                                       BLAST_DB_NAME).toString();
            String blastCommand = (command + " -db " + blastDb);
            Logger.info("Blast: " + blastCommand);
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
                    return processBlastOutput(reader.toString(), query.getSequence().length());

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

    private static SearchResult parseSequenceIdentifier(String line) {
        long id;
        EntryType recordType;
        String name;
        String partNumber;
        SearchResult info = null;

        // new record
        String[] idLineFields = line.substring(1).split(DELIMITER);
        if (idLineFields.length == 4) {
            id = Long.decode(idLineFields[0]);
            recordType = EntryType.nameToType(idLineFields[1]);
            name = idLineFields[2];
            partNumber = idLineFields[3];

            PartData view = new PartData(recordType);
            view.setId(id);
            view.setPartId(partNumber);
            view.setName(name);

            info = new SearchResult();
            info.setEntryInfo(view);

            String summary = new EntryRetriever().getEntrySummary(info.getEntryInfo().getId());
            info.getEntryInfo().setShortDescription(summary);
//                searchResult.setAlignmentLength(alignmentLength);
//                searchResult.setPercentId(percentId);
        }
        return info;
    }

    private static LinkedHashMap<String, SearchResult> processBlastOutput(String blastOutput, int queryLength) {
        LinkedHashMap<String, SearchResult> hashMap = new LinkedHashMap<>();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(blastOutput.split("\n")));

        for (int i = 0; i < lines.size(); i += 1) {
            String line = lines.get(i);

            if (line.trim().isEmpty() || !line.startsWith(">"))
                continue;

            // process alignment details for above match
            SearchResult info = parseSequenceIdentifier(line.substring(1));
            if (info == null)
                continue;

            info.setQueryLength(queryLength);
            while (i < lines.size() - 1) {
                i += 1;
                line = lines.get(i);
                if (line.startsWith("Length")) {
                    int sequenceLength = Integer.valueOf(line.substring(7).trim());
//                    System.out.println(info.getQueryLength() + ", " + sequenceLength / 2);
                    continue;
                }

                // next result encountered
                if (line.startsWith(">")) {
                    i -= 1;
                    break;
                }

                // bit score and e-value
                // eg. Score = 3131 bits (1695),  Expect = 0.0
                if (line.contains("Score")) {
                    String[] split = line.split("=");
                    String evalue = split[2].trim();
                    info.seteValue(evalue);

                    String scoreString = split[1].substring(1, split[1].indexOf(",")).split(" ")[0];
                    if (NumberUtils.isNumber(scoreString)) {
                        info.setScore(Float.valueOf(scoreString));
                    }
                }

                // aligned bp and aligned identity %
                // e.g. Identities = 1692/1692 (100%), Gaps = 0/1692 (0%)
                if (line.contains("Identities")) {
                    String[] split = line.split("=");
                    String aligned = split[1].substring(1, split[1].indexOf(","));
                    info.setAlignment(aligned);
//                    if (!aligned.trim().isEmpty()) {
//                        info.setAlignmentLength(Integer.valueOf(aligned).intValue());
//                    }
                }

                info.getMatchDetails().add(line);

                String idString = Long.toString(info.getEntryInfo().getId());
                SearchResult currentResult = hashMap.get(idString);
                // if there is an existing record for same entry with a lower relative score then replace
                if (currentResult == null)
                    hashMap.put(idString, info);
//                else {
//                    if (info.getScore() > currentResult.getRelativeScore()) {
//                        hashMap.put(idString, info);
//                    }
//                }
            }
        }

        return hashMap;
    }

    private static boolean blastDatabaseExists() {
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        Path path = FileSystems.getDefault().getPath(dataDir, BLAST_DB_FOLDER, BLAST_DB_NAME + ".nsq");
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public static void rebuildDatabase(boolean force) throws BlastException {
        Path blastDir = Paths.get(Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR));
        if (!Files.exists(blastDir))
            throw new BlastException("Could not locate Blast installation in " + blastDir.toAbsolutePath().toString());

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        final Path blastFolder = Paths.get(dataDir, BLAST_DB_FOLDER);
        File lockFile = Paths.get(blastFolder.toString(), LOCK_FILE_NAME).toFile();
        if (lockFile.exists()) {
            if (lockFile.lastModified() <= (System.currentTimeMillis() - (1000 * 60 * 60 * 24)))
                lockFile.delete();
            else {
                Logger.info("Blast db locked (lockfile - " + lockFile.getAbsolutePath() + "). Rebuild aborted!");
                return;
            }
        }

        try {
            if (!Files.exists(blastFolder)) {
                Logger.info("Blast folder (" + blastFolder.toString() + ") does not exist. Creating...");
                Files.createDirectories(blastFolder);
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
                rebuildSequenceDatabase(blastDir);
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
     * <p/>
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
     * <p/>
     * <p/>First dump the sequences from the sql database into a fasta file, than create the blast
     * database by calling formatBlastDb.
     *
     * @param blastInstall the installation directory path for blast
     * @throws BlastException
     */
    private static void rebuildSequenceDatabase(Path blastInstall) throws BlastException {
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        final Path blastDb = Paths.get(dataDir, BLAST_DB_FOLDER);

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
            writeBigFastaFile(write);
        } catch (IOException ioe) {
            throw new BlastException(ioe);
        }

        formatBlastDb(blastDb, blastInstall);
        try {
            Path fastaFile = Paths.get(dataDir, BLAST_DB_FOLDER, "bigfastafile");
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
        Set<Sequence> sequencesList;
        sequencesList = DAOFactory.getSequenceDAO().getAllSequences();
        for (Sequence sequence : sequencesList) {
            long id = sequence.getEntry().getId();
//            boolean circular = false;
//            if (sequence.getEntry() instanceof Plasmid) {
//                circular = ((Plasmid) sequence.getEntry()).getCircular();
//            }
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
                        Logger.debug("invalid characters in sequence for " + sequence.getEntry().getRecordId());
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
