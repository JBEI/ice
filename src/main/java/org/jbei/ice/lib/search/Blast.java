package org.jbei.ice.lib.search;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class Blast {

    private static boolean rebuilding;

    private String blastBlastall;
    private String bl2seq;
    private String blastDatabaseName;
    private String bigFastaFile = "bigfastafile";
    private String blastFormatLogFile;

    public Blast() {
        blastBlastall = JbeirSettings.getSetting("BLAST_BLASTALL");
        bl2seq = JbeirSettings.getSetting("BLAST_BL2SEQ");
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
        // String result = "";
        StringBuilder result = new StringBuilder();
        int counter = 0;
        int index = 0;
        int end = input.length();
        while (index < end) {
            result = result.append(input.substring(index, index + 1));
            counter = counter + 1;
            index = index + 1;

            if (counter == 59) {
                result = result.append("\n");
                counter = 0;
            }
        }
        return result.toString();
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
            String msg = "Rebuild blast database failed: ";
            Logger.error(msg + e1.toString(), e1);
        } catch (SecurityException e) {
            String msg = "Rebuild blast database failed: ";
            Logger.error(msg + e.toString(), e);
        } catch (Exception e) {
            String msg = "Rebuild blast database failed: ";
            Logger.error(msg + e.toString(), e);
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
        try {
            newBlastDir.renameTo(currentBlastDir);
        } catch (SecurityException e) {
            String msg = "Could not rename Blast db" + e.toString();
            Logger.error(msg, e);
        } catch (NullPointerException e) {
            String msg = "Could not rename Blast db" + e.toString();
            Logger.error(msg, e);
        }
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
        List<Sequence> sequencesList = SequenceManager.getAllVisible();
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
     * @throws ProgramTookTooLongException
     * @throws BlastException
     */
    public ArrayList<BlastResult> queryDistinct(String queryString, String blastProgram)
            throws ProgramTookTooLongException, BlastException {
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
     * @throws ProgramTookTooLongException
     * @throws BlastException
     */
    public ArrayList<BlastResult> query(String queryString, String blastProgram)
            throws ProgramTookTooLongException, BlastException {
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
                    throw new BlastException(e);
                }
            }

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

            result = processBlastOutput(runExternalProgram(queryString, commandString));
        }
        return result;
    }

    public List<String> runBl2Seq(String reference, List<String> subjects) throws BlastException,
            ProgramTookTooLongException {
        ArrayList<String> result = new ArrayList<String>();

        String resultItem = "";
        String dataDirectory = JbeirSettings.getSetting("DATA_DIRECTORY");
        String referenceFileName = Utils.encryptSHA(reference);
        File referenceFile = new File(dataDirectory + File.separator + referenceFileName);
        try {
            FileWriter referenceFileWriter = new FileWriter(referenceFile);
            referenceFileWriter.write(reference);
            referenceFileWriter.close();
        } catch (IOException e) {
            throw new BlastException(e);
        }

        for (String subject : subjects) {
            String subjectFileName = Utils.encryptSHA(subject);
            File subjectFile = new File(dataDirectory + File.separator + subjectFileName);
            try {
                FileWriter subjectFileWriter = new FileWriter(subjectFile);
                subjectFileWriter.write(subject);
                subjectFileWriter.close();
            } catch (IOException e) {
                throw new BlastException(e);
            }
            ArrayList<String> commands = new ArrayList<String>();
            commands.add(bl2seq);
            commands.add("-p");
            commands.add("blastn");
            commands.add("-i");
            commands.add(referenceFile.getPath());
            commands.add("-j");
            commands.add(subjectFile.getPath());
            String commandString = Utils.join(" ", commands);
            Logger.info("Bl2seq query: " + commandString);
            resultItem = runExternalProgram(commandString);

            result.add(resultItem);
            subjectFile.delete();
        }
        referenceFile.delete();
        return result;
    }

    /**
     * Run bl2seq. Because bl2seq needs two inputs, it has to use external files.
     * 
     * @param reference
     * @param subject
     * @return
     * @throws BlastException
     * @throws ProgramTookTooLongException
     */
    public String runBl2Seq(String reference, String subject) throws BlastException,
            ProgramTookTooLongException {
        String result = "";
        String dataDirectory = JbeirSettings.getSetting("DATA_DIRECTORY");
        String referenceFileName = Utils.encryptSHA(reference);
        File referenceFile = new File(dataDirectory + File.separator + referenceFileName);
        String subjectFileName = Utils.encryptSHA(subject);
        File subjectFile = new File(dataDirectory + File.separator + subjectFileName);
        try {
            FileWriter referenceFileWriter = new FileWriter(referenceFile);
            referenceFileWriter.write(reference);
            referenceFileWriter.close();
        } catch (IOException e) {
            throw new BlastException(e);
        }

        try {
            FileWriter subjectFileWriter = new FileWriter(subjectFile);
            subjectFileWriter.write(subject);
            subjectFileWriter.close();
        } catch (IOException e) {
            throw new BlastException(e);
        }
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(bl2seq);
        commands.add("-p");
        commands.add("blastn");
        commands.add("-i");
        commands.add(referenceFile.getPath());
        commands.add("-j");
        commands.add(subjectFile.getPath());

        String commandString = Utils.join(" ", commands);
        Logger.info("Bl2seq query: " + commandString);
        result = runExternalProgram(commandString);

        referenceFile.delete();
        subjectFile.delete();

        return result;
    }

    private String runExternalProgram(String commandString) throws ProgramTookTooLongException,
            BlastException {
        return runExternalProgram("", commandString);
    }

    private String runExternalProgram(String inputString, String commandString)
            throws ProgramTookTooLongException, BlastException {

        Runtime runTime = Runtime.getRuntime();
        String outputString = "";
        String errorString = "";

        try {
            Process process = runTime.exec(commandString);
            // output from program
            BufferedInputStream programOutputStream = new BufferedInputStream(process
                    .getInputStream());
            // error from program
            BufferedInputStream programErrorStream = new BufferedInputStream(process
                    .getErrorStream());

            if (inputString.length() > 0) {
                // Input into program
                BufferedOutputStream programInputStream = new BufferedOutputStream(process
                        .getOutputStream());

                programInputStream.write(inputString.getBytes());
                programInputStream.flush();
                programInputStream.close();
            }

            byte[] outputBuffer = new byte[4096];
            byte[] errorBuffer = new byte[4096];

            long maxWait = 4000L;
            long startTime = System.currentTimeMillis();
            while (true) {
                // consume output stream to prevent process from blocking
                int temp = programOutputStream.read(outputBuffer);
                int temp2 = programErrorStream.read(errorBuffer);
                if (temp2 != -1) {
                    errorString = errorString + new String(errorBuffer);
                }

                if (temp == -1) {
                    break;
                } else {
                    outputString = outputString + new String(outputBuffer);
                }

                if (System.currentTimeMillis() - startTime > maxWait) {
                    throw new ProgramTookTooLongException();
                }
            }

            programOutputStream.close();
            process.destroy();

        } catch (IOException e) {
            Logger.warn("IO exception in running external program: " + e.toString());
        }

        if (errorString.length() > 0) {
            throw new BlastException(errorString);
        }
        return outputString;
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
        String reference = "ATGAGCAAAGGCGAAGAACTGTTTACCGGCGTGGTGCCGATTCTGGTGGAACTGGATGGCGATGTGAACGGCCATAAATTTAGCGTGAGCGGCGAAGGCGAAGGCGATGCGACCTATGGCAAACTGACCCTGAAATTTATTTGCACCACCGGCAAACTGCCGGTGCCGTGGCCGACCCTGGTGACCACCTTTACCTATGGCGTGCAGTGCTTTGCGCGCTATCCGGATCATATGAAACAGCATGATTTTTTTAAAAGCGCGATGCCGGAAGGCTATGTGCAGGAACGCACCATTTTTTTTAAAGATGATGGCAACTATAAAACCCGCGCGGAAGTGAAATTTGAAGGCGATACCCTGGTGAACCGCATTGAACTGAAAGGCATTGATTTTAAAGAAGATGGCAACATTCTGGGCCATAAACTGGAATATAACTATAACAGCCATAAAGTGTATATTACCGCGGATAAACAGAAAAACGGCATTAAAGTGAACTTTAAAACCCGCCATAACATTGAAGATGGCAGCGTGCAGCTGGCGGATCATTATCAGCAGAACACCCCGATTGGCGATGGCCCGGTGCTGCTGCCGGATAACCATTATCTGAGCACCCAGAGCGCGCTGAGCAAAGATCCGAACGAAAAACGCGATCATATGGTGCTGCTGGAATTTGTGACCGCGGCGGGCATTACCCATGGCATGGATGAACTGTATAAAa";
        String subject = "ATGAGCAAAGGCGAAGAACTGTTTACCGGCGTGGTGCCGATTCTGGTGGAACTGGATGGCGATGTGAACGGCCATAAATTTAGCGTGCGCGGCGAAGGCGAAGGCGATGCGACCAACGGCAAACTGACCCTGAAATTTATTTGCACCACCGGCAAACTGCCGGTGCCGTGGCCGACCCTGGTGACCACCCTGACCTATGGCGTGCAGTGCTTTAGCCGCTATCCGGATCATATGAAACAGCATGATTTTTTTAAAAGCGCGATGCCGGAAGGCTATGTGCAGGAACGCACCATTAGCTTTAAAGATGATGGCACCTATAAAACCCGCGCGGAAGTGAAATTTGAAGGCGATACCCTGGTGAACCGCATTGAACTGAAAGGCATTGATTTTAAAGAAGATGGCAACATTCTGGGCCATAAACTGGAATATAACTTTAACAGCCATAACGTGTATATTACCGCGGATAAACAGAAAAACGGCATTAAAGCGAACTTTAAAATTCGCCATAACGTGGAAGATGGCAGCGTGCAGCTGGCGGATCATTATCAGCAGAACACCCCGATTGGCGATGGCCCGGTGCTGCTGCCGGATAACCATTATCTGAGCACCCAGAGCGTGCTGAGCAAAGATCCGAACGAAAAACGCGATCATATGGTGCTGCTGGAATTTGTGACCGCGGCGGGCATTACCCATGGCATGGATGAACTGTATAAA";
        String subject2 = "ATGAGCAAAGGCGAAGAACTGTTTACCGGCGTGGTGCCGATTCTGGTGGAACTGGATGGCGATGTGAACGGCCATAAATTTAGCGTGAGCGGCGAAGGCGAAGGCGATGCGACCTATGGCAAACTGACCCTGAAATTTATTTGCACCACCGGCAAACTGCCGGTGCCGTGGCCGACCCTGGTGACCACCCTGACCTATGGCGTGCAGTGCTTTAGCCGCTATCCGGATCATATGAAACAGCATGATTTTTTTAAAAGCGCGATGCCGGAAGGCTATGTGCAGGAACGCACCATTTTTTTTAAAGATGATGGCAACTATAAAACCCGCGCGGAAGTGAAATTTGAAGGCGATACCCTGGTGAACCGCATTGAACTGAAAGGCATTGATTTTAAAGAAGATGGCAACATTCTGGGCCATAAACTGGAATATAACTATAACAGCCATAACGTGTATATTATGGCGGATAAACAGAAAAACGGCATTAAAGTGAACTTTAAAATTCGCCATAACATTGAAGATGGCAGCGTGCAGCTGGCGGATCATTATCAGCAGAACACCCCGATTGGCGATGGCCCGGTGCTGCTGCCGGATAACCATTATCTGAGCACCCAGAGCGCGCTGAGCAAAGATCCGAACGAAAAACGCGATCATATGGTGCTGCTGGAATTTGTGACCGCGGCGGGCATTACCCATGGCATGGATGAACTGTATAAA";
        try {
            String result = b.runBl2Seq(reference, subject);
            System.out.println(result);
        } catch (BlastException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProgramTookTooLongException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ArrayList<String> list = new ArrayList<String>();
        list.add(subject);
        list.add(subject2);
        try {
            List<String> results = b.runBl2Seq(reference, list);
            for (String result : results) {
                System.out.println("###############");
                System.out.println(result);
            }
        } catch (BlastException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProgramTookTooLongException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static synchronized void setRebuilding(boolean rebuilding) {
        Blast.rebuilding = rebuilding;
    }

    private static synchronized boolean isRebuilding() {
        return rebuilding;
    }

}
