package org.jbei.ice.lib.shared.dto.entry;

public class StrainData extends PartData {

    private static final long serialVersionUID = 1L;

    private String host;
    private String genotypePhenotype;

    public StrainData() {
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
}
