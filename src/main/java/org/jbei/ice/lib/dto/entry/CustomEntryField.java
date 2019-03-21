package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryField implements IDataTransferModel {

    private long id;
    private String label;
    private FieldType fieldType;
    private EntryType entryType;
    private boolean required;
    private List<CustomField> options = new ArrayList<>();
    private String value;

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
}
