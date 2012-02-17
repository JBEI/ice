package org.jbei.ice.client.common.header;

import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Options widget for search
 * 
 * @author Hector Plahar
 */
public class SearchOption extends Composite {

    private final ListBox options; // search options

    private FilterOperand operand;
    private final Button addFilter;
    private final FlexTable panel;

    public SearchOption() {
        panel = new FlexTable();
        panel.setCellPadding(5);
        panel.setCellSpacing(0);
        initWidget(panel);

        addFilter = new Button("Add Filter");
        options = new ListBox();
        options.setWidth("150px");
        options.setStyleName("pull_down");

        panel.setHTML(0, 0,
            "<span class=\"font-85em font-bold\" style=\"color: #999\">ADVANCED SEARCH</span>");
        panel.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        panel.getFlexCellFormatter().setHeight(0, 0, "20px");

        panel.setWidget(0, 1, options);
        panel.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);
    }

    public void setOptions(HashMap<String, String> listOptions) {
        if (listOptions == null)
            return;

        for (Map.Entry<String, String> entry : listOptions.entrySet()) {
            this.options.addItem(entry.getKey(), entry.getValue());
        }
    }

    public void setFilterOperands(Widget operand) {
        panel.setWidget(1, 0, operand);
        panel.getFlexCellFormatter().setColSpan(1, 0, 2);
        panel.setWidget(2, 0, addFilter);
        panel.getFlexCellFormatter().setColSpan(2, 0, 2);
        panel.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasAlignment.ALIGN_RIGHT);
    }

    public ListBox getFilterOptions() {
        return this.options;
    }

    public HasClickHandlers getAddFilter() {
        return this.addFilter;
    }

    public SearchFilterType getSearchType() {
        return operand.getType();
    }

    public QueryOperator getOperator() {
        return operand.getSelectedOperator();
    }

    public String getOperand() {
        return operand.getOperand();
    }
}
