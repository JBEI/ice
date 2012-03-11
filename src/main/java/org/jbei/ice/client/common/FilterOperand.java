package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.HashSet;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.Composite;

/**
 * Composite of components for each search filter.
 * Represents the sub-expressions for the query filter.
 * 
 * @author Hector Plahar
 */
public abstract class FilterOperand extends Composite {

    protected final SearchFilterType type;

    public FilterOperand(SearchFilterType type) {
        super();

        this.type = type;
    }

    public SearchFilterType getType() {
        return this.type;
    }

    public abstract QueryOperator getSelectedOperator();

    public abstract ArrayList<QueryOperator> getOperatorList();

    public abstract String getSelectedOperand();

    /**
     * 
     * @return list of possible operands that are displayed to the user.
     *         e.g. for status, the list will contain (currently) In Progress, Completed
     *         and Planned
     */
    public abstract HashSet<String> getPossibleOperands();
}