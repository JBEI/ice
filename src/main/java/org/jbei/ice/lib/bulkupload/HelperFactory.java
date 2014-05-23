package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * Factory class that determines which upload helper to instantiate based on
 * EntryAddType
 *
 * @author Hector Plahar
 */
public class HelperFactory {

    public static BulkCSVUpload createCSVUpload(String account, EntryType type, Path csvFilePath) {
        if (type == null)
            throw new IllegalArgumentException("Cannot create upload object for null entry add type");

        switch (type) {
            case PART:
                return new PartBulkCSVUpload(type, account, csvFilePath);

            case STRAIN:
                return new StrainBulkCSVUpload(type, account, csvFilePath);

            case PLASMID:
                return new PlasmidBulkCSVUpload(type, account, csvFilePath);

            case ARABIDOPSIS:
                return new SeedBulkCSVUpload(type, account, csvFilePath);

            default:
                return null;
        }
    }
}
