package org.jbei.ice.web.panels;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.pages.FeedbackPage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class FooterPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public FooterPanel(String id) {
        super(id);

        ResourceReference sponsorImage1 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "doe-bioenergy-research-cent.gif");
        ResourceReference sponsorImage2 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "lbnl-logo.gif");
        ResourceReference sponsorImage3 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sandia-lab-logo.gif");
        ResourceReference sponsorImage4 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "ucb-logo.gif");
        ResourceReference sponsorImage5 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "ucdavis-logo.gif");
        ResourceReference sponsorImage6 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "carnegie-insitution-logo.gif");
        ResourceReference sponsorImage7 = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "llnl-logo.gif");

        add(new Image("sponsorImage1", sponsorImage1));
        add(new Image("sponsorImage2", sponsorImage2));
        add(new Image("sponsorImage3", sponsorImage3));
        add(new Image("sponsorImage4", sponsorImage4));
        add(new Image("sponsorImage5", sponsorImage5));
        add(new Image("sponsorImage6", sponsorImage6));
        add(new Image("sponsorImage7", sponsorImage7));

        add(new BookmarkablePageLink<FeedbackPage>("feedbackLink", FeedbackPage.class));
    }
}
