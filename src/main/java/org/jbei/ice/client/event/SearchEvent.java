package org.jbei.ice.client.event;

import java.util.ArrayList;

import org.jbei.ice.client.common.FilterOperand;

import com.google.gwt.event.shared.GwtEvent;

public class SearchEvent extends GwtEvent<SearchEventHandler> {

    public static Type<SearchEventHandler> TYPE = new Type<SearchEventHandler>();
    private FilterOperand operand;
    private ArrayList<FilterOperand> operands; // TODO : this should probably replace the above
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

    public FilterOperand getOperand() {
        return operand;
    }

    public void setOperand(FilterOperand operand) {
        this.operand = operand;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean isAdd) {
        this.isAdd = isAdd;
    }

    public ArrayList<FilterOperand> getOperands() {
        return operands;
    }

    public void setOperands(ArrayList<FilterOperand> operands) {
        this.operands = operands;
    }

    public ArrayList<Long> getResults() {
        return results;
    }

    public void setResults(ArrayList<Long> results) {
        this.results = results;
    }
}