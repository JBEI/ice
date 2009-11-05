package org.jbei.ice.lib.value_objects;

import java.util.Date;
import java.util.Set;

import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;

public interface EntryValueObject {

	public abstract int getId();

	public abstract void setId(int id);

	public abstract String getRecordId();

	public abstract void setRecordId(String recordId);

	public abstract String getVersionId();

	public abstract void setVersionId(String versionId);

	public abstract String getRecordType();

	public abstract void setRecordType(String recordType);

	public abstract Set<Name> getNames();

	public abstract void setNames(Set<Name> names);

	public abstract Set<PartNumber> getPartNumbers();

	public abstract void setPartNumbers(Set<PartNumber> partNumbers);

	public abstract String getOwner();

	public abstract void setOwner(String owner);

	public abstract String getOwnerEmail();

	public abstract void setOwnerEmail(String ownerEmail);

	public abstract String getCreator();

	public abstract void setCreator(String creator);

	public abstract String getCreatorEmail();

	public abstract void setCreatorEmail(String creatorEmail);

	public abstract int getVisibility();

	public abstract void setVisibility(int visibility);

	public abstract String getStatus();

	public abstract void setStatus(String status);

	public abstract String getAlias();

	public abstract void setAlias(String alias);

	public abstract Set<SelectionMarker> getSelectionMarkers();

	public abstract void setSelectionMarkers(
			Set<SelectionMarker> selectionMarkers);

	public abstract Set<Link> getLinks();

	public abstract void setLinks(Set<Link> links);

	public abstract String getKeywords();

	public abstract void setKeywords(String keywords);

	public abstract String getShortDescription();

	public abstract void setShortDescription(String shortDescription);

	public abstract String getLongDescription();

	public abstract void setLongDescription(String longDescription);

	public abstract String getReferences();

	public abstract void setReferences(String references);

	public abstract Date getCreationTime();

	public abstract void setCreationTime(Date creationTime);

	public abstract Date getModificationTime();

	public abstract void setModificationTime(Date modificationTime);

	public abstract void setSequence(Sequence sequence);

	public abstract Sequence getSequence();

}