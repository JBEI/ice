package org.jbei.ice.client.bulkupload.sheet;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.sheet.header.*;
import org.jbei.ice.shared.EntryAddType;

// utility class for returning the headers for specific import type
public class ImportTypeHeaders {

    public static BulkUploadHeaders getHeadersForType(EntryAddType type) {

        switch (type) {
            case STRAIN:
                return new StrainHeaders();

            case PLASMID:
                return new PlasmidHeader();

            case PART:
                return new PartHeader();

            case ARABIDOPSIS:
                return new ArabidopsisSeedHeaders();

            case STRAIN_WITH_PLASMID:
                return new StrainWithPlasmidHeaders();

            default:
                return null;
        }
    }

    public static SampleHeaders getSampleHeaders(EntryAddType type, ArrayList<String> locations) {
        switch (type) {
            case PLASMID:
                return new PlasmidSampleHeaders(locations);

            case STRAIN:
                return new StrainSampleHeader(locations);

            case PART:
                return new PartSampleHeader(locations);

            case ARABIDOPSIS:
                return new ArabidopsisSeedSampleHeader(locations);

            default:
                return null;
        }
    }
}
