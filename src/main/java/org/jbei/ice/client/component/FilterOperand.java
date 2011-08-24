package org.jbei.ice.client.component;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Composite of components for each search filter.
 * Represents the sub-expressions for the query filter.
 * 
 * @author Hector Plahar
 */
public abstract class FilterOperand extends Composite {

    protected final SearchFilterType type;
    protected final HorizontalPanel hPanel;

    public FilterOperand(SearchFilterType type) {
        super();

        this.type = type;
        this.hPanel = new HorizontalPanel();
        initWidget(hPanel);
    }

    public SearchFilterType getType() {
        return this.type;
    }

    public abstract QueryOperator getOperator();

    public abstract String getOperand();

    public abstract void addWidgets(HorizontalPanel panel);
}