package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.entry.CustomEntryField;
import org.jbei.ice.lib.dto.entry.EntryFieldLabel;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.FieldType;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_entry_field")
@SequenceGenerator(name = "custom_entry_field_id", sequenceName = "custom_entry_field_id_seq", allocationSize = 1)
public class CustomEntryFieldModel implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "custom_entry_field_id")
    private long id;

    // unique across same entry types for non-disabled fields
    @Column(name = "label")
    private String label;

    // type of field (e.g. multi choice etc)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    @Column(name = "existing_entry_field")
    @Enumerated(EnumType.STRING)
    private EntryFieldLabel existingField;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;    // type of entry this custom field is for

    @Column(name = "required")
    private Boolean required = Boolean.FALSE;

    @Column(name = "disabled")
    private Boolean disabled = Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<CustomEntryFieldOptionModel> customFieldLabels = new ArrayList<>();

    @Override
    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public EntryFieldLabel getExistingField() {
        return existingField;
    }

    public void setExistingField(EntryFieldLabel existingField) {
        this.existingField = existingField;
    }

    public List<CustomEntryFieldOptionModel> getCustomFieldLabels() {
        return customFieldLabels;
    }

    @Override
    public CustomEntryField toDataTransferObject() {
        CustomEntryField field = new CustomEntryField();
        field.setId(id);
        field.setLabel(label);
        field.setEntryType(entryType);
        field.setFieldType(fieldType);
        field.setRequired(required);
        field.setExistingField(existingField);

        for (CustomEntryFieldOptionModel model : customFieldLabels) {
            field.getOptions().add(model.toDataTransferObject());
        }

        return field;
    }
}
