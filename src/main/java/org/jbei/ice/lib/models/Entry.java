package org.jbei.ice.lib.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.IEntryValueObject;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.lib.utils.JbeirSettings;

@Entity
@Table(name = "entries")
@SequenceGenerator(name = "sequence", sequenceName = "entries_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
public class Entry implements IEntryValueObject, IModel {
    private static final long serialVersionUID = 1L;

    public static final String STRAIN_ENTRY_TYPE = "strain";
    public static final String PLASMID_ENTRY_TYPE = "plasmid";
    public static final String PART_ENTRY_TYPE = "part";
    public static final String ARABIDOPSIS_SEED_ENTRY_TYPE = "arabidopsis";

    //TODO actually use these types
    public enum MarkupType {
        text, wiki, confluence
    }

    // TODO actually use these types
    public enum EntryType {
        strain(STRAIN_ENTRY_TYPE), plasmid(PLASMID_ENTRY_TYPE), part(PART_ENTRY_TYPE), arabidopsis(
                ARABIDOPSIS_SEED_ENTRY_TYPE);

        private String name;

        EntryType(String name) {
            this.name = name;
        }

        public static EntryType nameToType(String name) {
            for (EntryType type : EntryType.values()) {
                if (name.equals(type.getName()))
                    return type;
            }

            return null;
        }

        public String getName() {
            return this.name;
        }
    }

    // TODO use these enums. Currently "in progress" with a space is used. 
    public enum StatusOptions {
        complete, in_progress, planned
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "record_id", length = 36, nullable = false)
    private String recordId;

    @Column(name = "version_id", length = 36, nullable = false)
    private String versionId;

    @Column(name = "record_type", length = 127, nullable = false)
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

    @Column(name = "long_description_type", length = 31, nullable = false)
    private String longDescriptionType;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private Set<SelectionMarker> selectionMarkers = new LinkedHashSet<SelectionMarker>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private final Set<Link> links = new LinkedHashSet<Link>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private final Set<Name> names = new LinkedHashSet<Name>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private final Set<PartNumber> partNumbers = new LinkedHashSet<PartNumber>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private final Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "entry")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "entries_id")
    @OrderBy("id")
    private final List<Parameter> parameters = new ArrayList<Parameter>();

    public Entry() {
    }

    public Entry(String recordId, String versionId, String recordType, String owner,
            String ownerEmail, String creator, String creatorEmail, String status, String alias,
            String keywords, String shortDescription, String longDescription,
            String longDescriptionType, String references, Date creationTime, Date modificationTime) {
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
        this.longDescriptionType = longDescriptionType;
        this.references = references;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
    }

    @Override
    @XmlTransient
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getRecordId() {
        return recordId;
    }

    @Override
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    @Override
    @XmlTransient
    public String getVersionId() {
        return versionId;
    }

    @Override
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @Override
    @XmlTransient
    public String getRecordType() {
        return recordType;
    }

    @Override
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @Override
    public Set<Name> getNames() {
        return names;
    }

    @Override
    public void setNames(Set<Name> inputNames) {
        /*
         * Warning! This is a hibernate workaround. 
         */

        // for JAXB webservices should be this way
        if (inputNames == null) {
            names.clear();

            return;
        }

        if (inputNames != names) {
            names.clear();
            names.addAll(inputNames);
        }
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

    @Override
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
        // prefer local part number prefix over other prefixes
        if (partNumbers.size() > 0) {
            for (PartNumber partNumber : partNumbers) {
                if (partNumber.getPartNumber().contains(
                    JbeirSettings.getSetting("PART_NUMBER_PREFIX"))) {
                    result = partNumber;
                }
            }
            if (result == null) {
                result = (PartNumber) partNumbers.toArray()[0];
            }
        }
        return result;
    }

    @Override
    public void setPartNumbers(Set<PartNumber> inputPartNumbers) {
        // for JAXB webservices should be this way
        if (inputPartNumbers == null) {
            partNumbers.clear();

            return;
        }

        if (inputPartNumbers != partNumbers) {
            partNumbers.clear();
            partNumbers.addAll(inputPartNumbers);
        }
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwnerEmail() {
        return ownerEmail;
    }

    @Override
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @Override
    public String getCreatorEmail() {
        return creatorEmail;
    }

    @Override
    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
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

    @Override
    public void setSelectionMarkers(Set<SelectionMarker> inputSelectionMarkers) {
        if (inputSelectionMarkers == null) {
            selectionMarkers.clear();

            return;
        }

        if (inputSelectionMarkers == selectionMarkers) {
            selectionMarkers = inputSelectionMarkers;
        } else {
            selectionMarkers.clear();
            selectionMarkers.addAll(inputSelectionMarkers);
        }
    }

    @Override
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

    @Override
    public void setLinks(Set<Link> inputLinks) {
        if (inputLinks == null) {
            links.clear();

            return;
        }

        if (inputLinks != links) {
            links.clear();
            links.addAll(inputLinks);
        }
    }

    @Override
    public String getKeywords() {
        return keywords;
    }

    @Override
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public String getShortDescription() {
        return shortDescription;
    }

    @Override
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Override
    public String getLongDescription() {
        return longDescription;
    }

    @Override
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getLongDescriptionType() {
        return longDescriptionType;
    }

    public void setLongDescriptionType(String longDescriptionType) {
        this.longDescriptionType = longDescriptionType;
    }

    @Override
    public String getReferences() {
        return references;
    }

    @Override
    public void setReferences(String references) {
        this.references = references;
    }

    @Override
    @XmlTransient
    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    @XmlTransient
    public Date getModificationTime() {
        return modificationTime;
    }

    @Override
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

    public void setEntryFundingSources(Set<EntryFundingSource> inputEntryFundingSources) {
        if (inputEntryFundingSources == null) {
            entryFundingSources.clear();

            return;
        }

        if (inputEntryFundingSources != entryFundingSources) {
            entryFundingSources.clear();
            entryFundingSources.addAll(inputEntryFundingSources);
        }
    }

    public Set<EntryFundingSource> getEntryFundingSources() {
        return entryFundingSources;
    }

    public void setParameters(List<Parameter> inputParameters) {
        if (inputParameters == null) {
            parameters.clear();
            return;
        }
        if (inputParameters != parameters) {
            for (Parameter parameter : inputParameters) {
                parameter.setEntry(this);
            }
            parameters.clear();
            parameters.addAll(inputParameters);
        }
    }

    public List<Parameter> getParameters() {
        return parameters;
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

    public static Map<String, String> getMarkupTypeMap() {
        Map<String, String> resultMap = new LinkedHashMap<String, String>();

        resultMap.put(Entry.MarkupType.text.name(), "Text");
        resultMap.put(Entry.MarkupType.wiki.name(), "Wiki");
        resultMap.put(Entry.MarkupType.confluence.name(), "Confluence");

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

        resultMap.put(Entry.EntryType.plasmid.name(), "Plasmid");
        resultMap.put(Entry.EntryType.strain.name(), "Strain");
        resultMap.put(Entry.EntryType.part.name(), "Part");
        resultMap.put(Entry.EntryType.arabidopsis.name(), "Arabidopsis");

        return resultMap;
    }
}
