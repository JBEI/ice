package org.jbei.ice.client.common.search;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Represents a row of search filter options.
 * Comprises of the search filter type (e.g. NAME_OR_ALIAS),
 * an operator (e.g. IS, CONTAINS) and a user entered
 * operand. With boolean operators, the operand is implicitly the
 * search filter type and is not user entered
 * 
 * @author Hector Plahar
 */

public class SearchFilter extends Composite {

    private final ListBox options;
    private final HorizontalPanel panel;
    private FilterOperand operand;

    public SearchFilter() {
        panel = new HorizontalPanel();
        initWidget(panel);

        options = new ListBox();

        for (SearchFilterType type : SearchFilterType.values())
            this.options.addItem(type.displayName(), type.name());

        panel.add(options);

        bind();
    }

    private void bind() {

        options.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                // get type from value of new selection
                int index = options.getSelectedIndex();
                String value = options.getValue(index);
                SearchFilterType type = SearchFilterType.valueOf(value);
                if (type == null)
                    return;

                if (panel.getWidgetCount() > 1) {
                    panel.remove(1);
                }

                // set the filter widgets 
                operand = type.getOperatorAndOperands();
                if (operand != null)
                    panel.add(operand);
            }
        });
    }

    public SearchFilterType getSearchType() {
        return operand.getType();
    }

    public QueryOperator getOperator() {
        return operand.getOperator();
    }

    public String getOperand() {
        return operand.getOperand();
    }
}
