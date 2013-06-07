package org.jbei.ice.lib.entry.model;

import com.google.common.base.Objects;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.*;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.filter.BlastFilterFactory;
import org.jbei.ice.lib.entry.filter.EntryHasFilterFactory;
import org.jbei.ice.lib.entry.filter.EntrySecurityFilterFactory;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.Visibility;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

import org.jbei.ice.lib.entry.model.Parameter;

/**
 * Entry class is the most important class in gd-ice. Other record types extend this class.
 * <p/>
 * Entry class represent the unique handle for each record in the system. It provides the common fields, such as the
 * recordId (uuid), timestamps, owner and creator information, etc.
 * <p/>
 * Many of the fields accept mediawiki style linking tags. For example, "[[jbei:JBx_000001|Descriptive Name]]" will
 * automatically generate a clickable link to the part JBx_000001 with text "Descriptive Name". The wiki link prefix
 * (jbei:) in this case can be configured in the configuration file. In the future, links to other registries can be
 * specified via the configuration, similar to other mediawiki links.
 * <p/>
 * Description of Entry fields:
 * <p/>
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
 * field, instead of the database id of {@link org.jbei.ice.lib.account.model.Account}. This means that other classes
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
 * etc) for this entry.</li> <li><b>selectionMarkers:</b> {@link org.jbei.ice.lib.models.SelectionMarker}s for this
 * entry. In the future, this field will propagate to other entries based on inheritance.</li> <li><b>links:</b> URL or
 * other links that point outside of this instance of gd-ice.</li> <lli><b>names: </b> {@link Name}s for this
 * entry.</li> <li><b>partNumbers: </b> {@link PartNumber}s for this entry.</li> <li><b>entryFundingSources</b> {@link
 * EntryFundingSource}s for this entry.</li> <li><b>parameters: {@link Parameter}s for this entry.</b></li> </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@Entity
@Indexed(index = "Entry")

@FullTextFilterDefs({
        @FullTextFilterDef(name = "security", impl = EntrySecurityFilterFactory.class),
        @FullTextFilterDef(name = "blastFilter", impl = BlastFilterFactory.class),
        @FullTextFilterDef(name = "boolean", impl = EntryHasFilterFactory.class)
})
@Table(name = "entries")
@SequenceGenerator(name = "sequence", sequenceName = "entries_id_seq", allocationSize = 1)
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
public class Entry implements IModel {
    private static final long serialVersionUID = 1L;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    private Set<SelectionMarker> selectionMarkers = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    private final Set<Link> links = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    private final Set<Name> names = new LinkedHashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    private final Set<PartNumber> partNumbers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "entry", fetch = FetchType.EAGER)
    @IndexedEmbedded
    private final Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "entry", orphanRemoval = true, fetch = FetchType.EAGER)
    private final List<Parameter> parameters = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, mappedBy = "entry",
            orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded
    private final Set<Permission> permissions = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "contents")
    private Set<Folder> folders = new HashSet<>();

    public Entry() {
        setStatus("Complete");
        setLongDescriptionType("text");
        setBioSafetyLevel(new Integer(1));
    }

    @XmlTransient
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

    @XmlTransient
    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @XmlTransient
    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public Set<Name> getNames() {
        return names;
    }

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
        String result;
        ArrayList<String> names = new ArrayList<>();
        for (Name name : this.names) {
            names.add(name.getName());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", names);
        return result;
    }

    public Set<PartNumber> getPartNumbers() {
        return partNumbers;
    }

    /**
     * Return the first {@link PartNumber} associated with this entry, preferring the PartNumber local to this instance
     * of gd-ice.
     *
     * @return PartNumber.
     */
    public PartNumber getOnePartNumber() {
        PartNumber result = null;
        // prefer local part number prefix over other prefixes
        if (partNumbers.size() > 0) {
            for (PartNumber partNumber : partNumbers) {
                String partNumberPrefix = Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX);
                if (partNumber.getPartNumber().contains(partNumberPrefix)) {
                    result = partNumber;
                    break;
                }
            }
            if (result == null) {
                result = (PartNumber) partNumbers.toArray()[0];
            }
        }
        return result;
    }

//    public void setPartNumbers(Set<PartNumber> inputPartNumbers) {
//        // for JAXB webservices should be this way
//        if (inputPartNumbers == null) {
//            partNumbers.clear();
//            return;
//        }
//
//        if (inputPartNumbers != partNumbers) {
//            partNumbers.clear();
//            partNumbers.addAll(inputPartNumbers);
//        }
//    }

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

    /**
     * Generate a String representation of the {@link SelectionMarker}s associated with this entry.
     *
     * @return Comma separated selection markers.
     */
    public String getSelectionMarkersAsString() {
        String result;
        ArrayList<String> markers = new ArrayList<>();
        for (SelectionMarker marker : selectionMarkers) {
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

    /**
     * String representation of {@link Link}s.
     *
     * @return Comma separated list of links.
     */
    public String getLinksAsString() {
        String result;
        ArrayList<String> links = new ArrayList<>();
        for (Link link : this.links) {
            links.add(link.getLink());
        }
        result = org.jbei.ice.lib.utils.Utils.join(", ", links);

        return result;
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

    @XmlTransient
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @XmlTransient
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

    public Set<Permission> getPermissions() {
        return this.permissions;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), getRecordId());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final Entry other = (Entry) obj;

        return Objects.equal(this.recordId, other.getRecordId())
                && Objects.equal(this.recordType, other.getRecordType())
                && Objects.equal(this.getId(), other.getId());
    }

    public Set<Folder> getFolders() {
        return folders;
    }

    public Set<EntryFundingSource> getFundingSources() {
        return entryFundingSources;
    }
}
