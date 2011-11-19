package org.jbei.ice.client.entry.add.field;

public class EntryTypeField {

    private String name;
    private boolean required;

    public EntryTypeField(EntryTypeField field, String name, Boolean required) // TODO : function param at the end
    {
        this.name = name;
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public boolean getRequired() {
        return this.required;
    }
}
