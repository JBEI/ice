package org.jbei.ice.lib.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "feature_feature_relationship")
@SequenceGenerator(name = "sequence", sequenceName = "feature_feature_relationship_id_seq", allocationSize = 1)
public class FeatureFeatureRelationship implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "subject")
    private Feature subject;

    @ManyToOne
    @JoinColumn(name = "object")
    private Feature object;

    @ManyToOne
    @JoinColumn(name = "relationship")
    private FeatureRelationship relationship;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Feature getSubject() {
        return subject;
    }

    public void setSubject(Feature subject) {
        this.subject = subject;
    }

    public Feature getObject() {
        return object;
    }

    public void setObject(Feature object) {
        this.object = object;
    }

    public FeatureRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(FeatureRelationship relationship) {
        this.relationship = relationship;
    }

}
