package org.jbei.ice.lib.permissions;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.jbei.ice.lib.models.Account;

public class Group {
	protected int id;
	protected String uuid;
	protected String label;
	protected String description;
	protected TreeSet<Integer> cache;
	protected Serializable serializedCache;
	protected Group parent;

	public Set<Account> getUsers() {
		return null;
	
	}
	
	public void updateUsers() {
		
	}
	
	private String serializeCache() {
		return null;
	}
	
	private String deserializeCache() {
		return null;
	}
	
	// Getters and setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}
	public Serializable getSerializedCache() {
		return serializedCache;
	}
	public void setSerializedCache(Serializable serializedCache) {
		this.serializedCache = serializedCache;
	}
	
}
