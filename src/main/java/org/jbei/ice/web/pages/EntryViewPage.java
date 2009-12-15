package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.panels.PartPanel;
import org.jbei.ice.web.panels.PlasmidPanel;
import org.jbei.ice.web.panels.StrainPanel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;

public class EntryViewPage extends UnprotectedPage {
	public EntryViewPage(PageParameters parameters) {
		super(parameters);
		int entryId = parameters.getInt("0");
		Entry entry;
		try {
			entry = EntryManager.get(entryId);
			String recordType = entry.getRecordType();
			if (recordType.equals("strain")) {
				StrainPanel panel = new StrainPanel("entry", (Strain) entry);
				add (panel);
			} else if (recordType.equals("plasmid")) {
				PlasmidPanel panel = new PlasmidPanel("entry", (Plasmid) entry);
				add (panel);
			} else if (recordType.equals("part")) {
				PartPanel panel = new PartPanel("entry", (Part) entry);
				add(panel);
				//PartPanel panel = new PartPanel("entry", (Part) entry);
			}
			
		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
