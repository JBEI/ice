package org.jbei.ice.client.bulkupload.sheet;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkupload.EntryInfoDelegate;
import org.jbei.ice.client.bulkupload.sheet.header.*;
import org.jbei.ice.shared.EntryAddType;

/**
 * utility class for returning the headers for specific import type
 *
 * @author Hector Plahar
 */
public class ImportTypeHeaders {

    public static BulkUploadHeaders getHeadersForType(EntryAddType type, EntryInfoDelegate delegate,
            HashMap<String, String> preferences) {
        switch (type) {
            case STRAIN:
                return new StrainHeaders(delegate, preferences);

            case PLASMID:
                return new PlasmidHeader(delegate, preferences);

            case PART:
                return new PartHeader(delegate, preferences);

            case ARABIDOPSIS:
                return new ArabidopsisSeedHeaders(delegate, preferences);

            case STRAIN_WITH_PLASMID:
                return new StrainWithPlasmidHeaders(delegate, preferences);

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
