package org.jbei.ice.lib.models.interfaces;

public interface IFeatureValueObject {
    public abstract String getName();

    public abstract void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getHash();

    void setHash(String hash);

    String getSequence();

    void setSequence(String sequence);

    String getIdentification();

    void setIdentification(String identification);

    int getAutoFind();

    void setAutoFind(int autoFind);

    String getGenbankType();

    void setGenbankType(String genbankType);

    void setId(long id);

    long getId();
}