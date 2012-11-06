package org.jbei.ice.client.event;

import java.util.ArrayList;

import org.jbei.ice.shared.dto.SearchFilterInfo;

import com.google.gwt.event.shared.GwtEvent;

public class SearchEvent extends GwtEvent<SearchEventHandler> {

    public static Type<SearchEventHandler> TYPE = new Type<SearchEventHandler>();
    private ArrayList<SearchFilterInfo> filters;
    private boolean isAdd;
    private ArrayList<Long> results;

    @Override
    public Type<SearchEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SearchEventHandler handler) {
        handler.onSearch(this);
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean isAdd) {
        this.isAdd = isAdd;
    }

    public ArrayList<SearchFilterInfo> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<SearchFilterInfo> operands) {
        this.filters = operands;
    }

    public ArrayList<Long> getResults() {
        return results;
    }

    public void setResults(ArrayList<Long> results) {
        this.results = results;
    }
}