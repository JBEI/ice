package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.query.Query;
import org.jbei.ice.web.panels.EntriesTablePanel;

public class WorkspacePage extends HomePage {
	public WorkspacePage(PageParameters parameters) {
		super(parameters);

		Query q = new Query();
		ArrayList<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"name_or_alias", "~pbb"});
		
		
		LinkedHashSet<Entry> results = q.query(data, 0, 200);
		
		ArrayList<Entry> entries = new ArrayList<Entry>();

		for (Entry entry : results) {
			entries.add(entry);
		}
		
		EntriesTablePanel entriesTablePanel = new EntriesTablePanel("entriesTable", entries, 20);
		add(entriesTablePanel);
	}
}
