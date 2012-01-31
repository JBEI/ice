package org.jbei.ice.client.common.search;

import java.util.ArrayList;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class SearchFilterOperand extends FilterOperand {

    private final ArrayList<QueryOperator> operators;
    private final ListBox list;
    private final TextBox box;

    public SearchFilterOperand(SearchFilterType type, QueryOperator... operators) {
        super(type);

        list = new ListBox();
        this.operators = new ArrayList<QueryOperator>();
        for (QueryOperator operator : operators) {
            this.operators.add(operator);
            list.addItem(operator.operator(), operator.value());
        }

        box = new TextBox();
        this.addWidgets(hPanel);
    }

    @Override
    public void addWidgets(HorizontalPanel panel) {
        panel.add(list);
        panel.add(box);
    }

    public String getOperandValues() {

        int selectedIdx = list.getSelectedIndex();
        String selectedValue = list.getValue(selectedIdx);

        return (selectedValue + box.getText());
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int indx = this.list.getSelectedIndex();
        return this.operators.get(indx);
    }

    @Override
    public String getOperand() {
        return this.box.getText();
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        return operators;
    }
}
