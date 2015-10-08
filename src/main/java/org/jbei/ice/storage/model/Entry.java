package org.jbei.ice.storage.model;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.pattern.PatternReplaceFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.filter.EntryHasFilterFactory;
import org.jbei.ice.storage.hibernate.filter.EntrySecurityFilterFactory;

import javax.persistence.*;
import java.util.*;

import org.jbei.ice.storage.model.Parameter;

/**
 * Entry class is the most important class in gd-ice. Other record types extend this class.
 * <p>
 * Entry class represent the unique handle for each record in the system. It provides the common fields, such as the
 * recordId (uuid), timestamps, owner and creator information, etc.
 * <p>
 * Description of Entry fields:
 * <p>
 * <ul> <li><b>id:</b> database id of an entry.</li> <li><b>recordId:</b> 36 character globally unique identifier.
 * Implemented as UUIDv4.</li> <li><b>versionId:</b> 36 character globally unique identifier.</li>
 * <li><b>recordType:</b> The type of record. Currently there are plasmids, strains, arabidopsis seeds, and parts.
 * Parts
 * represent linear DNA in a packaging format, such as Biobricks, or raw DNA for ligationless assembly.</li>
 * <li><b>owner:</b> Owner is the person that has complete read and write access to a part. This field is the user
 * friendly string, such as their full name, and is not used by the system for identification and association. See
 * ownerEmail for that functionality. This field is also distinct from the creator field.</li> <li><b>ownerEmail:</b>
 * Email address of the owner. Because an entry can be exchanged between different registries, without having the
 * corresponding account records be exchanged along with it, association of entry with a user is done through this
 * field, instead of the database id of {@link Account}. This means that other classes
 * (such as {@link org.jbei.ice.lib .entry.sample.model.Sample}) also associate via the email address. Consequently,
 * email address must be unique to a gd-ice instance.</li> <li><b>creator:</b> Creator is the person that has created
 * this entry. If the entry came from somewhere else, or was entered into this instance of gd-ice by someone other than
 * the creator, then the owner and the creator fields would be different. This field is the user friendly string, such
 * as their full name, and is not used by the system for identification and association.</li> <li><b>creatorEmail:</b>
 * Email address of the creator. For some very old entries, or entries that came from a third party, email address
 * maybe
 * empty.</li> <li><b>alias:</b> Comma separated list of alias names.</li> <li><b>keywords:</b> Comma separated list of
 * keywords.</li> <li><b>status:</b> Status of this entry. Currently the options are complete, in progress, or planned.
 * This field in the future should be used to filter search results.</li> <li><b>shortDescription:</b> A summary of the
 * entry in a few words. This is what is displayed in the search result summaries, and brevity is best.</li>
 * <li><b>longDescription:</b> Longer description for the entry. Details that are not part of the summary description
 * should be placed in this field. This field accepts markup text of different styles. see longDescriptionType</li>
 * <li><b>longDescriptionType: Markup syntax used for long description. Currently plain text, mediawiki, and confluence
 * markup syntax is supported.</b>/ <li> <li><b>references:</b> References for this entry.</li>
 * <li><b>creationTime:</b>
 * Timestamp of creation of this entry.</li> <li><b>modificationTime:</b> Timestamp of last modification of this
 * entry.</li> <li><b>bioSafetyLevel:</b> The biosafety level of this entry. In the future, this field will propagate
 * to
 * other entries, so that a strain entry holding a higher level bioSafetyLevel plasmid entry will inherit the higher
 * bioSafetyLevel.</li> <li><b>intellectualProperty:</b> Information about intellectual property (patent filing
 * numbers,
 * etc) for this entry.</li> <li><b>selectionMarkers:</b> {@link SelectionMarker}s for this
 * entry. In the future, this field will propagate to other entries based on inheritance.</li> <li><b>links:</b> URL or
 * other links that point outside of this instance of gd-ice.</li> <li><b>name: </b> name for this
 * entry.</li> <li><b>partNumber: </b> human readable unique identifier for this entry.</li>
 * <li><b>parameters: {@link Parameter}s for this entry.</b></li> </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@Entity
@Indexed(index = "Entry")
@FullTextFilterDefs({
        @FullTextFilterDef(name = "security", impl = EntrySecurityFilterFactory.class, cache = FilterCacheModeType.INSTANCE_ONLY),
        @FullTextFilterDef(name = "boolean", impl = EntryHasFilterFactory.class, cache = FilterCacheModeType.INSTANCE_ONLY)
})
@AnalyzerDef(name = "customanalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
                        @org.hibernate.search.annotations.Parameter(name = "pattern", value = "[_-]"),
                        @org.hibernate.search.annotations.Parameter(name = "replacement", value = " ")
                })
        })
@Table(name = "entries")
@SequenceGenerator(name = "sequence", sequenceName = "entries_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.JOINED)
public class Entry implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @DocumentId
    private long id;

    @Column(name = "record_id", length = 36, nullable = false, unique = true)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String recordId;

    @Column(name = "version_id", length = 36, nullable = false)
    private String versionId;

    @Column(name = "record_type", length = 127, nullable = false)
    @Field(store = Store.YES, analyze = Analyze.NO)
    @SortableField(forField = "recordType")
    private String recordType;

    @Column(name = "owner", length = 127)
    @Field(store = Store.YES)
    private String owner;

    @Column(name = "owner_email", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String ownerEmail;

    @Column(name = "creator", length = 127)
    @Field(store = Store.YES)
    private String creator;

    @Column(name = "creator_email", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String creatorEmail;

    @Column(name = "alias", length = 127)
    @Field(store = Store.YES)
    private String alias;

    @Column(name = "name", length = 127)
    @Field(store = Store.YES, boost = @Boost(2f))
    private String name;

    @Column(name = "part_number", length = 127)
    @Fields({
            @Field(boost = @Boost(2f), store = Store.YES),
            @Field(name = "partNumber_forSort", analyze = Analyze.NO, store = Store.YES)
    })
    @Analyzer(definition = "customanalyzer")
    private String partNumber;

    @Column(name = "keywords", length = 127)
    @Field
    @Boost(1.2f)
    private String keywords;

    @Column(name = "status", length = 127)
    private String status;

    @Column(name = "visibility")
    @Field(analyze = Analyze.NO)
    private Integer visibility = Visibility.OK.getValue();

    @Column(name = "short_description")
    @Field
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String shortDescription;

    @Column(name = "long_description")
    @Field
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String longDescription;

    @Column(name = "long_description_type", length = 31, nullable = false)
    private String longDescriptionType = "text";

    @Column(name = "literature_references")
    @Field
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String references;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES)
    @DateBridge(resolution = Resolution.DAY)
    @SortableField(forField = "creationTime")
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES)
    @DateBridge(resolution = Resolution.DAY)
    private Date modificationTime;

    @Column(name = "bio_safety_level")
    @Field(analyze = Analyze.NO)
    private Integer bioSafetyLevel;

    @Column(name = "intellectual_property")
    @Field
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String intellectualProperty;

    @Column(name = "funding_source", length = 512)
    @Field
    private String fundingSource;

    @Column(name = "principal_investigator", length = 512)
    @Field
    private String principalInvestigator;

    @Column(name = "principal_investigator_email", length = 127)
    @Field(store = Store.YES, analyze = Analyze.NO)
    private String principalInvestigatorEmail;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    private Set<SelectionMarker> selectionMarkers = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    private final Set<Link> links = new LinkedHashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    private final List<Parameter> parameters = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, mappedBy = "entry",
            orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(depth = 1)
    private final Set<Permission> permissions = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "contents")
    private Set<Folder> folders = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "entry_entry", joinColumns = {@JoinColumn(name = "entry_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "linked_entry_id", nullable = false)})
    private Set<Entry> linkedEntries = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(depth = 1)
    private final Set<Sample> samples = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(depth = 1)
    private final Set<Attachment> attachments = new HashSet<>();

    @OneToOne(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(depth = 1)
    private Sequence sequence;

    public Entry() {
        longDescriptionType = "text";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        String result;
        ArrayList<String> markers = new ArrayList<>();
        for (SelectionMarker marker : this.selectionMarkers) {
            markers.add(marker.getName());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", markers);

        return result;
    }

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

    public Set<Link> getLinks() {
        return links;
    }

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

    public String getLongDescriptionType() {
        return longDescriptionType;
    }

    public void setLongDescriptionType(String longDescriptionType) {
        this.longDescriptionType = longDescriptionType;
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
        if (modificationTime == null)
            return creationTime;
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

    public Integer getVisibility() {
        if (visibility == null)
            return Visibility.OK.getValue();
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        if (visibility == null)
            this.visibility = Visibility.OK.getValue();
        else
            this.visibility = visibility;
    }

    public String getIntellectualProperty() {
        return intellectualProperty;
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

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getRecordId());
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public Set<Entry> getLinkedEntries() {
        return linkedEntries;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final Entry other = (Entry) obj;

        return this.recordId.equals(other.getRecordId()) && this.id == other.getId();
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public PartData toDataTransferObject() {
        return ModelToInfoFactory.getCommon(new PartData(EntryType.nameToType(this.getRecordType())), this);
    }

    public String getPrincipalInvestigatorEmail() {
        return principalInvestigatorEmail;
    }

    public void setPrincipalInvestigatorEmail(String principalInvestigatorEmail) {
        this.principalInvestigatorEmail = principalInvestigatorEmail;
    }
}
