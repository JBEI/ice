package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.web.panels.LoginStatusPanel;
import org.jbei.ice.web.panels.MenuPanel;

/**
 * Homepage
 */
public class HomePage extends WebPage {
	

	protected static final long serialVersionUID = 1L;

	private int counter = 0;
	private Label label;
	// TODO Add any page properties or variables here

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public HomePage(final PageParameters parameters) {
    	//TODO: move css to someplace logical
    	add(new StyleSheetReference("stylesheet", HomePage.class, "main.css"));

        // Add the simplest type of label
        add(new Label("message", "If you see this message wicket is properly configured and running"));

        // TODO Add your page's components here
        add(new AjaxFallbackLink("link") {
        	@Override
        	public void onClick(AjaxRequestTarget target) {
        		System.out.println("Clicked");
        		counter++;
        		if (target != null) {
        			target.addComponent(label);	
        		}	
        		Logger.info("Hello");
        	}
        });
        label = new Label("label", new PropertyModel(this, "counter"));
        label.setOutputMarkupId(true);
        add(label);
        
        add(new Label("title", "Home - JBEI Registry"));

        add(new LoginStatusPanel("loginPanel"));
        add(new MenuPanel("menuPanel"));
    }
}
