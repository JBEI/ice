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

@Entity
@Table(name = "entry_labels")
@SequenceGenerator(name = "sequence", sequenceName = "entry_labels_id_seq", allocationSize = 1)
public class EntryLabel implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "labels_id")
    private Label label;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id")
    private Entry entry;

    public int getId() {
        return id;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

}
