package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.utils.JbeiConstants;

public class PartViewPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PartViewPanel(String id, Part part) {
		super(id);
		
		ArrayList<Component> elements = new ArrayList<Component>();
		
		elements.add(new Label("titleName", 
				part.getOneName().getName()));
		elements.add(new Label("partNumber", 
				part.getOnePartNumber().getPartNumber()));
		elements.add(new Label("names", 
				part.getNamesAsString()));
		elements.add(new Label("alias", part.getAlias()));
		elements.add(new Label("packageFormat", 
				JbeiConstants.getPackageFormat(part.getPackageFormat())));
		elements.add(new Label("creator", 
				part.getCreator()));
		elements.add(new Label("status", 
				org.jbei.ice.lib.utils.JbeiConstants.getStatus(part.getStatus())));
		
		elements.add(new Label("linkToOwner", part.getOwner()));
		elements.add(new Label("links", part.getLinksAsString()));

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
		String creationTime = dateFormat.format(part.getCreationTime());
		elements.add(new Label("creationTime", creationTime));
				
		String modificationTime = dateFormat.format(part.getModificationTime());
		elements.add(new Label("modificationTime", modificationTime));
		
		elements.add(new Label("keywords", part.getKeywords()));
		elements.add(new Label("shortDescription", part.getShortDescription()));
		
		//TODO
		elements.add(new Label("attachments", "attachment?"));
		elements.add(new Label("samples", "spmales?"));
		elements.add(new Label("sequence", "sequence?"));
		
		elements.add(new Label("references", part.getReferences()));
		elements.add(new Label("longDescription", part.getLongDescription()));
		
		
		for (Component item : elements) {
			add(item);
		}
		
		
	}
}
