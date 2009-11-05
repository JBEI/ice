package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Plasmid;

public class PlasmidFormPanel extends EntryFormPanel {

	public PlasmidFormPanel(String id) {
		super(id);
		
		entry = new Plasmid();
		
		super.populateCommonElements();
		populateElements();
		sortElements();
		renderForm();
	}

	private void sortElements() {
		ArrayList<FormPanelHelper> newElements = new ArrayList<FormPanelHelper>();
		newElements.add(elements.get(0)); //name
		newElements.add(elements.get(3)); //creator
		newElements.add(elements.get(4)); //creatorEmail
		newElements.add(elements.get(5)); // status
		newElements.add(elements.get(6)); // visibility
		newElements.add(elements.get(2)); //alias
		newElements.add(elements.get(7)); //links
		newElements.add(elements.get(15)); //circular
		newElements.add(elements.get(12)); //backbone
		newElements.add(elements.get(1)); // selectionMarkers
		newElements.add(elements.get(13)); // origin
		newElements.add(elements.get(14)); // promoters
		newElements.add(elements.get(8)); // keywords
		newElements.add(elements.get(9)); // summary
		newElements.add(elements.get(10)); // notes
		newElements.add(elements.get(11)); // references
		
		elements = newElements;
	}

	private void populateElements() {
		FormPanelHelper backbonePanel = new FormPanelHelper("Backbone", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "backbone")));
		backbonePanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(backbonePanel);
		
		FormPanelHelper originPanel = new FormPanelHelper("Origin of Replication", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "originOfReplication")));
		originPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(originPanel);
		
		FormPanelHelper promotersPanel = new FormPanelHelper("Promoters", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "promoters")));
		promotersPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(promotersPanel);
		
		LinkedHashMap circularChoices = new LinkedHashMap<String, String> ();
		circularChoices.put("1", "True");
		circularChoices.put("0", "False");
		ChoiceRendererHelper circularChoicesHelper= new ChoiceRendererHelper(circularChoices);
		//TODO: Handle default value
		((Plasmid) entry).setCircular(true);
		FormPanelHelper circularPanel = new FormPanelHelper("Circular", 
				new DropDownChoicePanel("itemPanel", 
				new PropertyModel(entry, "circular"), circularChoicesHelper)); 
		elements.add(circularPanel);
		
	}

	@Override
	protected void renderForm() {
		Form plasmidForm = new Form("plasmidForm") {
			@Override
			protected void onSubmit() {
				entry.setNames(names.getItemsAsSet());
				entry.setLinks(links.getItemsAsSet());
				entry.setSelectionMarkers(selectionMarkers.getItemsAsSet());
				
				try {
					Plasmid temp = (Plasmid) entry;
					Plasmid newEntry = EntryManager.createPlasmid(temp);
					
					System.out.println("created new plasmid" + newEntry.getId());
				} catch (ManagerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			protected void onError() {
				System.out.println("error");
			}
		};
		
		
		ListView listView = new ListView("itemPanels", elements) {
			@Override
			protected void populateItem(ListItem item) {
				FormPanelHelper helper = (FormPanelHelper) item.getModelObject();
				
				item.add(new Label("itemLabel", helper.getName()));
				item.add(helper.getPanel());
			}
		};
		listView.setReuseItems(true);
		
		plasmidForm.add(listView);
		plasmidForm.add(new Button("submitButton", new Model("submmited")));
		
		this.add(plasmidForm);
	}

}
