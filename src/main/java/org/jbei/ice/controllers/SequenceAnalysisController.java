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
            savedTraceSequence = TraceSequenceManager.save(traceSequence, inputStream);
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
            TraceSequenceAlignment traceSequenceAlignment = TraceSequenceManager
                    .getAlignment(traceSequence);

            if (traceSequenceAlignment != null) {
                TraceSequenceManager.deleteAlignment(traceSequenceAlignment);
            }

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

        try {
            traces = TraceSequenceManager.getByEntry(entry);
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

    public TraceSequenceAlignment getTraceSequenceAlignment(TraceSequence traceSequence)
            throws ControllerException {
        TraceSequenceAlignment traceSequenceAlignment = null;

        try {
            traceSequenceAlignment = TraceSequenceManager.getAlignment(traceSequence);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return traceSequenceAlignment;
    }

    public TraceSequenceAlignment buildOrRebuildAlignment(TraceSequence traceSequence,
            Sequence sequence) throws ControllerException {
        if (traceSequence == null) {
            throw new ControllerException("Failed to rebuild alignment for null trace sequence!");
        }

        try {
            TraceSequenceAlignment traceSequenceAlignment = TraceSequenceManager
                    .getAlignment(traceSequence);

            if (traceSequenceAlignment != null) {
                TraceSequenceManager.deleteAlignment(traceSequenceAlignment);
            }
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        // if sequence is null => delete alignment
        if (sequence == null) {
            return null;
        }

        // actually build alignment
        String traceSequenceString = traceSequence.getSequence();
        String entrySequenceString = sequence.getSequence();

        String bl2seqOutput = "";

        Blast blast = new Blast();
        try {
            bl2seqOutput = blast.runBl2Seq(traceSequenceString, entrySequenceString);
        } catch (BlastException e) {
            throw new ControllerException(e);
        } catch (ProgramTookTooLongException e) {
            throw new ControllerException(e);
        }

        if (bl2seqOutput == null || bl2seqOutput.isEmpty()) {
            return null;
        }

        TraceSequenceAlignment traceSequenceAlignment = null;

        try {
            List<Bl2SeqResult> bl2seqAlignmentResults = Bl2SeqParser.parse(bl2seqOutput);

            if (bl2seqAlignmentResults.size() > 0) {
                int maxAlignedSequenceLength = -1;
                Bl2SeqResult maxBl2SeqResult = null;

                for (Bl2SeqResult bl2seqResult : bl2seqAlignmentResults) {
                    if (maxAlignedSequenceLength < bl2seqResult.getQuerySequence().length()) {
                        maxAlignedSequenceLength = bl2seqResult.getQuerySequence().length();
                        maxBl2SeqResult = bl2seqResult;
                    }
                }

                if (maxBl2SeqResult != null) {
                    traceSequenceAlignment = new TraceSequenceAlignment(traceSequence,
                            maxBl2SeqResult.getScore(), maxBl2SeqResult.getQueryStart(),
                            maxBl2SeqResult.getQueryEnd(), maxBl2SeqResult.getSubjectStart(),
                            maxBl2SeqResult.getSubjectEnd(), maxBl2SeqResult.getQuerySequence(),
                            maxBl2SeqResult.getSubjectSequence(), new Date());

                    TraceSequenceManager.saveAlignment(traceSequenceAlignment);
                }
            }
        } catch (Bl2SeqException e) {
            throw new ControllerException(e);
        } catch (ManagerException e) {
            new ControllerException(e);
        }

        return traceSequenceAlignment;
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
