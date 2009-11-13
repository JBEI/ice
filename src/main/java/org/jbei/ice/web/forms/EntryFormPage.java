package org.jbei.ice.web.forms;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
//TODO: This is only for testing
import org.jbei.ice.web.pages.HomePage;

public class EntryFormPage extends HomePage {
	public EntryFormPage(PageParameters parameters) {
		super(parameters);
		
		add(new Label("entryText", "This is placeholder text"));
		EntryFormPanel formPanel = new PlasmidFormPanel("entryFormPanel");
		add(formPanel);
	}

}
