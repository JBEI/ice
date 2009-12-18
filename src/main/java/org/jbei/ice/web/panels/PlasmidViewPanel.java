package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.web.pages.EntryUpdatePage;

public class PlasmidViewPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public PlasmidViewPanel(String id, Plasmid plasmid) {
		super(id);
		
		ArrayList<Component> elements = new ArrayList<Component>();
		
		elements.add(new Label("titleName", 
				plasmid.getOneName().getName()));
		elements.add(new Label("partNumber", 
				plasmid.getOnePartNumber().getPartNumber()));
		elements.add(new Label("selectionMarkers", 
				plasmid.getSelectionMarkersAsString()));
		elements.add(new Label("names", 
				plasmid.getNamesAsString()));
		elements.add(new Label("backbone", plasmid.getBackbone()));
		elements.add(new Label("alias", plasmid.getAlias()));
		elements.add(new Label("originOfReplication", 
				plasmid.getOriginOfReplication()));
		elements.add(new Label("creator", 
				plasmid.getCreator()));
		elements.add(new Label("promoters", 
				plasmid.getPromoters()));
		elements.add(new Label("status", 
				org.jbei.ice.lib.utils.JbeiConstants.getStatus(plasmid.getStatus())));
		//TODO: link to strains
		elements.add(new Label("linksToStrains", ""));
		elements.add(new Label("linkToOwner", plasmid.getOwner()));
		elements.add(new Label("links", plasmid.getLinksAsString()));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
		String creationTime = dateFormat.format(plasmid.getCreationTime());
		elements.add(new Label("creationTime", creationTime));

		String modificationTime = dateFormat.format(plasmid.getModificationTime());
		elements.add(new Label("modificationTime", modificationTime));
		
		elements.add(new Label("keywords", plasmid.getKeywords()));
		elements.add(new Label("shortDescription", plasmid.getShortDescription()));
		
		//TODO
		elements.add(new Label("attachments", "attachment?"));
		elements.add(new Label("samples", "spmales?"));
		elements.add(new Label("sequence", "sequence?"));
		
		elements.add(new Label("references", plasmid.getReferences()));
		elements.add(new Label("longDescription", plasmid.getLongDescription()));
		elements.add(new BookmarkablePageLink("updateLink", EntryUpdatePage.class,
				new PageParameters("0=" + plasmid.getId())));
		
		for (Component item : elements) {
			add(item);
		}
		
		
	}
}
