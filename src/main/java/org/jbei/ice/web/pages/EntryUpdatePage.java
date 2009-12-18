package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.web.forms.PlasmidFormPanel;

public class EntryUpdatePage extends ProtectedPage {
	public EntryUpdatePage(PageParameters parameters) {
		super(parameters);
		int entryId = parameters.getInt("0");
		Entry entry;
		try {
			entry = EntryManager.get(entryId);
			String recordType = entry.getRecordType();
			if (recordType.equals("strain")) {
				//StrainUpdatePanel panel = new StrainUpdatePanel("entry", (Strain) entry);
				//add (panel);
			} else if (recordType.equals("plasmid")) {
				PlasmidFormPanel panel = new PlasmidFormPanel("entry", (Plasmid) entry);
				add (panel);
			} else if (recordType.equals("part")) {
				//PartViewPanel panel = new PartViewPanel("entry", (Part) entry);
				//add(panel);
			}
			
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
