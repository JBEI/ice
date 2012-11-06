package org.jbei.ice.server;

public class QueryFilterParams {

    private final String selection;
    private final String criterion;
    private final String from;

    public QueryFilterParams(String selection, String from, String criterion) {

        this.selection = selection;
        this.from = from;
        this.criterion = criterion;
    }

    public String getCriterion() {
        return this.criterion;
    }

    public String getSelection() {
        return this.selection;
    }

    public String getFrom() {
        return this.from;
    }
}
