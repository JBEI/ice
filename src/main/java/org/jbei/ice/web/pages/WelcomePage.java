package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.jbei.ice.web.panels.LoginPanel;

public class WelcomePage extends WebPage{
	protected static final long serialVersionUID = 1L;

	public WelcomePage(final PageParameters paramaters) {
		//TODO: move css to someplace logical
		add(new StyleSheetReference("stylesheet", WelcomePage.class, "main.css"));
		
		add(new Label("title", "Welcome - JBEI Registry"));
		add(new LoginPanel("loginPanel"));
		
		
	}

}
