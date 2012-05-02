package org.jbei.ice.client.common.search;

import java.util.ArrayList;
import java.util.HashSet;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class SearchFilterOperand extends FilterOperand {

    private final ArrayList<QueryOperator> operators;
    private final ListBox list;
    private final TextBox box;
    private final HTMLPanel layout;

    public SearchFilterOperand(SearchFilterType type, QueryOperator... operators) {
        super(type);

        list = new ListBox();
        this.operators = new ArrayList<QueryOperator>();
        for (QueryOperator operator : operators) {
            this.operators.add(operator);
            list.addItem(operator.operator(), operator.value());
        }

        box = new TextBox();
        String listId = "_" + type.getShortName();
        String boxId = "_" + type.getShortName();
        layout = new HTMLPanel("<span id=\"" + listId + "\"></span> <span id=\"" + boxId
                + "\"></span>");
        layout.add(list, listId);
        layout.add(box, boxId);
        initWidget(layout);
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
    public String getSelectedOperand() {
        return this.box.getText();
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        return operators;
    }

    @Override
    public HashSet<String> getPossibleOperands() {
        return null;
    }
}
