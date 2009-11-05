package org.jbei.ice.lib.value_objects;

public interface StrainValueObject extends EntryValueObject {

	public abstract String getHost();

	public abstract void setHost(String host);

	public abstract String getGenotypePhenotype();

	public abstract void setGenotypePhenotype(String genotypePhenotype);

	public abstract String getPlasmids();

	public abstract void setPlasmids(String plasmids);

}