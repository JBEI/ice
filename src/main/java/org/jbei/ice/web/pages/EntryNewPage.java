package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;
import org.jbei.ice.web.panels.EmptyEntryNewFormPanel;
import org.jbei.ice.web.panels.SelectNewEntryTypePanel;

public class EntryNewPage extends ProtectedPage {
	public EntryNewPage(PageParameters parameters) {
		super(parameters);
		
		SelectNewEntryTypePanel selectNewEntryTypePanel = new SelectNewEntryTypePanel("selectNewEntryTypePanel");
		add(selectNewEntryTypePanel);
		
		Panel formPanel = new EmptyEntryNewFormPanel("formPanel");
		formPanel.setOutputMarkupId(true);
		add(formPanel);
		
	}

}
