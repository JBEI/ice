package org.jbei.ice.server;

import org.jbei.ice.lib.entry.model.Entry;

public class EntryFilterCallback extends FilterCallback {

    private final String field;

    public EntryFilterCallback(String field) {
        this.field = field;
    }

    @Override
    public String getField() {
        return this.field;
    }

    @Override
    public String getSelection() {
        return "entry.id";
    }

    @Override
    public String getFrom() {
        return Entry.class.getName() + " entry";
    }
}
