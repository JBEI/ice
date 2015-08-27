package org.jbei.ice.services.rest;

/**
 * Helper class which provides utility methods for converting extensions to mime types
 *
 * @author Hector Plahar
 */
public class ExtensionToMimeType {

    /**
     * Determines the appropriate mime type (from list of hardcoded values) using the provided extension.
     *
     * @param extension file extension used to determine mimetype. Should not include the <code>.</code>
     *                  <p> example: <code>csv</code> not <code>.csv</code>
     * @return the appropriate content mimet ype or a default
     */
    public static String getMimeType(String extension) {
        switch (extension) {
            case "csv":
                return "text/csv";

            default:
                return "application/octet-stream";
        }
    }
}
