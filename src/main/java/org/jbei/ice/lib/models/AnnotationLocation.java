package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

@Entity
@Table(name = "sequence_annotation_location")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_annotation_location_id_seq", allocationSize = 1)
public class AnnotationLocation implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "genbank_start", nullable = false)
    private int genbankStart;

    @Column(name = "genbank_end", nullable = false)
    // This column is named genbank_end because "end" is an sql keyword, and hibernate
    // does not quote table names during creation.  
    // if annotating only one base, set end=genbankStart, instead of end=null
    private int end;

    @Column(name = "single_residue")
    // genbank "single residue chosen from range of residues" notation (e.g. 3.7)
    private boolean singleResidue;

    @Column(name = "inbetween")
    // genbank "site between two residues" notation (e.g. 4^5)
    private boolean inbetween;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sequence_feature_id")
    private SequenceFeature sequenceFeature;

    public AnnotationLocation() {
        super();
    }

    public AnnotationLocation(int genbankStart, int end, SequenceFeature sequenceFeature) {
        super();

        this.genbankStart = genbankStart;
        this.end = end;
        this.sequenceFeature = sequenceFeature;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setGenbankStart(int genbankStart) {
        this.genbankStart = genbankStart;
    }

    public int getGenbankStart() {
        return genbankStart;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setSingleResidue(boolean singleResidue) {
        this.singleResidue = singleResidue;
    }

    public boolean isSingleResidue() {
        return singleResidue;
    }

    public void setInbetween(boolean inbetween) {
        this.inbetween = inbetween;
    }

    public boolean isInbetween() {
        return inbetween;
    }

    public void setSequenceFeature(SequenceFeature sequenceFeature) {
        this.sequenceFeature = sequenceFeature;
    }

    public SequenceFeature getSequenceFeature() {
        return sequenceFeature;
    }

}
