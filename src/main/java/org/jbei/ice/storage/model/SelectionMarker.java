package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * Represents a selection marker for entry.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "selection_markers")
@SequenceGenerator(name = "selection_markers_id", sequenceName = "selection_markers_id_seq", allocationSize = 1)
public class SelectionMarker implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "selection_markers_id")
    private long id;

    @Column(name = "name", length = 50, nullable = false)
    @GenericField
    private String name;

    //    @ContainedIn
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private org.jbei.ice.storage.model.Entry entry;

    public SelectionMarker() {
    }

    public SelectionMarker(String name, org.jbei.ice.storage.model.Entry entry) {
        this.name = name;
        this.entry = entry;
    }

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

    public org.jbei.ice.storage.model.Entry getEntry() {
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
