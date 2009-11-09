package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Set;

import org.jbei.ice.lib.value_objects.FeatureValueObject;

public class Feature implements FeatureValueObject, Serializable {
	private int id;
	private String name;
	private String description;
	private String identification;
	private String uuid;
	private int autoFind;
	private String genbankType;
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
