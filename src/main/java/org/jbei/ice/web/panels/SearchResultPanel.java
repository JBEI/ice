package org.jbei.ice.web.panels;

import java.util.ArrayList;
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
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.search.SearchResult;

public class SearchResultPanel extends Panel {

	private static final long serialVersionUID = 1L;

	
	public SearchResultPanel(String id, ArrayList<SearchResult> searchResults, int limit) {
		super(id);
		@SuppressWarnings("unchecked")
		PageableListView listView = new PageableListView("itemRows", searchResults, limit) {

			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			protected void populateItem(ListItem item) {
				SearchResult searchResult = (SearchResult) item.getModelObject();
				Entry entry = searchResult.getEntry();
				item.add(new CheckBox("checkBox", new Model<Boolean>(false)));
				Set<Name> nameSet = entry.getNames();
				Set<PartNumber> partNumberSet = entry.getPartNumbers();
				
				PartNumber temp = (PartNumber) partNumberSet.toArray()[0];
				item.add(new Label("partNumber", temp.getPartNumber()));
				
				Name temp2 = (Name) nameSet.toArray()[0];
				item.add(new Label("name", temp2.getName()));
				
				item.add(new Label("description", entry.getShortDescription()));
		
				item.add(new Label("date", entry.getCreationTime().toString()));
			}

		};
		add(listView);
		add(new PagingNavigator("navigator", listView));
		
	}


}
