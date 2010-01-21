package org.jbei.ice.lib.models;

import java.io.Serializable;

import org.jbei.ice.lib.value_objects.IFeatureValueObject;

public class Feature implements IFeatureValueObject, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private String identification;
    private String uuid;
    private int autoFind;
    private String genbankType;

    public Feature() {
        super();
    }

    public Feature(String name, String description, String identification, String uuid,
            int autoFind, String genbankType) {
        super();

        this.name = name;
        this.description = description;
        this.identification = identification;
        this.uuid = uuid;
        this.autoFind = autoFind;
        this.genbankType = genbankType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getAutoFind() {
        return autoFind;
    }

    public void setAutoFind(int autoFind) {
        this.autoFind = autoFind;
    }

    public String getGenbankType() {
        return genbankType;
    }

    public void setGenbankType(String genbankType) {
        this.genbankType = genbankType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
