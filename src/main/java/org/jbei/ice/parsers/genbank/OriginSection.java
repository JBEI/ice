package org.jbei.ice.parsers.genbank;

import org.jbei.ice.dto.FeaturedDNASequence;
import org.jbei.ice.utils.Utils;

import java.util.Arrays;

/**
 * @author Hector Plahar
 */
public class OriginSection extends GenBankSection {

    private final StringBuilder sequenceString;

    public OriginSection(FeaturedDNASequence sequence) {
        super(sequence);
        sequenceString = new StringBuilder();
    }

    public void process(String line) {
        if (line.contains("ORIGIN"))
            return;

        if (line.trim().equals("//"))
            sequence.setSequence(sequenceString.toString());

        line = cleanSequence(line);
        String[] chunks = line.split("\\s+");
        if (chunks[0].matches("\\d*")) { // sometimes sequence block is un-numbered fasta
            chunks[0] = "";
        }
        sequenceString.append(Utils.join("", Arrays.asList(chunks)).toLowerCase());
    }
}
