package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.panels.FooterPanel;
import org.jbei.ice.web.panels.HeaderPanel;
import org.jbei.ice.web.panels.MenuPanel;
import org.jbei.ice.web.panels.SearchBarFormPanel;

public class UnprotectedPage extends WebPage {
    public static final String IMAGES_RESOURCE_LOCATION = "static/images/";
    public static final String STYLES_RESOURCE_LOCATION = "static/styles/";
    public static final String JS_RESOURCE_LOCATION = "static/scripts/";
    public static final String VE_RESOURCE_LOCATION = "static/ve/";
    public static final String VV_RESOURCE_LOCATION = "static/vv/";
    public static final String SC_RESOURCE_LOCATION = "static/sc/";

    protected static final long serialVersionUID = 1L;

    private boolean isPageRendered = false;

    /**
     * Constructor that is invoked when page is invoked without a session.
     */
    public UnprotectedPage() {
        this(new PageParameters());
    }

    public UnprotectedPage(PageParameters parameters) {
        super(parameters);
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
        add(new Label("title", new Model<String>(getTitle())));
        add(new HeaderPanel("headerPanel"));
        add(new MenuPanel("menuPanel"));
        add(new SearchBarFormPanel("searchBarPanel"));
        add(new FooterPanel("footerPanel"));
    }

    public void handleException(Throwable throwable) {
        if (throwable != null) {
            Logger.error(throwable.getMessage(), throwable);
        }
    }

    @Override
    protected void onBeforeRender() {
        if (!isPageRendered) {
            initializeStyles();

            initializeJavascript();

            initializeComponents();

            isPageRendered = true;
        }

        super.onBeforeRender();
    }

    protected String getTitle() {
        return JbeirSettings.getSetting("PROJECT_NAME");
    }
}
