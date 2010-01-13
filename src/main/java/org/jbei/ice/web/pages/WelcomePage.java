package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.jbei.ice.web.IceSession;

public class WelcomePage extends WebPage {
	public WelcomePage(final PageParameters paramaters) {
		if (IceSession.get().isAuthenticated()) {
			throw new RestartResponseAtInterceptPageException(WorkSpacePage.class);
		}

		add(new StyleSheetReference("stylesheet", WelcomePage.class, "main.css"));

		add(new Label("title", "Welcome - JBEI Registry"));
	}
}
