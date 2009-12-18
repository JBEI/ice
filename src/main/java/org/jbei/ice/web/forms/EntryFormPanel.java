package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.HashMap;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static int SHORT_FIELD_MAX_LENGTH = 127;
	ArrayList<FormPanelHelper> sortedElements = null;
	HashMap<String, FormPanelHelper> elements = null;
	transient Entry entry = null;
	
	CommaSeparatedField<Name> names = null;
	CommaSeparatedField<Link> links = null;
	//TODO: Move this to appropriate part
	CommaSeparatedField<SelectionMarker> selectionMarkers = null;
	
	public EntryFormPanel(String id) {
		super(id);

		sortedElements = new ArrayList<FormPanelHelper>();
		elements = new HashMap<String, FormPanelHelper>();
	}
	
	@SuppressWarnings("unchecked")
	protected void renderForm() {
		Form entryForm = new Form("entryForm") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
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
		
		entryForm.add(listView);
		entryForm.add(new Button("submitButton", new Model("submmited")));
		
		add(entryForm);
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCommonElements() {
		
		names = new CommaSeparatedField(Name.class, "getName", "setName");
		links = new CommaSeparatedField(Link.class, "getLink", "setLink");
		
		FormPanelHelper namePanel = new FormPanelHelper("Name", new TextFieldPanel("itemPanel", 
				new PropertyModel(names, "string")));
		namePanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//namePanel.getFormComponent().setRequired(true);		
		elements.put("namePanel", namePanel);
		
		//TODO: move this to appropriate place later
		selectionMarkers = new CommaSeparatedField(SelectionMarker.class, "getName", "setName");
		FormPanelHelper selectionMarkersPanel = new FormPanelHelper("Selection Markers", 
				new TextFieldPanel("itemPanel", new PropertyModel(selectionMarkers, "string")));
		selectionMarkersPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox selectionMarkersInput"));
		elements.put("selectionMarkersPanel", selectionMarkersPanel);
		
		FormPanelHelper aliasPanel = new FormPanelHelper("Alias", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "alias")));
		aliasPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		aliasPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.put("aliasPanel", aliasPanel);
		
		FormPanelHelper creatorPanel = new FormPanelHelper("Creator", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "creator")));
		creatorPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		creatorPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//creatorPanel.getFormComponent().setRequired(true);		
		elements.put("creatorPanel", creatorPanel);
		
		FormPanelHelper creatorEmailPanel = new FormPanelHelper("Creator's Email", new TextFieldPanel("itemPanel", 
				new PropertyModel(entry, "creatorEmail")));
		creatorEmailPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		//creatorEmailPanel.getFormComponent().setRequired(true);		
		elements.put("creatorEmailPanel", creatorEmailPanel);
		
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
		statusPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		elements.put("statusPanel", statusPanel);
		
		LinkedHashMap visibilityChoices = new LinkedHashMap<String, String>();
		visibilityChoices.put("9", "Public");
		visibilityChoices.put("5", "Hidden");
		visibilityChoices.put("0", "Private");
		ChoiceRendererHelper visibilityChoicesHelper = new ChoiceRendererHelper(visibilityChoices);
		//TODO: Handle default value
		entry.setVisibility(5);
		FormPanelHelper visibilityPanel = new FormPanelHelper("Visibility", new DropDownChoicePanel("itemPanel",
				new PropertyModel(entry, "visibility"), visibilityChoicesHelper)); 
		visibilityPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		elements.put("visibilityPanel", visibilityPanel);
		//TODO: Handle links
		
		FormPanelHelper linksPanel = new FormPanelHelper("Links", new TextFieldPanel("itemPanel",
				new PropertyModel(links, "string")));
		linksPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		linksPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.put("linksPanel", linksPanel);
		
		FormPanelHelper keywordsPanel = new FormPanelHelper("Keywords", new TextFieldPanel("itemPanel",
				new PropertyModel(entry, "keywords")));
		keywordsPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		keywordsPanel.getFormComponent().add(new SimpleAttributeModifier("maxlength", ""+ SHORT_FIELD_MAX_LENGTH));
		elements.put("keywordsPanel", keywordsPanel);
		
		FormPanelHelper summaryPanel = new FormPanelHelper("Summary", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "shortDescription")));
		summaryPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		//summaryPanel.getFormComponent().setRequired(true);
		elements.put("summaryPanel", summaryPanel);
		
		FormPanelHelper notesPanel = new FormPanelHelper("Notes", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "longDescription"))); 
		notesPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		elements.put("notesPanel", notesPanel);
		
		FormPanelHelper referencesPanel = new FormPanelHelper("References", new TextAreaPanel("itemPanel",
				new PropertyModel(entry, "references"))); 
		referencesPanel.getFormComponent().add(new SimpleAttributeModifier("class", "inputbox"));
		elements.put("referencesPanel", referencesPanel);
	}

}
