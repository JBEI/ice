package org.jbei.ice.lib.models;

import java.util.HashSet;
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

@Entity
@Table(name = "sequences")
@SequenceGenerator(name = "sequence", sequenceName = "sequences_id_seq", allocationSize = 1)
public class Sequence implements ISequenceValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = true, unique = true)
    private Entry entry;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "sequence")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "sequence_id")
    @OrderBy("id")
    private Set<SequenceFeature> sequenceFeatures = new HashSet<SequenceFeature>();

    public Sequence() {
    }

    public Sequence(String sequence, String sequenceUser, String fwdHash, String revHash,
            Entry entry, Set<SequenceFeature> sequenceFeatures) {
        super();

        this.sequence = sequence;
        this.sequenceUser = sequenceUser;
        this.fwdHash = fwdHash;
        this.revHash = revHash;
        this.entry = entry;
        this.sequenceFeatures = sequenceFeatures;
    }

    @XmlTransient
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
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

    public void setSequenceFeatures(Set<SequenceFeature> inputSequenceFeatures) {
        // for JAXB webservices should be this way
        if (inputSequenceFeatures == null) {
            this.sequenceFeatures.clear();

            return;
        }

        if (inputSequenceFeatures != this.sequenceFeatures) {
            this.sequenceFeatures.clear();
            this.sequenceFeatures.addAll(inputSequenceFeatures);
        }
    }

    public Set<SequenceFeature> getSequenceFeatures() {
        return sequenceFeatures;
    }
}
