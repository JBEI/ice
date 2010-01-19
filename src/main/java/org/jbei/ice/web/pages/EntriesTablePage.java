package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.web.panels.EntryPagingPanel;

public class EntriesTablePage extends ProtectedPage {
	public EntriesTablePage(PageParameters parameters) {
		super(parameters);
		ArrayList<Entry> entries = new ArrayList<Entry>();
		
		try {
			entries.add(EntryManager.get(300));
			entries.add(EntryManager.get(301));
		} catch (ManagerException e) {
			e.printStackTrace();
		}
		
		Query q = new Query();
		//HashSet<Integer> results = q.filterName("!~Keasling");
		//HashSet<Integer> results = q.filterSelectionMarker("*");
		
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"name_or_alias", "~pbb"});
		LinkedHashSet<Entry> results = q.query(data, 0, 200);
		
		for (Entry entry : results) {
			entries.add(entry);
		}
		
		EntryPagingPanel entriesTablePanel = new EntryPagingPanel("entriesTable", entries, 20);
		add(entriesTablePanel);
	}
}
