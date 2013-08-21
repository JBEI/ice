package org.jbei.ice.client.entry.display.view;

import org.jbei.ice.client.common.widget.FAIconType;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget to visually indicate that an entry is being loaded.
 * It is intended to be used in the detailed entry view
 *
 * @author Hector Plahar
 */
public class EntryLoadingWidget extends Composite {
    private final FlexTable panel;

    public EntryLoadingWidget() {
        panel = new FlexTable();
        panel.setWidth("100%");
        initWidget(panel);
    }

    public void showLoad() {
        Widget widget = this.getParent();
        if (widget != null)
            panel.setHeight(widget.getOffsetHeight() + "px");
        else
            panel.setHeight("450px");
        panel.setStyleName("entry_loading_indicator");
        panel.setHTML(0, 0, "<i class=\"icon-spinner icon-spin icon-3x\"></i><br><h1>LOADING CONTENT</h1>");
        panel.getFlexCellFormatter().setAlignment(0, 0, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);
    }

    public void showErrorLoad() {
        panel.setHeight("300px");
        panel.setStyleName("alert_indicator");
        panel.setHTML(0, 0, "<span style=\"line-height: 1px\">"
                + "<i class=\"" + FAIconType.WARNING_SIGN.getStyleName()
                + "\" style=\"font-size: 9em; color: darkred\"></i>"
                + "<br><h2>COULD NOT LOAD CONTENT</h2>"
                + "<h5>THIS IS LIKELY DUE TO INSUFFICIENT ACCESS PRIVILEGES OR THE PART DOES NOT EXIST</h5></span>");
        panel.getFlexCellFormatter().setAlignment(0, 0, HasAlignment.ALIGN_CENTER, HasAlignment.ALIGN_MIDDLE);
    }
}
