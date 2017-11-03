package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

public class ProteinData implements IDataTransferModel {

    private String organism;
    private String fullName;
    private String geneName;
    private String uploadedFrom;

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getUploadedFrom() {
        return uploadedFrom;
    }

    public void setUploadedFrom(String uploadedFrom) {
        this.uploadedFrom = uploadedFrom;
    }

}
