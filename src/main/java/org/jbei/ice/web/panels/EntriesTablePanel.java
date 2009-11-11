package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;

public class EntriesTablePanel extends Panel {

	public EntriesTablePanel(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	public EntriesTablePanel(String id, ArrayList<Entry> entries, int limit) {
		super(id);
		
		PageableListView listView = new PageableListView("itemRows", entries, limit) {

			@Override
			protected void populateItem(ListItem item) {
				Entry entry = (Entry) item.getModelObject();
				item.add(new CheckBox("checkBox", new Model(false)));
				Set<Name> nameSet = entry.getNames();
				Name temp = (Name) nameSet.toArray()[0];
				
				item.add(new Label("name", temp.getName()));
				item.add(new Label("description", entry.getShortDescription()));
		
				item.add(new Label("date", entry.getCreationTime().toString()));
			}

		};
		add(listView);
		add(new PagingNavigator("navigator", listView));
		
	}

}
