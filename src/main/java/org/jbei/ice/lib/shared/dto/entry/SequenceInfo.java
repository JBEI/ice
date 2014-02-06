package org.jbei.ice.lib.shared.dto.entry;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * @author Hector Plahar
 */
public class SequenceInfo implements IDTOModel {

    private String name;
    private String fileId;

    public SequenceInfo() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
