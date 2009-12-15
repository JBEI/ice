package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.web.panels.LoginStatusPanel;
import org.jbei.ice.web.panels.MenuPanel;
import org.jbei.ice.web.panels.SearchBarFormPanel;

/**
 * Homepage
 */
public class UnprotectedPage extends WebPage {
	

	protected static final long serialVersionUID = 1L;

	// TODO Add any page properties or variables here

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    
	public UnprotectedPage(final PageParameters parameters) {
    	//TODO: move css to someplace logical
    	add(new StyleSheetReference("stylesheet", UnprotectedPage.class, "main.css"));

        // TODO Add your page's components here
        
        add(new Label("title", "Home - JBEI Registry"));
        add(new LoginStatusPanel("loginPanel"));
        add(new MenuPanel("menuPanel"));
        add(new SearchBarFormPanel("searchBarPanel"));
        
    }
}
