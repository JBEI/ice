package org.jbei.ice.lib.search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.lib.utils.Utils;

import java.util.Collections;

import java.util.Arrays;

public class Blast {

    private static boolean rebuilding;

    private String blastBlastall;
    private String blastDatabaseName;
    private String bigFastaFile = "bigfastafile";
    private String blastFormatLogFile;

    public Blast() {
        blastBlastall = JbeirSettings.getSetting("BLAST_BLASTALL");
        blastDatabaseName = JbeirSettings.getSetting("BLAST_DIRECTORY") + File.separator
                + JbeirSettings.getSetting("BLAST_DATABASE_NAME");

        blastFormatLogFile = JbeirSettings.getSetting("BLAST_DATABASE_NAME") + ".log";

        if (!isBlastDatabaseExists()) {
            Logger.info("Creating blast db for the first time");
            JobCue jobCue = JobCue.getInstance();
            jobCue.addJob(Job.REBUILD_BLAST_INDEX);
            jobCue.processIn(1000);
        }
    }

    private boolean isBlastDatabaseExists() {
        File blastDatabaseFile = new File(blastDatabaseName + ".nsq");
        return blastDatabaseFile.exists();
    }

    private String breakUpLines(String input) {
        String result = "";

        int counter = 0;
        int index = 0;
        int end = input.length();
        while (index < end) {
            result = result + input.substring(index, index + 1);
            counter = counter + 1;
            index = index + 1;

            if (counter == 59) {
                result = result + "\n";
                counter = 0;
            }
        }
        return result;
    }

