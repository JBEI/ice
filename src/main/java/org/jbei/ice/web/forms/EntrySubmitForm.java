package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CommaSeparatedField;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.forms.ArabidopsisSeedNewFormPanel.ArabidopsisSeedNewForm;
import org.jbei.ice.web.forms.PartNewFormPanel.PartNewForm;
import org.jbei.ice.web.forms.PlasmidNewFormPanel.PlasmidNewForm;
import org.jbei.ice.web.forms.StrainNewFormPanel.StrainNewForm;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.panels.AbstractMarkupPanel;
import org.jbei.ice.web.panels.ConfluenceMarkupPanel;
import org.jbei.ice.web.panels.MarkupAttachmentsPanel;
import org.jbei.ice.web.panels.TextMarkupPanel;
import org.jbei.ice.web.panels.WikiMarkupPanel;
import org.jbei.ice.web.panels.sample.SchemeValue;
import org.jbei.ice.web.panels.sample.SchemeValueEditPanel;

public class EntrySubmitForm<T extends Entry> extends StatelessForm<Object> {
    private MarkupAttachmentsPanel markupAttachmentsPanel;

    public MarkupAttachmentsPanel getMarkupAttachmentsPanel() {
        return markupAttachmentsPanel;
    }

    public void setMarkupAttachmentsPanel(MarkupAttachmentsPanel markupAttachmentsPanel) {
        this.markupAttachmentsPanel = markupAttachmentsPanel;
    }

    protected void renderMarkupAttachmentsPanel() {
        markupAttachmentsPanel = new MarkupAttachmentsPanel("markupAttachmentsPanel");
        markupAttachmentsPanel.setOutputMarkupId(true);
        markupAttachmentsPanel.setOutputMarkupPlaceholderTag(true);

        add(markupAttachmentsPanel);
    }

    private static final long serialVersionUID = 1L;

    private T entry;
    private Sample sample;

    private AbstractMarkupPanel markupPanel;

    // entry fields
    private String links;
    private String names;
    private String alias;
    private String creator;
    private String creatorEmail;
    private CustomChoice status;
    private String keywords;
    private String summary;
    private CustomChoice notesMarkupType;
    private String references;
    private CustomChoice bioSafetyLevel;
    private String intellectualProperty;
    private String fundingSource;
    private String principalInvestigator;
    private String sampleName;
    private String sampleNotes;
    private ArrayList<String> sampleLocation = new ArrayList<String>();
    private ArrayList<CustomChoice> schemeChoices = new ArrayList<CustomChoice>();
    private CustomChoice schemeChoice;
    private ArrayList<SchemeValue> schemeValues = new ArrayList<SchemeValue>();

    private Panel sampleLocationPanel;

    public EntrySubmitForm(String id) {
        super(id);

        setCreator(IceSession.get().getAccount().getFullName());
        setCreatorEmail(IceSession.get().getAccount().getEmail());

        setModel(new CompoundPropertyModel<Object>(this));

        initializeElements();

        initializeResources();
    }

    protected void initializeElements() {
        add(new TextField<String>("names", new PropertyModel<String>(this, "names")).setRequired(
            true).setLabel(new Model<String>("Name")));
        add(new TextField<String>("links", new PropertyModel<String>(this, "links")));
        add(new TextField<String>("alias", new PropertyModel<String>(this, "alias")));
        add(new TextField<String>("creator", new PropertyModel<String>(this, "creator"))
                .setRequired(true).setLabel(new Model<String>("Creator")));
        add(new TextField<String>("creatorEmail", new PropertyModel<String>(this, "creatorEmail"))
                .setLabel(new Model<String>("Creator's Email")));

        renderStatuses();

        add(new TextField<String>("keywords", new PropertyModel<String>(this, "keywords")));
        add(new TextArea<String>("summary", new PropertyModel<String>(this, "summary"))
                .setRequired(true).setLabel(new Model<String>("Summary")));
        add(new TextArea<String>("references", new PropertyModel<String>(this, "references")));

        renderBioSafetyLevels();

        add(new TextArea<String>("intellectualProperty", new PropertyModel<String>(this,
                "intellectualProperty")));
        add(new TextField<String>("fundingSource", new PropertyModel<String>(this, "fundingSource")));
        add(new TextField<String>("principalInvestigator", new PropertyModel<String>(this,
                "principalInvestigator")).setRequired(true).setLabel(
            new Model<String>("Principal Investigator")));

        renderNotes();

        // only new forms get scheme choices
        if (this instanceof PlasmidNewForm || this instanceof StrainNewForm
                || this instanceof PartNewForm || this instanceof ArabidopsisSeedNewForm) {
            renderSample();
        }

        renderMarkupPanel();

        //renderMarkupAttachmentsPanel();
    }

