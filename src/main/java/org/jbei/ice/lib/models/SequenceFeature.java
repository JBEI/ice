package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.vo.ISequenceFeatureValueObject;

@Entity
@Table(name = "sequence_feature")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_feature_id_seq", allocationSize = 1)
public class SequenceFeature implements ISequenceFeatureValueObject, IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "sequence_id")
    private Sequence sequence;

    @OneToOne
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "feature_start")
    private int start;

    @Column(name = "feature_end")
    private int end;

    @Column(name = "strand")
    private int strand;

    @Column(name = "name", length = 127)
    private String name;

    public SequenceFeature() {
        super();
    }

    public SequenceFeature(Sequence sequence, Feature feature, int start, int end, int strand,
            String name) {
        super();

        this.sequence = sequence;
        this.feature = feature;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
