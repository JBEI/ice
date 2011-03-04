package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.ISequenceFeatureValueObject;

@Entity
@Table(name = "sequence_feature")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_feature_id_seq", allocationSize = 1)
public class SequenceFeature implements ISequenceFeatureValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sequence_id")
    private Sequence sequence;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "feature_start")
    private int genbankStart;

    @Column(name = "feature_end")
    private int end;

    /**
     * +1 or -1
     */
    @Column(name = "strand")
    private int strand;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    @Column(name = "flag")
    @Enumerated(EnumType.STRING)
    private AnnotationType annotationType;

    public SequenceFeature() {
        super();
    }

    public SequenceFeature(Sequence sequence, Feature feature, int genbankStart, int end,
            int strand, String name, String description, String genbankType,
            AnnotationType annotationType) {
        super();
        this.sequence = sequence;
        this.feature = feature;
        this.genbankStart = genbankStart;
        this.end = end;
        this.strand = strand;
        this.name = name;
        this.description = description;
        this.genbankType = genbankType;
        this.annotationType = annotationType;
    }

    public enum AnnotationType {
        PREFIX, SUFFIX, SCAR, INNER, SUBINNER;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    @XmlTransient
    public long getId() {
        return id;
    }

    @Override
    @XmlTransient
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public Feature getFeature() {
        return feature;
    }

    @Override
    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    /**
     * This is 1 based ala Genbank
     */
    @Override
    public int getGenbankStart() {
        return genbankStart;
    }

    @Override
    public void setGenbankStart(int genbankStart) {
        this.genbankStart = genbankStart;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public int getStrand() {
        return strand;
    }

    @Override
    public void setStrand(int strand) {
        this.strand = strand;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenbankType() {
        return genbankType;
    }

    public void setGenbankType(String genbankType) {
        this.genbankType = genbankType;
    }

    public void setAnnotationType(AnnotationType annotationType) {
        this.annotationType = annotationType;
    }

    public AnnotationType getAnnotationType() {
        return annotationType;
    }
}
