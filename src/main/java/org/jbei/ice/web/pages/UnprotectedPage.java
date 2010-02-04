package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.panels.FooterPanel;
import org.jbei.ice.web.panels.HeaderPanel;
import org.jbei.ice.web.panels.MenuPanel;
import org.jbei.ice.web.panels.SearchBarFormPanel;

public class UnprotectedPage extends WebPage {
    public static final String IMAGES_RESOURCE_LOCATION = "static/images/";
    public static final String STYLES_RESOURCE_LOCATION = "static/styles/";
    public static final String JS_RESOURCE_LOCATION = "static/scripts/";

    protected static final long serialVersionUID = 1L;

    private String searchParameters = "";

    /**
     * Constructor that is invoked when page is invoked without a session.
     */
    public UnprotectedPage() {
        this(new PageParameters());
    }

    public UnprotectedPage(final PageParameters parameters) {
        super(parameters);

        initializeStyles();

        initializeJavascript();

        initializeComponents();

        searchParameters = parameters.getString("search");
    }

    protected void initializeStyles() {
        add(CSSPackageResource.getHeaderContribution(new CompressedResourceReference(
                UnprotectedPage.class, STYLES_RESOURCE_LOCATION + "main.css")));
    }

    protected void initializeJavascript() {
        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                JS_RESOURCE_LOCATION + "jquery-1.3.2.js"));
    }

    protected void initializeComponents() {
        add(new Label("title", "JBEI Registry"));
        add(new HeaderPanel("headerPanel"));
        add(new MenuPanel("menuPanel"));
        add(new SearchBarFormPanel("searchBarPanel", searchParameters));
        add(new FooterPanel("footerPanel"));
    }

    public void handleException(Throwable throwable) {
        String body = Utils.stackTraceToString(throwable);
        String subject = (throwable.getMessage().length() > 50) ? (throwable.getMessage()
                .substring(0, 50) + "...") : throwable.getMessage();

        Emailer.error(JbeirSettings.getSetting("ERROR_EMAIL_EXCEPTION_PREFIX") + subject, body);

        Logger.error(throwable.getMessage());
        Logger.error(body);
    }
}
