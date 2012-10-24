package org.jbei.ice.lib.models;

import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.utils.SequenceFeatureCollection;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;

import org.hibernate.annotations.Type;

/**
 * Stores the unique sequence for an {@link org.jbei.ice.lib.entry.model.Entry} object.
 * <p/>
 * <ul>
 * <li><b>sequence: </b>Normalized (lower cased, trimmed) sequence for {@link org.jbei.ice.lib.entry.model.Entry}.</li>
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
public class Sequence implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "sequence")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequence;

    @Column(name = "sequence_user")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String sequenceUser;

    @Column(name = "fwd_hash", length = 40)
    private String fwdHash;

    @Column(name = "rev_hash", length = 40)
    private String revHash;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "entries_id", nullable = true, unique = true)
    private Entry entry;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "sequence")
    @OrderBy("id")
    private Set<SequenceFeature> sequenceFeatures = new SequenceFeatureCollection();

    public Sequence() {
    }

    public Sequence(String sequence, String sequenceUser, String fwdHash, String revHash,
            Entry entry) {
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

        /* Hibernate hack.
        To use costum collections with Hibernate, I have to implement all sorts
        of hibernate methods to do this correctly. Instead, I just replace this set
        when I do a get method here with the SequenceFeatureCollection.
        */
        if (sequenceFeatures instanceof SequenceFeatureCollection) {

        } else {
            SequenceFeatureCollection newSequenceFeatures = new SequenceFeatureCollection();
            newSequenceFeatures.addAll(sequenceFeatures);
            sequenceFeatures = newSequenceFeatures;
        }
        return sequenceFeatures;
    }
}
