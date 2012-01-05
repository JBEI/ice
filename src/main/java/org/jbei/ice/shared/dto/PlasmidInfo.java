package org.jbei.ice.shared.dto;

import java.util.HashMap;

public class PlasmidInfo extends EntryInfo {

    private String backbone;
    private String originOfReplication;
    private String promoters;
    private boolean circular;
    private HashMap<Long, String> strains; // id -> partNumber

    public PlasmidInfo() {
        this.setType(EntryType.PLASMID);
        setStrains(new HashMap<Long, String>());
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

    public HashMap<Long, String> getStrains() {
        return strains;
    }

    public void setStrains(HashMap<Long, String> strains) {
        this.strains = strains;
    }
}
