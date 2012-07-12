package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * Factory class for extracting the value from an EntryInfo
 * based on type and the header. Used to load a sheet with existing data
 *
 * @author Hector Plahar
 */
public class InfoValueExtractorFactory {

    private static StrainValueExtractor strainExtractor = new StrainValueExtractor();
    private static PlasmidValueExtractor plasmidExtractor = new PlasmidValueExtractor();
    private static PartValueExtractor partExtractor = new PartValueExtractor();
    private static ArabidopsisExtractor arabidopsisExtractor = new ArabidopsisExtractor();

    public static String extractEntryValue(EntryAddType type, Header header, EntryInfo primary, int index) {

        if (header == null || primary == null)
            return "";

        switch (type) {
            case STRAIN:
                return strainExtractor.extractValue(header, primary, index);

            case PART:
                return partExtractor.extractValue(header, primary, index);

            case PLASMID:
                return plasmidExtractor.extractValue(header, primary, index);

            case STRAIN_WITH_PLASMID:
                // strain
                String value = strainExtractor.extractValue(header, primary, index);
                if (value != null)
                    return value;

                // plasmid
                return plasmidExtractor.extractValue(header, primary.getInfo(), index);

            case ARABIDOPSIS:
                return arabidopsisExtractor.extractValue(header, primary, index);
        }

        return "";
    }
}
