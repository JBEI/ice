package org.jbei.ice.controllers;

import java.io.File;
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
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;

public class SequenceAnalysisController extends Controller {
    public SequenceAnalysisController(Account account) {
        super(account, new SequenceAnalysisPermissionVerifier());
    }

    public boolean hasReadPermission(TraceSequence traceSequence) {
        return getPermissionVerifier().hasReadPermissions(traceSequence, getAccount());
    }

    public boolean hasWritePermission(TraceSequence traceSequence) {
        return getPermissionVerifier().hasWritePermissions(traceSequence, getAccount());
    }

    public TraceSequence uploadTraceSequence(Entry entry, String filename, String depositor,
            String sequence, InputStream inputStream) throws ControllerException {
        if (entry == null) {
            throw new ControllerException("Failed to save trace sequence with null entry!");
        }

        if (filename == null || filename.isEmpty()) {
            throw new ControllerException("Failed to save trace sequence without filename!");
        }

        if (sequence == null || sequence.isEmpty()) {
            throw new ControllerException("Failed to save trace sequence without sequence!");
        }

        TraceSequence traceSequence = new TraceSequence(entry, Utils.generateUUID(), filename,
                depositor, sequence, new Date());

        TraceSequence savedTraceSequence = null;
        try {
            savedTraceSequence = TraceSequenceManager.create(traceSequence, inputStream);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return savedTraceSequence;
    }

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

    public File getFile(TraceSequence traceSequence) throws ControllerException {
        File result = null;

        try {
            result = TraceSequenceManager.getFile(traceSequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public int getNumberOfTraceSequences(Entry entry) throws ControllerException {
        int result = 0;

        try {
            result = TraceSequenceManager.getNumberOfTraceSequences(entry);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

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
                        if (queryEnd > entrySequenceLength - 1) {
                            queryEnd = queryEnd - entrySequenceLength;
                        }

                        if (subjectEnd > entrySequenceLength - 1) {
                            subjectEnd = subjectEnd - entrySequenceLength;
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
