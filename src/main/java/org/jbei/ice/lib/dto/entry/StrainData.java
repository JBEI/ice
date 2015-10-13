package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

public class StrainData implements IDataTransferModel {

    private static final long serialVersionUID = 1L;

    private String host;
    private String genotypePhenotype;

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
