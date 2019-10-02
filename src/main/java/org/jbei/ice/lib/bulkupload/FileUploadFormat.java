package org.jbei.ice.lib.bulkupload;

/**
 * Supported formats for bulk file upload
 *
 * @author Hector Plahar
 */
public enum FileUploadFormat {
    CSV,
    ZIP,
    SBOL;

    public static FileUploadFormat fromString(String extension) {
        switch (extension.trim()) {
            case "csv":
            default:
                return CSV;

            case "xml":
            case "sbol":
                return SBOL;

            case "zip":
                return ZIP;
        }
    }
}
