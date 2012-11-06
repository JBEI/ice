package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.client.model.OperandValue;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Filter widget for displaying enum operand type options for filtering (e.g. Status is [Complete])
 * Valid operators are "is" and "is not"
 *
 * @author Hector Plahar
 */
public class EnumFilterWidget extends FilterWidget {

    private final ListBox operators;
    private final ListBox operands;
    private final FlexTable layout;

    public EnumFilterWidget(SearchFilterType filterType, List<OperandValue> operands) {

        super(filterType);

        layout = new FlexTable();
        layout.setCellPadding(0);

        initWidget(layout);

        ArrayList<QueryOperator> operatorsList = new ArrayList<QueryOperator>();

        // entry types
        this.operators = new ListBox();
        this.operators.setStyleName("pull_down");
        for (QueryOperator operator : getOperatorList()) {
            this.operators.addItem(operator.operator(), operator.name());
            operatorsList.add(operator);
        }

        this.operands = new ListBox();
        this.operands.setStyleName("pull_down");
        HashSet<String> operandList = new HashSet<String>();
        for (OperandValue value : operands) {
            this.operands.addItem(value.getDisplay(), value.getValue());
            operandList.add(value.getValue());
        }

        // layout options
        layout.setWidget(0, 0, this.operators);
        layout.setWidget(0, 1, this.operands);
    }

    @Override
    public void setWidth(String width) {
        layout.setWidth(width);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int index = this.operators.getSelectedIndex();
        String value = this.operators.getValue(index);
        return QueryOperator.valueOf(value);
    }

    @Override
    public List<QueryOperator> getOperatorList() {
        return Arrays.asList(QueryOperator.IS, QueryOperator.IS_NOT);
    }

    @Override
    // TODO : this is the type<E>
    public String getSelectedOperand() {
        int index = this.operands.getSelectedIndex();
        return this.operands.getValue(index);
    }
}
