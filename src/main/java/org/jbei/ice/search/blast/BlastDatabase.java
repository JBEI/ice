package org.jbei.ice.search.blast;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BlastDatabase {

    protected static final String DELIMITER = ",";
    protected final Path indexPath;
    protected final String dbName;

    public BlastDatabase(String folderName) {
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        if (StringUtils.isEmpty(dataDir)) {
            Logger.error("No data directory value.");
            throw new IllegalArgumentException("Invalid data dir");
        }

        this.indexPath = Paths.get(dataDir, folderName);
        if (!Files.exists(this.indexPath)) {
            try {
                Files.createDirectories(this.indexPath);
            } catch (IOException e) {
                Logger.error("Could not create database at " + indexPath, e);
                throw new IllegalArgumentException(e);
            }
        }
        this.dbName = "ice";
    }
}
