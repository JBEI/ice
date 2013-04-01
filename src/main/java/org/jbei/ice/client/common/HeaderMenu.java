package org.jbei.ice.client.common;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Page;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Menu right below main header on top of all protected pages
 *
 * @author Hector Plahar
 */

public class HeaderMenu extends Composite {

    private final Anchor newsLink;
    private final Anchor bulkImportLink;
    private final Anchor collectionsLink;
    private final Anchor adminLink;

    public HeaderMenu() {
        boolean isAdmin = ClientController.account != null && ClientController.account.isAdmin();
        newsLink = new Anchor("News", "#" + Page.NEWS.getLink());
        bulkImportLink = new Anchor("Bulk Import", "#" + Page.BULK_IMPORT.getLink());
        collectionsLink = new Anchor("Collections", "#" + Page.COLLECTIONS.getLink());
        adminLink = new Anchor("Admin", "#" + Page.ADMIN.getLink());

        String html = "<ul id=\"indented\">"
                + "<li><span id=\"collections_link\"></span></li>"
                + "<li><span id=\"news_link\"></span></li>"
                + "<li><span id=\"bi_link\"></span></li>";

        if (isAdmin) {
            html += ("<li><span id=\"admin_link\"></span></li>");
        }

        html += ("</ul>");
        HTMLPanel panel = new HTMLPanel(html);
        panel.add(collectionsLink, "collections_link");
        panel.add(newsLink, "news_link");
        if (isAdmin)
            panel.add(adminLink, "admin_link");
        panel.add(bulkImportLink, "bi_link");
        initWidget(panel);
    }

    public void setSelected(Page page) {
        switch (page) {
            case COLLECTIONS:
            case MAIN:
                collectionsLink.setStyleName("selected");
                newsLink.removeStyleName("selected");
                adminLink.removeStyleName("selected");
                bulkImportLink.removeStyleName("selected");
                break;

            case BULK_IMPORT:
                bulkImportLink.setStyleName("selected");
                collectionsLink.removeStyleName("selected");
                adminLink.removeStyleName("selected");
                newsLink.removeStyleName("selected");
                break;

            case ADMIN:
                if (!ClientController.account.isAdmin())
                    break;
                adminLink.setStyleName("selected");
                collectionsLink.removeStyleName("selected");
                newsLink.removeStyleName("selected");
                bulkImportLink.removeStyleName("selected");
                break;

            case NEWS:
                newsLink.setStyleName("selected");
                collectionsLink.removeStyleName("selected");
                adminLink.removeStyleName("selected");
                bulkImportLink.removeStyleName("selected");
                break;

            default:
                newsLink.removeStyleName("selected");
                collectionsLink.removeStyleName("selected");
                adminLink.removeStyleName("selected");
                bulkImportLink.removeStyleName("selected");
                break;
        }
    }
}
