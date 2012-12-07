package org.jbei.ice.lib.models;

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

/**
 * Many-to-many relationship table between two {@link Feature}s and {@link FeatureRelationship}.
 * <p/>
 * For example, Feature1 "is derived from" Feature2 is represented as subject = Feature1, object =
 * Feature2, and relationship = "derived from".
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "feature_feature_relationship")
@SequenceGenerator(name = "sequence", sequenceName = "feature_feature_relationship_id_seq", allocationSize = 1)
public class FeatureFeatureRelationship implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject")
    private Feature subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "object")
    private Feature object;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "relationship")
    private FeatureRelationship relationship;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
