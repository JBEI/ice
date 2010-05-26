package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.FooterPanel;
import org.jbei.ice.web.panels.LoginPanel;

public class WelcomePage extends WebPage {
    public static final String STYLES_RESOURCE_LOCATION = "static/styles/";

    public WelcomePage(PageParameters parameters) {
        super(parameters);

        if (IceSession.get().isAuthenticated()) {
            throw new RestartResponseAtInterceptPageException(UserPage.class);
        }
        add(new BookmarkablePageLink<String>("homeLink", WelcomePage.class).add(new Image(
                "logoImage", new ResourceReference(UnprotectedPage.class,
                        UnprotectedPage.IMAGES_RESOURCE_LOCATION + "logo.gif"))));
        add(CSSPackageResource.getHeaderContribution(new CompressedResourceReference(
                UnprotectedPage.class, STYLES_RESOURCE_LOCATION + "main.css")));
        add(new LoginPanel("loginPanel"));
        add(new FooterPanel("footerPanel"));
    }
}
