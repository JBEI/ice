package org.jbei.ice.client.common;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HorizontalPanel;
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

        this.addWidgets(hPanel);

    }

    @Override
    public QueryOperator getOperator() {
        return operator;
    }

    @Override
    public String getOperand() {
        return Boolean.toString(yesRadio.getValue());
    }

    @Override
    public void addWidgets(HorizontalPanel panel) {
        panel.add(yesRadio);
        panel.add(noRadio);
    }
}
