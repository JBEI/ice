package org.jbei.ice.storage;

import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.utils.Utils;

public class FileStorage {

    public void saveAttachment() {
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
//        Path path = Paths.get(dataDir, "attachments", attachment.getFileId());
//        Files.write(path, IOUtils.toByteArray(attachmentStream));
    }
}
