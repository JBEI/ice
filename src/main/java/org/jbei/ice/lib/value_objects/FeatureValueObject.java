package org.jbei.ice.lib.value_objects;

public interface FeatureValueObject {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String getDescription();

	public abstract void setDescription(String description);

	public abstract String getIdentification();

	public abstract void setIdentification(String identification);

	public abstract String getUuid();

	public abstract void setUuid(String uuid);

	public abstract int getAutoFind();

	public abstract void setAutoFind(int autoFind);

	public abstract String getGenbankType();

	public abstract void setGenbankType(String genbankType);

	public abstract void setId(int id);

	public abstract int getId();

}