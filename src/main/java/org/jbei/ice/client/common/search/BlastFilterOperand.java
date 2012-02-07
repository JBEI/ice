package org.jbei.ice.client.common.search;

import java.util.ArrayList;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

// TODO : this combines model and view, separate
public class BlastFilterOperand extends FilterOperand {

    private final FlexTable layout;
    private final TextArea area;
    private final ListBox list;

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

        for (BlastProgram program : BlastProgram.values()) {
            list.addItem(program.getName(), program.name());
        }

        layout.setWidget(1, 0, list);
    }

    @Override
    public QueryOperator getSelectedOperator() {
        return QueryOperator.CONTAINS;
    }

    @Override
    public ArrayList<QueryOperator> getOperatorList() {
        return null;
    }

    @Override
    public String getOperand() {
        return area.getText();
    }
}
