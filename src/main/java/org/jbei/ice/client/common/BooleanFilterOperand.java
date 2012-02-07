package org.jbei.ice.client.common;

import java.util.ArrayList;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.RadioButton;

public class BooleanFilterOperand extends FilterOperand {

    private final QueryOperator operator;
    private final RadioButton yesRadio;
    private final RadioButton noRadio;

    public BooleanFilterOperand(SearchFilterType type) {
        super(type);

        operator = QueryOperator.BOOLEAN;
        yesRadio = new RadioButton(operator.operator(), "Yes");
        noRadio = new RadioButton(operator.operator(), "No");
        //        this.addWidgets(hPanel);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        return operator;
    }

    @Override
    public String getOperand() {
        return Boolean.toString(yesRadio.getValue());
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        ArrayList<QueryOperator> operators = new ArrayList<QueryOperator>();
        operators.add(operator);
        return operators;
    }
}
