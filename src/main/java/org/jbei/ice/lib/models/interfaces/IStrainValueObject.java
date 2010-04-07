package org.jbei.ice.lib.models.interfaces;

public interface IStrainValueObject extends IEntryValueObject {
    String getHost();

    void setHost(String host);

    String getGenotypePhenotype();

    void setGenotypePhenotype(String genotypePhenotype);

    String getPlasmids();

    void setPlasmids(String plasmids);
}