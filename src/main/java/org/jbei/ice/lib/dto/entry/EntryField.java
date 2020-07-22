package org.jbei.ice.lib.dto.entry;

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
    private EntryType entryType;
    private String label;
    private boolean required;
    private boolean custom;
    private String value;

    private final List<CustomField> options = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
