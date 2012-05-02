package org.jbei.ice.client.common;

import java.util.ArrayList;
import java.util.HashSet;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;

public class BooleanFilterOperand extends FilterOperand {

    private final QueryOperator operator;
    private final RadioButton yesRadio;
    private final RadioButton noRadio;
    private final HorizontalPanel panel;
    private final HashSet<String> operands;

    public BooleanFilterOperand(SearchFilterType type) {
        super(type);

        operator = QueryOperator.BOOLEAN;
        yesRadio = new RadioButton(operator.operator(), "Yes");
        noRadio = new RadioButton(operator.operator(), "No");
        panel = new HorizontalPanel();
        panel.add(yesRadio);
        panel.add(noRadio);
        initWidget(panel);

        this.operands = new HashSet<String>();
        this.operands.add(yesRadio.getValue().toString());
        this.operands.add(noRadio.getValue().toString());
    }

    @Override
    public QueryOperator getSelectedOperator() {
        return operator;
    }

    @Override
    public String getSelectedOperand() {
        return Boolean.toString(yesRadio.getValue());
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        ArrayList<QueryOperator> operators = new ArrayList<QueryOperator>();
        operators.add(operator);
        return operators;
    }

    @Override
    public HashSet<String> getPossibleOperands() {
        return operands;
    }
}
