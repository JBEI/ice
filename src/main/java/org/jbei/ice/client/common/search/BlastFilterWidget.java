package org.jbei.ice.client.common.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.client.common.FilterWidget;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

public class BlastFilterWidget extends FilterWidget {
    private final TextArea area;
    private final ListBox list;
    private final ArrayList<QueryOperator> operators;

    public BlastFilterWidget() {
        super(SearchFilterType.BLAST);
        FlexTable layout = new FlexTable();
        layout.setCellPadding(0);

        initWidget(layout);

        // widgets
        area = new TextArea();
        area.setStyleName("input_box");
        area.setSize("350px", "200px");
        layout.setWidget(0, 0, area);

        list = new ListBox();
        this.operators = new ArrayList<QueryOperator>();
        this.operators.add(QueryOperator.BLAST_N);
        list.addItem(QueryOperator.BLAST_N.operator(), QueryOperator.BLAST_N.symbol());

        this.operators.add(QueryOperator.TBLAST_X);
        list.addItem(QueryOperator.TBLAST_X.operator(), QueryOperator.TBLAST_X.symbol());

        HashSet<String> operands = new HashSet<String>();
        operands.add(QueryOperator.BLAST_N.symbol());
        operands.add(QueryOperator.TBLAST_X.symbol());

        layout.setWidget(1, 0, list);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int indx = list.getSelectedIndex();
        return this.operators.get(indx);
    }

    @Override
    public String getSelectedOperand() {
        return area.getText();
    }

    public List<QueryOperator> getOperatorList() {
        return Arrays.asList(QueryOperator.BLAST_N);
    }
}
