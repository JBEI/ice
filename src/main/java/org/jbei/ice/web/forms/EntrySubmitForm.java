package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CommaSeparatedField;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class EntrySubmitForm<T extends Entry> extends StatelessForm<Object> {
    private static final long serialVersionUID = 1L;

    private T entry;

    // entry fields
    private String links;
    private String names;
    private String alias;
    private String creator;
    private String creatorEmail;
    private CustomChoice status;
    private String keywords;
    private String summary;
    private String notes;
    private String references;
    private CustomChoice bioSafetyLevel;
    private String intellectualProperty;
    private String fundingSource;
    private String principalInvestigator;

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
        add(new TextArea<String>("notes", new PropertyModel<String>(this, "notes")));
        add(new TextArea<String>("references", new PropertyModel<String>(this, "references")));

        renderBioSafetyLevels();

        add(new TextArea<String>("intellectualProperty", new PropertyModel<String>(this,
                "intellectualProperty")));
        add(new TextField<String>("fundingSource", new PropertyModel<String>(this, "fundingSource")));
        add(new TextField<String>("principalInvestigator", new PropertyModel<String>(this,
                "principalInvestigator")).setRequired(true).setLabel(
                new Model<String>("Principal Investigator")));
    }

    protected void initializeResources() {
        IModel<Map<String, Object>> autocompleteDataMap = new AbstractReadOnlyModel<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;

            private Map<String, Object> dataMap;

            @Override
            public Map<String, Object> getObject() {
                if (dataMap == null) {
                    TreeSet<String> uniqueSelectionMarkers = UtilsManager
                            .getUniqueSelectionMarkers();
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
                        selectionMarkersCollection.append("'").append(
                                Utils.escapeSpecialJavascriptCharacters(selectionMarker)).append(
                                "',");
                    }
                    for (String promoter : uniquePromoters) {
                        promotersCollection.append("'").append(
                                Utils.escapeSpecialJavascriptCharacters(promoter)).append("',");
                    }
                    for (String originOfReplication : uniqueOriginOfReplications) {
                        originOfReplicationsCollection.append("'").append(
                                Utils.escapeSpecialJavascriptCharacters(originOfReplication))
                                .append("',");
                    }
                    for (String plasmid : uniquePlasmids) {
                        plasmidsCollection.append("'").append(
                                Utils.escapeSpecialJavascriptCharacters(plasmid)).append("',");
                    }

                    dataMap
                            .put("selectionMarkersCollection", selectionMarkersCollection
                                    .toString());
                    dataMap.put("promotersCollection", promotersCollection.toString());
                    dataMap.put("originOfReplicationsCollection", originOfReplicationsCollection
                            .toString());
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
        entry.setLongDescription(getNotes());
        entry.setReferences(getReferences());
        entry.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel().getValue()));
        entry.setIntellectualProperty(getIntellectualProperty());

        FundingSource fundingSource = new FundingSource();
        fundingSource.setFundingSource((getFundingSource() != null) ? getFundingSource() : "");
        fundingSource.setPrincipalInvestigator(getPrincipalInvestigator());
        EntryFundingSource newEntryFundingSource = new EntryFundingSource();
        newEntryFundingSource.setEntry(entry);
        newEntryFundingSource.setFundingSource(fundingSource);
        // TODO: Handle multiple funding sources
        LinkedHashSet<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
        entryFundingSources.add(newEntryFundingSource);
        entry.setEntryFundingSources(entryFundingSources);
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

        try {
            Entry newEntry = entryController.createEntry(entry);

            setResponsePage(EntryViewPage.class, new PageParameters("0=" + newEntry.getId()));
        } catch (ControllerException e) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public T getEntry() {
        return entry;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }
}
