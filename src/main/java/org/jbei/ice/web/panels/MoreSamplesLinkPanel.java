package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.pages.EntryViewPage;

public class MoreSamplesLinkPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static String SAMPLES_URL_KEY = "samples";

    public MoreSamplesLinkPanel(String id, Entry entry) {
        super(id);
        BookmarkablePageLink<Object> moreLink = new BookmarkablePageLink<Object>("moreLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                        + SAMPLES_URL_KEY));
        add(moreLink);
    }

}
