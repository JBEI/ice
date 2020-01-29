package org.jbei.ice.lib.search.blast;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.search.BlastProgram;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.jbei.ice.lib.utils.SequenceUtils.breakUpLines;

/**
 * Standard blast database for sequences
 *
 * @author Hector Plahar
 */
public class StandardBlastDatabase extends BlastDatabase {

    private static StandardBlastDatabase INSTANCE;
    private final Object LOCK;
    private BlastPlus blastPlus;
    private BlastFastaFile blastFastaFile;
    private SequenceDAO sequenceDAO;

    private StandardBlastDatabase() {
        super("blast");
        blastPlus = new BlastPlus();
        sequenceDAO = DAOFactory.getSequenceDAO();
        blastFastaFile = new BlastFastaFile(indexPath);
        LOCK = new Object();
    }

    public static StandardBlastDatabase getInstance() {
        if (INSTANCE == null)
            INSTANCE = new StandardBlastDatabase();
        return INSTANCE;
    }

    private static String getSequenceFasta(Sequence sequence) {
        long id = sequence.getEntry().getId();

        String sequenceString = "";
        String temp = sequence.getSequence();

        if (temp != null) {
            SymbolList symL;
            try {
                symL = DNATools.createDNA(sequence.getSequence().trim());
            } catch (IllegalSymbolException e1) {
                // maybe it's rna?
                try {
                    symL = RNATools.createRNA(sequence.getSequence().trim());
                } catch (IllegalSymbolException e2) {
                    // skip this sequence
                    Logger.debug("Invalid characters in sequence for " + sequence.getEntry().getId()
                            + ". Skipped for indexing");
                    Logger.debug(e2.toString());
                    return null;
                }
            }

            sequenceString = breakUpLines(symL.seqString() + symL.seqString());
        }

        if (StringUtils.isEmpty(sequenceString))
            return null;

        String idString = ">" + id;
        idString += DELIMITER + sequence.getEntry().getRecordType();
        String name = sequence.getEntry().getName() == null ? "None" : sequence.getEntry().getName();
        idString += DELIMITER + name;
        String pNumber = sequence.getEntry().getPartNumber();
        idString += DELIMITER + pNumber;
        idString += "\n";
        return (idString + sequenceString + "\n");
    }


    /**
     * Run a blast query using the following output format options
     * <ul>
     * <li><code>stitle</code> - subject title</li>
     * <li><code>qstart</code> - query match start index</li>
     * <li><code>qend</code> - query match end index</li>
     * <li><code>sstart</code> - subject match start index</li>
     * <li><code>send</code></li>
     * <li><code>sstrand</code></li>
     * <li><code>evalue</code></li>
     * <li><code>bitscore</code></li>
     * <li><code>length</code> - alignment length</li>
     * <li><code>nident</code> - number of identical matches</li>
     * </ul>
     *
     * @param query wrapper around blast query
     * @return map of unique entry identifier (whose sequence was a subject) to the search result hit details
     * @throws BlastException if results of running blast is null
     */
    public HashMap<String, SearchResult> runBlast(BlastQuery query) throws BlastException {
        List<String> options;
        if (query.getBlastProgram() == null || query.getBlastProgram() == BlastProgram.BLAST_N)
            options = Arrays.asList("-perc_identity", "70", "-outfmt",
                    "10 stitle qstart qend sstart send sstrand evalue bitscore score length nident");
        else
            options = Arrays.asList("-outfmt",
                    "10 stitle qstart qend sstart send sstrand evalue bitscore score length nident");

        BlastSearch blastSearch = new BlastSearch(this.indexPath, this.dbName);
        String result = blastSearch.run(query, options.toArray(new String[]{}));
        if (result == null)
            throw new BlastException("Exception running blast");
        return processBlastOutput(result, query.getSequence().length());
    }

