package org.jbei.ice.lib.search.blast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.search.BlastProgram;
import org.jbei.ice.shared.dto.search.SearchResultInfo;

import org.apache.commons.io.IOUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

/**
 * Manage blast functions.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class Blast {

    private static final String BL2SEQ_COMMAND_PATTERN = "%s -p blastn -i %s -j %s -r 2 -F F";
    private static final String BLASTALL_COMMAND_PATTERN = "%s -p %s -d %s -m 8";

    private final String BLASTALL;
    private final String BL2SEQ;
    private final String BLAST_DATABASE_DIR;      // e.g. /tmp/blast/jbeiblast
    private final String BIG_FASTA_FILE;
    private final String FEATURE_BLAST_DIRECTORY;   // /tmp/blast/FEATURES
    private final String FEATURE_BLAST_FILE;
    private final String BLAST_NSQ_PATH;        // e.g. /tmp/blast/jbeiblast/ice.nsq

    public Blast() {
        String BLAST_DIRECTORY = Utils.getConfigValue(ConfigurationKey.BLAST_DIRECTORY);
        String BLAST_DATABASE_NAME = Utils.getConfigValue(ConfigurationKey.BLAST_DATABASE_NAME);
        BLASTALL = Utils.getConfigValue(ConfigurationKey.BLAST_BLASTALL);
        BL2SEQ = Utils.getConfigValue(ConfigurationKey.BLAST_BL2SEQ);

        FEATURE_BLAST_DIRECTORY = BLAST_DIRECTORY + File.separator + "FEATURES";
        BIG_FASTA_FILE = "bigfastafile";
        FEATURE_BLAST_FILE = "featurefastafile";
        BLAST_DATABASE_DIR = BLAST_DIRECTORY + File.separator + BLAST_DATABASE_NAME;
        BLAST_NSQ_PATH = BLAST_DATABASE_DIR + File.separator + "ice.nsq";
    }

    /**
     * Rebuild blast database.
     *
     * @throws BlastException
     */
    public void rebuildDatabase(boolean force) throws BlastException {
        try {
            if (!Files.exists(Paths.get(BLAST_DATABASE_DIR)))
                Files.createDirectory(Paths.get(BLAST_DATABASE_DIR));

            if (!force && isBlastDatabaseExists()) {
                Logger.info("Blast database exists");
                return;
            }

            FileOutputStream fos = new FileOutputStream(BLAST_DATABASE_DIR + File.separator + "write.lock");
            try (FileLock lock = fos.getChannel().tryLock()) {
                if (lock == null)
                    return;
                Logger.info("Rebuilding blast database");
                rebuildSequenceDatabase();
                rebuildFeatureBlastDatabase();
                Logger.info("Blast database rebuild complete");
            }
        } catch (OverlappingFileLockException l) {
            Logger.warn("Could not obtain lock file for blast.");
        } catch (IOException eio) {
            throw new BlastException(eio);
        }
    }

    private boolean isBlastDatabaseExists() {
        Path path = FileSystems.getDefault().getPath(BLAST_NSQ_PATH);
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Run Blast against the existing blast database.
     * <p/>
     * If the database does not exist, create it.
     *
     * @param queryString sequence to be blasted.
     * @return ArrayList of {@link BlastResult}s.
     * @throws ProgramTookTooLongException
     * @throws BlastException
     */
    public HashMap<String, SearchResultInfo> query(Account account, String queryString, BlastProgram program)
            throws ProgramTookTooLongException, BlastException {
        String cmdString = String.format(BLASTALL_COMMAND_PATTERN, BLASTALL, program.getName(), BLAST_DATABASE_DIR
                + File.separator + "ice");
        Logger.info("Running blast query: " + cmdString);
        String blastOutput = runExternalProgram(queryString, cmdString);
        return processBlastOutput(account, blastOutput, queryString.length());
    }

    /**
     * Process the raw blast output
     *
     * @param blastOutput result of the blast search
     * @return ArrayList of SearchResultInfo.
     */
    private HashMap<String, SearchResultInfo> processBlastOutput(Account account, String blastOutput, int queryLength) {
        EntryController controller = ControllerFactory.getEntryController();
        HashMap<String, SearchResultInfo> hashMap = new HashMap<>();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(blastOutput.split("\n")));
        for (String line : lines) {
            String[] columns = line.split("\t");
            if (columns.length == 12) {
                String idLine = columns[1];
                String[] idLineFields = idLine.split(":");
                int sLength = 0;
                boolean circular = false;
                if (idLineFields.length == 1) {
                    Logger.info("Old Blast db format detected. Schedule rebuild");
                    ApplicationController.scheduleBlastIndexRebuildTask(true);
                    return null;
                } else if (idLineFields.length == 3) {
                    idLine = idLineFields[0].trim();
                    sLength = Integer.parseInt(idLineFields[1]);
                    if ("circular".equals(idLineFields[2])) {
                        circular = true;
                    }
                }

                Entry entry;
                try {
                    entry = controller.getByRecordId(account, idLine);
                } catch (Exception e) {
                    continue;
                }

                String recordId = idLine;
                float percentId = Float.parseFloat(columns[2]);
                int alignmentLength = Integer.parseInt(columns[3]);
                int sStart = Integer.parseInt(columns[8]);
                int sEnd = Integer.parseInt(columns[9]);
                float eValue = Float.parseFloat(columns[10]);
                float bitScore = Float.parseFloat(columns[11]);
                float relativeScore = (percentId * alignmentLength * bitScore);

                if (sStart > sLength || sEnd > sLength) {
                    if (circular) {
                        if (sStart > sLength && sEnd > sLength) {
                            // both start and end are longer than the length. Skip this
                            continue;
                        }
                    } else {
                        // skip this match.
                        continue;
                    }
                }

                SearchResultInfo info = new SearchResultInfo();
                info.setBitScore(bitScore);
                EntryInfo view = ModelToInfoFactory.getSummaryInfo(entry);
                info.setEntryInfo(view);

                info.seteValue(eValue);
                info.setAlignmentLength(alignmentLength);
                info.setPercentId(percentId);
                info.setQueryLength(queryLength);

                SearchResultInfo currentResult = hashMap.get(recordId);
                // if there is an existing record for same entry with a lower relative score then replace
                if (currentResult == null)
                    hashMap.put(recordId, info);
                else {
                    if (relativeScore > currentResult.getRelativeScore()) {
                        hashMap.put(recordId, info);
                    }
                }
            }
        }
        return hashMap;
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
    public String runBl2Seq(String query, String subject) throws BlastException, ProgramTookTooLongException {
        String result;
        try {
            Path queryFilePath = Files.write(Files.createTempFile("query-", ".seq"), query.getBytes());
            Path subjectFilePath = Files.write(Files.createTempFile("subject-", ".seq"), subject.getBytes());
            String commandString = String.format(BL2SEQ_COMMAND_PATTERN, BL2SEQ,
                                                 queryFilePath.toString(), subjectFilePath.toString());
            Logger.info("Bl2seq query: " + commandString);
            result = runSimpleExternalProgram(commandString);
            Files.deleteIfExists(subjectFilePath);
            Files.deleteIfExists(queryFilePath);
        } catch (IOException e) {
            throw new BlastException(e);
        }

        return result;
    }

    /**
     * Rename the blast database on disk.
     *
     * @param newBigFastaFileDir
     * @param baseBlastDirName
     * @throws IOException
     * @throws BlastException
     */
    private void renameBlastDb(File newBigFastaFileDir, String baseBlastDirName) throws IOException, BlastException {
        Path oldBlast = Paths.get(baseBlastDirName + ".old");
        deleteDirectoryIfExists(oldBlast);

        Path currentBlastPath = Paths.get(baseBlastDirName);
        if (Files.exists(currentBlastPath) && Files.isDirectory(currentBlastPath)) {
            Files.walkFileTree(currentBlastPath, new CopyDirVisitor(currentBlastPath, oldBlast));
        }

        Path newBigFastaPath = Paths.get(newBigFastaFileDir.toURI());
        Files.walkFileTree(newBigFastaPath, new CopyDirVisitor(newBigFastaPath, currentBlastPath));
    }

    /**
     * Create a new blast database on disk using formatdb program.
     *
     * @param fastaFileDir  directory where the fasta file is located.
     * @param fastaFileName name of the fasta file.
     * @param logFileName   name of the log output file.
     * @param databaseName  name of the blast database.
     * @throws BlastException
     */
    private static void formatBlastDb(File fastaFileDir, String fastaFileName, String logFileName,
            String databaseName) throws BlastException {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(Utils.getConfigValue(ConfigurationKey.BLAST_FORMATDB));
        commands.add("-i");
        commands.add(fastaFileName);
        commands.add("-l");
        commands.add(logFileName);
        commands.add("-n");
        commands.add(databaseName);
        commands.add("-o");
        commands.add("-pF");
        commands.add("-t");
        commands.add(databaseName);
        String commandString = Utils.join(" ", commands);
        Logger.info("formatdb: " + commandString);

        Runtime runTime = Runtime.getRuntime();

        try {
            Process process = runTime.exec(commandString, new String[0], fastaFileDir);
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
                throw new IOException("Could not format blast db");
            }
        } catch (InterruptedException e) {
            throw new BlastException("Could not run formatdb", e);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    /**
     * Wrapper to run an external program, and collect its output.
     *
     * @param commandString command to run.
     * @return Output string from the program.
     * @throws BlastException
     */
    private String runSimpleExternalProgram(String commandString) throws BlastException {
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
     * Wrapper to run an external program, feeding it inputs and collecting its output.
     *
     * @param inputString   input to the program.
     * @param commandString external command to run.
     * @return Output from the program as string.
     * @throws ProgramTookTooLongException
     * @throws BlastException
     */
    private String runExternalProgram(String inputString, String commandString)
            throws ProgramTookTooLongException, BlastException {
        Runtime runTime = Runtime.getRuntime();
        StringBuilder outputString = new StringBuilder();
        StringBuilder errorString = new StringBuilder();

        try {
            Process process = runTime.exec(commandString);

            if (inputString.length() > 0) {
                BufferedWriter programInputWriter = new BufferedWriter(new OutputStreamWriter(
                        process.getOutputStream()));

                programInputWriter.write(inputString);
                programInputWriter.flush();
                programInputWriter.close();
                process.getOutputStream().close();
            }

            long maxWait = 5000L;
            long startTime = System.currentTimeMillis();

            BufferedReader programOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader programErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String tempError;
            String tempOutput;

            /*
             * How to Deal with External Blast
             * 
             * Blast seems to be blocked by the stdin not being flushed, and stdout
             * not being emptied. This means that this thread should read the stdout
             * of blast periodically. But since it may take a while for blast to to start
             * filling stdout, it will be empty for a while before there is any data in it.
             * 
             * So, to prevent blocking as well as doing proper time out, this loop
             * tries to read from the stdout and sterr. If nothing is read for a long 
             * time, it times out. If something is read, it's collected. If nothing is 
             * read after getting some output, it quits. 
             * 
             * @ tham
             * 
             */
            boolean errorStreamFinished = false;
            boolean outputStreamFinished = false;
            boolean somethingWasRead = false;
            while (true) {
                if (programOutputReader.ready()) {
                    somethingWasRead = true;
                    tempOutput = programOutputReader.readLine();

                    outputString.append(tempOutput);
                    outputString.append("\n");
                } else if (somethingWasRead) {
                    outputStreamFinished = true;
                }

                if (programErrorReader.ready()) {
                    somethingWasRead = true;
                    tempError = programErrorReader.readLine();

                    errorString.append(tempError);
                    errorString.append("\n");

                } else if (somethingWasRead) {
                    errorStreamFinished = true;
                }
                if (errorStreamFinished && outputStreamFinished) {
                    break;
                }
                if (System.currentTimeMillis() - startTime > maxWait) {
                    throw new ProgramTookTooLongException("Blast took too long");
                }
            }

            programOutputReader.close();
            programErrorReader.close();
            process.destroy();

        } catch (IOException e) {
            Logger.warn("IO exception in running external program: " + e.toString());
        }

        if (errorString.length() > 0) {
            throw new BlastException(errorString.toString());
        }
        return outputString.toString();
    }

    /**
     * Build the blast database.
     * <p/>
     * First dump the sequences from the sql database into a fasta file, than create the blast
     * database by calling formatBlastDb.
     * <p/>
     * It creates a new database in a separate directory, and if successful, replaces the existing
     * directory with the new one.
     *
     * @throws BlastException
     */
    private void rebuildSequenceDatabase() throws BlastException {
        Path newFastaFileDirPath = Paths.get(BLAST_DATABASE_DIR + ".new");
        deleteDirectoryIfExists(newFastaFileDirPath);
        try {
            Files.createDirectory(newFastaFileDirPath);
            Path fastaFilePath = Paths.get(newFastaFileDirPath.toString(), BIG_FASTA_FILE);
            try (BufferedWriter write = Files.newBufferedWriter(fastaFilePath, Charset.defaultCharset(),
                                                                StandardOpenOption.CREATE_NEW)) {
                writeBigFastaFile(write);
            }

            Blast.formatBlastDb(newFastaFileDirPath.toFile(), fastaFilePath.getFileName().toString(),
                                "ice.log", "ice");
            renameBlastDb(newFastaFileDirPath.toFile(), BLAST_DATABASE_DIR);
        } catch (IOException ioe) {
            throw new BlastException(ioe);
        }
    }

    /**
     * Build a blast database consisting only of individual sequence features.
     *
     * @throws BlastException
     */
    private void rebuildFeatureBlastDatabase() throws BlastException {
        Path newFeatureFastaDirPath = Paths.get(FEATURE_BLAST_DIRECTORY + ".new");
        deleteDirectoryIfExists(newFeatureFastaDirPath);
        try {
            Files.createDirectory(newFeatureFastaDirPath);
            Path fastaFilePath = Paths.get(newFeatureFastaDirPath.toString(), FEATURE_BLAST_FILE);
            try (BufferedWriter writer = Files.newBufferedWriter(fastaFilePath, Charset.defaultCharset(),
                                                                 StandardOpenOption.CREATE_NEW)) {
                writeFeatureFastaFile(writer);
            }
            Blast.formatBlastDb(newFeatureFastaDirPath.toFile(), fastaFilePath.getFileName().toString(),
                                "ice_" + FEATURE_BLAST_FILE + ".log", "features");
            renameBlastDb(newFeatureFastaDirPath.toFile(), FEATURE_BLAST_DIRECTORY);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    protected void deleteDirectoryIfExists(Path path) throws BlastException {
        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new DeleteDirVisitor());
            } catch (IOException e) {
                throw new BlastException(e);
            }
        }
    }

    public class DeleteDirVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
            throw exc;
        }
    }

    public class CopyDirVisitor extends SimpleFileVisitor<Path> {

        private final Path source;
        private final Path dest;

        public CopyDirVisitor(Path source, Path dest) {
            this.source = source;
            this.dest = dest;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, dest.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = dest.resolve(source.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Retrieve all the sequences from the database, and writes it out to a fasta file on disk.
     *
     * @param writer filewriter to write to.
     * @throws BlastException
     */
    private void writeBigFastaFile(BufferedWriter writer) throws BlastException {
        List<Sequence> sequencesList;
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        try {
            sequencesList = sequenceController.getAllSequences(); // TODO potential performance impact
        } catch (ControllerException e) {
            throw new BlastException(e);
        }
        for (Sequence sequence : sequencesList) {
            String recordId = sequence.getEntry().getRecordId();
            boolean circular = false;
            if (sequence.getEntry() instanceof Plasmid) {
                circular = ((Plasmid) sequence.getEntry()).getCircular();
            }
            String sequenceString = "";
            String temp = sequence.getSequence();
            int sequenceLength = 0;
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

                sequenceLength = symL.seqString().length();
                sequenceString = SequenceUtils.breakUpLines(symL.seqString() + symL.seqString());
            }
            if (sequenceString.length() > 0) {
                try {
                    String idString = ">" + recordId + ":" + sequenceLength;
                    idString += ":" + (circular ? "circular" : "linear");
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
     * Retrieve all the feature sequences from the database, and write it out to disk.
     *
     * @param fastaFileWriter filewrite to write to.
     * @throws BlastException
     */
    private void writeFeatureFastaFile(BufferedWriter fastaFileWriter) throws BlastException {
        ArrayList<Feature> featureList;
        SequenceController sequenceController = ControllerFactory.getSequenceController();

        try {
            featureList = sequenceController.getAllFeatures();
        } catch (ControllerException e) {
            throw new BlastException(e);
        }
        for (Feature feature : featureList) {
            String hashId = feature.getHash();
            String temp = feature.getSequence();
            String sequenceString = "";
            if (temp != null) {
                SymbolList symL = null;
                try {
                    symL = DNATools.createDNA(feature.getSequence().trim());
                } catch (IllegalSymbolException e1) {
                    // skip this sequence
                    Logger.debug("Invalid characters in sequence for " + feature.getHash());
                    Logger.debug(e1.getMessage());
                }
                if (symL != null) {
                    sequenceString = SequenceUtils.breakUpLines(symL.seqString());
                }
            }
            if (sequenceString.length() > 2) {
                try {
                    fastaFileWriter.write(">" + hashId + "\n");
                    fastaFileWriter.write(sequenceString + "\n");
                } catch (IOException e) {
                    throw new BlastException(e);
                }
            }
        }
    }
}
