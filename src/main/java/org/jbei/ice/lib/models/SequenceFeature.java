package org.jbei.ice.lib.models;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cascade;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.interfaces.ISequenceFeatureValueObject;

@Entity
@Table(name = "sequence_feature")
@SequenceGenerator(name = "sequence", sequenceName = "sequence_feature_id_seq", allocationSize = 1)
public class SequenceFeature implements ISequenceFeatureValueObject, IModel {

    public static final String DESCRIPTION = "description";

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "sequenceFeature")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "sequence_feature_id")
    @OrderBy("id")
    private final Set<AnnotationLocation> annotationLocations = new LinkedHashSet<AnnotationLocation>();

    /**
     * Use locations instead. This field exists to allow scripted migration of data using
     * the new database schema.
     */
    @Deprecated
    @Column(name = "feature_start")
    private int genbankStart;

    /**
     * Use locations instead. This field exists to allow scripted migration of data using
     * the new database schema.
     */
    @Deprecated
    @Column(name = "feature_end")
    private int end;

    /**
     * +1 or -1
     */
    @Column(name = "strand")
    private int strand;

    @Column(name = "name", length = 127)
    private String name;

    /**
     * Deprecated since schema 0.8.0. Use SequenceFeatureAttribute with "description" as key
     */
    @Deprecated
    @Column(name = "description")
    @Lob
    private String description;

    @Column(name = "genbank_type", length = 127)
    private String genbankType;

    @Column(name = "flag")
    @Enumerated(EnumType.STRING)
    private AnnotationType annotationType;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "sequenceFeature")
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @JoinColumn(name = "sequence_feature_id")
    @OrderBy("id")
    private final Set<SequenceFeatureAttribute> sequenceFeatureAttributes = new LinkedHashSet<SequenceFeatureAttribute>();

    public SequenceFeature() {
        super();
    }

    public SequenceFeature(Sequence sequence, Feature feature, int strand, String name,
            String genbankType, AnnotationType annotationType) {
        super();
        this.sequence = sequence;
        this.feature = feature;
        this.strand = strand;
        this.name = name;
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

    public void setAnnotationLocations(Set<AnnotationLocation> annotationLocations) {
        // for JAXB web services
        if (annotationLocations == null) {
            this.annotationLocations.clear();
            return;
        }
        if (annotationLocations != this.annotationLocations) {
            annotationLocations.clear();
            this.annotationLocations.addAll(annotationLocations);
        }
    }

    public Set<AnnotationLocation> getAnnotationLocations() {
        return annotationLocations;
    }

    @Deprecated
    public int getGenbankStart() {
        return genbankStart;
    }

    @Deprecated
    public void setGenbankStart(int genbankStart) {
        this.genbankStart = genbankStart;
    }

    @Deprecated
    public int getEnd() {
        return end;
    }

    @Deprecated
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

    /**
     * Deprecated since schema > 0.8.0. Use SequenceFeatureAttribute with "description" as key
     * 
     * @return
     */
    @Deprecated
    public String getDescription() {
        return description;
    }

    /**
     * Deprecated since schema > 0.8.0. Use SequenceFeatureAttribute with "description" as key
     * 
     * @param description
     */
    @Deprecated
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

    public Set<SequenceFeatureAttribute> getSequenceFeatureAttributes() {
        return sequenceFeatureAttributes;
    }

    public void setSequenceFeatureAttributes(Set<SequenceFeatureAttribute> sequenceFeatureAttributes) {
        if (sequenceFeatureAttributes == null) {
            this.sequenceFeatureAttributes.clear();
            return;
        }

        if (this.sequenceFeatureAttributes != sequenceFeatureAttributes) {
            sequenceFeatureAttributes.clear();
            sequenceFeatureAttributes.addAll(sequenceFeatureAttributes);
        }

    }

    public Integer getUniqueGenbankStart() {
        Integer result = null;
        if (getAnnotationLocations() != null && getAnnotationLocations().size() == 1) {
            result = ((AnnotationLocation) getAnnotationLocations().toArray()[0]).getGenbankStart();
        }
        return result;
    }

    public Integer getUniqueEnd() {
        Integer result = null;
        if (getAnnotationLocations() != null && getAnnotationLocations().size() == 1) {
            result = ((AnnotationLocation) getAnnotationLocations().toArray()[getAnnotationLocations()
                    .size() - 1]).getEnd();
        }
        return result;
    }

}
