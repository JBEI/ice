package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.TraceSequenceAnalysis;
import org.jbei.ice.lib.entry.EntryAuthorization;
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

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Trace sequence for alignment against a specific sequence
 *
 * @author Hector Plahar
 */
public class TraceSequences {

    private final TraceSequenceDAO dao;
    private final EntryAuthorization entryAuthorization;
    private final Entry entry;
    private final String userId;
    public static final String TRACES_DIR_NAME = "traces";

    public TraceSequences(String userId, long partId) {
        this.dao = DAOFactory.getTraceSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.entry = DAOFactory.getEntryDAO().get(partId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with id " + partId);
        this.userId = userId;
    }

    public boolean addTraceSequence(File file, String uploadFileName) {

        entryAuthorization.expectRead(userId, entry);

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.error(e);
            return false;
        }

        if (uploadFileName.toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry;
                while (true) {
                    zipEntry = zis.getNextEntry();

                    if (zipEntry != null) {
                        if (!zipEntry.isDirectory() && !zipEntry.getName().startsWith("__MACOSX")) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int c;
                            while ((c = zis.read()) != -1) {
                                byteArrayOutputStream.write(c);
                            }

                            boolean parsed = parseTraceSequence(zipEntry.getName(),
                                    byteArrayOutputStream.toByteArray());
                            if (!parsed) {
                                String errMsg = ("Could not parse \"" + zipEntry.getName()
                                        + "\". Only Fasta, GenBank & ABI files are supported.");
                                Logger.error(errMsg);
                                return false;
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                String errMsg = ("Could not parse zip file.");
                Logger.error(errMsg);
                return false;
            }
        } else {
            try {
                boolean parsed = parseTraceSequence(uploadFileName, IOUtils.toByteArray(inputStream));
                if (!parsed) {
                    String errMsg = ("Could not parse \"" + uploadFileName
                            + "\". Only Fasta, GenBank & ABI files are supported.");
                    Logger.error(errMsg);
                    return false;
                }
            } catch (IOException e) {
                Logger.error(e);
                return false;
            }
        }

        return true;
    }

    public Results<TraceSequenceAnalysis> getTraces(int start, int limit) {
        entryAuthorization.expectRead(userId, entry);
        List<TraceSequence> sequences = dao.getByEntry(entry, start, limit);

        Results<TraceSequenceAnalysis> results = new Results<>();
        if (sequences == null)
            return results;

        for (TraceSequence traceSequence : sequences) {
            TraceSequenceAnalysis analysis = traceSequence.toDataTransferObject();
            AccountTransfer accountTransfer = new AccountTransfer();

            String depositor = traceSequence.getDepositor();
            boolean canEdit = canEdit(userId, depositor, entry);
            analysis.setCanEdit(canEdit);

            Account account = DAOFactory.getAccountDAO().getByEmail(traceSequence.getDepositor());

            if (account != null) {
                accountTransfer.setFirstName(account.getFirstName());
                accountTransfer.setLastName(account.getLastName());
                accountTransfer.setEmail(account.getEmail());
                accountTransfer.setId(account.getId());
            }

            analysis.setDepositor(accountTransfer);
            results.getData().add(analysis);
        }

        // get count
        int count = dao.getCountByEntry(entry);
        results.setResultCount(count);
        return results;
    }

    private boolean parseTraceSequence(String fileName, byte[] bytes) {
        DNASequence dnaSequence = null;

        // First try parsing as ABI
        ABIParser abiParser = new ABIParser();

        try {
            dnaSequence = abiParser.parse(bytes);
        } catch (InvalidFormatParserException e) {
            //
        }

        if (dnaSequence == null) {
            // try parsing as fasta, genbank, etc
            dnaSequence = GeneralParser.getInstance().parse(bytes);
            if (dnaSequence == null || dnaSequence.getSequence() == null) {
                String errMsg = ("Could not parse \"" + fileName
                        + "\". Only Fasta, GenBank & ABI files are supported.");
                Logger.error(errMsg);
                return false;
            }
        }

        TraceSequence traceSequence = importTraceSequence(fileName, dnaSequence.getSequence().toLowerCase(),
                new ByteArrayInputStream(bytes));

        if (traceSequence == null)
            return false;

        Sequence sequence = DAOFactory.getSequenceDAO().getByEntry(entry);
        if (sequence == null)
            return true;
        buildOrRebuildAlignment(traceSequence, sequence);
        return true;
    }

    /**
     * Create a new {@link TraceSequence} record and associated with the {@link Entry} entry.
     * <p/>
     * Unlike importTraceSequence this method auto generates uuid and timestamp.
     *
     * @param filename    name of the file uploaded by the user
     * @param sequence    sequence string
     * @param inputStream input stream for uploaded file
     * @return Saved traceSequence
     */
    public TraceSequence importTraceSequence(String filename, String sequence, InputStream inputStream) {
        if (entry == null) {
            throw new IllegalArgumentException("Failed to save trace sequence with null entry!");
        }

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Failed to save trace sequence without filename!");
        }

        if (sequence == null || sequence.isEmpty()) {
            throw new IllegalArgumentException("Failed to save trace sequence without sequence!");
        }

        String uuid = Utils.generateUUID();
        TraceSequence traceSequence = new TraceSequence(entry, uuid, filename, this.userId, sequence, new Date());
        File tracesDir = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY), TRACES_DIR_NAME).toFile();
        return dao.create(tracesDir, traceSequence, inputStream);
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

                    dao.save(traceSequence);
                }
            }
        } catch (BlastException e) {
            Logger.error(e);
        }
    }


    /**
     * Rebuild the trace sequence alignments for entry using bl2seq. This is intended to be called
     * when the sequence associated with the entry is updated and therefore requires that the sequence
     * alignment be re-calculated
     */
    public void rebuildAlignments(TraceSequence traceSequence, Sequence sequence) {

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

                    dao.save(traceSequence);
                }
            }
        } catch (BlastException e) {
            Logger.error(e);
        }
    }

    protected boolean canEdit(String userId, String depositor, Entry entry) {
        return userId.equalsIgnoreCase(depositor) || entryAuthorization.canWriteThoroughCheck(userId, entry);
    }
}
