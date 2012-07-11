package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.StrainInfo;

public class StrainValueExtractor extends InfoValueExtractor {

    public String extractValue(Header header, EntryInfo info, int index) {

        String value = super.extractCommon(header, info, index);
        if (value != null)
            return value;

        StrainInfo strain = (StrainInfo) info;
        switch (header) {
        case PARENTAL_STRAIN:
            return strain.getHost();

        case GEN_PHEN:
            return strain.getGenotypePhenotype();

        case PLASMIDS:
            return strain.getPlasmids();
        }

        return null;
    }

}
