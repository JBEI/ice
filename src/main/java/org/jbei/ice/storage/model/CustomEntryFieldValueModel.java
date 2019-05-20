package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.entry.CustomEntryField;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;

@Entity
@Table(name = "custom_entry_field_value_model")
@SequenceGenerator(name = "custom_entry_field_value_model_id", sequenceName = "custom_entry_field_value_model_id_seq", allocationSize = 1)
public class CustomEntryFieldValueModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_entry_field_value_model_id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entries_id", nullable = false)
    private Entry entry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "custom_entry_field_id", nullable = false)
    private CustomEntryFieldModel field;

    @Column(name = "value")
    private String value;

    @Override
    public long getId() {
        return id;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Entry getEntry() {
        return this.entry;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public CustomEntryFieldModel getField() {
        return field;
    }

    public void setField(CustomEntryFieldModel field) {
        this.field = field;
    }

    @Override
    public CustomEntryField toDataTransferObject() {
        CustomEntryField field = new CustomEntryField();
        field.setFieldType(this.field.getFieldType());
        field.setId(this.id);
        field.setLabel(this.field.getLabel());
        field.setValue(this.value);
        return field;
    }
}
