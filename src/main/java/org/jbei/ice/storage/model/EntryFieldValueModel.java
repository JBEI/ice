package org.jbei.ice.storage.model;

import jakarta.persistence.*;
import org.jbei.ice.dto.entry.EntryField;
import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.storage.DataModel;

/**
 * Model for storing user entered values for built-in entry fields (e.g. name, alias)
 * Note that this is different from custom entries values @see{{@link CustomEntryFieldValueModel}}
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "entry_field_value")
@SequenceGenerator(name = "entry_field_value_id", sequenceName = "entry_field_value_id_seq", allocationSize = 1)
public class EntryFieldValueModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "entry_field_value_id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @Column(name = "field_label")
    @Enumerated(EnumType.STRING)
    private EntryFieldLabel label;

    @Column(name = "\"value\"", nullable = false, length = 511)
    private String value;

    @Override
    public long getId() {
        return id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public EntryFieldLabel getLabel() {
        return label;
    }

    public void setLabel(EntryFieldLabel label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public EntryField toDataTransferObject() {
        EntryField entryField = new EntryField();
        entryField.setId(this.id);
        entryField.setValue(this.value);
        return entryField;
    }
}
