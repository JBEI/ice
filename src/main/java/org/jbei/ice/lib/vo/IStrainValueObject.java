package org.jbei.ice.lib.vo;

public interface IStrainValueObject extends IEntryValueObject {

    public abstract String getHost();

    public abstract void setHost(String host);

    public abstract String getGenotypePhenotype();

    public abstract void setGenotypePhenotype(String genotypePhenotype);

    public abstract String getPlasmids();

    public abstract void setPlasmids(String plasmids);

}