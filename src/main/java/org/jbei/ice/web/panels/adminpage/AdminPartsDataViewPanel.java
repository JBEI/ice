package org.jbei.ice.web.panels.adminpage;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.dataProviders.UserEntriesDataProvider;
import org.jbei.ice.web.forms.JavascriptEventConfirmation;
import org.jbei.ice.web.pages.AdminPage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.panels.AbstractEntriesDataView;
import org.jbei.ice.web.panels.JbeiPagingNavigator;

public class AdminPartsDataViewPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private UserEntriesDataProvider sortableDataProvider;
	
	ResourceReference blankImage;
    ResourceReference hasAttachmentImage;
    ResourceReference hasSequenceImage;
    ResourceReference hasSampleImage;

	public AdminPartsDataViewPanel(String id) {
		super(id);
		
		Account systemAccount;
		try {
			systemAccount = AccountController.getSystemAccount();
		} catch (ControllerException e) {
			throw new ViewException(e);
		}
		sortableDataProvider = new UserEntriesDataProvider(systemAccount);
		
		blankImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
        hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
        hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        add(new Image("attachmentHeaderImage", hasAttachmentImage));
        add(new Image("sequenceHeaderImage", hasSequenceImage));
        add(new Image("sampleHeaderImage", hasSampleImage));
        
        SystemAssignedEntriesDataView dataView = new SystemAssignedEntriesDataView("entriesDataView",sortableDataProvider, 50);
		add(dataView);
		add(new JbeiPagingNavigator("navigator", dataView));
	}
	
	private class SystemAssignedEntriesDataView extends AbstractEntriesDataView<Entry> {

		private static final long serialVersionUID = 1L;

		public SystemAssignedEntriesDataView(String id,
				UserEntriesDataProvider dataProvider, int perPage) {
			super(id, dataProvider, perPage);
		}
		
		@Override
		protected void populateItem(Item<Entry> item) {
			super.populateItem(item);
			
			// add additional columns
			renderUndelete(item);
		}
		
		private void renderUndelete(final Item<Entry> item) {
			final Entry entry = item.getModelObject();
			final String prevOwner = parseOutPreviousOwner(entry).trim();
			
			AjaxFallbackLink deleteLink = new AjaxFallbackLink("undelete_link") {
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					
					if (prevOwner == null)
						throw new ViewException("Could not retrieve previous owner!");
					
					entry.setOwnerEmail(prevOwner);
					
					try {
						EntryManager.save(entry);
						JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
		                JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
					} catch (ManagerException e) {
						throw new ViewException(e);
					}
					
					setResponsePage(AdminPage.class, new PageParameters("0=parts"));
				}
	        };

	        deleteLink.add(new JavascriptEventConfirmation("onclick", "Restore entry \\'" + entry.getPartNumbersAsString() + "\\' to previous owner \\'" + prevOwner + "\\'?"));
	        deleteLink.add(new Label("undelete_link_label","undelete"));
	        item.add(deleteLink);   
		}
		
		private String parseOutPreviousOwner(Entry entry) {
			String description = entry.getLongDescription();
			String[] split = description.split("\n");
			if (split.length < 1)
				return null;
			
			String prevOwner = split[0].substring(split[0].lastIndexOf(' '));
			return prevOwner;
		}

		@Override
		protected Entry getEntry(Item<Entry> item) {
			return item.getModelObject();
		}
	}
}
