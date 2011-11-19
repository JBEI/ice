package org.jbei.ice.shared.dto;


public class PlasmidInfo extends EntryInfo {

    private String backbone;
    private String originOfReplication;
    private String promoters;
    private boolean circular;

    public PlasmidInfo() {
        this.setType(EntryType.PLASMID);
    }

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

    public boolean getCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }
}
