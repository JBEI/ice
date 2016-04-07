package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.search.BlastQuery;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class Annotations {

    private final Entry entry;
    private final EntryDAO entryDAO;
    private final SequenceDAO sequenceDAO;

    public Annotations(long entryId) {
        entryDAO = DAOFactory.getEntryDAO();
        this.entry = entryDAO.get(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry for " + entryId);
        this.sequenceDAO = DAOFactory.getSequenceDAO();
    }

    /**
     * Auto generate annotations
     */
    public FeaturedDNASequence generate() {
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        String sequenceString = sequence.getSequence();
        BlastQuery query = new BlastQuery();
        query.setSequence(sequenceString);

        try {
            List<DNAFeature> features = BlastPlus.runCheckFeatures(query);
            FeaturedDNASequence dnaSequence = new FeaturedDNASequence();
            dnaSequence.setLength(sequenceString.length());
            dnaSequence.setFeatures(features);
            return dnaSequence;
        } catch (BlastException e) {
            Logger.error(e);
            return null;
        }
    }
}
