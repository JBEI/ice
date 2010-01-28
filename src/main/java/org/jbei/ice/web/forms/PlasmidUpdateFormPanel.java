package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
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
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

@SuppressWarnings("unused")
public class PlasmidUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Plasmid plasmid = null;

    public PlasmidUpdateFormPanel(String id, Plasmid plasmid) {
        super(id);

        this.setPlasmid(plasmid);

        populateElements();
    }

    private void populateElements() {
        class PlasmidForm extends StatelessForm<Object> {

            private static final long serialVersionUID = 1L;

            //entry fields
            private String links;
            private String names;
            private String selectionMarkers;
            private String alias;
            private String creator;
            private String creatorEmail;
            private CustomChoice status;
            private CustomChoice visibility;
            private String keywords;
            private String summary;
            private String notes;
            private String references;
            private CustomChoice bioSafetyLevel;
            private String intellectualProperty;
            private String FundingSource;
            private String PrincipalInvestigator;

            //plasmid only fields
            private String backbone;
            private String originOfReplication;
            private String promoters;
            private boolean circular = true;

            public PlasmidForm(String id) {
                super(id);

                setLinks(plasmid.getLinksAsString());
                setNames(plasmid.getNamesAsString());
                setSelectionMarkers(plasmid.getSelectionMarkersAsString());
                setAlias(plasmid.getAlias());
                setCreator(plasmid.getCreator());
                setCreatorEmail(plasmid.getCreatorEmail());
                setStatus(new CustomChoice("", plasmid.getStatus()));
                setVisibility(new CustomChoice("", "" + plasmid.getVisibility()));
                setKeywords(plasmid.getKeywords());
                setSummary(plasmid.getShortDescription());
                setNotes(plasmid.getLongDescription());
                setReferences(plasmid.getReferences());

                setIntellectualProperty((plasmid.getIntellectualProperty() != null) ? plasmid
                        .getIntellectualProperty() : "");

                Set<EntryFundingSource> entryFundingSources = plasmid.getEntryFundingSources();
                // TODO: handle multiple funding sources
                for (EntryFundingSource entryFundingSource : entryFundingSources) {
                    FundingSource fundingSource = entryFundingSource.getFundingSource();
                    setFundingSource(fundingSource.getFundingSource());
                    setPrincipalInvestigator(fundingSource.getPrincipalInvestigator());
                }

                setBackbone(plasmid.getBackbone());
                setOriginOfReplication(plasmid.getOriginOfReplication());
                setPromoters(plasmid.getPromoters());
                setCircular(plasmid.getCircular());

                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("names").setRequired(true).setLabel(
                        new Model<String>("Name")));
                add(new TextField<String>("links"));
                add(new TextField<String>("selectionMarkers"));
                add(new TextField<String>("alias"));
                add(new TextField<String>("creator"));
                add(new TextField<String>("creatorEmail"));

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

                if (plasmid.getStatus().equals("planned")) {
                    this.status = planned;
                } else if (plasmid.getStatus().equals("complete")) {
                    this.status = complete;
                } else if (plasmid.getStatus().equals("in progress")) {
                    this.status = inProgress;
                }

                add(new DropDownChoice<CustomChoice>("status", new PropertyModel<CustomChoice>(
                        this, "status"), statusChoices, new ChoiceRenderer<CustomChoice>("name",
                        "value")));

                CustomChoice visible9 = new CustomChoice(JbeiConstants.getVisibility(9), "9");
                CustomChoice visible5 = new CustomChoice(JbeiConstants.getVisibility(5), "5");
                CustomChoice visible0 = new CustomChoice(JbeiConstants.getVisibility(0), "0");

                ArrayList<CustomChoice> visibilityChoices = new ArrayList<CustomChoice>();
                visibilityChoices.add(visible9);
                visibilityChoices.add(visible5);
                visibilityChoices.add(visible0);

                if (plasmid.getVisibility() == 9) {
                    this.visibility = visible9;
                } else if (plasmid.getVisibility() == 5) {
                    this.visibility = visible5;
                } else if (plasmid.getVisibility() == 0) {
                    this.visibility = visible0;
                }

                add(new DropDownChoice<CustomChoice>("visibility", new PropertyModel<CustomChoice>(
                        this, "visibility"), visibilityChoices, new ChoiceRenderer<CustomChoice>(
                        "name", "value")));

                add(new TextField<String>("keywords"));
                add(new TextArea<String>("summary").setRequired(true).setLabel(
                        new Model<String>("Summary")));
                add(new TextArea<String>("notes"));
                add(new TextArea<String>("references"));

                CustomChoice level1 = new CustomChoice("Level 1", "1");
                CustomChoice level2 = new CustomChoice("Level 2", "2");
                if (plasmid.getBioSafetyLevel() == null) {
                    this.bioSafetyLevel = level1;
                } else if (plasmid.getBioSafetyLevel() == 1) {
                    this.bioSafetyLevel = level1;
                } else if (plasmid.getBioSafetyLevel() == 2) {
                    this.bioSafetyLevel = level2;
                }

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
                add(new TextField<String>("backbone"));
                add(new TextField<String>("originOfReplication"));
                add(new TextField<String>("promoters"));
                add(new CheckBox("circular"));
                add(new Label("initializeCollectionsScript", "initializeCollections();")
                        .setEscapeModelStrings(false));
            }

            @Override
            protected void onSubmit() {
                try {
                    CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(
                            Link.class, "getLink", "setLink");
                    linksField.setString(getLinks());
                    plasmid.setLinks(linksField.getItemsAsSet());

                    CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(
                            Name.class, "getName", "setName");
                    namesField.setString(getNames());
                    plasmid.setNames(namesField.getItemsAsSet());

                    CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                            SelectionMarker.class, "getName", "setName");
                    selectionMarkersField.setString(getSelectionMarkers());
                    plasmid.setSelectionMarkers(selectionMarkersField.getItemsAsSet());

                } catch (FormException e) {
                    e.printStackTrace();
                }

                plasmid.setAlias(getAlias());
                plasmid.setStatus(getStatus().getValue());
                plasmid.setVisibility(Integer.parseInt(getVisibility().getValue()));
                plasmid.setKeywords(getKeywords());
                plasmid.setShortDescription(getSummary());
                plasmid.setLongDescription(getNotes());
                plasmid.setReferences(getReferences());
                plasmid.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel().getValue()));
                plasmid.setIntellectualProperty(getIntellectualProperty());

                FundingSource fundingSource = new FundingSource();
                fundingSource.setFundingSource((getFundingSource() != null) ? getFundingSource()
                        : "");
                fundingSource.setPrincipalInvestigator(getPrincipalInvestigator());
                EntryFundingSource newEntryFundingSource = new EntryFundingSource();
                newEntryFundingSource.setEntry(plasmid);
                newEntryFundingSource.setFundingSource(fundingSource);
                // TODO: Handle multiple funding sources
                LinkedHashSet<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
                entryFundingSources.add(newEntryFundingSource);
                plasmid.setEntryFundingSources(entryFundingSources);

                plasmid.setBackbone(getBackbone());
                plasmid.setOriginOfReplication(getOriginOfReplication());
                plasmid.setPromoters(getPromoters());
                plasmid.setCircular(getCircular());

                try {
                    AuthenticatedEntryManager.save(plasmid, IceSession.get().getSessionKey());
                    JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
                    JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
                    setResponsePage(EntryViewPage.class, new PageParameters("0=" + plasmid.getId()));
                } catch (ManagerException e) {
                    String msg = "System Error: Could not save! ";
                    Logger.error(msg + e.getMessage());
                    error(msg);
                    e.printStackTrace();
                } catch (PermissionException e) {
                    error(e.getMessage());
                }
            }

            // Getters and setters for PlasmidForm
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

            public CustomChoice getVisibility() {
                return visibility;
            }

            public void setVisibility(CustomChoice visibility) {
                this.visibility = visibility;
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

            public String getBackbone() {
                return backbone;
            }

            public void setBackbone(String backbone) {
                this.backbone = backbone;
            }

            public String getOriginOfReplication() {
                return originOfReplication;
            }

            public void setOriginOfReplication(String originOfReplication) {
                this.originOfReplication = originOfReplication;
            }

            public String getPromoters() {
                return promoters;
            }

            public void setPromoters(String promoters) {
                this.promoters = promoters;
            }

            public boolean getCircular() {
                return circular;
            }

            public void setCircular(boolean circular) {
                this.circular = circular;
            }
        }

        PlasmidForm form = new PlasmidForm("plasmidForm");
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    public void setPlasmid(Plasmid plasmid) {
        this.plasmid = plasmid;
    }

    public Plasmid getPlasmid() {
        return plasmid;
    }
}
