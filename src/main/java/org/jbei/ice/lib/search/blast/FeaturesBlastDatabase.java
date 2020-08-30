package org.jbei.ice.lib.search.blast;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FeatureDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureDAO;
import org.jbei.ice.storage.model.Feature;
import org.jbei.ice.storage.model.SequenceFeature;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class FeaturesBlastDatabase extends BlastDatabase {

    private BlastPlus blastPlus;
    private BlastFastaFile blastFastaFile;

    public FeaturesBlastDatabase() {
        super("auto-annotation");
        blastPlus = new BlastPlus();
        blastFastaFile = new BlastFastaFile(indexPath);
    }

    /**
     * Run a blast query against the sequence features blast database.
     *
     * @param query wrapper around sequence to blast
     * @return list of DNA features that match the query according to the parameters
     * @throws BlastException on null result or exception processing the result
     */
    public List<DNAFeature> runBlast(BlastQuery query) throws BlastException {   // todo add e-value
        BlastSearch blastSearch = new BlastSearch(this.indexPath, this.dbName);
        String result = blastSearch.run(query, "-perc_identity", "100",
                "-outfmt", "10 stitle qstart qend sstart send sstrand");
        if (result == null)
            throw new BlastException("Exception running blast");
        return processBlastOutput(result);
    }

    /**
     * Process the output of the blast run for features
     * into a list of feature objects
     * <br>
     * Expected format for the output (per line) is
     * <code>feature_id, label, type, qstart, qend, sstart, send, sstrand</code>
     * Therefore line[0] is feature_id, line[1] is label etc
     * <br>Since we are only interested in features that have a full match (covers entire feature) some matches are
     * manually eliminated. The results returned by blast can cover only a subset of the sequence. e.g.
     * given query = 'ATGC' and feature1 = 'ATG' and feature2 = 'TATGT', the query will return
     * 1,3,1,3 and 1,3,2,4.
     *
     * @param blastOutput blast program output
     * @return list of feature objects resulting from processing the blast output
     */
    private List<DNAFeature> processBlastOutput(String blastOutput) {
        List<DNAFeature> hashMap = new ArrayList<>();
        HashSet<String> duplicates = new HashSet<>();

        try (CSVReader reader = new CSVReader(new StringReader(blastOutput))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                if (line.length != 9) {
                    continue;
                }

                long id = Long.decode(line[0]);
                String label = line[1];
                String type = line[2];
                int strand = Integer.decode(line[3]);
                int queryStart = Integer.decode(line[4]);
                int queryEnd = Integer.decode(line[5]);
                int subjectStart = Integer.decode(line[6]);
                int subjectEnd = Integer.decode(line[7]);
//                int strand = "plus".equalsIgnoreCase(line[7]) ? 1 : -1;

                if (!duplicates.add(label + ":" + queryStart + ":" + queryEnd + ":" + strand)) {
                    continue;
                }

                if (subjectStart != 1 && (queryEnd - queryStart) + 1 != subjectEnd)
                    continue;

                // check for full feature coverage
                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setId(id);
                dnaFeature.setName(label);
                dnaFeature.setType(type);
                DNAFeatureLocation location = new DNAFeatureLocation();
                location.setGenbankStart(queryStart);
                location.setEnd(queryEnd);
                dnaFeature.getLocations().add(location);
                dnaFeature.setStrand(strand);
                hashMap.add(dnaFeature);
            }

            return hashMap;
        } catch (IOException | CsvException e) {
            Logger.error(e);
            return null;
        }
    }

    /**
     * Writes the fasta file (part of the blast database) that contains all the features that exists on this system.
     * This routine is expected to be called as part of the blast sequence feature database rebuild
     *
     * @throws BlastException on {@link IOException}
     */
    public void rebuild() throws BlastException {
        Iterable<String> iterable = AllFeaturesStream::new;
        blastFastaFile.write(iterable);
        blastPlus.formatBlastDb(blastFastaFile, this.dbName); // todo
    }

    private static class AllFeaturesStream implements Iterator<String> {

        private long available;
        private int currentOffset;
        private String nextValue;
        private FeatureDAO featureDAO;
        private SequenceFeatureDAO sequenceFeatureDAO;

        public AllFeaturesStream() {
            featureDAO = DAOFactory.getFeatureDAO();
            sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();
            available = featureDAO.getFeatureCount(null);
        }

        @Override
        public boolean hasNext() {
            while (currentOffset < available) {
                List<Feature> features = featureDAO.getFeatures(currentOffset++, 1, null);
                Feature feature = features.get(0);
                String featureName = feature.getName();
                if (featureName == null || featureName.trim().isEmpty())
                    continue;

                if (feature.getCuration() != null && feature.getCuration().isExclude())
                    continue;

                boolean hasNegativeStrand = false;
                boolean hasPositiveStrand = false;

                List<SequenceFeature> sequenceFeatures = sequenceFeatureDAO.getByFeature(feature);
                if (sequenceFeatures == null || sequenceFeatures.isEmpty()) {
                    hasPositiveStrand = true;
                } else {
                    for (SequenceFeature sequenceFeature : sequenceFeatures) {
                        if (sequenceFeature.getStrand() == 1) {
                            hasPositiveStrand = true;
                        } else if (sequenceFeature.getStrand() == -1) {
                            hasNegativeStrand = true;
                        }
                    }
                }

                String sequenceString = feature.getSequence().trim();
                if (StringUtils.isEmpty(sequenceString))
                    continue;

                String line = "";

                if (hasNegativeStrand) {
                    try {
                        SymbolList symbolList = DNATools.createDNA(sequenceString);
                        symbolList = DNATools.reverseComplement(symbolList);
                        line = getSequenceString(feature, symbolList.seqString(), -1);
                    } catch (IllegalSymbolException | IllegalAlphabetException e) {
                        Logger.warn(e.getMessage());
                        continue;
                    }
                }

                if (hasPositiveStrand) {
                    line += getSequenceString(feature, sequenceString, 1);
                }

                if (!StringUtils.isEmpty(line)) {
                    nextValue = line;
                    return true;
                }
            }
            return false;
        }

        private String getSequenceString(Feature feature, String seq, int strand) {
            String idString = ">"
                    + feature.getId() + DELIMITER
                    + feature.getName() + DELIMITER
                    + feature.getGenbankType() + DELIMITER
                    + strand;
            idString += "\n";
            idString += (seq + "\n");
            return idString;
        }

        @Override
        public String next() {
            if (StringUtils.isEmpty(nextValue))
                throw new IllegalStateException("No value available. Make sure call to hasNext() returns true");
            return nextValue;
        }
    }
}
