package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.pages.EntryViewPage;

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
				
				item.add(new BookmarkablePageLink("partIdLink", EntryViewPage.class, 
						new PageParameters("0=" + entry.getId())).
						add(new Label("partNumber", entry.getOnePartNumber().getPartNumber())));
								
				item.add(new Label("name", entry.getOneName().getName()));
				
				item.add(new Label("description", entry.getShortDescription()));
				item.add(new Label("status", JbeiConstants.getStatus(entry.getStatus())));
				item.add(new Label("visibility", JbeiConstants.getVisibility(entry.getVisibility())));
				
				ResourceReference blankImage = new ResourceReference(EntryPagingPanel.class, "blank.png");
				ResourceReference hasAttachmentImage = new ResourceReference(EntryPagingPanel.class, "attachment.gif");
				ResourceReference hasSequenceImage = new ResourceReference(EntryPagingPanel.class, "sequence.gif");
				ResourceReference hasSampleImage = new ResourceReference(EntryPagingPanel.class, "sample.png");
				
				ResourceReference hasAttachment = (AttachmentManager.hasAttachment(entry)) ? 
						hasAttachmentImage : blankImage;
				item.add(new Image("hasAttachment", hasAttachment));

				ResourceReference hasSequence = (SequenceManager.hasSequence(entry)) ? 
						hasSequenceImage: blankImage;
				item.add(new Image("hasSequence", hasSequence));
				
				ResourceReference hasSample= (SampleManager.hasSample(entry)) ? 
						hasSampleImage : blankImage;
				item.add(new Image("hasSample", hasSample));
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
				String dateString = dateFormat.format(entry.getCreationTime());
				item.add(new Label("date", dateString));
			}
		};
		add(listView);
		add(new PagingNavigator("navigator", listView));
		
	}

}
