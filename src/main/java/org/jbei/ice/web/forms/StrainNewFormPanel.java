package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

public class StrainNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Strain strain = null;

    public StrainNewFormPanel(String id) {
        super(id);
        strain = new Strain();
        populateElements();
    }

    private void populateElements() {
        class StrainForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            // entry fields
            private String links;
            private String names;
            private String selectionMarkers;
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
            private String FundingSource;
            private String PrincipalInvestigator;

            // strain only fields
            private String host;
            private String genotypePhenotype;
            private String plasmids;

            public StrainForm(String id) {
                super(id);

                setLinks(strain.getLinksAsString());
                setNames(strain.getNamesAsString());
                setSelectionMarkers(strain.getSelectionMarkersAsString());
                setAlias(strain.getAlias());
                String creatorName = IceSession.get().getAccount().getFirstName() + " "
                        + IceSession.get().getAccount().getLastName();
                setCreator(creatorName);
                setCreatorEmail(IceSession.get().getAccount().getEmail());
                setStatus(new CustomChoice("", strain.getStatus()));
                setKeywords(strain.getKeywords());
                setSummary(strain.getShortDescription());
                setNotes(strain.getLongDescription());
                setReferences(strain.getReferences());
                setIntellectualProperty((strain.getIntellectualProperty() != null) ? strain
                        .getIntellectualProperty() : "");

                Set<EntryFundingSource> entryFundingSources = strain.getEntryFundingSources();
                // TODO: handle multiple funding sources
                for (EntryFundingSource entryFundingSource : entryFundingSources) {
                    FundingSource fundingSource = entryFundingSource.getFundingSource();
                    setFundingSource(fundingSource.getFundingSource());
                    setPrincipalInvestigator(fundingSource.getPrincipalInvestigator());
                }

                setHost(strain.getHost());
                setGenotypePhenotype(strain.getGenotypePhenotype());
                setPlasmids(strain.getPlasmids());

                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("names").setRequired(true).setLabel(
                        new Model<String>("Name")));
                add(new TextField<String>("links"));
                add(new TextField<String>("selectionMarkers"));
                add(new TextField<String>("alias"));
                add(new TextField<String>("creator").setRequired(true).setLabel(
                        new Model<String>("Creator")));
                add(new TextField<String>("creatorEmail").setRequired(true).setLabel(
                        new Model<String>("Creator's Email")));

                CustomChoice planned = new CustomChoice(JbeiConstants.getStatus("planned"),
                        "planned");
                CustomChoice complete = new CustomChoice(JbeiConstants.getStatus("complete"),
                        "complete");
                CustomChoice inProgress = new CustomChoice(JbeiConstants.getStatus("in progress"),
                        "in progress");

                ArrayList<CustomChoice> statusChoices = new ArrayList<CustomChoice>();
                statusChoices.add(planned);
                statusChoices.add(complete);
                statusChoices.add(inProgress);
                setStatus(complete);
                add(new DropDownChoice<CustomChoice>("status", new PropertyModel<CustomChoice>(
                        this, "status"), statusChoices, new ChoiceRenderer<CustomChoice>("name",
                        "value")));

                add(new TextField<String>("keywords"));
                add(new TextArea<String>("summary").setRequired(true).setLabel(
                        new Model<String>("Summary")));
                add(new TextArea<String>("notes"));
                add(new TextArea<String>("references"));

                CustomChoice level1 = new CustomChoice("Level 1", "1");
                CustomChoice level2 = new CustomChoice("Level 2", "2");
                setBioSafetyLevel(level1);
                ArrayList<CustomChoice> bioSafetyChoices = new ArrayList<CustomChoice>();
                bioSafetyChoices.add(level1);
                bioSafetyChoices.add(level2);
                add(new DropDownChoice<CustomChoice>("bioSafetyLevel",
                        new PropertyModel<CustomChoice>(this, "bioSafetyLevel"), bioSafetyChoices,
                        new ChoiceRenderer<CustomChoice>("name", "value")));

                add(new TextArea<String>("intellectualProperty"));
                add(new TextField<String>("fundingSource"));
                add(new TextField<String>("principalInvestigator").setRequired(true).setLabel(
                        new Model<String>("Principal Investigator")));
                add(new TextField<String>("host"));
                add(new TextField<String>("genotypePhenotype"));
                add(new TextField<String>("plasmids"));

                add(new Label("initializeCollectionsScript", "initializeCollections();")
                        .setEscapeModelStrings(false));
            }

            @Override
            protected void onSubmit() {
                try {
                    CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(
                            Link.class, "getLink", "setLink");
                    linksField.setString(getLinks());
                    strain.setLinks(linksField.getItemsAsSet());

                    CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(
                            Name.class, "getName", "setName");
                    namesField.setString(getNames());
                    strain.setNames(namesField.getItemsAsSet());

                    CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                            SelectionMarker.class, "getName", "setName");
                    selectionMarkersField.setString(getSelectionMarkers());
                    strain.setSelectionMarkers(selectionMarkersField.getItemsAsSet());

                } catch (FormException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                strain.setCreator(getCreator());
                strain.setCreatorEmail(getCreatorEmail());
                strain.setOwner(IceSession.get().getAccount().getFirstName() + " "
                        + IceSession.get().getAccount().getLastName());
                strain.setOwnerEmail(IceSession.get().getAccount().getEmail());
                strain.setAlias(getAlias());
                strain.setStatus(getStatus().getValue());
                strain.setKeywords(getKeywords());
                strain.setShortDescription(getSummary());
                strain.setLongDescription(getNotes());
                strain.setReferences(getReferences());
                strain.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel().getValue()));
                strain.setIntellectualProperty(getIntellectualProperty());
                FundingSource fundingSource = new FundingSource();
                fundingSource.setFundingSource((getFundingSource() != null) ? getFundingSource()
                        : "");
                fundingSource.setPrincipalInvestigator(getPrincipalInvestigator());
                EntryFundingSource newEntryFundingSource = new EntryFundingSource();
                newEntryFundingSource.setEntry(strain);
                newEntryFundingSource.setFundingSource(fundingSource);
                // TODO: Handle multiple funding sources
                LinkedHashSet<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
                entryFundingSources.add(newEntryFundingSource);
                strain.setEntryFundingSources(entryFundingSources);

                strain.setHost(getHost());
                strain.setGenotypePhenotype(getGenotypePhenotype());
                strain.setPlasmids(getPlasmids());

                try {
                    Strain newStrain = AuthenticatedEntryManager.createStrain(strain, IceSession
                            .get().getSessionKey());
                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + newStrain.getId()));
                } catch (ManagerException e) {
                    // TODO Auto-generated catch block
                    String msg = "System Error: Could not save! ";
                    Logger.error(msg + e.getMessage());
                    error(msg);
                    e.printStackTrace();
                }

            }

            @Override
            protected void onError() {

            }

            // Getters and setters for StrainForm

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

            public void setSelectionMarkers(String selectionMarkers) {
                this.selectionMarkers = selectionMarkers;
            }

            public String getSelectionMarkers() {
                return selectionMarkers;
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
                FundingSource = fundingSource;
            }

            public String getFundingSource() {
                return FundingSource;
            }

            public void setPrincipalInvestigator(String principalInvestigator) {
                PrincipalInvestigator = principalInvestigator;
            }

            public String getPrincipalInvestigator() {
                return PrincipalInvestigator;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public String getGenotypePhenotype() {
                return genotypePhenotype;
            }

            public void setGenotypePhenotype(String genotypePhenotype) {
                this.genotypePhenotype = genotypePhenotype;
            }

            public String getPlasmids() {
                return plasmids;
            }

            public void setPlasmids(String plasmids) {
                this.plasmids = plasmids;
            }
        }

        StrainForm form = new StrainForm("strainForm");
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    public void setStrain(Strain strain) {
        this.strain = strain;
    }

    public Strain getStrain() {
        return strain;
    }
}
