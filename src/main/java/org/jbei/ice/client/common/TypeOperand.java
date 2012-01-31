package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.model.OperandValue;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class TypeOperand extends FilterOperand {

    private final ListBox operators;
    private final ListBox operands;
    private final ArrayList<QueryOperator> operatorsList;

    public TypeOperand(SearchFilterType filterType, List<OperandValue> operands,
            QueryOperator... operators) {

        super(filterType);

        operatorsList = new ArrayList<QueryOperator>();

        // entry types
        this.operators = new ListBox();
        for (QueryOperator operator : operators) {
            this.operators.addItem(operator.operator(), operator.name());
            operatorsList.add(operator);
        }

        this.operands = new ListBox();
        for (OperandValue value : operands)
            this.operands.addItem(value.getDisplay(), value.getValue());

        this.addWidgets(hPanel);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int index = this.operators.getSelectedIndex();
        String value = this.operators.getValue(index);
        return QueryOperator.valueOf(value);
    }

    @Override
    // TODO : this is the type<E>
    public String getOperand() {
        int index = this.operands.getSelectedIndex();
        return this.operands.getValue(index);
    }

    @Override
    public void addWidgets(HorizontalPanel panel) {

        panel.add(operators);
        panel.add(operands);
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        return operatorsList;
    }
}
