package org.jbei.ice.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SequenceAnalysisPermissionVerifier;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.TraceSequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.parsers.ABIParser;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.SequenceTraceFile;

/**
 * ABI to manipulate DNA sequence trace analysis
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class SequenceAnalysisController extends Controller {
    public SequenceAnalysisController(Account account) {
        super(account, new SequenceAnalysisPermissionVerifier());
    }

    /**
     * Check if the user has read permission for {@link TraceSequence}.
     * 
     * @param traceSequence
     * @return True if user has read permission for traceSequence
     */
    public boolean hasReadPermission(TraceSequence traceSequence) {
        return getPermissionVerifier().hasReadPermissions(traceSequence, getAccount());
    }

    /**
     * Check if the user has write permission for {@link TraceSequence}.
     * 
     * @param traceSequence
     * @return True if user has write permission for traceSequence
     */
    public boolean hasWritePermission(TraceSequence traceSequence) {
        return getPermissionVerifier().hasWritePermissions(traceSequence, getAccount());
    }

    /**
     * Create a new {@link TraceSequence} record and associated with the {@link Entry} entry.
     * <p>
     * Creates a database record and write the inputStream to disk.
     * 
     * @param entry
     * @param filename
     * @param depositor
     * @param sequence
     * @param uuid
     * @param date
     * @param inputStream
     * @return Saved traceSequence
     * @throws ControllerException
     */
    public TraceSequence importTraceSequence(Entry entry, String filename, String depositor,
            String sequence, String uuid, Date date, InputStream inputStream)
            throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to save trace sequence with null entry!");
        }

        if (filename == null || filename.isEmpty()) {
            throw new ControllerException("Failed to save trace sequence without filename!");
        }

        if (sequence == null || sequence.isEmpty()) {
            throw new ControllerException("Failed to save trace sequence without sequence!");
        }

        TraceSequence traceSequence = new TraceSequence(entry, uuid, filename, depositor, sequence,
                date);

        TraceSequence savedTraceSequence = null;
        try {
            savedTraceSequence = TraceSequenceManager.create(traceSequence, inputStream);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return savedTraceSequence;
    }

    /**
     * Create a new {@link TraceSequence} record and associated with the {@link Entry} entry.
     * <p>
     * Unlike importTraceSequence this method auto generates uuid and timestamp.
     * 
     * @param entry
     * @param filename
     * @param depositor
     * @param sequence
     * @param inputStream
     * @return Saved traceSequence
     * @throws ControllerException
     */
    public TraceSequence uploadTraceSequence(Entry entry, String filename, String depositor,
            String sequence, InputStream inputStream) throws ControllerException {

        return importTraceSequence(entry, filename, depositor, sequence, Utils.generateUUID(),
            new Date(), inputStream);
    }

    /**
     * Remove a {@link TraceSequence} from the database and disk.
     * 
     * @param traceSequence
     * @throws ControllerException
     * @throws PermissionException
     */
    public void removeTraceSequence(TraceSequence traceSequence) throws ControllerException,
            PermissionException {
        if (traceSequence == null) {
            throw new ControllerException("Failed to delete null Trace Sequence!");
        }

        if (!hasWritePermission(traceSequence)) {
            throw new PermissionException("No permissions to delete trace sequence!");
        }

        try {
            TraceSequenceManager.delete(traceSequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve the {@link TraceSequence} associated with the given {@link Entry} entry.
     * 
     * @param entry
     * @return Retrieved TraceSequence
     * @throws ControllerException
     */
    public List<TraceSequence> getTraceSequences(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to get trace sequences for null entry!");
        }

        List<TraceSequence> traces = null;

        SequenceController sequenceController = new SequenceController(getAccount());

        try {
            Sequence sequence = sequenceController.getByEntry(entry);

            if (sequence == null) { // it will remove invalid alignments
                rebuildAllAlignments(entry);

                traces = TraceSequenceManager.getByEntry(entry);
            } else {
                traces = TraceSequenceManager.getByEntry(entry);

                boolean wasUpdated = false;
                for (TraceSequence traceSequence : traces) {
                    if (traceSequence.getTraceSequenceAlignment() == null
                            || traceSequence.getTraceSequenceAlignment().getSequenceHash() == null
                            || traceSequence.getTraceSequenceAlignment().getSequenceHash()
                                    .isEmpty()
                            || !traceSequence.getTraceSequenceAlignment().getSequenceHash()
                                    .equals(sequence.getFwdHash())) {
                        buildOrRebuildAlignment(traceSequence, sequence);

                        wasUpdated = true;
                    }
                }

                if (wasUpdated) { // fetch again because alignment has been updated
                    traces = TraceSequenceManager.getByEntry(entry);
                }
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return traces;
    }

    public TraceSequence getTraceSequenceByFileId(String fileId) throws ControllerException {
        TraceSequence traceSequence = null;
        try {
            traceSequence = TraceSequenceManager.getByFileId(fileId);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return traceSequence;

    }

    /**
     * Parses a given sequence file (Genbank, Fasta, ABI) and return an {@link IDNASequence}.
     * 
     * @param bytes
     * @return Parsed Sequence as {@link IDNASequence}.
     * @throws ControllerException
     */
    public IDNASequence parse(byte[] bytes) throws ControllerException {
        if (bytes.length == 0) {
            return null;
        }

        // Trying to parse as Fasta, Genbank, etc
        IDNASequence dnaSequence = GeneralParser.getInstance().parse(bytes);

        if (dnaSequence == null) {
            // Trying to parse as ABI

            ABIParser abiParser = new ABIParser();

            try {
                dnaSequence = abiParser.parse(bytes);
            } catch (InvalidFormatParserException e) {
                return null;
            }
        }

        return dnaSequence;
    }

    /**
     * Retrieve the {@link File} associated with the given {@link TraceSequence}.
     * 
     * @param traceSequence
     * @return {@link File} object.
     * @throws ControllerException
     */
    public File getFile(TraceSequence traceSequence) throws ControllerException {
        File result = null;

        try {
            result = TraceSequenceManager.getFile(traceSequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Retrieve the {@link SequenceTraceFile} value object of the given TraceSequence.
     * 
     * @param traceSequence
     * @return SequenceTraceFile object
     * @throws ControllerException
     */
    public SequenceTraceFile getSequenceTraceFile(TraceSequence traceSequence)
            throws ControllerException {
        if (traceSequence == null) {
            return null;
        }

        String base64Data = null;

        File file = getFile(traceSequence);

        byte[] bytes = null;

        try {
            FileInputStream fileStream = new FileInputStream(file);
            bytes = new byte[(int) (file.length())];

            fileStream.read(bytes);

        } catch (FileNotFoundException e) {
            throw new ControllerException(e);
        } catch (IOException e) {
            throw new ControllerException(e);
        }

        base64Data = SerializationUtils.serializeBytesToString(bytes);
        SequenceTraceFile result = new SequenceTraceFile();

        result.setDepositorEmail(traceSequence.getDepositor());
        result.setFileId(traceSequence.getFileId());
        result.setFileName(traceSequence.getFilename());
        result.setTimeStamp(new Date());

        result.setBase64Data(base64Data);

        return result;
    }

    /**
     * Retrieve the number of {@link TraceSequence}s associated with the given {@link Entry}.
     * 
     * @param entry
     * @return Number of trace sequences.
     * @throws ControllerException
     */
    public long getNumberOfTraceSequences(Entry entry) throws ControllerException {
        long result = 0;

        try {
            result = TraceSequenceManager.getNumberOfTraceSequences(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    /**
     * Calculate sequence alignment between the given {@link TraceSequence} and {@link Sequence}
     * using bl2seq, and save the result into the database.
     * 
     * @param traceSequence
     * @param sequence
     * @throws ControllerException
     */
    public void buildOrRebuildAlignment(TraceSequence traceSequence, Sequence sequence)
            throws ControllerException {
        if (traceSequence == null) {
            throw new ControllerException("Failed to rebuild alignment for null trace sequence!");
        }

        // if sequence is null => delete alignment
        if (sequence == null || sequence.getEntry() == null) {
            return;
        }

        // actually build alignment
        String traceSequenceString = traceSequence.getSequence();
        String entrySequenceString = sequence.getSequence();

        int entrySequenceLength = entrySequenceString.length();

        String bl2seqOutput = "";

        boolean isCircular = (sequence.getEntry() instanceof Plasmid)
                && ((Plasmid) sequence.getEntry()).getCircular();

        if (isCircular) {
            entrySequenceString += entrySequenceString;
        }

        Blast blast = new Blast();
        try {
            bl2seqOutput = blast.runBl2Seq(entrySequenceString, traceSequenceString);
        } catch (BlastException e) {
            throw new ControllerException(e);
        } catch (ProgramTookTooLongException e) {
            throw new ControllerException(e);
        }

        if (bl2seqOutput == null || bl2seqOutput.isEmpty()) {
            return;
        }

        try {
            List<Bl2SeqResult> bl2seqAlignmentResults = Bl2SeqParser.parse(bl2seqOutput);

            if (bl2seqAlignmentResults.size() > 0) {
                int maxAlignedSequenceLength = -1;
                Bl2SeqResult maxBl2SeqResult = null;

                for (Bl2SeqResult bl2seqResult : bl2seqAlignmentResults) {
                    int querySequenceLength = bl2seqResult.getQuerySequence().length();

                    if (maxAlignedSequenceLength < querySequenceLength) {
                        maxAlignedSequenceLength = querySequenceLength;
                        maxBl2SeqResult = bl2seqResult;
                    }
                }

                if (maxBl2SeqResult != null) {
                    int strand = maxBl2SeqResult.getOrientation() == 0 ? 1 : -1;

                    TraceSequenceAlignment traceSequenceAlignment = traceSequence
                            .getTraceSequenceAlignment();

                    int queryStart = maxBl2SeqResult.getQueryStart();
                    int queryEnd = maxBl2SeqResult.getQueryEnd();
                    int subjectStart = maxBl2SeqResult.getSubjectStart();
                    int subjectEnd = maxBl2SeqResult.getSubjectEnd();

                    if (isCircular) {
                        if (queryStart > entrySequenceLength - 1) {
                            queryStart = queryStart - entrySequenceLength;
                        }

                        if (queryEnd > entrySequenceLength - 1) {
                            queryEnd = queryEnd - entrySequenceLength;
                        }

                        if (subjectEnd > entrySequenceLength - 1) {
                            subjectEnd = subjectEnd - entrySequenceLength;
                        }

                        if (subjectStart > entrySequenceLength - 1) {
                            subjectStart = subjectStart - entrySequenceLength;
                        }
                    }

                    if (traceSequenceAlignment == null) {
                        traceSequenceAlignment = new TraceSequenceAlignment(traceSequence,
                                maxBl2SeqResult.getScore(), strand, queryStart, queryEnd,
                                subjectStart, subjectEnd, maxBl2SeqResult.getQuerySequence(),
                                maxBl2SeqResult.getSubjectSequence(), sequence.getFwdHash(),
                                new Date());

                        traceSequence.setTraceSequenceAlignment(traceSequenceAlignment);
                    } else {
                        traceSequenceAlignment.setModificationTime(new Date());
                        traceSequenceAlignment.setScore(maxBl2SeqResult.getScore());
                        traceSequenceAlignment.setStrand(strand);
                        traceSequenceAlignment.setQueryStart(queryStart);
                        traceSequenceAlignment.setQueryEnd(queryEnd);
                        traceSequenceAlignment.setSubjectStart(subjectStart);
                        traceSequenceAlignment.setSubjectEnd(subjectEnd);
                        traceSequenceAlignment
                                .setQueryAlignment(maxBl2SeqResult.getQuerySequence());
                        traceSequenceAlignment.setSubjectAlignment(maxBl2SeqResult
                                .getSubjectSequence());

                        traceSequenceAlignment.setSequenceHash(sequence.getFwdHash());
                    }

                    TraceSequenceManager.save(traceSequence);
                }
            }
        } catch (Bl2SeqException e) {
            throw new ControllerException(e);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Calculate sequence alignments between the sequence associated with an {@link Entry} entry
     * with all the {@link TraceSequence}s associated with that entry.
     * <p>
     * Calls buildOrReplaceAlignment on each TraceSequence.
     * 
     * @param entry
     * @throws ControllerException
     */
    public void rebuildAllAlignments(Entry entry) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to rebuild alignment for null entry!");
        }

        SequenceController sequenceController = new SequenceController(getAccount());
        Sequence sequence = sequenceController.getByEntry(entry);

        if (sequence == null) {
            return;
        }

        List<TraceSequence> traceSequences = getTraceSequences(entry);
        for (TraceSequence traceSequence : traceSequences) {
            buildOrRebuildAlignment(traceSequence, sequence);
        }
    }
}
