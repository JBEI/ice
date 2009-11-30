package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.panels.PlasmidPanel;
import org.jbei.ice.lib.models.Plasmid;

public class PlasmidPage extends UnprotectedPage {
	public PlasmidPage(PageParameters parameters) {
		super(parameters);
		
		Plasmid entry;
		try {
			entry = (Plasmid) EntryManager.get(3786);
			PlasmidPanel plasmidPanel = new PlasmidPanel("plasmid", entry);
			add(plasmidPanel);
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
