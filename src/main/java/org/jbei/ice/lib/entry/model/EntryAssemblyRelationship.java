package org.jbei.ice.lib.entry.model;

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
import org.jbei.ice.lib.models.AssemblyRelationship;

/**
 * Many-to-Many relationship table between two {@link Entry} and {@link org.jbei.ice.lib.models.AssemblyRelationship}.
 * <p/>
 * For example, Part1 "is a subcomponent of" Part2 is represented as subject = Part1, object =
 * Part2, and relationship = "sub component of".
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "entry_entry_assembly_relationship")
@SequenceGenerator(name = "sequence", sequenceName = "entry_entry_assembly_relationship_id_seq", allocationSize = 1)
public class EntryAssemblyRelationship implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject")
    private Entry subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "object")
    private Entry object;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "relationship")
    private AssemblyRelationship relationship;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Entry getSubject() {
        return subject;
    }

    public void setSubject(Entry subject) {
        this.subject = subject;
    }

    public Entry getObject() {
        return object;
    }

    public void setObject(Entry object) {
        this.object = object;
    }

    public AssemblyRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(AssemblyRelationship relationship) {
        this.relationship = relationship;
    }

}
