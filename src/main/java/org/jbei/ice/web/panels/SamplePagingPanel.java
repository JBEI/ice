package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.pages.EntryViewPage;

public class SamplePagingPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	public SamplePagingPanel(String id) {
		super(id);
	}

	public SamplePagingPanel(String id, ArrayList<Sample> samples, int limit) {
		super(id);
		
		@SuppressWarnings({"unchecked", "serial"})
		PageableListView listView = new PageableListView("itemRows", samples, limit) {
	
			@Override
			protected void populateItem(ListItem item) {
				Sample sample = (Sample) item.getModelObject();
				Entry entry = sample.getEntry();
				
				//item.add(new CheckBox("checkBox", new Model(false)));
				item.add(new Label("index", "" + (item.getIndex() + 1)));
				item.add(new Label("label", sample.getLabel()));
				item.add(new Label("notes", sample.getNotes()));
				
				item.add(new Label("location", "TODO GET LOCATION"));
				item.add(new Label("type", entry.getRecordType()));
				Name temp = (Name) entry.getNames().toArray()[0];
				item.add(new Label("name", temp.getName()));
				item.add(new BookmarkablePageLink("partIdLink", EntryViewPage.class, 
						new PageParameters("0=" + entry.getId())).
						add(new Label("partNumber", entry.getOnePartNumber().getPartNumber())));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
				String dateString = dateFormat.format(entry.getCreationTime());
				item.add(new Label("date", dateString));
				
			}
		};
		add(listView);
		add(new PagingNavigator("navigator", listView));
	
	}
}
