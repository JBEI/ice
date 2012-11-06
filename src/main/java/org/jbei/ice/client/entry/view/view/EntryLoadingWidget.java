package org.jbei.ice.client.entry.view.view;

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

    public EntryLoadingWidget() {
        //        String html = "<div>"
        //                + "<div style=\"position:absolute; z-index:2147483647; text-align: center; width:600px;
        // color:#FFF;\">"
        //                + "<div style=\"font-size:30px; font-weight:bold;\">LOADING...</div>"
        //                + "<div style=\"position:absolute;z-index:2147483646; width: 100%; height: 100%;
        // filter:alpha(opacity=60);opacity:0.6; background-color:#FFF;\"></div>"
        //                + "</div>";
        //        HTMLPanel panel = new HTMLPanel(html);
        FlexTable panel = new FlexTable();
        panel.setStyleName("entry_loading_indicator");
        panel.setWidth("100%");
        Widget widget = this.getParent();
        if (widget != null)
            panel.setHeight(widget.getOffsetHeight() + "px");
        else
            panel.setHeight("450px");

        panel.setHTML(0, 0, "<span>LOADING...</span>");
        panel.getFlexCellFormatter().setAlignment(0, 0, HasAlignment.ALIGN_CENTER,
                                                  HasAlignment.ALIGN_MIDDLE);
        //        panel.setHeight("100%");
        initWidget(panel);
    }

}
