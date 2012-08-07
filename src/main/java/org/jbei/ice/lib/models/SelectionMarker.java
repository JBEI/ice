package org.jbei.ice.lib.models;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;

/**
 * Represents a selection marker for entry.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "selection_markers")
@SequenceGenerator(name = "sequence", sequenceName = "selection_markers_id_seq", allocationSize = 1)
public class SelectionMarker implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    public SelectionMarker() {
    }

    public SelectionMarker(String name, Entry entry) {
        this.name = name;
        this.entry = entry;
    }

    @XmlTransient
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

    @XmlTransient
    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
