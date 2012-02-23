package org.jbei.ice.shared.dto;

public class StrainInfo extends EntryInfo {

    private String host;
    private String genotypePhenotype;
    private String plasmids;

    public StrainInfo() {
        super(EntryType.STRAIN);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGenotypePhenotype() {
        return genotypePhenotype;
    }

    public void setGenotypePhenotype(String genotypePhenotype) {
        this.genotypePhenotype = genotypePhenotype;
    }

    public String getPlasmids() {
        return plasmids;
    }

    public void setPlasmids(String plasmids) {
        this.plasmids = plasmids;
    }
}
