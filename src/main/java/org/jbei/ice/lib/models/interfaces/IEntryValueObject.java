package org.jbei.ice.lib.models.interfaces;

import java.util.Date;
import java.util.Set;

import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.SelectionMarker;

public interface IEntryValueObject {
    long getId();

    void setId(long id);

    String getRecordId();

    void setRecordId(String recordId);

    String getVersionId();

    void setVersionId(String versionId);

    String getRecordType();

    void setRecordType(String recordType);

    Set<Name> getNames();

    void setNames(Set<Name> names);

    Set<PartNumber> getPartNumbers();

    void setPartNumbers(Set<PartNumber> partNumbers);

    String getOwner();

    void setOwner(String owner);

    String getOwnerEmail();

    void setOwnerEmail(String ownerEmail);

    String getCreator();

    void setCreator(String creator);

    String getCreatorEmail();

    void setCreatorEmail(String creatorEmail);

    String getStatus();

    void setStatus(String status);

    String getAlias();

    void setAlias(String alias);

    Set<SelectionMarker> getSelectionMarkers();

    void setSelectionMarkers(Set<SelectionMarker> selectionMarkers);

    Set<Link> getLinks();

    void setLinks(Set<Link> links);

    String getKeywords();

    void setKeywords(String keywords);

    String getShortDescription();

    void setShortDescription(String shortDescription);

    String getLongDescription();

    void setLongDescription(String longDescription);

    String getReferences();

    void setReferences(String references);

    Date getCreationTime();

    void setCreationTime(Date creationTime);

    Date getModificationTime();

    void setModificationTime(Date modificationTime);
}