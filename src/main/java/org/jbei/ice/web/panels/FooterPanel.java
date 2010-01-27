package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.WelcomePage;

public class FooterPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public FooterPanel(String id) {
        super(id);

        add(new BookmarkablePageLink<WelcomePage>("homeLink", WelcomePage.class));
        add(new BookmarkablePageLink<FeedbackPage>("feedbackLink", FeedbackPage.class));
    }
}
