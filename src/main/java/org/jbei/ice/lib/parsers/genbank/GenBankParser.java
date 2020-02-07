package org.jbei.ice.lib.parsers.genbank;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.parsers.AbstractParser;

import java.util.Iterator;

/**
 * Parser for a GenBank file
 *
 * @author Hector Plahar
 */
public class GenBankParser extends AbstractParser {

    private FeaturedDNASequence sequence;

    private String getFirstWordFromLine(String line) {
        if (StringUtils.isBlank(line))
            return "";

        String[] chunks = line.split("\\s+");
        if (chunks.length == 0)
            return "";

        return chunks[0];
    }

    /**
     * More like a section factory.
     * Uses the GenBank tag that has been detected to obtain the appropriate GenBank section
     *
     * @param genbankTag tag to use to determine section class
     * @return section class based on tag or null
     */
    private GenBankSection process(GenbankTag genbankTag) {
        switch (genbankTag) {
            case LOCUS:
                return new LocusSection(sequence);

            case ORIGIN:
                return new OriginSection(sequence);

            case ACCESSION:
                return new AccessionSection(sequence);

            case FEATURES:
                return new FeaturesSection(sequence);

            case REFERENCE:
                return new ReferenceSection(sequence);
        }
        return null;
    }

    @Override
    public FeaturedDNASequence parse(Iterator<String> iterator, String... entryType) {
        sequence = new FeaturedDNASequence();
        GenBankSection currentSection = null;

        while (iterator.hasNext()) {
            String line = iterator.next();
            String firstWord = getFirstWordFromLine(line);
            GenbankTag genbankTag = GenbankTag.getTagForString(firstWord);

            // encountered new tag
            if (genbankTag != null) {
                currentSection = process(genbankTag);
            }

            if (currentSection != null)
                currentSection.process(line);
        }

        // todo : at this point we have the entire sequence converted into this object
        // todo : however, it is still being held in memory
        return sequence;
    }
}
