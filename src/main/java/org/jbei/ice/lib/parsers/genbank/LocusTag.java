package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.FeaturedDNASequence;

import java.util.Arrays;

/**
 * @author Hector Plahar
 */
public class LocusTag extends Tag {

    public LocusTag(FeaturedDNASequence sequence) {
        super(sequence);
    }

    @Override
    public void process(String line) {
        line = cleanSequence(line);
        String[] split = line.split("\\s+");
        sequence.setIsCircular(Arrays.asList(split).contains("circular") || Arrays.asList(split).contains("CIRCULAR"));

        if (Arrays.asList(split).indexOf("bp") == 3) {
            sequence.setName(split[1].trim());
        } else {
            sequence.setName("undefined");
        }
    }
}
