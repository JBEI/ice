package org.jbei.ice.web.panels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;

public class PlasmidPanel extends Panel {
	public PlasmidPanel(String id, Plasmid plasmid) {
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
		//TODO:Capitalize correctly
		elements.add(new Label("status", 
				plasmid.getStatus()));
		//TODO: link to strains
		elements.add(new Label("linksToStrains", ""));
		elements.add(new Label("linkToOwner", plasmid.getOwner()));
		String time = "";
		 Date cTime = plasmid.getCreationTime();
		 if (cTime != null) {
			 time = cTime.toString();
		 } 
		
		elements.add(new Label("creationTime", time));
		elements.add(new Label("links", plasmid.getLinksAsString()));
		
		time = "";
		 Date modTime = plasmid.getModificationTime();
		 if (modTime != null) {
			 time = modTime.toString();
		 } 
		
		elements.add(new Label("modificationTime", time));
		
		elements.add(new Label("keywords", plasmid.getKeywords()));
		elements.add(new Label("shortDescription", plasmid.getShortDescription()));
		
		//TODO
		elements.add(new Label("attachments", "attachment?"));
		elements.add(new Label("samples", "spmales?"));
		elements.add(new Label("sequence", "sequence?"));
		
		elements.add(new Label("references", plasmid.getReferences()));
		elements.add(new Label("longDescription", plasmid.getLongDescription()));
		
		
		for (Component item : elements) {
			add(item);
		}
		
	}
}
