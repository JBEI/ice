package org.jbei.ice.entry.sequence;

import org.jbei.ice.dto.DNAFeature;
import org.jbei.ice.dto.FeaturedDNASequence;
import org.jbei.ice.dto.common.Results;
import org.jbei.ice.entry.HasEntry;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import java.util.List;

public class Features extends HasEntry {

    private final String sequenceIdentifier;
    private final SequenceFeatureDAO dao;

    /**
     * @param identifier unique identifier for sequence
     */
    public Features(String userId, String identifier) {
        this.sequenceIdentifier = identifier;
        this.dao = DAOFactory.getSequenceFeatureDAO();
    }

    public Results<DNAFeature> get(int start, int count) {
        Entry entry = super.getEntry(this.sequenceIdentifier);

        List<SequenceFeature> list = dao.pageSequenceFeatures(entry, start, count);
        long number = dao.countSequenceFeatures(entry);

        Sequence sequence = new Sequence();
        sequence.setEntry(entry);

        FeaturedDNASequence featuredDNASequence = SequenceUtil.sequenceToDNASequence(sequence, list);

        Results<DNAFeature> results = new Results<>();
        results.setResultCount(number);
        results.setData(featuredDNASequence.getFeatures());

        return results;
    }
}
