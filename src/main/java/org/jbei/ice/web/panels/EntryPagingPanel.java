package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.utils.JbeiConstants;

public class EntryPagingPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public EntryPagingPanel(String id) {
		super(id);
	}
	
	public EntryPagingPanel(String id, ArrayList<Entry> entries, int limit) {
		super(id);
		@SuppressWarnings({ "unchecked", "serial" })
		PageableListView listView = new PageableListView("itemRows", entries, limit) {

			@Override
			protected void populateItem(ListItem item) {
				
				Entry entry = (Entry) item.getModelObject();
				//item.add(new CheckBox("checkBox", new Model(false)));
				item.add(new Label("index", "" + (item.getIndex() + 1)));
				item.add(new Label("recordType", entry.getRecordType()));
				
				Set<Name> nameSet = entry.getNames();
				Set<PartNumber> partNumberSet = entry.getPartNumbers();
				PartNumber temp = (PartNumber) partNumberSet.toArray()[0];
				item.add(new Label("partNumber", temp.getPartNumber()));
				
				Name temp2 = (Name) nameSet.toArray()[0];
				item.add(new Label("name", temp2.getName()));
				
				item.add(new Label("description", entry.getShortDescription()));
				item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));
				item.add(new Label("visibility", JbeiConstants.getVisibility(entry.getVisibility())));
				
				String hasAttachment = (AttachmentManager.hasAttachment(entry)) ? "Y" : "N";
				item.add(new Label("hasAttachment", hasAttachment));
				
				String hasSequence = (SequenceManager.hasSequence(entry)) ? "Y": "N";
				item.add(new Label("hasSequence", hasSequence));
				
				String hasSample = (SampleManager.hasSample(entry)) ? "Y" : "N";
				item.add(new Label("hasSample", hasSample));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
				String dateString = dateFormat.format(entry.getCreationTime());
				item.add(new Label("date", dateString));
			}
		};
		add(listView);
		add(new PagingNavigator("navigator", listView));
		
	}

}
