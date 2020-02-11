package org.jbei.ice.lib.entry.sequence.analysis;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.hibernate.dao.TraceSequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Hector Plahar
 */
public class TraceSequences {

    public static final String TRACES_DIR_NAME = "traces";
    private final TraceSequenceDAO dao;
    private EntryAuthorization authorization;

    public TraceSequences() {
        dao = DAOFactory.getTraceSequenceDAO();
        authorization = new EntryAuthorization();
    }

    /**
     * Bulk update from zip file. Associates data contained in the zip file with multiple
     * entries. This method requires administrator privileges
     *
     * @param inputStream input stream of zip file
     */
    public List<String> bulkUpdate(String userId, InputStream inputStream) {
        new EntryAuthorization().expectAdmin(userId);

        List<String> results = new ArrayList<>();
        try (ZipInputStream stream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;

            while ((zipEntry = stream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.startsWith("__MACOSX") || fileName.endsWith(File.separator))
                    continue;

                // filename should be a part number
                String[] split = fileName.split(File.separator);
                if (split.length < 1)
                    continue;

                String partNumber = split[split.length - 1];
                if (partNumber.startsWith(".") || partNumber.startsWith("_"))   // todo or get the settings from
                    continue;

                // cleanup part number
                partNumber = partNumber.substring(0, partNumber.indexOf('.'));

                Entry entry = DAOFactory.getEntryDAO().getByPartNumber(partNumber);
                if (entry == null) {
                    Logger.error("Part number \"" + partNumber + "\" generated from \"" + fileName + "\" not a valid entry");
                    continue;
                }

                results.add(entry.getPartNumber());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int c;
                while ((c = stream.read()) != -1) {
                    byteArrayOutputStream.write(c);
                }

                String zipFilename = Paths.get(zipEntry.getName()).getFileName().toString();
                add(userId, entry, zipFilename, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            }
        } catch (IOException e) {
            Logger.error(e);
        }

        return results;
    }

    /**
     * Remove a {@link TraceSequence} from the database and disk.
     *
     * @param traceSequence dto for trace sequence information
     */
    public void removeTraceSequence(TraceSequence traceSequence) {
        if (traceSequence == null)
            return;

        Path tracesDir = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), TRACES_DIR_NAME);
        dao.delete(tracesDir, traceSequence);
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

            traces = dao.getByEntry(entry, 0, Integer.MAX_VALUE);
        } else {
            traces = dao.getByEntry(entry, 0, Integer.MAX_VALUE);

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
                traces = dao.getByEntry(entry, 0, Integer.MAX_VALUE);
            }
        }

        return traces;
    }

    public TraceSequence getTraceSequenceByFileId(String fileId) {
        Optional<TraceSequence> result = dao.getByFileId(fileId);
        return result.orElse(null);
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
        boolean isCircular = (sequence.getEntry().getRecordType().equalsIgnoreCase("plasmid")) && ((Plasmid) sequence.getEntry()).getCircular();

        if (isCircular) {
            entrySequenceString += entrySequenceString;
        }

        try {
            List<Bl2SeqResult> bl2seqAlignmentResults = new BlastPlus().runBlast2Seq(entrySequenceString, traceSequenceString);

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

                dao.save(traceSequence);
            }
        } catch (BlastException e) {
            Logger.error(e);
        }
    }

    /**
     * Calculate sequence alignments between the sequence associated with an {@link Entry} entry
     * with all the {@link TraceSequence}s associated with that entry.
     * <p/>
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

    public boolean deleteTraceSequence(String userId, long entryId, long traceId) {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);
        if (entry == null)
            return false;

        TraceSequenceDAO traceSequenceDAO = DAOFactory.getTraceSequenceDAO();
        TraceSequence traceSequence = traceSequenceDAO.get(traceId);
        if (traceSequence == null || !canEdit(userId, traceSequence.getDepositor(), entry))
            return false;

        try {
            new TraceSequences().removeTraceSequence(traceSequence);
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
        return true;
    }

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || authorization.canWrite(userId, entry);
    }

    protected boolean add(String userId, Entry entry, String fileName, InputStream fileInputStream) {
        ShotgunSequenceDAO dao = DAOFactory.getShotgunSequenceDAO();
        String storageName = Utils.generateUUID();
        try {
            dao.writeSequenceFileToDisk(storageName, fileInputStream);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
        return dao.create(fileName, userId, entry, storageName, new Date()) != null;
    }
}
