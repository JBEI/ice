package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.parsers.ABIParser;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.TraceSequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

/**
 * ABI to manipulate DNA sequence trace analysis
 *
 * @author Zinovii Dmytriv
 */
public class SequenceAnalysisController {

    private final TraceSequenceDAO traceDao;

    public static final String TRACES_DIR_NAME = "traces";

    public SequenceAnalysisController() {
        traceDao = DAOFactory.getTraceSequenceDAO();
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
     */
    public TraceSequence importTraceSequence(Entry entry, String filename, String depositor, String sequence,
                                             String uuid, Date date, InputStream inputStream) {
        if (entry == null) {
            throw new IllegalArgumentException("Failed to save trace sequence with null entry!");
        }

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Failed to save trace sequence without filename!");
        }

        if (sequence == null || sequence.isEmpty()) {
            throw new IllegalArgumentException("Failed to save trace sequence without sequence!");
        }

        TraceSequence traceSequence = new TraceSequence(entry, uuid, filename, depositor, sequence, date);
        File tracesDir = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), TRACES_DIR_NAME).toFile();
        return traceDao.create(tracesDir, traceSequence, inputStream);
    }

    /**
     * Create a new {@link TraceSequence} record and associated with the {@link Entry} entry.
     * <p>
     * Unlike importTraceSequence this method auto generates uuid and timestamp.
     *
     * @param entry       entry information
     * @param filename    name of the file uploaded by the user
     * @param depositor   email user depositing the information
     * @param sequence    sequence string
     * @param inputStream input stream for uploaded file
     * @return Saved traceSequence
     */
    public TraceSequence uploadTraceSequence(Entry entry, String filename, String depositor,
                                             String sequence, InputStream inputStream) {
        return importTraceSequence(entry, filename, depositor, sequence, Utils.generateUUID(), new Date(), inputStream);
    }

    /**
     * Remove a {@link TraceSequence} from the database and disk.
     *
     * @param traceSequence dto for trace sequence information
     */
    public void removeTraceSequence(TraceSequence traceSequence) {
        if (traceSequence == null)
            return;

        File tracesDir = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), TRACES_DIR_NAME).toFile();
        traceDao.delete(tracesDir, traceSequence);
    }

    /**
     * Retrieve the {@link TraceSequence} associated with the given {@link Entry} entry.
     *
     * @param entry entry object
     * @return Retrieved TraceSequence
     */
    public List<TraceSequence> getTraceSequences(Entry entry) {
        if (entry == null)
            return null;

        List<TraceSequence> traces;
        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);

        if (sequence == null) { // it will remove invalid alignments
            rebuildAllAlignments(entry);

            traces = traceDao.getByEntry(entry, 0, Integer.MAX_VALUE);
        } else {
            traces = traceDao.getByEntry(entry, 0, Integer.MAX_VALUE);

            boolean wasUpdated = false;
            for (TraceSequence traceSequence : traces) {
                if (traceSequence.getTraceSequenceAlignment() == null
                        || traceSequence.getTraceSequenceAlignment().getSequenceHash() == null
                        || traceSequence.getTraceSequenceAlignment().getSequenceHash().isEmpty()
                        || !traceSequence.getTraceSequenceAlignment().getSequenceHash().equals(sequence.getFwdHash())) {
                    buildOrRebuildAlignment(traceSequence, sequence);
                    wasUpdated = true;
                }
            }

            if (wasUpdated) { // fetch again because alignment has been updated
                traces = traceDao.getByEntry(entry, 0, Integer.MAX_VALUE);
            }
        }

        return traces;
    }

    public TraceSequence getTraceSequenceByFileId(String fileId) {
        return traceDao.getByFileId(fileId);
    }

    /**
     * Parses a given sequence file (Genbank, Fasta, ABI) and return an {@link DNASequence}.
     *
     * @param bytes bytes representation of the sequence information
     * @return Parsed Sequence as {@link DNASequence}.
     */
    public DNASequence parse(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }

        // Trying to parse as Fasta, Genbank, etc
        DNASequence dnaSequence = GeneralParser.getInstance().parse(bytes);

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
     * @param traceSequence dto for trace sequence information
     * @return {@link File} object.
     */
    public File getFile(TraceSequence traceSequence) {
        return Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), TRACES_DIR_NAME,
                traceSequence.getFileId()).toFile();
    }

    /**
     * Calculate sequence alignment between the given {@link TraceSequence} and {@link Sequence}
     * using bl2seq, and save the result into the database.
     *
     * @param traceSequence traceSequence
     * @param sequence      sequence
     */
    public void buildOrRebuildAlignment(TraceSequence traceSequence, Sequence sequence) {
        if (traceSequence == null) {
            throw new IllegalArgumentException("Failed to rebuild alignment for null trace sequence!");
        }

        // if sequence is null => delete alignment
        if (sequence == null || sequence.getEntry() == null) {
            return;
        }

        // actually build alignment
        String traceSequenceString = traceSequence.getSequence();
        String entrySequenceString = sequence.getSequence();

        int entrySequenceLength = entrySequenceString.length();
        boolean isCircular = (sequence.getEntry() instanceof Plasmid) && ((Plasmid) sequence.getEntry()).getCircular();

        if (isCircular) {
            entrySequenceString += entrySequenceString;
        }

        try {
            List<Bl2SeqResult> bl2seqAlignmentResults = BlastPlus.runBlast2Seq(entrySequenceString, traceSequenceString);

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
                    TraceSequenceAlignment traceSequenceAlignment = traceSequence.getTraceSequenceAlignment();
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
                                maxBl2SeqResult.getScore(), strand,
                                queryStart, queryEnd,
                                subjectStart, subjectEnd,
                                maxBl2SeqResult.getQuerySequence(),
                                maxBl2SeqResult.getSubjectSequence(),
                                sequence.getFwdHash(),
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
                        traceSequenceAlignment.setQueryAlignment(maxBl2SeqResult.getQuerySequence());
                        traceSequenceAlignment.setSubjectAlignment(maxBl2SeqResult.getSubjectSequence());
                        traceSequenceAlignment.setSequenceHash(sequence.getFwdHash());
                    }

                    traceDao.save(traceSequence);
                }
            }
        } catch (BlastException e) {
            Logger.error(e);
        }
    }

    /**
     * Calculate sequence alignments between the sequence associated with an {@link Entry} entry
     * with all the {@link TraceSequence}s associated with that entry.
     * <p>
     * Calls buildOrReplaceAlignment on each TraceSequence.
     *
     * @param entry entry object
     */

    public void rebuildAllAlignments(Entry entry) {
        if (entry == null)
            return;

        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
        if (sequence == null) {
            return;
        }

        List<TraceSequence> traceSequences = getTraceSequences(entry);
        for (TraceSequence traceSequence : traceSequences) {
            buildOrRebuildAlignment(traceSequence, sequence);
        }
    }
}
