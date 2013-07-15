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
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryInfo;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.shared.dto.entry.PartInfo;
import org.jbei.ice.lib.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.lib.shared.dto.entry.StrainInfo;
import org.jbei.ice.lib.shared.dto.search.BlastProgram;
import org.jbei.ice.lib.shared.dto.search.BlastQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResultInfo;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

/**
 * Blast Search functionality for BLAST+
 *
 * @author Hector Plahar
 */
public class BlastPlus {

    private static final String BLAST_DB_FOLDER = "ice_blast";
    private static final String BLAST_DB_NAME = "ice";
    private static final String DELIMITER = ",";

    public static HashMap<String, SearchResultInfo> runBlast(Account account, BlastQuery query) throws BlastException {
        try {
            String command = Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR) + File.separator
                    + query.getBlastProgram().getName();
            String blastDb = Utils.getConfigValue(ConfigurationKey.BLAST_DIR) + File.separator + BLAST_DB_FOLDER
                    + File.separator + BLAST_DB_NAME;
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

    private static SearchResultInfo parseSequenceIdentifier(String line) {
        long id;
        EntryType recordType;
        String name;
        String partNumber;
        SearchResultInfo info = null;

        // new record
        String[] idLineFields = line.substring(1).split(DELIMITER);
        if (idLineFields.length == 4) {
            id = Long.decode(idLineFields[0]);
            recordType = EntryType.nameToType(idLineFields[1]);
            name = idLineFields[2];
            partNumber = idLineFields[3];

            EntryInfo view;
            switch (recordType) {
                case PART:
                default:
                    view = new PartInfo();
                    break;

                case ARABIDOPSIS:
                    view = new ArabidopsisSeedInfo();
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

            info = new SearchResultInfo();
            info.setEntryInfo(view);

            try {
                String summary = ControllerFactory.getEntryController().getEntrySummary(info.getEntryInfo().getId());
                info.getEntryInfo().setShortDescription(summary);
            } catch (ControllerException e) {
                Logger.error(e);
            }
//                searchResult.setAlignmentLength(alignmentLength);
//                searchResult.setPercentId(percentId);
        }
        return info;
    }

    private static HashMap<String, SearchResultInfo> processBlastOutput(String blastOutput, int queryLength) {
        HashMap<String, SearchResultInfo> hashMap = new HashMap<>();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(blastOutput.split("\n")));

        for (int i = 0; i < lines.size(); i += 1) {
            String line = lines.get(i);

            if (line.trim().isEmpty() || !line.startsWith(">"))
                continue;


            // process alignment details for above match
            SearchResultInfo info = parseSequenceIdentifier(line.substring(1));
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

                // bit score and evalue
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
                SearchResultInfo currentResult = hashMap.get(idString);
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

    private static void renameBlastDb(File newBigFastaFileDir, String baseBlastDirName)
            throws IOException, BlastException {
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
        Set<Sequence> sequencesList;
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        try {
            sequencesList = sequenceController.getAllSequences();
        } catch (ControllerException e) {
            throw new BlastException(e);
        }
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
                    String name = sequence.getEntry().getOneName() == null ? "None" : sequence.getEntry().getOneName()
                                                                                              .getName();
                    idString += DELIMITER + name;
                    Set<PartNumber> numbers = sequence.getEntry().getPartNumbers();
                    PartNumber partNumber = numbers == null || numbers.isEmpty() ? null : (PartNumber) numbers
                            .toArray()[0];
                    String pNumber = partNumber == null ? "None" : partNumber.getPartNumber();
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
