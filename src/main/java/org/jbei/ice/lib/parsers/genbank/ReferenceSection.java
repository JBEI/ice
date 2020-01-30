package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.FeaturedDNASequence;

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
