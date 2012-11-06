package org.jbei.ice.client.common;

import java.util.List;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.Composite;

/**
 * Composite of components for each search filter.
 * Represents the sub-expressions for the query filter.
 *
 * @author Hector Plahar
 */
public abstract class FilterWidget extends Composite {

    protected final SearchFilterType type;

    public FilterWidget(SearchFilterType type) {
        super();
        this.type = type;
    }

    public SearchFilterType getType() {
        return this.type;
    }

    public abstract QueryOperator getSelectedOperator();

    public abstract List<QueryOperator> getOperatorList();

    public abstract String getSelectedOperand();

    public void setInputPlaceholder(String value) {
    }
}