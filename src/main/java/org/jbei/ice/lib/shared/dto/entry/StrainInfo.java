package org.jbei.ice.lib.shared.dto.entry;

public class StrainInfo extends EntryInfo {

    private static final long serialVersionUID = 1L;

    private String host;
    private String genotypePhenotype;
    private String plasmids;
    private String linkifiedPlasmids;
    private String linkifiedHost;

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

    public String getLinkifiedPlasmids() {
        return linkifiedPlasmids;
    }

    public void setLinkifiedPlasmids(String linkifiedPlasmids) {
        this.linkifiedPlasmids = linkifiedPlasmids;
    }

    public String getLinkifiedHost() {
        return linkifiedHost;
    }

    public void setLinkifiedHost(String linkifiedHost) {
        this.linkifiedHost = linkifiedHost;
    }
}
