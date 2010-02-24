package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.lib.value_objects.IEntryValueObject;

@Entity
@Table(name = "entries")
@SequenceGenerator(name = "sequence", sequenceName = "entries_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.JOINED)
public class Entry implements IEntryValueObject, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "record_id", length = 36, nullable = false)
    private String recordId;

    @Column(name = "version_id", length = 36, nullable = false)
    private String versionId;

    @Column(name = "record_type", length = 10, nullable = false)
    private String recordType;

    @Column(name = "owner", length = 127)
    private String owner;

    @Column(name = "owner_email", length = 127)
    private String ownerEmail;

    @Column(name = "creator", length = 127)
    private String creator;

    @Column(name = "creator_email", length = 127)
    private String creatorEmail;

    @Column(name = "alias", length = 127)
    private String alias;

    @Column(name = "keywords", length = 127)
    private String keywords;

    @Column(name = "status", length = 127)
    private String status;

    @Column(name = "short_description")
    @Lob
    private String shortDescription;

    @Column(name = "long_description")
    @Lob
    private String longDescription;

    @Column(name = "literature_references")
    @Lob
    private String references;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "bio_safety_level")
    private Integer bioSafetyLevel;

    @Column(name = "intellectual_property")
    @Lob
    private String intellectualProperty;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private Sequence sequence;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<SelectionMarker> selectionMarkers = new LinkedHashSet<SelectionMarker>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<Link> links = new LinkedHashSet<Link>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<Name> names = new LinkedHashSet<Name>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();

    public Entry() {
    }

    public Entry(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription, String references,
            Date creationTime, Date modificationTime) {
        this.recordId = recordId;
        this.versionId = versionId;
        this.recordType = recordType;
        this.owner = owner;
        this.ownerEmail = ownerEmail;
        this.creator = creator;
        this.creatorEmail = creatorEmail;
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

    public Name getOneName() {
        Name result = null;
        if (names.size() > 0) {
            result = (Name) names.toArray()[0];
        }
        return result;
    }

    public String getNamesAsString() {
        String result = "";
        ArrayList<String> names = new ArrayList<String>();
        for (Name name : this.names) {
            names.add(name.getName());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", names);

        return result;
    }

    public void setNames(Set<Name> names) {
        this.names.clear();
        this.names.addAll(names);
    }

    public Set<PartNumber> getPartNumbers() {
        return partNumbers;
    }

    public String getPartNumbersAsString() {
        String result = "";
        ArrayList<String> numbers = new ArrayList<String>();
        for (PartNumber number : partNumbers) {
            numbers.add(number.getPartNumber());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", numbers);

        return result;
    }

    public PartNumber getOnePartNumber() {
        PartNumber result = null;

        if (partNumbers.size() > 0) {
            result = (PartNumber) partNumbers.toArray()[0];
        }
        return result;
    }

    public void setPartNumbers(Set<PartNumber> partNumbers) {
        this.partNumbers.clear();
        this.partNumbers.addAll(partNumbers);
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

    public String getSelectionMarkersAsString() {
        String result = "";
        ArrayList<String> markers = new ArrayList<String>();
        for (SelectionMarker marker : selectionMarkers) {
            markers.add(marker.getName());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", markers);

        return result;
    }

    public void setSelectionMarkers(Set<SelectionMarker> selectionMarkers) {
        this.selectionMarkers.clear();
        this.selectionMarkers.addAll(selectionMarkers);
    }

    public Set<Link> getLinks() {
        return links;
    }

    public String getLinksAsString() {
        String result = "";
        ArrayList<String> links = new ArrayList<String>();
        for (Link link : this.links) {
            links.add(link.getLink());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", links);

        return result;
    }

    public void setLinks(Set<Link> links) {
        this.links.clear(); //This way lets Hibernate know the set has been updated
        this.links.addAll(links);
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

    public void setBioSafetyLevel(Integer bioSafetyLevel) {
        this.bioSafetyLevel = bioSafetyLevel;
    }

    public Integer getBioSafetyLevel() {
        return bioSafetyLevel;
    }

    public void setIntellectualProperty(String intellectualProperty) {
        this.intellectualProperty = intellectualProperty;
    }

    public String getIntellectualProperty() {
        return intellectualProperty;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setEntryFundingSources(Set<EntryFundingSource> entryFundingSources) {
        this.entryFundingSources.clear();
        this.entryFundingSources.addAll(entryFundingSources);
    }

    public Set<EntryFundingSource> getEntryFundingSources() {
        return entryFundingSources;
    }

    public String principalInvestigatorToString() {
        String principalInvestigator = "";

        for (EntryFundingSource entryFundingSource : entryFundingSources) {
            principalInvestigator = entryFundingSource.getFundingSource()
                    .getPrincipalInvestigator();
        }

        if (principalInvestigator == null) {
            principalInvestigator = "";
        }

        return principalInvestigator;
    }

    public String fundingSourceToString() {
        String fundingSource = "";

        for (EntryFundingSource entryFundingSource : entryFundingSources) {
            fundingSource = entryFundingSource.getFundingSource().getFundingSource();
        }

        if (fundingSource == null) {
            fundingSource = "";
        }

        return fundingSource;
    }

    public static Map<String, String> getBioSafetyLevelOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put("1", "Level 1");
        resultMap.put("2", "Level 2");

        return resultMap;
    }

    public static Map<String, String> getStatusOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put("complete", JbeiConstants.getStatus("complete"));
        resultMap.put("in progress", JbeiConstants.getStatus("in progress"));
        resultMap.put("planned", JbeiConstants.getStatus("planned"));

        return resultMap;
    }

    public static Map<String, String> getEntryTypeOptionsMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put("plasmid", "Plasmid");
        resultMap.put("strain", "Strain");
        resultMap.put("part", "Part");

        return resultMap;
    }
}
