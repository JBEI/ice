package org.jbei.ice.storage;

import org.jbei.ice.storage.local.LocalFileSystemStorageFactory;
import org.jbei.ice.storage.minio.MinIOStorageFactory;

/**
 * Factory for generating storage types or factories (todo) based on user selected data storage type
 */
public class StorageTypeFactory {

    public static StorageFactory createStorageFactory(DataStorageType type) {
        switch (type) {
            case LOCAL -> {
                return new LocalFileSystemStorageFactory();
            }
            case MINIO -> {
                return new MinIOStorageFactory();
            }
        }
        return new LocalFileSystemStorageFactory();
    }
}
