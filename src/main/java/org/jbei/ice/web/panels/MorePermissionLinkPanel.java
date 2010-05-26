package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.pages.EntryViewPage;

public class MorePermissionLinkPanel extends Panel {
    private static String PERMISSION_URL_KEY = "permission";

    public MorePermissionLinkPanel(String id, Entry entry) {
        super(id);
        BookmarkablePageLink<Object> moreLink = new BookmarkablePageLink<Object>("moreLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                        + PERMISSION_URL_KEY));
        add(moreLink);
    }

    private static final long serialVersionUID = 1L;

}
