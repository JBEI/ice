package org.jbei.ice.lib.search.blast;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.IndexBuildStatus;
import org.jbei.ice.lib.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Enables (command line) interaction with BLAST+
 * <p>
 * Current usage is for blast searches and auto-annotation support
 *
 * @author Hector Plahar
 */
public class BlastPlus {

    private Path getBlastInstallDirectory() {
        return Paths.get(Utils.getConfigValue(ConfigurationKey.BLAST_INSTALL_DIR));
    }

    /**
     * Determines if blast can be run on this instance based on whether there is a valid blast installation
     * set
     *
     * @return true if blast can be run, false otherwise
     */
    public boolean canRunBlast() {
        Path path = getBlastInstallDirectory();
        return Files.exists(path) && Files.isExecutable(Paths.get(path.toString(), BlastProgram.BLAST_N.getName()));
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

    public void formatBlastDb(BlastFastaFile fastaFile, String dbName) throws BlastException {
        if (!canRunBlast()) {
            Logger.error("Cannot format blast db due to invalid blast installation");
            return;
        }

        ArrayList<String> commands = new ArrayList<>();
        Path filePath = fastaFile.getFilePath();
        String makeBlastDbCmd = getBlastInstallDirectory().toAbsolutePath().toString() + File.separator + "makeblastdb";
        commands.add(makeBlastDbCmd);
        commands.add("-dbtype nucl");
        commands.add("-in");
        commands.add(filePath.getFileName().toString());
        commands.add("-logfile");
        commands.add(dbName + ".log");
        commands.add("-out");
        commands.add(dbName);
//        commands.add("-title");
//        commands.add("ICE Blast DB");
        String commandString = Utils.join(" ", commands);
        Logger.info("makeblastdb: " + commandString);

        Runtime runTime = Runtime.getRuntime();

        try {
            Process process = runTime.exec(commandString, new String[0], filePath.getParent().toFile());
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
            throw new BlastException("Could not run makeblastdb [BlastDBPath is " + getBlastInstallDirectory().toString() + "]", e);
        } catch (IOException e) {
            throw new BlastException(e);
        }
    }

    public IndexBuildStatus getStatus() {
//        Path path = Paths.get(indexPath.toString(), LOCK_FILE_NAME);
//
//        if (Files.exists(path)) {
//            try {
//                BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
//                long seconds = attributes.creationTime().to(TimeUnit.SECONDS);
//                if ((Instant.now().getEpochSecond() - seconds) > 3 * 24 * 60) {
//                    Files.delete(path);
//                    return new IndexBuildStatus(0, 0);
//                }
//            } catch (IOException e) {
//                // ok to ignore
//            }
//            return new IndexBuildStatus(0, 1);
//        }
        return new IndexBuildStatus(0, 0);
    }
}
