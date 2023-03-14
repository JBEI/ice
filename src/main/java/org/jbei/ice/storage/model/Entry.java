package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.TypeBinderRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.entry.Visibility;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.hibernate.bridge.EntryBooleanPropertiesBridge;

import java.util.*;

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
 * field, instead of the database id of {@link AccountModel}. This means that other classes
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
 * <li><b>parameters: {@link ParameterModel}s for this entry.</b></li> </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@Entity
@Indexed(index = "Entry")
@TypeBinding(binder = @TypeBinderRef(type = EntryBooleanPropertiesBridge.class))
//@FullTextFilterDefs({
//    @FullTextFilterDef(name = "security", impl = EntrySecurityFilterFactory.class, cache = FilterCacheModeType.INSTANCE_ONLY),
//    @FullTextFilterDef(name = "boolean", impl = EntryHasFilterFactory.class, cache = FilterCacheModeType.INSTANCE_ONLY)
//})
//@AnalyzerDef(name = "customanalyzer",
//    tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
//    filters = {
//        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
//        @TokenFilterDef(factory = PatternReplaceFilterFactory.class, params = {
//            @org.hibernate.search.annotations.Parameter(name = "pattern", value = "[_-]"),
//            @org.hibernate.search.annotations.Parameter(name = "replacement", value = " ")
//        })
//    })
@Table(name = "entries")
@SequenceGenerator(name = "sequence", sequenceName = "entries_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.JOINED)
public class Entry implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @DocumentId
    private long id;

    @Column(name = "record_id", length = 36, nullable = false, unique = true)
    @KeywordField
    private String recordId;

    @Column(name = "version_id", length = 36, nullable = false)
    private String versionId;

    @Column(name = "record_type", length = 127, nullable = false)
    @KeywordField(name = "type", sortable = Sortable.YES, projectable = Projectable.YES)
    private String recordType;

    @Column(name = "owner", length = 127)
    @GenericField
    private String owner;

    @Column(name = "owner_email", length = 127)
    @KeywordField
    private String ownerEmail;

    @Column(name = "part_number", length = 127)
    @KeywordField(name = "partNumber_sort", projectable = Projectable.YES, sortable = Sortable.YES)
    private String partNumber;

    @Column(name = "visibility")
    @GenericField
    private Integer visibility = Visibility.OK.getValue();

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
//    @KeywordField(name = "created_sort", sortable = Sortable.YES, projectable = Projectable.YES)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(includeDepth = 1)
    private final List<ParameterModel> parameters = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, mappedBy = "entry",
        orphanRemoval = true, fetch = FetchType.LAZY)
    @IndexedEmbedded(includeDepth = 1)
    private final Set<Permission> permissions = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "contents", fetch = FetchType.LAZY)
    @IndexedEmbedded(includeDepth = 1)
    private final Set<Folder> folders = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(name = "entry_entry", joinColumns = {@JoinColumn(name = "entry_id", nullable = false)},
        inverseJoinColumns = {@JoinColumn(name = "linked_entry_id", nullable = false)})
    private final Set<Entry> linkedEntries = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(includeDepth = 1)
    private final Set<Sample> samples = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(includeDepth = 1)
    private final Set<Attachment> attachments = new HashSet<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "entry")
    @IndexedEmbedded(includeDepth = 1)
    private final Set<EntryFieldValueModel> fieldValues = new HashSet<>();

    @OneToOne(orphanRemoval = true, mappedBy = "entry")
    @IndexedEmbedded(includeDepth = 1)
    private Sequence sequence;

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

    public Integer getVisibility() {
        if (visibility == null)
            return Visibility.OK.getValue();
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = Objects.requireNonNullElseGet(visibility, Visibility.OK::getValue);
    }

    public void setParameters(List<ParameterModel> inputParameters) {
        if (inputParameters == null) {
            parameters.clear();
            return;
        }
        if (inputParameters != parameters) {
            for (ParameterModel parameter : inputParameters) {
                parameter.setEntry(this);
            }
            parameters.clear();
            parameters.addAll(inputParameters);
        }
    }

    public List<ParameterModel> getParameters() {
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
        PartData partData = new PartData(EntryType.nameToType(this.getRecordType()));
        partData.setId(this.id);
        partData.setOwner(this.owner);
        partData.setOwnerEmail(this.ownerEmail);
        partData.setCreationTime(this.creationTime.getTime());
        partData.setPartId(this.partNumber);
        return partData;
    }
}
