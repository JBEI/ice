package org.jbei.ice.lib.models.interfaces;

public interface IFeatureValueObject {
    public abstract String getName();

    public abstract void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getIdentification();

    void setIdentification(String identification);

    String getUuid();

    void setUuid(String uuid);

    int getAutoFind();

    void setAutoFind(int autoFind);

    String getGenbankType();

    void setGenbankType(String genbankType);

    void setId(int id);

    int getId();
}