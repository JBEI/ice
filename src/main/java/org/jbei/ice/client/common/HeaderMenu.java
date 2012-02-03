package org.jbei.ice.client.common;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Menu right below main header on top of all protected pages
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

        String html = "<div id=\"dark\"><div id=\"indented\">"
                + "<ul>"
                + "<li style=\"width: 7%\" class=\"font-90em\"><a href=\"#page=main\">Home</a></li>"
                + "<li style=\"width: 10%\" class=\"font-90em\"><a href=\"#page=news\">News</a></li>"
                + "<li style=\"width: 12%\" class=\"font-90em\"><a href=\"#page=collections\">Collections</a></li>"
                + "<li style=\"width: 12%\" class=\"font-90em\"><a href=\"#page=bulk\">Bulk Import</a></li>"
                + "<li style=\"height: 30px; text-align: right \">&nbsp;</li>"
                + "</ul></div></div>";
        HTMLPanel panel = new HTMLPanel(html);
        initWidget(panel);
    }
}
