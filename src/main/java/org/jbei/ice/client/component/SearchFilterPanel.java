package org.jbei.ice.client.component;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.shared.FilterTrans;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * Panel for displaying the list of search filters
 * When initially loaded, there is a single filter row that is shown.
 * Adding and subtracting filters is user controlled with "+" and
 * "-" buttons
 * 
 * @author Hector Plahar
 */
public class SearchFilterPanel extends FlexTable {

    private final Button evaluateButton;
    private final LinkedList<SearchFilter> filters;

    public SearchFilterPanel() {

        filters = new LinkedList<SearchFilter>();

        // add initial row
        addRow();

        // add the row with evaluate button
        evaluateButton = new Button("Evaluate");
        this.setWidget(1, 0, evaluateButton);
        this.getFlexCellFormatter().setColSpan(1, 0, 4);
        this.getFlexCellFormatter()
                .setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    /**
     * Adds a row containing the drop down filter to the
     * table. There is a minimum of two rows in the table
     * Rows are added before the row containing the evaluate button
     */
    private void addRow() {

        int row;
        if (this.getRowCount() == 0)
            row = 0;
        else
            row = this.insertRow(this.getRowCount() - 1);

        SearchFilter filter = new SearchFilter();
        Button addFilterButton = new Button(" + ");
        Button removeFilterButton = new Button(" - ");
        this.setWidget(row, 0, filter);
        this.setWidget(row, 2, addFilterButton);
        this.setWidget(row, 3, removeFilterButton);

        bind(filter, addFilterButton, removeFilterButton);
    }

    private void bind(final SearchFilter filter, Button addButton, Button removeButton) {

        this.filters.add(filter);

        //        filter.addValueChangeHandler(new ValueChangeHandler<String>() {
        //
        //            @Override
        //            public void onValueChange(ValueChangeEvent<String> event) {
        //
        //                String value = event.getValue();
        //                Window.alert("Filter changed. new value is " + value);
        //                // TODO : each represents one pull down. need to find a way to keep track of all of them
        //            }
        //        });

        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                // TODO : get the row the click occurred in and 
                // append row there
                addRow();
            }
        });

        removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                HTMLTable.Cell cell = getCellForEvent(event);

                if (cell != null && cell.getRowIndex() > 0) {
                    removeRow(cell.getRowIndex());
                    filters.remove(cell.getRowIndex());
                }
            }
        });
    }

    public Button getEvaluateButton() {
        return this.evaluateButton;
    }

    public ArrayList<FilterTrans> getFilters() {
        ArrayList<FilterTrans> filterTrans = new ArrayList<FilterTrans>();

        for (SearchFilter filter : filters) {

            String type = filter.getSearchType().name();
            String operator = filter.getOperator().name();
            String operand = filter.getOperand();

            FilterTrans trans = new FilterTrans(type, operator, operand);
            filterTrans.add(trans);
        }

        return filterTrans;
    }
}
