package org.jbei.ice.client.common;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Menu right below main header on top of all protected pages
 * Note that as of now, this composite performs no validation.
 * 
 * @author Hector Plahar
 */

public class HeaderMenu extends Composite {

    private static HeaderMenu INSTANCE;

    public static HeaderMenu getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HeaderMenu();

        return INSTANCE;
    }

    public HeaderMenu() {
        String html = "<div id=\"dark\"><div id=\"indented\"><ul><li><a href=\"#page=main\">Home</a></li><li>"
                + "<a href=\"#page=collections\">Collections</a></li><li><a href=\"#page=add\">Add New Entry</a></li><li><a href=\"#page=bulk\">"
                + "Bulk Import</a></li><li><a href=\"#page=query\">"
                + "Advanced Search</a></li><li><a href=\"#page=blast\">"
                + "Blast</a></li></ul></div></div>";
        HTMLPanel panel = new HTMLPanel(html);
        initWidget(panel);
    }
}
