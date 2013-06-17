package org.jbei.ice.lib.search.blast;

import org.apache.commons.io.IOUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.controllers.ApplicationController;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.entry.*;
import org.jbei.ice.shared.dto.search.BlastProgram;
import org.jbei.ice.shared.dto.search.BlastQuery;
import org.jbei.ice.shared.dto.search.SearchResultInfo;

import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Blast Search functionality for BLAST+
 *
 * @author Hector Plahar
 */
public class BlastPlus {

    private static final String BLAST_DB_FOLDER = "ice_blast";
    private static final String BLAST_DB_NAME = "ice";

    public HashMap<String, SearchResultInfo> runBlast(Account account, BlastQuery query) throws BlastException {
        try {
            String command = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + query.getBlastProgram().getName();
            String blastDb = Utils.getConfigValue(ConfigurationKey.BLAST_DIR) + File.separator + BLAST_DB_FOLDER
                    + File.separator + BLAST_DB_NAME;
            String blastCommand = (command + " -db " + blastDb + " -outfmt 10");
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
                    Logger.error(account.getEmail() + ": Error in query sequence(s) or BLAST options: "
                            + error.toString());
                    break;

                case 2:
                    Logger.error(account.getEmail() + ": Error in BLAST database: " + error.toString());
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

    private HashMap<String, SearchResultInfo> processBlastOutput(String blastOutput, int queryLength) {
        HashMap<String, SearchResultInfo> hashMap = new HashMap<>();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(blastOutput.split("\n")));
        for (String line : lines) {
            String[] columns = line.split(",");
            if (columns.length == 12) {
                String idLine = columns[1];
                String[] idLineFields = idLine.split(":");

                long id;
                int sLength;
                boolean circular = false;
                EntryType recordType;
                String name;
                String partNumber;

                if (idLineFields.length == 6) {
                    idLine = idLineFields[0].trim();
                    id = Long.decode(idLineFields[0]).longValue();
                    recordType = EntryType.nameToType(idLineFields[2]);
                    name = idLineFields[3];
                    partNumber = idLineFields[4];
                    if ("c".equals(idLineFields[1]))
                        circular = true;
                    sLength = Integer.parseInt(idLineFields[5]);
                } else if (idLineFields.length <= 2) {
                    Logger.info("Old Blast db format detected. Schedule rebuild");
                    ApplicationController.scheduleBlastIndexRebuildTask(true);
                    return null;
                } else {
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

                EntryInfo view;
                switch (recordType) {
                    case ARABIDOPSIS:
                        view = new ArabidopsisSeedInfo();
                        break;

                    case PART:
                    default:
                        view = new PartInfo();
                        break;

                    case PLASMID:
                        view = new PlasmidInfo();
                        break;

                    case STRAIN:
                        view = new StrainInfo();
                        break;
                }

                view.setId(id);
                view.setPartId(partNumber);
                view.setName(name);

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

    private static boolean blastDatabaseExists() {
        String blastDb = Utils.getConfigValue(ConfigurationKey.BLAST_DIR) + File.separator + BLAST_DB_FOLDER;
        Path path = FileSystems.getDefault().getPath(blastDb + File.separator + BLAST_DB_NAME + ".nsq");
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    public static void rebuildDatabase(boolean force) throws BlastException {
        final String blastFolder = Utils.getConfigValue(ConfigurationKey.BLAST_DIR) + File.separator + BLAST_DB_FOLDER;

        try {
            if (!Files.exists(Paths.get(blastFolder)))
                Files.createDirectories(Paths.get(blastFolder));

            if (!force && blastDatabaseExists()) {
                Logger.info("Blast database exists");
                return;
            }

            FileOutputStream fos = new FileOutputStream(blastFolder + File.separator + "write.lock");
            try (FileLock lock = fos.getChannel().tryLock()) {
                if (lock == null)
                    return;
                Logger.info("Rebuilding blast database");
                rebuildSequenceDatabase();
                Logger.info("Blast database rebuild complete");
            }
        } catch (OverlappingFileLockException l) {
            Logger.warn("Could not obtain lock file for blast at " + blastFolder + File.separator + "write.lock");
        } catch (IOException eio) {
            throw new BlastException(eio);
        }
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
     * First dump the sequences from the sql database into a fasta file, than create the blast
     * database by calling formatBlastDb.
     * <p/>
     * It creates a new database in a separate directory, and if successful, replaces the existing
     * directory with the new one.
     *
     * @throws BlastException
     */
    private static void rebuildSequenceDatabase() throws BlastException {
        final String blastDb = Utils.getConfigValue(ConfigurationKey.BLAST_DIR) + File.separator + BLAST_DB_FOLDER;
        Path newFastaFileDirPath = Paths.get(blastDb + ".new");
        deleteDirectoryIfExists(newFastaFileDirPath);
        try {
            Files.createDirectory(newFastaFileDirPath);
            Path fastaFilePath = Paths.get(newFastaFileDirPath.toString(), "bigfastafile");
            try (BufferedWriter write = Files.newBufferedWriter(fastaFilePath, Charset.defaultCharset(),
                    StandardOpenOption.CREATE_NEW)) {
                writeBigFastaFile(write);
            }

            formatBlastDb(newFastaFileDirPath.toFile(), fastaFilePath.toString(), blastDb);
            renameBlastDb(newFastaFileDirPath.toFile(), blastDb);
        } catch (IOException ioe) {
            throw new BlastException(ioe);
        }
    }

    private static void formatBlastDb(File fastaFileDir, String fastaFileName, String blastDb) throws BlastException {
        ArrayList<String> commands = new ArrayList<>();
        String makeBlast = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator + "makeblastdb";

        commands.add(makeBlast);
        commands.add("-dbtype nucl");
        commands.add("-in");
        commands.add(fastaFileName);
        commands.add("-logfile");
        commands.add(blastDb + ".log");
        commands.add("-out");
        commands.add(blastDb + File.separator + "ice");
//        commands.add("-title");
//        commands.add("ICE Blast DB");
        String commandString = Utils.join(" ", commands);
        Logger.info("makeblastdb: " + commandString);

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
                Logger.error(errorString);
                throw new IOException("Could not make blast db");
            }
        } catch (InterruptedException e) {
            throw new BlastException("Could not run makeblastdb", e);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    private static void renameBlastDb(File newBigFastaFileDir, String baseBlastDirName) throws IOException, BlastException {
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
     * Retrieve all the sequences from the database, and writes it out to a fasta file on disk.
     *
     * @param writer filewriter to write to.
     * @throws BlastException
     */
    private static void writeBigFastaFile(BufferedWriter writer) throws BlastException {
        List<Sequence> sequencesList;
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        try {
            sequencesList = sequenceController.getAllSequences(); //TODO potential performance impact
        } catch (ControllerException e) {
            throw new BlastException(e);
        }
        for (Sequence sequence : sequencesList) {
            if (sequence.getEntry().getVisibility().intValue() != Visibility.OK.getValue())
                continue;

            long id = sequence.getEntry().getId();
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
                    String idString = ">" + id;
                    idString += ":" + (circular ? "c" : "l");
                    idString += ":" + sequence.getEntry().getRecordType();
                    String name = sequence.getEntry().getOneName() == null ? "None" : sequence.getEntry().getOneName().getName();
                    idString += ":" + name;
                    Set<PartNumber> numbers = sequence.getEntry().getPartNumbers();
                    PartNumber partNumber = numbers == null || numbers.isEmpty() ? null : (PartNumber) numbers.toArray()[0];
                    String pNumber = partNumber == null ? "None" : partNumber.getPartNumber();
                    idString += ":" + pNumber;
                    idString += ":" + sequenceLength;
                    idString += "\n";
                    writer.write(idString);
                    writer.write(sequenceString + "\n");
                } catch (IOException e) {
                    throw new BlastException(e);
                }
            }
        }
    }

    private static void deleteDirectoryIfExists(Path path) throws BlastException {
        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new DeleteDirVisitor());
            } catch (IOException e) {
                throw new BlastException(e);
            }
        }
    }

    public static class DeleteDirVisitor extends SimpleFileVisitor<Path> {

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

    public static class CopyDirVisitor extends SimpleFileVisitor<Path> {

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
