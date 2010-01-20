package org.jbei.ice.web.panels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.web.pages.WelcomePage;
import org.jbei.ice.web.panels.LoginStatusPanel;

public class HeaderPanel extends Panel {
	private static final long serialVersionUID = 1L;

	public HeaderPanel(String id) {
		super(id);

		add(new BookmarkablePageLink<String>("homeLink", WelcomePage.class));
		add(new LoginStatusPanel("loginStatusPanel"));
		add(new Label("numberOfPartsLabel", new Model<String>(String.valueOf(EntryManager
				.getNumberOfPublicEntries()))));
		DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMMM d, yyyy");
		add(new Label("dateLabel", new Model<String>(dateFormat.format(new Date()))));
	}
}
