package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.jbei.ice.storage.model.SequenceFeature;

import java.util.Comparator;

/**
 * Comparator for the sequence feature
 *
 * @author Hector Plahar
 */
public class SequenceFeatureComparator implements Comparator<SequenceFeature> {

    @Override
    public int compare(SequenceFeature sf1, SequenceFeature sf2) {
        Integer o1Start = sf1.getUniqueGenbankStart();
        Integer o2Start = sf2.getUniqueGenbankStart();

        // both null are equal
        if (o1Start == null && o2Start == null)
            return 0;

        if (o1Start != null && o2Start == null)
            return 1;

        if (o1Start == null)
            return -1;

        // check ends if starts are equal
        if (o1Start.intValue() == o2Start.intValue()) {
            return compareEnds(sf1, sf2);
        }

        return o1Start.compareTo(o2Start);
    }

    protected int compareEnds(SequenceFeature sf1, SequenceFeature sf2) {
        Integer o1End = sf1.getUniqueEnd();
        Integer o2End = sf2.getUniqueEnd();

        if (o1End == null && o2End == null)
            return 0;

        if (o1End != null && o2End == null)
            return 1;

        if (o1End == null)
            return -1;

        return o1End.compareTo(o2End);
    }
}