    protected void initializeResources() {
        IModel<Map<String, Object>> autocompleteDataMap = new AbstractReadOnlyModel<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;

            private Map<String, Object> dataMap;

            @Override
            public Map<String, Object> getObject() {
                if (dataMap == null) {
                    TreeSet<String> uniqueSelectionMarkers = null;
                    try {
                        uniqueSelectionMarkers = UtilsManager.getUniqueSelectionMarkers();
                    } catch (ManagerException e) {
                        String msg = "Could not get Unique Selection Markers";
                        Logger.error(msg, e);
                    }
                    TreeSet<String> uniquePromoters = UtilsManager.getUniquePromoters();
                    TreeSet<String> uniqueOriginOfReplications = UtilsManager
                            .getUniqueOriginOfReplications();
                    TreeSet<String> uniquePlasmids = UtilsManager.getUniquePublicPlasmidNames();

                    dataMap = new MiniMap<String, Object>(4);

                    StringBuilder selectionMarkersCollection = new StringBuilder();
                    StringBuilder promotersCollection = new StringBuilder();
                    StringBuilder originOfReplicationsCollection = new StringBuilder();
                    StringBuilder plasmidsCollection = new StringBuilder();

                    for (String selectionMarker : uniqueSelectionMarkers) {
                        selectionMarkersCollection.append("'")
                                .append(Utils.escapeSpecialJavascriptCharacters(selectionMarker))
                                .append("',");
                    }
                    for (String promoter : uniquePromoters) {
                        promotersCollection.append("'")
                                .append(Utils.escapeSpecialJavascriptCharacters(promoter))
                                .append("',");
                    }
                    for (String originOfReplication : uniqueOriginOfReplications) {
                        originOfReplicationsCollection
                                .append("'")
                                .append(
                                    Utils.escapeSpecialJavascriptCharacters(originOfReplication))
                                .append("',");
                    }
                    for (String plasmid : uniquePlasmids) {
                        plasmidsCollection.append("'")
                                .append(Utils.escapeSpecialJavascriptCharacters(plasmid))
                                .append("',");
                    }

                    dataMap.put("selectionMarkersCollection", selectionMarkersCollection.toString());
                    dataMap.put("promotersCollection", promotersCollection.toString());
                    dataMap.put("originOfReplicationsCollection",
                        originOfReplicationsCollection.toString());
                    dataMap.put("plasmidsCollection", plasmidsCollection.toString());
                }

                return dataMap;
            }
        };

        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.autocomplete.css"));
        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.autocomplete.js"));
        add(TextTemplateHeaderContributor.forJavaScript(UnprotectedPage.class,
            UnprotectedPage.JS_RESOURCE_LOCATION + "autocompleteDataTemplate.js",
            autocompleteDataMap));

