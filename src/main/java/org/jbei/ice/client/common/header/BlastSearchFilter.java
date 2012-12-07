package org.jbei.ice.client.common.header;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class BlastSearchFilter extends Composite {

    private final HTML label;

    public BlastSearchFilter(String msg, String type) {
        FlexTable table = new FlexTable();
        table.setCellPadding(2);
        table.setCellSpacing(0);
        table.setStyleName("search_filter_widget");

        initWidget(table);

        label = new HTML("x");
        label.setStyleName("quick_search_widget_close");

        table.setHTML(0, 0, "<span class=\"tooltip\">" + type + "<span>" + msg + "</span></span>");
        table.setWidget(0, 1, label);
    }

    public void setCloseHandler(ClickHandler handler) {
        label.addClickHandler(handler);
    }
}
