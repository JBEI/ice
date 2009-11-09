package org.jbei.ice.lib.models;

import java.io.Serializable;

public class FeatureFeatureRelationship implements Serializable {
	private int id;
	private Feature subject;
	private Feature object;
	private FeatureRelationship relationship;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Feature getSubject() {
		return subject;
	}
	public void setSubject(Feature subject) {
		this.subject = subject;
	}
	public Feature getObject() {
		return object;
	}
	public void setObject(Feature object) {
		this.object = object;
	}
	public FeatureRelationship getRelationship() {
		return relationship;
	}
	public void setRelationship(FeatureRelationship relationship) {
		this.relationship = relationship;
	}
	
}
