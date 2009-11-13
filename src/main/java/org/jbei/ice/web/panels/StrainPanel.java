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
import org.jbei.ice.lib.models.Strain;

public class StrainPanel extends Panel {
	public StrainPanel(String id, Strain strain) {
		super(id);
		
		ArrayList<Component> elements = new ArrayList<Component>();
		
		elements.add(new Label("titleName", 
				strain.getOneName().getName()));
		elements.add(new Label("partNumber", 
				strain.getOnePartNumber().getPartNumber()));
		elements.add(new Label("selectionMarkers", 
				strain.getSelectionMarkersAsString()));
		elements.add(new Label("names", 
				strain.getNamesAsString()));
		elements.add(new Label("host", strain.getHost()));
		elements.add(new Label("alias", strain.getAlias()));
		elements.add(new Label("genotypePhenotype", 
				strain.getGenotypePhenotype()));
		elements.add(new Label("creator", 
				strain.getCreator()));
		elements.add(new Label("plasmids", "plasmids?"));
		//TODO:Capitalize correctly
		elements.add(new Label("status", 
				strain.getStatus()));
		
		elements.add(new Label("linkToOwner", strain.getOwner()));
		String time = "";
		 Date cTime = strain.getCreationTime();
		 if (cTime != null) {
			 time = cTime.toString();
		 } 
		
		elements.add(new Label("creationTime", time));
		elements.add(new Label("links", strain.getLinksAsString()));
		
		time = "";
		 Date modTime = strain.getModificationTime();
		 if (modTime != null) {
			 time = modTime.toString();
		 } 
		
		elements.add(new Label("modificationTime", time));
		
		elements.add(new Label("keywords", strain.getKeywords()));
		elements.add(new Label("shortDescription", strain.getShortDescription()));
		
		//TODO
		elements.add(new Label("attachments", "attachment?"));
		elements.add(new Label("samples", "spmales?"));
		elements.add(new Label("sequence", "sequence?"));
		
		elements.add(new Label("references", strain.getReferences()));
		elements.add(new Label("longDescription", strain.getLongDescription()));
		
		
		for (Component item : elements) {
			add(item);
		}
		
	}
}
