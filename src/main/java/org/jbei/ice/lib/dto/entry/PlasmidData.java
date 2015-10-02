package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

public class PlasmidData implements IDataTransferModel {

    private String backbone;
    private String originOfReplication;
    private String promoters;
    private Boolean circular;
    private String replicatesIn;

    public String getBackbone() {
        return backbone;
    }

    public void setBackbone(String backbone) {
        this.backbone = backbone;
    }

    public String getOriginOfReplication() {
        return originOfReplication;
    }

    public void setOriginOfReplication(String originOfReplication) {
        this.originOfReplication = originOfReplication;
    }

    public String getPromoters() {
        return promoters;
    }

    public void setPromoters(String promoters) {
        this.promoters = promoters;
    }

    public Boolean getCircular() {
        return circular;
    }

    public void setCircular(Boolean circular) {
        this.circular = circular;
    }

    public String getReplicatesIn() {
        return replicatesIn;
    }

    public void setReplicatesIn(String replicatesIn) {
        this.replicatesIn = replicatesIn;
    }
}
