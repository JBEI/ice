package org.jbei.ice.lib.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.dao.IModel;

/**
 * Represents the relationship between {@link Feature}s. For example, "mutant of" would be a
 * FeatureRelationship.
 *
 * @author Timothy Ham
 */
@Entity
@Table(name = "feature_relationship")
@SequenceGenerator(name = "sequence", sequenceName = "feature_relationship_id_seq", allocationSize = 1)
public class FeatureRelationship implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "description", length = 1023)
    private String description;

    @Column(name = "ontology", length = 128)
    private String ontology;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOntology() {
        return ontology;
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

}
