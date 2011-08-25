package org.jbei.ice.server;

public class NameFilterCallback extends FilterCallback {

    public NameFilterCallback() {
        super();
    }

    @Override
    public String getField() {
        return "lower(name.name)";
    }

    @Override
    public String getSelection() {
        return "name.entry.id";
    }

    @Override
    public String getFrom() {
        return "Name name";
    }
}
