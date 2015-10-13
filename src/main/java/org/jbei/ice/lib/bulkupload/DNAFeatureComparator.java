package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.DNAFeature;

import java.util.Comparator;

/**
 * @author Hector Plahar
 */
public class DNAFeatureComparator implements Comparator<DNAFeature> {

    @Override
    public int compare(DNAFeature sf1, DNAFeature sf2) {
        Integer o1Start = sf1.getLocations().get(0).getGenbankStart();
        Integer o2Start = sf2.getLocations().get(0).getGenbankStart();

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

    protected int compareEnds(DNAFeature sf1, DNAFeature sf2) {
        Integer o1End = sf1.getLocations().get(0).getEnd();
        Integer o2End = sf2.getLocations().get(0).getEnd();

        if (o1End == null && o2End == null)
            return 0;

        if (o1End != null && o2End == null)
            return 1;

        if (o1End == null)
            return -1;

        return o1End.compareTo(o2End);
    }

}
