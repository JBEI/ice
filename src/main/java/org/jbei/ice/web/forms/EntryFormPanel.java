package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.SelectionMarker;

public class EntryFormPanel extends Panel {
	static int SHORT_FIELD_MAX_LENGTH = 127;
	ArrayList<FormPanelHelper> elements = null;
	transient Entry entry = null;
	
	CommaSeparatedField<Name> names = null;
	CommaSeparatedField<Link> links = null;
	//TODO: Move this to appropriate part
	CommaSeparatedField<SelectionMarker> selectionMarkers = null;
	
	public EntryFormPanel(String id) {
		super(id);

		elements = new ArrayList<FormPanelHelper>();
		
	}
	
	protected void renderForm() {
		Form entryForm = new Form("entryForm") {
			@Override
			protected void onSubmit() {
				System.out.println("Got something");
				System.out.println(entry.getRecordId());
				System.out.println(entry.getStatus());
				System.out.println(""+entry.getVisibility());
				try {
					
					System.out.println(names.getString());
					System.out.println(links.getString());
				} catch (FormException e) {
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
		
		entryForm.add(listView);
		entryForm.add(new Button("submitButton", new Model("submmited")));
		
		add(entryForm);
	}
	
	protected void populateCommonElements() {
		
		names = new CommaSeparatedField(Name.class, "getName", "setName");
		links = new CommaSeparatedField(Link.class, "getLink", "setLink");
		
		FormPanelHelper namePanel = new FormPanelHelper("Name", new TextFieldPanel("itemPanel", 
				new PropertyModel(names, "string")));
		namePanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//namePanel.getFormComponent().setRequired(true);		
		elements.add(namePanel);
		
		//TODO: move this to appropriate place later
		selectionMarkers = new CommaSeparatedField(SelectionMarker.class, "getName", "setName");
		FormPanelHelper selectionMarkerPanel = new FormPanelHelper("Selection Markers", 
				new TextFieldPanel("itemPanel", new PropertyModel(selectionMarkers, "string")));
		elements.add(selectionMarkerPanel);
		
		FormPanelHelper aliasPanel = new FormPanelHelper("Alias", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "alias")));
		aliasPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(aliasPanel);
		
		FormPanelHelper creatorPanel = new FormPanelHelper("Creator", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "creator")));
		creatorPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//creatorPanel.getFormComponent().setRequired(true);		
		elements.add(creatorPanel);
		
		FormPanelHelper creatorEmailPanel = new FormPanelHelper("Creator's Email", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "creatorEmail")));
		creatorEmailPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//creatorEmailPanel.getFormComponent().setRequired(true);		
		elements.add(creatorEmailPanel);
		
		LinkedHashMap statusChoices = new LinkedHashMap<String, String> ();
		statusChoices.put("complete", "Complete");
		statusChoices.put("in progress", "In Progress");
		statusChoices.put("planned", "Planned");
		ChoiceRendererHelper statusChoicesHelper= new ChoiceRendererHelper(statusChoices);
		//TODO: Handle default value
		entry.setStatus("in progress");
		FormPanelHelper statusPanel = new FormPanelHelper("Status", 
				new DropDownChoicePanel("itemPanel", 
				new PropertyModel(entry, "status"), statusChoicesHelper)); 
		elements.add(statusPanel);
		
		LinkedHashMap visibilityChoices = new LinkedHashMap<String, String>();
		visibilityChoices.put("9", "Public");
		visibilityChoices.put("5", "Hidden");
		visibilityChoices.put("0", "Private");
		ChoiceRendererHelper visibilityChoicesHelper = new ChoiceRendererHelper(visibilityChoices);
		//TODO: Handle default value
		entry.setVisibility(5);
		FormPanelHelper visibilityPanel = new FormPanelHelper("Visibility", new DropDownChoicePanel("itemPanel",
				new PropertyModel(entry, "visibility"), visibilityChoicesHelper)); 
		elements.add(visibilityPanel);
		
		//TODO: Handle links
		
		FormPanelHelper linksPanel = new FormPanelHelper("Links", new TextFieldPanel("itemPanel",
				new PropertyModel(links, "string")));
		linksPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(linksPanel);
		
		FormPanelHelper keywordsPanel = new FormPanelHelper("Keywords", new TextFieldPanel("itemPanel",
				new PropertyModel(entry, "keywords")));
		keywordsPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.add(keywordsPanel);
		
		FormPanelHelper summaryPanel = new FormPanelHelper("Summary", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "shortDescription")));
		//summaryPanel.getFormComponent().setRequired(true);
		elements.add(summaryPanel);
		
		FormPanelHelper notesPanel = new FormPanelHelper("Notes", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "longDescription"))); 
		elements.add(notesPanel);
		
		FormPanelHelper referencesPanel = new FormPanelHelper("References", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "references"))); 
		elements.add(referencesPanel);
		
	}

}
