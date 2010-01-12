package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;

public class EntryViewPage extends ProtectedPage {
	public EntryViewPage(PageParameters parameters) {
		super(parameters);
		int entryId = parameters.getInt("0");
		Entry entry;
		try {
			entry = EntryManager.get(entryId);
			String recordType = entry.getRecordType();
			if (recordType.equals("strain")) {
				StrainViewPanel panel = new StrainViewPanel("entry",
						(Strain) entry);
				add(panel);
			} else if (recordType.equals("plasmid")) {
				PlasmidViewPanel panel = new PlasmidViewPanel("entry",
						(Plasmid) entry);
				add(panel);
			} else if (recordType.equals("part")) {
				PartViewPanel panel = new PartViewPanel("entry", (Part) entry);
				add(panel);
			}

		} catch (ManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
