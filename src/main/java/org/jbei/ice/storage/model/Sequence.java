package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.entry.SequenceInfo;
import org.jbei.ice.entry.sequence.SequenceFormat;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.utils.SequenceUtils;
import org.jbei.ice.utils.UtilityException;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores the unique sequence for an {@link org.jbei.ice.storage.model.Entry} object.
 * <p>
 * <ul>
 * <li><b>sequence: </b>Normalized (lower cased, trimmed) sequence for {@link org.jbei.ice.storage.model.Entry}.</li>
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
@SequenceGenerator(name = "sequences_id", sequenceName = "sequences_id_seq", allocationSize = 1)
public class Sequence implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequences_id")
    private long id;

    @Column(name = "sequence")
    @Lob
    private String sequence;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "sequence_user")
    @Lob
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
    @JoinColumn(name = "entries_id", unique = true)
//    @Field(bridge = @FieldBridge(impl = EntryBooleanPropertiesBridge.class, params = {
//            @org.hibernate.search.annotations.Parameter(name = "boolean", value = "hasSequence")
//    }))
    private org.jbei.ice.storage.model.Entry entry;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "sequence")
    private Set<SequenceFeature> sequenceFeatures = new HashSet<>();

    public Sequence() {
    }

    public Sequence(String sequence, String sequenceUser, String fwdHash, String revHash, org.jbei.ice.storage.model.Entry entry) {
        super();

        this.sequence = sequence;
        this.sequenceUser = sequenceUser;
        this.fwdHash = fwdHash;
        this.revHash = revHash;
        this.entry = entry;
    }

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

    public String getSequenceUser() {
        return sequenceUser;
    }

    public void setSequenceUser(String sequenceUser) {
        this.sequenceUser = sequenceUser;
    }

    public String getFwdHash() {
        return fwdHash;
    }

    public void setFwdHash(String fwdHash) {
        this.fwdHash = fwdHash;
    }

    public String getRevHash() {
        return revHash;
    }

    public void setRevHash(String revHash) {
        this.revHash = revHash;
    }

    public org.jbei.ice.storage.model.Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Set<SequenceFeature> getSequenceFeatures() {
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

    public SequenceFormat getFormat() {
        return this.format;
    }

    public void setFormat(SequenceFormat format) {
        this.format = format;
    }

    @Override
    public SequenceInfo toDataTransferObject() {
        SequenceInfo info = new SequenceInfo();
        if (this.entry != null) {
            info.setEntryId(entry.getId());
        }
        info.setFilename(fileName);
        if (this.format != null)
            info.setFormat(this.format);
        return info;
    }
}
