package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.hibernate.bridge.EntryBooleanPropertiesBridge;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores the unique sequence for an {@link Entry} object.
 * <p>
 * <ul>
 * <li><b>sequence: </b>Normalized (lower cased, trimmed) sequence for {@link Entry}.</li>
 * <li><b>sequenceUser: </b>Original sequence uploaded by the user. For example, the unparsed
 * genbank file, if that was the original upload. If the original upload does not exist, then this
 * field is the same as sequence.</li>
 * <li><b>fwdHash, revHash: </b>sha1 hash of the normalized sequence for fast searches.</li>
 * <li><b>sequenceFeatures: </b>{@link SequenceFeature} objects.</li>
 * </ul>
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "sequences")
@SequenceGenerator(name = "sequence", sequenceName = "sequences_id_seq", allocationSize = 1)
public class Sequence implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "sequence")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequence;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "sequence_user")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequenceUser;

    @Column(name = "fwd_hash", length = 40)
    private String fwdHash;

    @Column(name = "rev_hash", length = 40)
    private String revHash;

    @Column(name = "uri")
    private String uri;

    @Column(name = "component_uri")
    private String componentUri;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "format")
    @Enumerated(value = EnumType.STRING)
    private SequenceFormat format;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "entries_id", nullable = true, unique = true)
    @Field(bridge = @FieldBridge(impl = EntryBooleanPropertiesBridge.class, params = {
            @org.hibernate.search.annotations.Parameter(name = "boolean", value = "hasSequence")
    }))
    private Entry entry;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "sequence")
    @OrderBy("id")
    private Set<SequenceFeature> sequenceFeatures = new HashSet<>();

    public Sequence() {
    }

    public Sequence(String sequence, String sequenceUser, String fwdHash, String revHash, Entry entry) {
        super();

        this.sequence = sequence;
        this.sequenceUser = sequenceUser;
        this.fwdHash = fwdHash;
        this.revHash = revHash;
        this.entry = entry;
    }

    @XmlTransient
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
        setFwdHash(SequenceUtils.calculateSequenceHash(sequence));
        try {
            setRevHash(SequenceUtils.calculateReverseComplementSequenceHash(sequence));
        } catch (UtilityException e) {
            setRevHash("");
        }
    }

    @XmlTransient
    public String getSequenceUser() {
        return sequenceUser;
    }

    public void setSequenceUser(String sequenceUser) {
        this.sequenceUser = sequenceUser;
    }

    @XmlTransient
    public String getFwdHash() {
        return fwdHash;
    }

    public void setFwdHash(String fwdHash) {
        this.fwdHash = fwdHash;
    }

    @XmlTransient
    public String getRevHash() {
        return revHash;
    }

    public void setRevHash(String revHash) {
        this.revHash = revHash;
    }

    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Set<SequenceFeature> getSequenceFeatures() {

//        /* Hibernate hack.
//        To use custom collections with Hibernate, I have to implement all sorts
//        of hibernate methods to do this correctly. Instead, I just replace this set
//        when I do a get method here with the SequenceFeatureCollection.
//        */
//        if (sequenceFeatures instanceof SequenceFeatureCollection) {
//
//        } else {
//            SequenceFeatureCollection newSequenceFeatures = new SequenceFeatureCollection();
//            newSequenceFeatures.addAll(sequenceFeatures);
//            sequenceFeatures = newSequenceFeatures;
//        }
        return sequenceFeatures;
    }

    public void setSequenceFeatures(Set<SequenceFeature> features) {
        sequenceFeatures = features;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return uri for component that this sequence is a part of
     */
    public String getComponentUri() {
        return componentUri;
    }

    public void setComponentUri(String componentUri) {
        this.componentUri = componentUri;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFormat(SequenceFormat format) {
        this.format = format;
    }

    public SequenceFormat getFormat() {
        return this.format;
    }

    @Override
    public SequenceInfo toDataTransferObject() {
        SequenceInfo info = new SequenceInfo();
        if (this.entry != null) {
            info.setEntryId(entry.getId());
        }
        info.setFilename(fileName);
        return info;
    }
}
