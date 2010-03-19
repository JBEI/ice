package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "feature_dna")
@SequenceGenerator(name = "sequence", sequenceName = "feature_dna_id_seq", allocationSize = 1)
public class FeatureDNA implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @Column(name = "hash", length = 40, nullable = false, unique = true)
    private String hash;

    @Column(name = "sequence", nullable = false, unique = true)
    @Lob
    private String sequence;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id", nullable = false, unique = true)
    private Feature feature;

    public FeatureDNA() {
        super();
    }

    public FeatureDNA(String hash, String sequence, Feature feature) {
        super();

        this.hash = hash;
        this.sequence = sequence;
        this.feature = feature;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
