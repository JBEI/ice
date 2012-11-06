package org.jbei.ice.client.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;

public class BooleanFilterWidget extends FilterWidget {

    private final RadioButton yesRadio;

    public BooleanFilterWidget(SearchFilterType type) {
        super(type);

        yesRadio = new RadioButton("Boolean_Filter", QueryOperator.BOOLEAN_YES.symbol());
        RadioButton noRadio = new RadioButton("Boolean_Filter", QueryOperator.BOOLEAN_NO.symbol());
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(yesRadio);
        panel.add(noRadio);
        initWidget(panel);

        HashSet<String> operands = new HashSet<String>();
        operands.add(yesRadio.getValue().toString());
        operands.add(noRadio.getValue().toString());
    }

    @Override
    public QueryOperator getSelectedOperator() {
        if (yesRadio.getValue())
            return QueryOperator.BOOLEAN_YES;
        return QueryOperator.BOOLEAN_NO;
    }

    @Override
    public List<QueryOperator> getOperatorList() {
        return Arrays.asList(QueryOperator.BOOLEAN_NO, QueryOperator.BOOLEAN_YES);
    }

    @Override
    public String getSelectedOperand() {
        return Boolean.toString(yesRadio.getValue());
    }
}
