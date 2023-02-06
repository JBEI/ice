package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;

/**
 * Type of data storage to use in storing ICE data (e.g. attachments, sequences)
 * <b>Note</b> that this storage type is different from type of database
 *
 * @author Hector Plahar
 */
public enum DataStorageType {
    // use local file storage
    LOCAL,

    // use remote object storage system
    MINIO;

    /**
     * Determine enum storage type based in string input. This method defaults to local
     *
     * @param type string value to match
     * @return matching storage type of LOCAL if no matches found (including null input)
     */
    public static DataStorageType fromString(String type) {
        if (StringUtils.isEmpty(type))
            return LOCAL;

        for (DataStorageType storageType : DataStorageType.values()) {
            if (storageType.name().toUpperCase().equalsIgnoreCase(type))
                return storageType;
        }
        return LOCAL;
    }
}
