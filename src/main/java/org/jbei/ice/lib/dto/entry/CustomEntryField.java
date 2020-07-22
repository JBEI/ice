package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryField implements IDataTransferModel {

    private long id;
    private String label;
    private FieldType fieldType;
    private EntryType entryType;
    private EntryFieldLabel existingField;
    private boolean required;
    private final List<CustomField> options = new ArrayList<>();
    private String value;
    private boolean disabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<CustomField> getOptions() {
        return options;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public EntryFieldLabel getExistingField() {
        return existingField;
    }

    public void setExistingField(EntryFieldLabel existingField) {
        this.existingField = existingField;
    }
}
