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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PlasmidFormPanel(String id) {
		super(id);
		
		entry = new Plasmid();
		
		super.populateCommonElements();
		populateElements();
		sortElements();
		renderForm();
	}

	public PlasmidFormPanel(String id, Plasmid plasmid) {
		super(id);
		entry = plasmid;
		
		super.populateCommonElements();
		populateElements();
		sortElements();
		renderForm();
		
	}
	private void sortElements() {
		ArrayList<FormPanelHelper> newElements = new ArrayList<FormPanelHelper>();
		newElements.add(elements.get("namePanel"));
		newElements.add(elements.get("creatorPanel"));
		newElements.add(elements.get("creatorEmailPanel"));
		newElements.add(elements.get("statusPanel"));
		newElements.add(elements.get("visibilityPanel"));
		newElements.add(elements.get("aliasPanel"));
		newElements.add(elements.get("linksPanel"));
		newElements.add(elements.get("circularPanel"));
		newElements.add(elements.get("backbonePanel"));
		newElements.add(elements.get("selectionMarkersPanel"));
		newElements.add(elements.get("originPanel"));
		newElements.add(elements.get("promotersPanel"));
		newElements.add(elements.get("keywordsPanel"));
		newElements.add(elements.get("summaryPanel"));
		newElements.add(elements.get("notesPanel"));
		newElements.add(elements.get("referencesPanel"));

		sortedElements = newElements;
	}

	@SuppressWarnings("unchecked")
	private void populateElements() {
		FormPanelHelper backbonePanel = new FormPanelHelper("Backbone", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "backbone")));
		backbonePanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		backbonePanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.put("backbonePanel", backbonePanel);
		
		FormPanelHelper originPanel = new FormPanelHelper("Origin of Replication", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "originOfReplication")));
		originPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox originOfReplicationsInput"));
		originPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.put("originPanel", originPanel);
		
		FormPanelHelper promotersPanel = new FormPanelHelper("Promoters", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "promoters")));
		promotersPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		promotersPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox promotersInput"));
		elements.put("promotersPanel", promotersPanel);
		
		LinkedHashMap circularChoices = new LinkedHashMap<String, String> ();
		circularChoices.put("1", "True");
		circularChoices.put("0", "False");
		ChoiceRendererHelper circularChoicesHelper= new ChoiceRendererHelper(circularChoices);
		//TODO: Handle default value
		((Plasmid) entry).setCircular(true);
		FormPanelHelper circularPanel = new FormPanelHelper("Circular", 
				new DropDownChoicePanel("itemPanel", 
				new PropertyModel(entry, "circular"), circularChoicesHelper));
		circularPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		elements.put("circularPanel", circularPanel);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void renderForm() {
		Form plasmidForm = new Form("plasmidForm") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
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
		
		
		ListView listView = new ListView("itemPanels", sortedElements) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
