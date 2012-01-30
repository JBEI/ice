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
 * Many-to-Many representation betwee {@link Entry} and {@link Label}.
 * <p>
 * This class explicitly spells out the many-to-many representation instead of relying on
 * Hibernate's automatic intermediate table generation due to historical database compatibility with
 * the python version.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
@Entity
@Table(name = "entry_labels")
@SequenceGenerator(name = "sequence", sequenceName = "entry_labels_id_seq", allocationSize = 1)
public class EntryLabel implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labels_id")
    private Label label;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id")
    private Entry entry;

    public long getId() {
        return id;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
