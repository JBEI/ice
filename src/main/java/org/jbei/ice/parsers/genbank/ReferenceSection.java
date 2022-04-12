package org.jbei.ice.parsers.genbank;

import org.jbei.ice.dto.FeaturedDNASequence;

/**
 * @author Hector Plahar
 */
public class ReferenceSection extends GenBankSection {

    public ReferenceSection(FeaturedDNASequence sequence) {
        super(sequence);
    }

    @Override
    public void process(String line) {
        final String putativeValue = line.split(" +")[1];
    }
}
