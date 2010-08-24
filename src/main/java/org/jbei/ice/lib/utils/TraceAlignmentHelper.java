package org.jbei.ice.lib.utils;

import java.util.List;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.lib.vo.TraceData;

public class TraceAlignmentHelper {
    public static TraceData traceSequenceToTraceData(TraceSequence traceSequence) {
        TraceData resultTraceData = new TraceData();

        if (traceSequence == null) {
            return resultTraceData;
        }

        resultTraceData.setFilename(traceSequence.getFilename());
        resultTraceData.setSequence(traceSequence.getSequence());

        if (traceSequence.getTraceSequenceAlignment() != null) {
            resultTraceData.setScore(traceSequence.getTraceSequenceAlignment().getScore());
            resultTraceData.setStrand(traceSequence.getTraceSequenceAlignment().getStrand());
            resultTraceData
                    .setQueryStart(traceSequence.getTraceSequenceAlignment().getQueryStart());
            resultTraceData.setQueryEnd(traceSequence.getTraceSequenceAlignment().getQueryEnd());
            resultTraceData.setSubjectStart(traceSequence.getTraceSequenceAlignment()
                    .getSubjectStart());
            resultTraceData
                    .setSubjectEnd(traceSequence.getTraceSequenceAlignment().getSubjectEnd());
            resultTraceData.setQueryAlignment(traceSequence.getTraceSequenceAlignment()
                    .getQueryAlignment());
            resultTraceData.setSubjectAlignment(traceSequence.getTraceSequenceAlignment()
                    .getSubjectAlignment());
        }

        return resultTraceData;
    }

    public static TraceSequence traceDataToTraceSequence(TraceData traceData, String depositor,
            Entry entry) {
        TraceSequence resultTraceSequence = new TraceSequence();

        if (traceData == null) {
            return resultTraceSequence;
        }

        resultTraceSequence.setFilename(traceData.getFilename());
        resultTraceSequence.setSequence(traceData.getSequence());
        resultTraceSequence.setEntry(entry);
        resultTraceSequence.setDepositor(depositor);

        TraceSequenceAlignment traceSequenceAlignment = new TraceSequenceAlignment();
        resultTraceSequence.setTraceSequenceAlignment(traceSequenceAlignment);

        if (traceData.getScore() >= 0) {
            traceSequenceAlignment.setScore(traceData.getScore());
            traceSequenceAlignment.setStrand(traceData.getStrand());
            traceSequenceAlignment.setQueryStart(traceData.getQueryStart());
            traceSequenceAlignment.setQueryEnd(traceData.getQueryEnd());
            traceSequenceAlignment.setSubjectStart(traceData.getSubjectStart());
            traceSequenceAlignment.setSubjectEnd(traceData.getSubjectEnd());
            traceSequenceAlignment.setQueryAlignment(traceData.getQueryAlignment());
            traceSequenceAlignment.setSubjectAlignment(traceData.getSubjectAlignment());
        }

        return resultTraceSequence;
    }

    public static TraceSequence traceDataToTraceSequence(TraceData traceData) {
        return traceDataToTraceSequence(traceData, "", null);
    }

    public static TraceData alignSequences(String querySequence, String traceSequence,
            String traceFileName, boolean isCircular) {
        int querySequenceLength = querySequence.length();

        String bl2seqOutput = "";

        if (isCircular) {
            querySequence += querySequence;
        }

        Blast blast = new Blast();
        try {
            bl2seqOutput = blast.runBl2Seq(querySequence, traceSequence);
        } catch (BlastException e) {

        } catch (ProgramTookTooLongException e) {

        }

        if (bl2seqOutput == null || bl2seqOutput.isEmpty()) {
            return null;
        }

        List<Bl2SeqResult> bl2seqAlignmentResults = null;

        try {
            bl2seqAlignmentResults = Bl2SeqParser.parse(bl2seqOutput);
        } catch (Bl2SeqException e) {
            return null;
        }

        if (bl2seqAlignmentResults != null && bl2seqAlignmentResults.size() > 0) {
            int maxAlignedSequenceLength = -1;
            Bl2SeqResult maxBl2SeqResult = null;

            for (Bl2SeqResult bl2seqResult : bl2seqAlignmentResults) {
                int bl2seqResultQuerySequenceLength = bl2seqResult.getQuerySequence().length();

                if (maxAlignedSequenceLength < bl2seqResultQuerySequenceLength) {
                    maxAlignedSequenceLength = bl2seqResultQuerySequenceLength;
                    maxBl2SeqResult = bl2seqResult;
                }
            }

            if (maxBl2SeqResult != null) {
                int strand = maxBl2SeqResult.getOrientation() == 0 ? 1 : -1;
                int queryStart = maxBl2SeqResult.getQueryStart();
                int queryEnd = maxBl2SeqResult.getQueryEnd();
                int subjectStart = maxBl2SeqResult.getSubjectStart();
                int subjectEnd = maxBl2SeqResult.getSubjectEnd();

                if (isCircular) {
                    if (queryStart > querySequenceLength) {
                        queryStart = queryStart - querySequenceLength;
                    }

                    if (queryEnd > querySequenceLength) {
                        queryEnd = queryEnd - querySequenceLength;
                    }

                    if (subjectStart > querySequenceLength) {
                        subjectStart = subjectStart - querySequenceLength;
                    }

                    if (subjectEnd > querySequenceLength) {
                        subjectEnd = subjectEnd - querySequenceLength;
                    }
                }

                TraceData resultTraceData = new TraceData(traceFileName, traceSequence,
                        maxBl2SeqResult.getScore(), strand, queryStart, queryEnd, subjectStart,
                        subjectEnd, maxBl2SeqResult.getQuerySequence(),
                        maxBl2SeqResult.getSubjectSequence());

                return resultTraceData;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}