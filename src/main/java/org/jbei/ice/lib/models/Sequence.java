package org.jbei.ice.lib.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.ISequenceValueObject;
import org.jbei.ice.lib.utils.SequenceFeatureCollection;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;

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
 * 
 */
@Entity
@Table(name = "sequences")
@SequenceGenerator(name = "sequence", sequenceName = "sequences_id_seq", allocationSize = 1)
public class Sequence implements ISequenceValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "sequence")
    @Lob
    private String sequence;

    @Column(name = "sequence_user")
    @Lob
    private String sequenceUser;

    @Column(name = "fwd_hash", length = 40)
    private String fwdHash;

    @Column(name = "rev_hash", length = 40)
    private String revHash;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "entries_id", nullable = true, unique = true)
    private Entry entry;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "sequence")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "sequence_id")
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
    public String getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(String sequence) {
        this.sequence = sequence;
        setFwdHash(SequenceUtils.calculateSequenceHash(sequence));
        try {
            setRevHash(SequenceUtils.calculateReverseComplementSequenceHash(sequence));
        } catch (UtilityException e) {
            setRevHash("");
        }

    }

    @Override
    @XmlTransient
    public String getSequenceUser() {
        return sequenceUser;
    }

    @Override
    public void setSequenceUser(String sequenceUser) {
        this.sequenceUser = sequenceUser;
    }

    @Override
    @XmlTransient
    public String getFwdHash() {
        return fwdHash;
    }

    @Override
    public void setFwdHash(String fwdHash) {
        this.fwdHash = fwdHash;
    }

    @Override
    @XmlTransient
    public String getRevHash() {
        return revHash;
    }

    @Override
    public void setRevHash(String revHash) {
        this.revHash = revHash;
    }

    @Override
    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    @Override
    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    @Override
    public void setSequenceFeatures(Set<SequenceFeature> inputSequenceFeatures) {
        // for JAXB webservices should be this way
        if (inputSequenceFeatures == null) {
            sequenceFeatures.clear();

            return;
        }

        if (inputSequenceFeatures != sequenceFeatures) {
            sequenceFeatures.clear();
            sequenceFeatures.addAll(inputSequenceFeatures);
        }
    }

    @Override
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
