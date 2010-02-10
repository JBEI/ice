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
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

public class PartNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Part part = null;

    public PartNewFormPanel(String id) {
        super(id);

        part = new Part();
        populateElements();
    }

    private void populateElements() {
        class PartForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

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
            private String FundingSource;
            private String PrincipalInvestigator;

            // part only fields
            private CustomChoice packageFormat;

            public PartForm(String id) {
                super(id);

                setLinks(part.getLinksAsString());
                setNames(part.getNamesAsString());
                setAlias(part.getAlias());
                String creatorName = IceSession.get().getAccount().getFirstName() + " "
                        + IceSession.get().getAccount().getLastName();
                setCreator(creatorName);
                setCreatorEmail(IceSession.get().getAccount().getEmail());
                setStatus(new CustomChoice("", part.getStatus()));
                setPackageFormat(new CustomChoice("", ""));
                setKeywords(part.getKeywords());
                setSummary(part.getShortDescription());
                setNotes(part.getLongDescription());
                setReferences(part.getReferences());
                setIntellectualProperty((part.getIntellectualProperty() != null) ? part
                        .getIntellectualProperty() : "");

                Set<EntryFundingSource> entryFundingSources = part.getEntryFundingSources();
                // TODO: handle multiple funding sources
                for (EntryFundingSource entryFundingSource : entryFundingSources) {
                    FundingSource fundingSource = entryFundingSource.getFundingSource();
                    setFundingSource(fundingSource.getFundingSource());
                    setPrincipalInvestigator(fundingSource.getPrincipalInvestigator());
                }

                CustomChoice nonePackage = new CustomChoice(JbeiConstants.getPackageFormat(""), "");
                CustomChoice bioBrickA = new CustomChoice(JbeiConstants
                        .getPackageFormat("biobricka"), "biobricka");
                CustomChoice bioBrickB = new CustomChoice(JbeiConstants
                        .getPackageFormat("biobrickb"), "biobrickb");
                ArrayList<CustomChoice> packageFormatChoices = new ArrayList<CustomChoice>();
                packageFormatChoices.add(nonePackage);
                packageFormatChoices.add(bioBrickA);
                packageFormatChoices.add(bioBrickB);
                setPackageFormat(nonePackage);
                add(new DropDownChoice<CustomChoice>("packageFormat",
                        new PropertyModel<CustomChoice>(this, "packageFormat"),
                        packageFormatChoices, new ChoiceRenderer<CustomChoice>("name", "value")));

                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("names").setRequired(true).setLabel(
                        new Model<String>("Name")));
                add(new TextField<String>("links"));
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

                add(new Label("initializeCollectionsScript", "initializeCollections();")
                        .setEscapeModelStrings(false));
            }

            @Override
            protected void onSubmit() {
                try {
                    CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(
                            Link.class, "getLink", "setLink");
                    linksField.setString(getLinks());
                    part.setLinks(linksField.getItemsAsSet());

                    CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(
                            Name.class, "getName", "setName");
                    namesField.setString(getNames());
                    part.setNames(namesField.getItemsAsSet());

                    CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                            SelectionMarker.class, "getName", "setName");
                    part.setSelectionMarkers(selectionMarkersField.getItemsAsSet());
                } catch (FormException e) {
                    e.printStackTrace();
                }

                part.setCreator(getCreator());
                part.setCreatorEmail(getCreatorEmail());
                part.setOwner(IceSession.get().getAccount().getFirstName() + " "
                        + IceSession.get().getAccount().getLastName());
                part.setOwnerEmail(IceSession.get().getAccount().getEmail());
                part.setAlias(getAlias());
                part.setStatus(getStatus().getValue());
                part.setKeywords(getKeywords());
                part.setShortDescription(getSummary());
                part.setLongDescription(getNotes());
                part.setReferences(getReferences());
                part.setBioSafetyLevel(Integer.parseInt(getBioSafetyLevel().getValue()));
                part.setIntellectualProperty(getIntellectualProperty());

                FundingSource fundingSource = new FundingSource();
                fundingSource.setFundingSource((getFundingSource() != null) ? getFundingSource()
                        : "");
                fundingSource.setPrincipalInvestigator(getPrincipalInvestigator());
                EntryFundingSource newEntryFundingSource = new EntryFundingSource();
                newEntryFundingSource.setEntry(part);
                newEntryFundingSource.setFundingSource(fundingSource);
                // TODO: Handle multiple funding sources
                LinkedHashSet<EntryFundingSource> entryFundingSources = new LinkedHashSet<EntryFundingSource>();
                entryFundingSources.add(newEntryFundingSource);
                part.setEntryFundingSources(entryFundingSources);

                part.setPackageFormat(getPackageFormat().getValue());

                try {
                    Part newPart = AuthenticatedEntryManager.createPart(part, IceSession.get()
                            .getSessionKey());
                    PermissionManager.addReadGroup(part, GroupManager.getEverybodyGroup());
                    setResponsePage(EntryViewPage.class, new PageParameters("0=" + newPart.getId()));
                } catch (ManagerException e) {
                    String msg = "System Error: Could not save! ";
                    Logger.error(msg + e.getMessage());
                    error(msg);
                    e.printStackTrace();
                }

            }

            // Getters and setters for PartForm

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

            public CustomChoice getPackageFormat() {
                return this.packageFormat;
            }

            public void setPackageFormat(CustomChoice packageFormat) {
                this.packageFormat = packageFormat;
            }
        }

        PartForm form = new PartForm("partForm");
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Part getPart() {
        return part;
    }
}
