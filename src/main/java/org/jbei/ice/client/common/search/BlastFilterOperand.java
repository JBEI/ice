package org.jbei.ice.client.common.search;

import java.util.ArrayList;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

public class BlastFilterOperand extends FilterOperand {
    private final FlexTable layout;
    private final TextArea area;
    private final ListBox list;
    private final ArrayList<QueryOperator> operators;

    public BlastFilterOperand() {
        super(SearchFilterType.BLAST);
        layout = new FlexTable();
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
        list.addItem(QueryOperator.BLAST_N.operator(), QueryOperator.BLAST_N.value());

        this.operators.add(QueryOperator.TBLAST_X);
        list.addItem(QueryOperator.TBLAST_X.operator(), QueryOperator.TBLAST_X.value());

        layout.setWidget(1, 0, list);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        int indx = list.getSelectedIndex();
        return this.operators.get(indx);
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        return operators;
    }

    @Override
    public String getOperand() {
        return area.getText();
    }
}