        add(new Label("initializeCollectionsScript",
                "try {initializeCollections();} catch (err) { }").setEscapeModelStrings(false));
    }

    protected void renderStatuses() {
        ArrayList<CustomChoice> statuses = customChoicesList(Entry.getStatusOptionsMap());

        add(new DropDownChoice<CustomChoice>("status", new PropertyModel<CustomChoice>(this,
                "status"), new Model<ArrayList<CustomChoice>>(statuses),
                new ChoiceRenderer<CustomChoice>("name", "value")).setRequired(true));

        setStatus(statuses.get(0));
    }

    protected void renderBioSafetyLevels() {
        ArrayList<CustomChoice> bioSafetyLevels = customChoicesList(Entry
                .getBioSafetyLevelOptionsMap());

        add(new DropDownChoice<CustomChoice>("bioSafetyLevel", new PropertyModel<CustomChoice>(
                this, "bioSafetyLevel"), new Model<ArrayList<CustomChoice>>(bioSafetyLevels),
                new ChoiceRenderer<CustomChoice>("name", "value")).setRequired(true));

        setBioSafetyLevel(bioSafetyLevels.get(0));
    }

    protected void renderNotes() {
        ArrayList<CustomChoice> markupTypes = customChoicesList(Entry.getMarkupTypeMap());

        DropDownChoice<CustomChoice> notesDropDownChoice = new DropDownChoice<CustomChoice>(
                "notesMarkupType", new PropertyModel<CustomChoice>(this, "notesMarkupType"),
                new Model<ArrayList<CustomChoice>>(markupTypes), new ChoiceRenderer<CustomChoice>(
                        "name", "value")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSelectionChanged(final CustomChoice newSelection) {
                CustomChoice tSelection = getNotesMarkupType();

                if (tSelection == null) {
                    return;
                }

                AbstractMarkupPanel markupPanel = getMarkupPanel();
                String currentMarkupData = markupPanel.getData();

                if (tSelection.getValue().equals("text")) {
                    markupPanel = new TextMarkupPanel("markupPanel");
                } else if (tSelection.getValue().equals("wiki")) {
                    markupPanel = new WikiMarkupPanel("markupPanel");
                } else if (tSelection.getValue().equals("confluence")) {
                    markupPanel = new ConfluenceMarkupPanel("markupPanel");
                } else {
                    markupPanel = new TextMarkupPanel("markupPanel");
                }

                markupPanel.setData(currentMarkupData);

                markupPanel.setOutputMarkupPlaceholderTag(true);
                markupPanel.setOutputMarkupId(true);
                getParent().replace(markupPanel);
                getParent().addOrReplace(markupPanel);

                setMarkupPanel(markupPanel);
            }

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
        };

        notesDropDownChoice.setRequired(true);

        add(notesDropDownChoice);

        setNotesMarkupType(markupTypes.get(0));
    }

    protected void renderMarkupPanel() {
        markupPanel = new TextMarkupPanel("markupPanel");
        markupPanel.setOutputMarkupId(true);
        markupPanel.setOutputMarkupPlaceholderTag(true);

        add(markupPanel);
    }

    protected ArrayList<CustomChoice> customChoicesList(Map<String, String> map) {
        ArrayList<CustomChoice> results = new ArrayList<CustomChoice>();

        for (Map.Entry<String, String> mapEntry : map.entrySet()) {
            results.add(new CustomChoice(mapEntry.getValue(), mapEntry.getKey()));
        }

        return results;
    }

    protected CustomChoice lookupCustomChoice(ArrayList<CustomChoice> choices, String key) {
        if (choices == null || choices.size() == 0) {
            return null;
        }

        CustomChoice result = null;

        for (CustomChoice choice : choices) {
            if (choice.getValue().equals(key)) {
                result = choice;

                break;
            }
        }

        return result;
    }

    protected void updateNotesMarkupEditor(String type) {
        AbstractMarkupPanel markupPanel = getMarkupPanel();

        if (type.equals("text")) {
            markupPanel = new TextMarkupPanel("markupPanel");
            ((TextMarkupPanel) markupPanel).setData(entry.getLongDescription());
        } else if (type.equals("wiki")) {
            markupPanel = new WikiMarkupPanel("markupPanel");
            ((WikiMarkupPanel) markupPanel).setData(entry.getLongDescription());
        } else if (type.equals("confluence")) {
            markupPanel = new ConfluenceMarkupPanel("markupPanel");
            ((ConfluenceMarkupPanel) markupPanel).setData(entry.getLongDescription());
        } else {
            markupPanel = new TextMarkupPanel("markupPanel");
            ((TextMarkupPanel) markupPanel).setData(entry.getLongDescription());
        }

        markupPanel.setOutputMarkupPlaceholderTag(true);
        markupPanel.setOutputMarkupId(true);
        //this.getParent().replace(markupPanel);
        //this.getParent().addOrReplace(markupPanel);
        addOrReplace(markupPanel);

        setMarkupPanel(markupPanel);
    }

    protected void renderSample() {
        add(new TextField<String>("sampleName", new PropertyModel<String>(this, "sampleName")));
        add(new TextArea<String>("sampleNotes", new PropertyModel<String>(this, "sampleNotes")));

        renderSchemeChoices();
        renderSchemeValues();
    }

    protected void renderSchemeChoices() {
        String id = "schemeChoices";
        DropDownChoice<CustomChoice> dropDownChoice = null;
        String recordType = "";

        if (this instanceof PlasmidNewForm) {
            recordType = Entry.PLASMID_ENTRY_TYPE;
        } else if (this instanceof StrainNewForm) {
            recordType = Entry.STRAIN_ENTRY_TYPE;
        } else if (this instanceof PartNewForm) {
            recordType = Entry.PART_ENTRY_TYPE;
        } else if (this instanceof ArabidopsisSeedNewForm) {
            recordType = Entry.ARABIDOPSIS_SEED_ENTRY_TYPE;
        }

        List<Storage> schemes = StorageManager.getStorageSchemesForEntryType(recordType);
        for (Storage scheme : schemes) {
            CustomChoice schemeChoice1 = new CustomChoice(scheme.getName(), String.valueOf(scheme
                    .getId()));
            schemeChoices.add(schemeChoice1);
            if (PopulateInitialDatabase.DEFAULT_PLASMID_STORAGE_SCHEME_NAME
                    .equals(scheme.getName())) {
                setSchemeChoice(schemeChoice1);

            } else if (PopulateInitialDatabase.DEFAULT_STRAIN_STORAGE_SCHEME_NAME.equals(scheme
                    .getName())) {
                setSchemeChoice(schemeChoice1);

            } else if (PopulateInitialDatabase.DEFAULT_PART_STORAGE_SCHEME_NAME.equals(scheme
                    .getName())) {
                setSchemeChoice(schemeChoice1);

            } else if (PopulateInitialDatabase.DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME
                    .equals(scheme.getName())) {
                setSchemeChoice(schemeChoice1);
            }
        }
        if (getSchemeChoices().size() == 0) {
            // TODO render nothing
        }
        dropDownChoice = new DropDownChoice<CustomChoice>(id, new PropertyModel<CustomChoice>(this,
                "schemeChoice"), new Model<ArrayList<CustomChoice>>(getSchemeChoices()),
                new ChoiceRenderer<CustomChoice>("name", "value")) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void onSelectionChanged(final CustomChoice choice) {
                EntrySubmitForm<T> form = (EntrySubmitForm<T>) getParent();
                form.setSchemeChoice(choice);
                form.getSampleLocation().clear();

                form.renderSchemeValues();
            }
        };

        add(dropDownChoice);
    }

    protected void renderSchemeValues() {
        String id = "schemeValueChoices";
        Storage currentScheme = null;
        Panel schemeValueEditPanel = null;
        schemeValues.clear();

        try {
            currentScheme = StorageManager.get(Long.parseLong(getSchemeChoice().getValue()));
        } catch (NumberFormatException e) {
            // log and pass
            Logger.error(e);
        } catch (ManagerException e) {
            // log and pass
            Logger.error(e);
        }

        if (currentScheme != null) {
            List<Storage> schemeList = currentScheme.getSchemes();

            for (Storage item : schemeList) {
                schemeValues.add(new SchemeValue(item.getName(), null));

            }
            schemeValueEditPanel = new SchemeValueEditPanel(id, schemeValues);
        } else {
            schemeValueEditPanel = new EmptyPanel(id);
        }

        schemeValueEditPanel.setOutputMarkupId(true);
        setSampleLocationPanel(schemeValueEditPanel);
        addOrReplace(schemeValueEditPanel);
    }

    protected void populateEntry() {
        CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(Link.class, "getLink",
                "setLink");
        linksField.setString(getLinks());
        entry.setLinks(linksField.getItemsAsSet());

        CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(Name.class, "getName",
                "setName");
        namesField.setString(getNames());
        entry.setNames(namesField.getItemsAsSet());

        populateEntryOwner();

        entry.setAlias(getAlias());
        entry.setCreator(getCreator());
        entry.setCreatorEmail(getCreatorEmail());
        entry.setStatus(getStatus().getValue());
        entry.setKeywords(getKeywords());
        entry.setShortDescription(getSummary());
        entry.setReferences(getReferences());
        entry.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel().getValue()));
        entry.setIntellectualProperty(getIntellectualProperty());
        entry.setLongDescriptionType(getNotesMarkupType().getValue());

        AbstractMarkupPanel markupPanel = getMarkupPanel();

        String notesString = "";
        if (markupPanel instanceof TextMarkupPanel) {
            notesString = ((TextMarkupPanel) markupPanel).getNotesTextArea()
                    .getDefaultModelObjectAsString();
        } else if (markupPanel instanceof WikiMarkupPanel) {
            notesString = ((WikiMarkupPanel) markupPanel).getMarkupTextArea()
                    .getDefaultModelObjectAsString();
        } else if (markupPanel instanceof ConfluenceMarkupPanel) {
            notesString = ((ConfluenceMarkupPanel) markupPanel).getMarkupTextArea()
                    .getDefaultModelObjectAsString();
        }

        entry.setLongDescription(notesString);

        FundingSource fundingSource = new FundingSource();
        fundingSource.setFundingSource((getFundingSource() != null) ? getFundingSource() : "");
        fundingSource.setPrincipalInvestigator(getPrincipalInvestigator());
        EntryFundingSource newEntryFundingSource = new EntryFundingSource();
        newEntryFundingSource.setEntry(entry);
        newEntryFundingSource.setFundingSource(fundingSource);
        // TODO: Tim; Handle multiple funding sources
        Set<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
        entryFundingSources.add(newEntryFundingSource);
        entry.setEntryFundingSources(entryFundingSources);

        int nullCounter = 0;
        for (SchemeValue item : getSchemeValues()) {
            if (item.getIndex() == null) {
                nullCounter++;
            }
        }

        if (getSampleName() == null) {
            if (nullCounter == getSchemeValues().size()) {
                // No sample and no location. Skip sample handling
            } else if (nullCounter != 0) {
                error("Must enter Sample Name to enter Location");
            }
        } else {
            SampleController sampleController = new SampleController(IceSession.get().getAccount());
            if (nullCounter == getSchemeValues().size()) {
                // create sample, but not location
                sample = sampleController.createSample(getSampleName(), IceSession.get()
                        .getAccount().getEmail(), getSampleNotes());
            } else if (nullCounter != 0) {
                error("Location cannot be partially filled.");
            } else if (nullCounter == 0) {
                // create sample and location
                sample = sampleController.createSample(getSampleName(), IceSession.get()
                        .getAccount().getEmail(), getSampleNotes());
                String[] labels = new String[getSchemeValues().size()];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = getSchemeValues().get(i).getIndex();
                }

                Storage storage = null;
                try {
                    Storage scheme = StorageManager.get(Long
                            .parseLong(getSchemeChoice().getValue()));
                    storage = StorageManager.getLocation(scheme, labels);
                } catch (NumberFormatException e) {
                    throw new ViewException(e);
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }

                sample.setStorage(storage);
            }
        }

    }

    protected void populateEntryOwner() {
        entry.setOwner(IceSession.get().getAccount().getFullName());
        entry.setOwnerEmail(IceSession.get().getAccount().getEmail());
    }

    @Override
    protected void onSubmit() {
        populateEntry();

        submitEntry();
    }

    protected void submitEntry() {
        EntryController entryController = new EntryController(IceSession.get().getAccount());
        SampleController sampleController = new SampleController(IceSession.get().getAccount());
        try {
            Entry newEntry = entryController.createEntry(entry);

            if (sample != null) {
                sample.setEntry(newEntry);
            }
            if (sample.getStorage() != null) {
                Storage storage = StorageManager.update(sample.getStorage());
                sample.setStorage(storage);
            }
            sampleController.saveSample(sample);

            setResponsePage(EntryViewPage.class, new PageParameters("0=" + newEntry.getId()));
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewException(e);
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public String getLinks() {
        return links;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getNames() {
        return names;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public CustomChoice getStatus() {
        return status;
    }

    public void setStatus(CustomChoice status) {
        this.status = status;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public void setBioSafetyLevel(CustomChoice bioSafetyLevel) {
        this.bioSafetyLevel = bioSafetyLevel;
    }

    public CustomChoice getBioSafetyLevel() {
        return bioSafetyLevel;
    }

    public void setNotesMarkupType(CustomChoice notesMarkupType) {
        this.notesMarkupType = notesMarkupType;
    }

    public CustomChoice getNotesMarkupType() {
        return notesMarkupType;
    }

    public void setIntellectualProperty(String intellectualProperty) {
        this.intellectualProperty = intellectualProperty;
    }

    public String getIntellectualProperty() {
        return intellectualProperty;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getSampleName() {
        return sampleName;
    }

    public T getEntry() {
        return entry;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }

    public AbstractMarkupPanel getMarkupPanel() {
        return markupPanel;
    }

    public void setMarkupPanel(AbstractMarkupPanel markupPanel) {
        this.markupPanel = markupPanel;
    }

    public void setSampleNotes(String sampleNotes) {
        this.sampleNotes = sampleNotes;
    }

    public String getSampleNotes() {
        return sampleNotes;
    }

    public void setSampleLocation(ArrayList<String> sampleLocation) {
        this.sampleLocation = sampleLocation;
    }

    public ArrayList<String> getSampleLocation() {
        return sampleLocation;
    }

    public void setSampleLocationPanel(Panel sampleLocationPanel) {
        this.sampleLocationPanel = sampleLocationPanel;
    }

    public Panel getSampleLocationPanel() {
        return sampleLocationPanel;
    }

    public void setSchemeChoices(ArrayList<CustomChoice> schemeChoices) {
        this.schemeChoices = schemeChoices;
    }

    public ArrayList<CustomChoice> getSchemeChoices() {
        return schemeChoices;
    }

    public void setSchemeChoice(CustomChoice schemeChoice) {
        this.schemeChoice = schemeChoice;
    }

    public CustomChoice getSchemeChoice() {
        return schemeChoice;
    }

    public ArrayList<SchemeValue> getSchemeValues() {
        return schemeValues;
    }

    public void setSchemeValues(ArrayList<SchemeValue> schemeValues) {
        this.schemeValues = schemeValues;
    }
}