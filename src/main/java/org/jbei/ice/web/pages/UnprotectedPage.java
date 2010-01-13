package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.panels.LoginStatusPanel;
import org.jbei.ice.web.panels.MenuPanel;
import org.jbei.ice.web.panels.SearchBarFormPanel;

public class UnprotectedPage extends WebPage {
	protected static final long serialVersionUID = 1L;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	public UnprotectedPage(final PageParameters parameters) {
		// TODO: move css to someplace logical
		add(new StyleSheetReference("stylesheet", UnprotectedPage.class, "main.css"));

		// TODO Add your page's components here
		add(new Label("title", "Home - JBEI Registry"));
		add(new LoginStatusPanel("loginPanel"));
		add(new MenuPanel("menuPanel"));
		add(new SearchBarFormPanel("searchBarPanel"));
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
