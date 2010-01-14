package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.pages.EntryUpdatePage;

public class PartViewPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public PartViewPanel(String id, Part entry) {
		super(id);
		
		ArrayList<Component> elements = new ArrayList<Component>();

		elements.add(new Label("partNumber", 
				entry.getOnePartNumber().getPartNumber()));
		elements.add(new Label("names", 
				entry.getNamesAsString()));
		elements.add(new Label("alias", entry.getAlias()));
		elements.add(new Label("packageFormat", 
				JbeiConstants.getPackageFormat(entry.getPackageFormat())));
		elements.add(new Label("creator", 
				entry.getCreator()));
		elements.add(new Label("status", 
				org.jbei.ice.lib.utils.JbeiConstants.getStatus(entry.getStatus())));
		
		elements.add(new Label("linkToOwner", entry.getOwner()));
		elements.add(new Label("links", entry.getLinksAsString()));

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
		String creationTime = dateFormat.format(entry.getCreationTime());
		elements.add(new Label("creationTime", creationTime));
				
		String modificationTime = "";
		Date modificationTimeStamp = entry.getModificationTime();
		if (modificationTimeStamp != null) {
			 modificationTime = dateFormat.format(entry.getModificationTime());
		}
		elements.add(new Label("modificationTime", modificationTime));
		
		elements.add(new Label("keywords", entry.getKeywords()));
		elements.add(new Label("shortDescription", entry.getShortDescription()));
		
		//TODO
		elements.add(new Label("attachments", "attachment?"));
		elements.add(new Label("samples", "spmales?"));
		elements.add(new Label("sequence", "sequence?"));
		
		elements.add(new Label("references", entry.getReferences()));
		elements.add(new Label("longDescription", entry.getLongDescription()));
		elements.add(new BookmarkablePageLink("updateLink", EntryUpdatePage.class,
				new PageParameters("0=" + entry.getId())));
		
		String bioSafetyLevel = "";
		if (entry.getBioSafetyLevel() != null) {
			bioSafetyLevel = entry.getBioSafetyLevel().toString();
		} 
		elements.add(new Label("bioSafety", bioSafetyLevel));
		String intellectualProperty = "";
		if (entry.getIntellectualProperty() != null) {
			intellectualProperty = entry.getIntellectualProperty();
		}
		elements.add(new Label("intellectualProperty", intellectualProperty));
		Set<EntryFundingSource> entryFundingSources = entry
				.getEntryFundingSources();
		String principalInvestigator = null;
		String fundingSource = null;
		// TODO: handle multiple funding sources
		for (EntryFundingSource entryFundingSource : entryFundingSources) {
			principalInvestigator = entryFundingSource.getFundingSource()
					.getFundingSource();
			fundingSource = entryFundingSource.getFundingSource()
					.getPrincipalInvestigator();
		}
		if (principalInvestigator == null) {
			principalInvestigator = "";
		}
		if (fundingSource == null) {
			fundingSource = "";
		}
		elements.add(new Label("principalInvestigator", principalInvestigator));
		elements.add(new Label("fundingSource", fundingSource));
		
		for (Component item : elements) {
			add(item);
		}
		
		
	}
}
