package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class EntryUpdatePage extends ProtectedPage {
	public EntryUpdatePage(PageParameters parameters) {
		super(parameters);
		int entryId = parameters.getInt("0");
		Entry entry;
		try {
			entry = EntryManager.get(entryId);
			String recordType = entry.getRecordType();
			if (recordType.equals("strain")) {
				StrainUpdateFormPanel panel = new StrainUpdateFormPanel("entry", (Strain) entry);
				add (panel);
			} else if (recordType.equals("plasmid")) {
				PlasmidUpdateFormPanel panel = new PlasmidUpdateFormPanel("entry", (Plasmid) entry);
				add (panel);
			} else if (recordType.equals("part")) {
				PartUpdateFormPanel panel = new PartUpdateFormPanel("entry", (Part) entry);
				add(panel);
			}
			
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