    public void rebuildDatabase() {
        Logger.info("Rebuilding blast database");
        try { // The big try
            File newbigFastaFileDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY") + ".new");
            newbigFastaFileDir.mkdir();
            File bigFastaFile = new File(newbigFastaFileDir.getPath() + File.separator
                    + this.bigFastaFile);
            FileWriter bigFastaWriter = new FileWriter(bigFastaFile);
            writeBigFastaFile(bigFastaWriter);
            formatBlastDb(newbigFastaFileDir);
            synchronized (this) {
                setRebuilding(true);
                renameBlastDb(newbigFastaFileDir);
                setRebuilding(false);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void renameBlastDb(File newBigFastaFileDir) throws IOException {
        File oldBlastDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY") + ".old");
        if (oldBlastDir.exists()) {
            FileUtils.deleteDirectory(oldBlastDir);
        }
        oldBlastDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY") + ".old");
        File currentBlastDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY"));
        currentBlastDir.renameTo(oldBlastDir);
        currentBlastDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY"));
        File newBlastDir = new File(JbeirSettings.getSetting("BLAST_DIRECTORY") + ".new");
        newBlastDir.renameTo(currentBlastDir);
    }

    private void formatBlastDb(File bigFastaFileDir) throws IOException {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(JbeirSettings.getSetting("BLAST_FORMATDB"));
        commands.add("-i");
        commands.add(this.bigFastaFile);
        commands.add("-l");
        commands.add(this.blastFormatLogFile);
        commands.add("-n");
        commands.add(JbeirSettings.getSetting("BLAST_DATABASE_NAME"));
        commands.add("-o");
        commands.add("-pF");
        commands.add("-t");
        commands.add(JbeirSettings.getSetting("BLAST_DATABASE_NAME"));
        String commandString = Utils.join(" ", commands);
        Logger.info("formatdb: " + commandString);

        Runtime runTime = Runtime.getRuntime();

        Process process = runTime.exec(commandString, new String[0], bigFastaFileDir);
        InputStream blastOutputStream = process.getInputStream();
        InputStream blastErrorStream = process.getErrorStream();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Could not run formatdb", e);
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(blastOutputStream, writer);
        blastOutputStream.close();
        String outputString = writer.toString();
        Logger.info("format output was: " + outputString);
        writer = new StringWriter();
        IOUtils.copy(blastErrorStream, writer);
        String errorString = writer.toString();
        Logger.info("format error was: " + errorString);
        process.destroy();

        if (errorString.length() > 0) {
            throw new IOException("Could not format blast db");
        }
    }

    private void writeBigFastaFile(FileWriter bigFastaWriter) throws IOException {
        List<Sequence> sequencesList = SequenceManager.getAll();
        for (Sequence sequence : sequencesList) {
            String recordId = sequence.getEntry().getRecordId();
            String sequenceString = "";

            String temp = sequence.getSequence();
            if (temp != null) {
                SymbolList symL = null;
                try {
                    symL = DNATools.createDNA(sequence.getSequence().trim());
                } catch (IllegalSymbolException e1) {
                    // maybe it's rna?
                    try {
                        symL = RNATools.createRNA(sequence.getSequence().trim());
                    } catch (IllegalSymbolException e2) {
                        // skip this sequence
                        Logger.warn("invalid characters in sequence for "
                                + sequence.getEntry().getRecordId());
                        Logger.warn(e2.toString());
                    }
                }
                if (symL != null) {
                    sequenceString = breakUpLines(symL.seqString());
                }
            }
            if (sequenceString.length() > 0) {
                bigFastaWriter.write(">" + recordId + "\n");
                bigFastaWriter.write(sequenceString + "\n");
            }
        }
        bigFastaWriter.flush();
        bigFastaWriter.close();
    }

    /**
     * get only the longest distinct matches. No partial matches for the
     * same record.
     * 
     * @param queryString
     *            Sequence to be queried
     * @return
     */
    public ArrayList<BlastResult> queryDistinct(String queryString, String blastProgram) {
        ArrayList<BlastResult> tempResults = query(queryString, blastProgram);

        HashMap<String, BlastResult> tempHashMap = new HashMap<String, BlastResult>();

        for (BlastResult result : tempResults) {
            if (tempHashMap.containsKey(result.getSubjectId())) {
                BlastResult currentResult = tempHashMap.get(result.getSubjectId());
                if (result.getRelativeScore() > currentResult.getRelativeScore()) {
                    tempHashMap.put(result.getSubjectId(), result);
                }
            } else {
                tempHashMap.put(result.getSubjectId(), result);
            }

        }

        ArrayList<BlastResult> outputResult = new ArrayList<BlastResult>(tempHashMap.values());
        Collections.sort(outputResult);

        return outputResult;
    }

    /**
     * Standard blast query
     * 
     * @param queryString
     * @return
     */
    public ArrayList<BlastResult> query(String queryString, String blastProgram) {
        ArrayList<BlastResult> result = new ArrayList<BlastResult>();

        if (!isBlastDatabaseExists()) {
            Logger.info("Creating blast database for the first time");
            JobCue jobCue = JobCue.getInstance();
            jobCue.addJob(Job.REBUILD_BLAST_INDEX);
            jobCue.processIn(5000);
        } else {

            while (isRebuilding()) {
                try {
                    wait(50);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            Runtime runTime = Runtime.getRuntime();
            String blastProgramName = "blastn";
            if (blastProgram.equals("tblastx")) {
                blastProgramName = "tblastx";
            }

            ArrayList<String> commands = new ArrayList<String>();
            commands.add(blastBlastall);
            commands.add("-p");
            commands.add(blastProgramName);
            commands.add("-d");
            commands.add(blastDatabaseName);
            commands.add("-m");
            commands.add("8");

            String commandString = Utils.join(" ", commands);

            Logger.info("Blast query: " + commandString);
            String outputString = "";

            try {
                Process process = runTime.exec(commandString);
                InputStream blastOutputStream = process.getInputStream();
                OutputStream blastInputStream = process.getOutputStream();

                blastInputStream.write(queryString.getBytes());
                blastInputStream.flush();
                blastInputStream.close();
                process.waitFor();

                StringWriter writer = new StringWriter();
                IOUtils.copy(blastOutputStream, writer);
                outputString = writer.toString();

                blastOutputStream.close();
                process.destroy();
                result = processBlastOutput(outputString);
            } catch (IOException e) {
                // Exception from exec()
                e.printStackTrace();
            } catch (InterruptedException e) {
                // Exception from waitFor()
                e.printStackTrace();
            }
        }
        return result;
    }

    public ArrayList<BlastResult> processBlastOutput(String blastOutput) {
        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(blastOutput.split("\n")));

        ArrayList<BlastResult> blastResults = new ArrayList<BlastResult>();

        for (String line : lines) {
            String[] columns = line.split("\t");
            if (columns.length == 12) {
                BlastResult blastResult = new BlastResult();

                blastResult.setQueryId(columns[0]);
                blastResult.setSubjectId(columns[1]);
                blastResult.setPercentId(Float.parseFloat(columns[2]));
                blastResult.setAlignmentLength(Integer.parseInt(columns[3]));
                blastResult.setMismatches(Integer.parseInt(columns[4]));
                blastResult.setGapOpenings(Integer.parseInt(columns[5]));
                blastResult.setqStart(Integer.parseInt(columns[6]));
                blastResult.setqEnd(Integer.parseInt(columns[7]));
                blastResult.setsStart(Integer.parseInt(columns[8]));
                blastResult.setsEnd(Integer.parseInt(columns[9]));
                blastResult.seteValue(Float.parseFloat(columns[10]));
                blastResult.setBitScore(Float.parseFloat(columns[11]));
                blastResult.setRelativeScore(blastResult.getPercentId()
                        * blastResult.getAlignmentLength() * blastResult.getBitScore());
                blastResults.add(blastResult);
            }
        }
        return blastResults;
    }

    public static void main(String[] args) {

        Blast b = new Blast();
        b.rebuildDatabase();

    }

    public static synchronized void setRebuilding(boolean rebuilding) {
        Blast.rebuilding = rebuilding;
    }

    public static synchronized boolean isRebuilding() {
        return rebuilding;
    }
}
