package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.value_objects.EntryValueObject;

public class Entry implements EntryValueObject, Serializable {
	private int id;
	private String recordId;
	private String versionId;
	private String recordType;
	private String owner;
	private String ownerEmail;
	private String creator;
	private String creatorEmail;
	private String alias;
	private String keywords;
	private int visibility;
	private String status;
	private String shortDescription;
	private String longDescription;
	private String references;
	private Date creationTime;
	private Date modificationTime;
	
	private Sequence sequence;
	
	private Set<SelectionMarker> selectionMarkers = new HashSet<SelectionMarker>();
	private Set<Link> links = new HashSet<Link>();
	private Set<Name> names = new HashSet<Name>();
	private Set<PartNumber> partNumbers = new HashSet<PartNumber>();
	
	public Entry() {
	}

	public Entry(String recordId, String versionId, String recordType,
			String owner, String ownerEmail, String creator,
			String creatorEmail, int visibility, String status, String alias,
			String keywords, String shortDescription, String longDescription,
			String references, Date creationTime, Date modificationTime) {
		this.recordId = recordId;
		this.versionId = versionId;
		this.recordType = recordType;
		this.owner = owner;
		this.ownerEmail = ownerEmail;
		this.creator = creator;
		this.creatorEmail = creatorEmail;
		this.visibility = visibility;
		this.status = status;
		this.alias = alias;
		this.keywords = keywords;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.references = references;
		this.creationTime = creationTime;
		this.modificationTime = modificationTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public Set<Name> getNames() {
		return names;
	}

	public void setNames(Set<Name> names) {
		this.names = names;
	}

	public Set<PartNumber> getPartNumbers() {
		return partNumbers;
	}

	public void setPartNumbers(Set<PartNumber> partNumbers) {
		this.partNumbers = partNumbers;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatorEmail() {
		return creatorEmail;
	}

	public void setCreatorEmail(String creatorEmail) {
		this.creatorEmail = creatorEmail;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Set<SelectionMarker> getSelectionMarkers() {
		return selectionMarkers;
	}

	public void setSelectionMarkers(Set<SelectionMarker> selectionMarkers) {
		this.selectionMarkers = selectionMarkers;
	}

	public Set<Link> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		this.links = links;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getReferences() {
		return references;
	}

	public void setReferences(String references) {
		this.references = references;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getModificationTime() {
		return modificationTime;
	}

	public void setModificationTime(Date modificationTime) {
		this.modificationTime = modificationTime;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public Sequence getSequence() {
		return sequence;
	}

}
