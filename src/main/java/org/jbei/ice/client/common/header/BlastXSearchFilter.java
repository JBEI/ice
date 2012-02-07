package org.jbei.ice.client.common.header;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

public class BlastXSearchFilter extends Composite {

    private final FlexTable table;

    public BlastXSearchFilter(String msg) {
        table = new FlexTable();
        table.setCellPadding(2);
        table.setCellSpacing(0);
        table.setStyleName("search_filter_widget");

        initWidget(table);

        table.setHTML(0, 0, "<span class=\"tooltip\">blastx<span>" + msg + "</span></span>");
        table.setHTML(0, 1, "<span style=\"\">x</span>");
    }
}
