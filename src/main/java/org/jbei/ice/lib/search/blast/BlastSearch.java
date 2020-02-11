package org.jbei.ice.lib.search.blast;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlastSearch {

    private final Path indexPath;
    private final String dbName;

    public BlastSearch(Path indexPath, String dbName) {
        if (!Files.exists(indexPath))
            throw new IllegalArgumentException("Cannot access index at " + indexPath);

        this.indexPath = indexPath;
        this.dbName = dbName;
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
    public String run(BlastQuery query, String... options) throws BlastException {
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
            if (options.length > 0)
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
