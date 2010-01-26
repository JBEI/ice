package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;



@Entity
@Table(name="groups")
@SequenceGenerator(name = "sequence", sequenceName = "groups_id_seq",
		allocationSize = 1)
public class Group implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
	protected int id;
	
	@Column(name = "uuid", length = 36, nullable=false)
	protected String uuid;
	
	@Column(name = "label", length = 127, nullable=false)
	protected String label;
	
	@Column(name = "description", length = 255, nullable=false)
	protected String description;
	
	@ManyToOne()
	@JoinColumn(name = "parent")
	protected Group parent;

	@Column(name = "serialized_cache")
	protected Serializable serializedCache;

	@Transient
	protected TreeSet<Integer> cache;		
	
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
