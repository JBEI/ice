package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.panels.StrainPanel;
import org.jbei.ice.lib.models.Strain;

public class StrainPage extends HomePage {
	public StrainPage(PageParameters parameters) {
		super(parameters);
		
		Strain entry;
		try {
			entry = (Strain) EntryManager.get(3787);
			StrainPanel strainPanel = new StrainPanel("strain", entry);
			add(strainPanel);
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
