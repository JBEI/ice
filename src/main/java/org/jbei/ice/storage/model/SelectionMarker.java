package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Represents a selection marker for entry.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "selection_markers")
@SequenceGenerator(name = "sequence", sequenceName = "selection_markers_id_seq", allocationSize = 1)
public class SelectionMarker implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 50, nullable = false)
    @Field
    private String name;

    @ContainedIn
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

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
