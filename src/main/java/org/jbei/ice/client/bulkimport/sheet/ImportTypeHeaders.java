package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.EntryAddType;

// utility class for returning the headers for specific import type
public class ImportTypeHeaders {

    public static Header[] getHeadersForType(EntryAddType type) {
        switch (type) {
        case STRAIN:
            return getStrainHeaders();

        case PLASMID:
            return getPlasmidHeaders();

        case PART:
            return getPartHeaders();

        case ARABIDOPSIS:
            return getSeedHeaders();

        case STRAIN_WITH_PLASMID:
            // TODO

        default:
            return null;
        }
    }

    public static Header[] getStrainHeaders() {
        Header[] headers = new Header[] { Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME, Header.SELECTION_MARKERS, Header.PARENTAL_STRAIN,
                Header.GEN_PHEN, Header.PLASMIDS };
        return headers;
    }

    public static Header[] getPlasmidHeaders() {
        Header[] headers = new Header[] { Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME, Header.SELECTION_MARKERS, Header.CIRCULAR, Header.BACKBONE,
                Header.PROMOTERS, Header.ORIGIN_OF_REPLICATION };
        return headers;
    }

    public static Header[] getPartHeaders() {
        Header[] headers = new Header[] { Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.SEQ_FILENAME,
                Header.ATT_FILENAME };
        return headers;
    }

    public static Header[] getSeedHeaders() {
        Header[] headers = new Header[] { Header.PI, Header.FUNDING_SOURCE, Header.IP,
                Header.BIOSAFETY, Header.NAME, Header.ALIAS, Header.KEYWORDS, Header.SUMMARY,
                Header.NOTES, Header.REFERENCES, Header.LINKS, Header.STATUS, Header.HOMOZYGOSITY,
                Header.ECOTYPE, Header.HARVEST_DATE, Header.GENERATION, Header.PLANT_TYPE,
                Header.PARENTS };
        return headers;
    }
}
