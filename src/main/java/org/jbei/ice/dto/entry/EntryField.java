package org.jbei.ice.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates details of the field associated with a specific type of entry
 *
 * @author Hector Plahar
 */
public class EntryField implements IDataTransferModel {

    private long id;
    private String label;   // display label
    private boolean required;
    private boolean custom;
    private String value;
    private FieldInputType fieldInputType;
    private EntryFieldLabel fieldType;
    private String field;
    private String subField;

    private final List<CustomField> options = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isCustom() {
        return this.custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public FieldInputType getFieldInputType() {
        return fieldInputType;
    }

    public void setFieldInputType(FieldInputType fieldInputType) {
        this.fieldInputType = fieldInputType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getSubField() {
        return subField;
    }

    public void setSubField(String subField) {
        this.subField = subField;
    }

    public EntryFieldLabel getFieldType() {
        return fieldType;
    }

    public void setFieldType(EntryFieldLabel fieldType) {
        this.fieldType = fieldType;
    }
}
