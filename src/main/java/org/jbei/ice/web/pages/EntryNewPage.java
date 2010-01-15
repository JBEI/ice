package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.SelectNewEntryTypePanel;

public class EntryNewPage extends ProtectedPage {
	public EntryNewPage(PageParameters parameters) {
		super(parameters);
		
		SelectNewEntryTypePanel selectNewEntryTypePanel = new SelectNewEntryTypePanel("selectNewEntryTypePanel");
		add(selectNewEntryTypePanel);
		
		Panel formPanel = new EmptyMessagePanel("formPanel", "");
		formPanel.setOutputMarkupId(true);
		add(formPanel);
		
	}

}
