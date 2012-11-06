package org.jbei.ice.server;

public class HasXFilterCallback extends FilterCallback {

    private final String selection;
    private final String from;

    public HasXFilterCallback(String selection, String from) {
        this.selection = selection;
        this.from = from;
    }

    @Override
    public String getField() {
        return null;
    }

    @Override
    public String getSelection() {
        return selection;
    }

    @Override
    public String getFrom() {
        return from;
    }
}