    /**
     * Processes the result of a blast search
     *
     * @param blastOutput result output from running blast on the command line
     * @param queryLength length of query sequence
     * @return mapping of entryId to search result object containing information about the blast search for that particular hit
     */
    private LinkedHashMap<String, SearchResult> processBlastOutput(String blastOutput, int queryLength) {
        LinkedHashMap<String, SearchResult> hashMap = new LinkedHashMap<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();
            reader.close();

            for (String[] line : lines) {
                SearchResult info = parseBlastOutputLine(line);
                if (info == null)
                    continue;

                info.setQueryLength(queryLength);
                String idString = Long.toString(info.getEntryInfo().getId());
                // if there is an existing record for same entry with a lower relative score then replace
                hashMap.putIfAbsent(idString, info);
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        return hashMap;
    }

    /**
     * Parses a blast output that represents a single hit
     *
     * @param line blast output for hit
     * @return object wrapper around details of the hit
     */
    private SearchResult parseBlastOutputLine(String[] line) {
        try {
            // extract part information
            PartData view = new PartData(EntryType.nameToType(line[1]));
            view.setId(Long.decode(line[0]));
            view.setName(line[2]);
            view.setPartId(line[3]);
            String summary = DAOFactory.getEntryDAO().getEntrySummary(view.getId());
            view.setShortDescription(summary);

            //search result object
            SearchResult searchResult = new SearchResult();
            searchResult.setEntryInfo(view);
            searchResult.seteValue(line[9]);
            searchResult.setScore(Float.parseFloat(line[11].trim()));
            searchResult.setAlignment(line[13]);
            searchResult.setQueryLength(Integer.parseInt(line[12].trim()));
            searchResult.setNident(Integer.parseInt(line[13].trim()));
            return searchResult;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Rebuilds the blast database if it doesn't exist. The rebuild can be forced
     * regardless of the existence of the database
     *
     * @param force whether to rebuild the database regardless of whether it exists or not
     * @throws BlastException on exception rebuilding sequence db
     */
    public void checkRebuild(boolean force) throws BlastException {
        if (!force && blastDatabaseExists()) {
            Logger.info("Blast database found in " + indexPath.toString());
            return;
        }

        // delete fasta file and create a new one with all sequences in database
        blastFastaFile.createNew();
        Iterable<String> iterable = () -> new AllSequencesStream(sequenceDAO);
        blastFastaFile.write(iterable);
        blastPlus.formatBlastDb(blastFastaFile, this.dbName);
    }

    public void addSequence(String partId) {
        Entry entry = DAOFactory.getEntryDAO().getByPartNumber(partId);
        if (entry == null) {
            Logger.error("Could not retrieve entry with id " + partId);
            return;
        }

        Sequence sequence = sequenceDAO.getByEntry(entry);
        final String blastFasta = getSequenceFasta(sequence);
        if (blastFasta == null)
            return;

        blastFastaFile.write(() -> new Iterator<String>() {
            private boolean called = false;

            @Override
            public boolean hasNext() {
                if (called)
                    return false;

                called = true;
                return true;
            }

            @Override
            public String next() {
                return blastFasta;
            }
        });

        try {
            blastPlus.formatBlastDb(blastFastaFile, this.dbName);
        } catch (BlastException e) {
            Logger.error(e);
        }
    }

    public void removeSequence(String partId) {
        try {
            blastFastaFile.delete(partId);
            blastPlus.formatBlastDb(blastFastaFile, this.dbName);
        } catch (IOException | BlastException e) {
            Logger.error(e);
        }
    }

    public void updateSequence(String partId) {
        // remove and add
        try {
            blastFastaFile.delete(partId);
        } catch (IOException e) {
            Logger.error(e);
            return;
        }
        addSequence(partId);
    }

    /**
     * Checks if a database exists for blast searches exists by checking for the existence of
     * the blast database name (currently <code>ice</code>) with <code>.nsq</code> extension
     *
     * @return true is a blast database is found, false otherwise
     */
    private boolean blastDatabaseExists() {
        return Files.exists(Paths.get(indexPath.toString(), this.dbName + ".nsq"));
    }

    private static class AllSequencesStream implements Iterator<String> {

        private int available;
        private int currentOffset;
        private SequenceDAO dao;
        private String nextValue;

        public AllSequencesStream(SequenceDAO sequenceDAO) {
            dao = sequenceDAO;
            available = dao.getSequenceCount();
        }

        @Override
        public boolean hasNext() {
            while (currentOffset < available) {
                Sequence sequence = dao.getSequence(currentOffset++);
                if (sequence == null || sequence.getEntry() == null)
                    continue;

                nextValue = getSequenceFasta(sequence);
                if (nextValue == null)
                    continue;

                return true;
            }

            return false;
        }

        @Override
        public String next() {
            if (StringUtils.isEmpty(nextValue))
                throw new IllegalStateException("No value available. Make sure call to hasNext() returns true");
            return nextValue;
        }
    }
}
