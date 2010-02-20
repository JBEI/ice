package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

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
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.CommaSeparatedField;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.common.FormException;
import org.jbei.ice.web.pages.EntryViewPage;

public class PlasmidStrainNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PlasmidStrainNewFormPanel(String id) {
        super(id);

        PlasmidStrainForm form = new PlasmidStrainForm("plasmidStrainForm");
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    @SuppressWarnings("unused")
    private class PlasmidStrainForm extends StatelessForm<Object> {
        private static final long serialVersionUID = 1L;
        // plasmid entry fields
        private String plasmidLinks;
        private String plasmidNames;
        private String plasmidSelectionMarkers;
        private String plasmidAlias;
        private String plasmidCreator;
        private String plasmidCreatorEmail;
        private CustomChoice plasmidStatus;
        private String plasmidKeywords;
        private String plasmidSummary;
        private String plasmidNotes;
        private String plasmidReferences;
        private CustomChoice plasmidBioSafetyLevel;
        private String plasmidIntellectualProperty;
        private String plasmidFundingSource;
        private String plasmidPrincipalInvestigator;
        // plasmid only fields
        private String plasmidBackbone;
        private String plasmidOriginOfReplication;
        private String plasmidPromoters;
        private boolean plasmidCircular = true;

        // strain entry fields
        private String strainLinks;
        private String strainNames;
        private String strainSelectionMarkers;
        private String strainAlias;
        private String strainCreator;
        private String strainCreatorEmail;
        private CustomChoice strainStatus;
        private String strainKeywords;
        private String strainSummary;
        private String strainNotes;
        private String strainReferences;
        private CustomChoice strainBioSafetyLevel;
        private String strainIntellectualProperty;
        private String strainFundingSource;
        private String strainPrincipalInvestigator;
        // strain only fields
        private String strainHost;
        private String strainGenotypePhenotype;
        private String strainPlasmids;

        public PlasmidStrainForm(String id) {
            super(id);

            setModel(new CompoundPropertyModel<Object>(this));

            add(new Label("initializeCollectionsScript", "initializeCollections();")
                    .setEscapeModelStrings(false));

            // plasmid fields
            setPlasmidCreator(IceSession.get().getAccount().getFullName());
            setPlasmidCreatorEmail(IceSession.get().getAccount().getEmail());
            setPlasmidCircular(true);
            add(new TextField<String>("plasmidNames", new PropertyModel<String>(this,
                    "plasmidNames")).setRequired(true).setLabel(new Model<String>("Plasmid Name")));
            add(new TextField<String>("plasmidLinks", new PropertyModel<String>(this,
                    "plasmidLinks")));
            add(new TextField<String>("plasmidSelectionMarkers", new PropertyModel<String>(this,
                    "plasmidSelectionMarkers")));
            add(new TextField<String>("plasmidAlias", new PropertyModel<String>(this,
                    "plasmidAlias")));
            add(new TextField<String>("plasmidCreator", new PropertyModel<String>(this,
                    "plasmidCreator")).setRequired(true).setLabel(new Model<String>("Creator")));
            add(new TextField<String>("plasmidCreatorEmail", new PropertyModel<String>(this,
                    "plasmidCreatorEmail")).setLabel(new Model<String>("Creator's Email")));
            add(new TextField<String>("plasmidKeywords", new PropertyModel<String>(this,
                    "plasmidKeywords")));
            add(new TextArea<String>("plasmidSummary", new PropertyModel<String>(this,
                    "plasmidSummary")).setRequired(true).setLabel(
                    new Model<String>("Plasmid Summary")));
            add(new TextArea<String>("plasmidNotes",
                    new PropertyModel<String>(this, "plasmidNotes")));
            add(new TextArea<String>("plasmidReferences", new PropertyModel<String>(this,
                    "plasmidReferences")));
            add(new TextArea<String>("plasmidIntellectualProperty", new PropertyModel<String>(this,
                    "plasmidIntellectualProperty")));
            add(new TextField<String>("plasmidFundingSource", new PropertyModel<String>(this,
                    "plasmidFundingSource")));
            add(new TextField<String>("plasmidPrincipalInvestigator", new PropertyModel<String>(
                    this, "plasmidPrincipalInvestigator")).setRequired(true).setLabel(
                    new Model<String>("Principal Investigator")));
            add(new TextField<String>("plasmidBackbone", new PropertyModel<String>(this,
                    "plasmidBackbone")));
            add(new TextField<String>("plasmidOriginOfReplication", new PropertyModel<String>(this,
                    "plasmidOriginOfReplication")));
            add(new TextField<String>("plasmidPromoters", new PropertyModel<String>(this,
                    "plasmidPromoters")));
            add(new CheckBox("plasmidCircular", new PropertyModel<Boolean>(this, "plasmidCircular")));
            renderStatuses();
            renderBioSafetyLevels();

            // shortened strain fields
            setStrainCreator(getPlasmidCreator());
            setStrainCreatorEmail(getPlasmidCreatorEmail());
            add(new TextField<String>("strainNames", new PropertyModel<String>(this, "strainNames"))
                    .setRequired(true).setLabel(new Model<String>("Strain Number")));
            add(new TextField<String>("strainLinks", new PropertyModel<String>(this, "strainLinks")));
            add(new TextField<String>("strainSelectionMarkers", new PropertyModel<String>(this,
                    "strainSelectionMarkers")));
            add(new TextField<String>("strainAlias", new PropertyModel<String>(this, "strainAlias")));
            add(new TextField<String>("strainKeywords", new PropertyModel<String>(this,
                    "strainKeywords")));
            add(new TextArea<String>("strainSummary", new PropertyModel<String>(this,
                    "strainSummary")).setRequired(true).setLabel(
                    new Model<String>("Strain Summary")));
            add(new TextArea<String>("strainNotes", new PropertyModel<String>(this, "strainNotes")));
            add(new TextArea<String>("strainReferences", new PropertyModel<String>(this,
                    "strainReferences")));
            add(new TextArea<String>("strainIntellectualProperty", new PropertyModel<String>(this,
                    "strainIntellectualProperty")));
            add(new TextField<String>("strainHost", new PropertyModel<String>(this, "strainHost")));
            add(new TextField<String>("strainGenotypePhenotype", new PropertyModel<String>(this,
                    "strainGenotypePhenotype")));
        }

        protected void renderStatuses() {
            ArrayList<CustomChoice> statuses = customChoicesListRenderer(Entry
                    .getStatusOptionsMap());

            add(new DropDownChoice<CustomChoice>("plasmidStatus", new PropertyModel<CustomChoice>(
                    this, "plasmidStatus"), new Model<ArrayList<CustomChoice>>(statuses),
                    new ChoiceRenderer<CustomChoice>("name", "value")));

            setPlasmidStatus(statuses.get(0));
        }

        protected void renderBioSafetyLevels() {
            ArrayList<CustomChoice> bioSafetyLevels = customChoicesListRenderer(Entry
                    .getBioSafetyLevelOptionsMap());

            add(new DropDownChoice<CustomChoice>("plasmidBioSafetyLevel",
                    new PropertyModel<CustomChoice>(this, "plasmidBioSafetyLevel"),
                    new Model<ArrayList<CustomChoice>>(bioSafetyLevels),
                    new ChoiceRenderer<CustomChoice>("name", "value")));

            setPlasmidStatus(bioSafetyLevels.get(0));
        }

        protected ArrayList<CustomChoice> customChoicesListRenderer(Map<String, String> map) {
            ArrayList<CustomChoice> results = new ArrayList<CustomChoice>();

            for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                results.add(new CustomChoice(mapEntry.getValue(), mapEntry.getKey()));
            }

            return results;
        }

        @Override
        protected void onSubmit() {
            Strain strain = new Strain();
            Plasmid plasmid = new Plasmid();

            // plasmid form processing
            try {
                CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(Link.class,
                        "getLink", "setLink");
                linksField.setString(getPlasmidLinks());
                plasmid.setLinks(linksField.getItemsAsSet());
                CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(Name.class,
                        "getName", "setName");
                namesField.setString(getPlasmidNames());
                plasmid.setNames(namesField.getItemsAsSet());
                CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                        SelectionMarker.class, "getName", "setName");
                selectionMarkersField.setString(getPlasmidSelectionMarkers());
                plasmid.setSelectionMarkers(selectionMarkersField.getItemsAsSet());
            } catch (FormException e) {
                e.printStackTrace();
            }
            plasmid.setCreator(getPlasmidCreator());
            plasmid.setCreatorEmail(getPlasmidCreatorEmail());
            plasmid.setOwner(IceSession.get().getAccount().getFirstName() + " "
                    + IceSession.get().getAccount().getLastName());
            plasmid.setOwnerEmail(IceSession.get().getAccount().getEmail());
            plasmid.setAlias(getPlasmidAlias());
            plasmid.setStatus(getPlasmidStatus().getValue());
            plasmid.setKeywords(getPlasmidKeywords());
            plasmid.setShortDescription(getPlasmidSummary());
            plasmid.setLongDescription(getPlasmidNotes());
            plasmid.setReferences(getPlasmidReferences());
            plasmid.setBioSafetyLevel(Integer.parseInt(getPlasmidBioSafetyLevel().getValue()));
            plasmid.setIntellectualProperty(getPlasmidIntellectualProperty());
            FundingSource fundingSource = new FundingSource();
            fundingSource
                    .setFundingSource((getPlasmidFundingSource() != null) ? getPlasmidFundingSource()
                            : "");
            fundingSource.setPrincipalInvestigator(getPlasmidPrincipalInvestigator());
            EntryFundingSource newPlasmidFundingSource = new EntryFundingSource();
            newPlasmidFundingSource.setEntry(plasmid);
            newPlasmidFundingSource.setFundingSource(fundingSource);
            // TODO: Handle multiple funding sources
            LinkedHashSet<EntryFundingSource> plasmidFundingSources = new LinkedHashSet<EntryFundingSource>();
            plasmidFundingSources.add(newPlasmidFundingSource);
            plasmid.setEntryFundingSources(plasmidFundingSources);
            plasmid.setBackbone(getPlasmidBackbone());
            plasmid.setOriginOfReplication(getPlasmidOriginOfReplication());
            plasmid.setPromoters(getPlasmidPromoters());
            plasmid.setCircular(getPlasmidCircular());

            // simplified strain form processing
            try {
                CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(Link.class,
                        "getLink", "setLink");
                linksField.setString(getStrainLinks());
                strain.setLinks(linksField.getItemsAsSet());
                CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(Name.class,
                        "getName", "setName");
                namesField.setString(getStrainNames());
                strain.setNames(namesField.getItemsAsSet());
                CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                        SelectionMarker.class, "getName", "setName");
                selectionMarkersField.setString(getStrainSelectionMarkers());
                strain.setSelectionMarkers(selectionMarkersField.getItemsAsSet());
            } catch (FormException e) {
                e.printStackTrace();
            }
            strain.setCreator(getPlasmidCreator());
            strain.setCreatorEmail(getPlasmidCreatorEmail());
            strain.setOwner(IceSession.get().getAccount().getFirstName() + " "
                    + IceSession.get().getAccount().getLastName());
            strain.setOwnerEmail(IceSession.get().getAccount().getEmail());
            strain.setAlias(getStrainAlias());
            strain.setStatus(getPlasmidStatus().getValue());
            strain.setKeywords(getStrainKeywords());
            strain.setShortDescription(getStrainSummary());
            strain.setLongDescription(getStrainNotes());
            strain.setReferences(getStrainReferences());
            strain.setBioSafetyLevel(Integer.parseInt(getPlasmidBioSafetyLevel().getValue()));
            strain.setIntellectualProperty(getStrainIntellectualProperty());
            EntryFundingSource newStrainFundingSource = new EntryFundingSource();
            newStrainFundingSource.setEntry(strain);
            newStrainFundingSource.setFundingSource(fundingSource);
            // TODO: Handle multiple funding sources
            LinkedHashSet<EntryFundingSource> strainFundingSources = new LinkedHashSet<EntryFundingSource>();
            strainFundingSources.add(newStrainFundingSource);
            strain.setEntryFundingSources(strainFundingSources);
            strain.setHost(getStrainHost());
            strain.setGenotypePhenotype(getStrainGenotypePhenotype());
            Name[] plasmidTempNames = plasmid.getNames().toArray(new Name[0]);
            String plasmidNameString = plasmidTempNames[0].getName();
            strain.setPlasmids(plasmidNameString);

            // persist
            try {
                Plasmid newPlasmid = AuthenticatedEntryManager.createPlasmid(plasmid, IceSession
                        .get().getSessionKey());
                String plasmidPartNumberString = "[[jbei:"
                        + plasmid.getPartNumbers().toArray(new PartNumber[0])[0].getPartNumber()
                        + "]]";
                strain.setPlasmids(plasmidPartNumberString);
                AuthenticatedEntryManager.createStrain(strain, IceSession.get().getSessionKey());
                setResponsePage(EntryViewPage.class, new PageParameters("0=" + newPlasmid.getId()));
            } catch (ManagerException e) {
                String msg = "System Error: Could not save! ";
                Logger.error(msg + e.getMessage());
                error(msg);
                e.printStackTrace();
            }
        }

        public String getPlasmidLinks() {
            return plasmidLinks;
        }

        public void setPlasmidLinks(String plasmidLinks) {
            this.plasmidLinks = plasmidLinks;
        }

        public String getPlasmidNames() {
            return plasmidNames;
        }

        public void setPlasmidNames(String plasmidNames) {
            this.plasmidNames = plasmidNames;
        }

        public String getPlasmidSelectionMarkers() {
            return plasmidSelectionMarkers;
        }

        public void setPlasmidSelectionMarkers(String plasmidSelectionMarkers) {
            this.plasmidSelectionMarkers = plasmidSelectionMarkers;
        }

        public String getPlasmidAlias() {
            return plasmidAlias;
        }

        public void setPlasmidAlias(String plasmidAlias) {
            this.plasmidAlias = plasmidAlias;
        }

        public String getPlasmidCreator() {
            return plasmidCreator;
        }

        public void setPlasmidCreator(String plasmidCreator) {
            this.plasmidCreator = plasmidCreator;
        }

        public String getPlasmidCreatorEmail() {
            return plasmidCreatorEmail;
        }

        public void setPlasmidCreatorEmail(String plasmidCreatorEmail) {
            this.plasmidCreatorEmail = plasmidCreatorEmail;
        }

        public CustomChoice getPlasmidStatus() {
            return plasmidStatus;
        }

        public void setPlasmidStatus(CustomChoice plasmidStatus) {
            this.plasmidStatus = plasmidStatus;
        }

        public String getPlasmidKeywords() {
            return plasmidKeywords;
        }

        public void setPlasmidKeywords(String plasmidKeywords) {
            this.plasmidKeywords = plasmidKeywords;
        }

        public String getPlasmidSummary() {
            return plasmidSummary;
        }

        public void setPlasmidSummary(String plasmidSummary) {
            this.plasmidSummary = plasmidSummary;
        }

        public String getPlasmidNotes() {
            return plasmidNotes;
        }

        public void setPlasmidNotes(String plasmidNotes) {
            this.plasmidNotes = plasmidNotes;
        }

        public String getPlasmidReferences() {
            return plasmidReferences;
        }

        public void setPlasmidReferences(String plasmidReferences) {
            this.plasmidReferences = plasmidReferences;
        }

        public CustomChoice getPlasmidBioSafetyLevel() {
            return plasmidBioSafetyLevel;
        }

        public void setPlasmidBioSafetyLevel(CustomChoice plasmidBioSafetyLevel) {
            this.plasmidBioSafetyLevel = plasmidBioSafetyLevel;
        }

        public String getPlasmidIntellectualProperty() {
            return plasmidIntellectualProperty;
        }

        public void setPlasmidIntellectualProperty(String plasmidIntellectualProperty) {
            this.plasmidIntellectualProperty = plasmidIntellectualProperty;
        }

        public String getPlasmidFundingSource() {
            return plasmidFundingSource;
        }

        public void setPlasmidFundingSource(String plasmidFundingSource) {
            this.plasmidFundingSource = plasmidFundingSource;
        }

        public String getPlasmidPrincipalInvestigator() {
            return plasmidPrincipalInvestigator;
        }

        public void setPlasmidPrincipalInvestigator(String plasmidPrincipalInvestigator) {
            this.plasmidPrincipalInvestigator = plasmidPrincipalInvestigator;
        }

        public String getPlasmidBackbone() {
            return plasmidBackbone;
        }

        public void setPlasmidBackbone(String plasmidBackbone) {
            this.plasmidBackbone = plasmidBackbone;
        }

        public String getPlasmidOriginOfReplication() {
            return plasmidOriginOfReplication;
        }

        public void setPlasmidOriginOfReplication(String plasmidOriginOfReplication) {
            this.plasmidOriginOfReplication = plasmidOriginOfReplication;
        }

        public String getPlasmidPromoters() {
            return plasmidPromoters;
        }

        public void setPlasmidPromoters(String plasmidPromoters) {
            this.plasmidPromoters = plasmidPromoters;
        }

        public boolean getPlasmidCircular() {
            return plasmidCircular;
        }

        public void setPlasmidCircular(boolean plasmidCircular) {
            this.plasmidCircular = plasmidCircular;
        }

        public String getStrainLinks() {
            return strainLinks;
        }

        public void setStrainLinks(String strainLinks) {
            this.strainLinks = strainLinks;
        }

        public String getStrainNames() {
            return strainNames;
        }

        public void setStrainNames(String strainNames) {
            this.strainNames = strainNames;
        }

        public String getStrainSelectionMarkers() {
            return strainSelectionMarkers;
        }

        public void setStrainSelectionMarkers(String strainSelectionMarkers) {
            this.strainSelectionMarkers = strainSelectionMarkers;
        }

        public String getStrainAlias() {
            return strainAlias;
        }

        public void setStrainAlias(String strainAlias) {
            this.strainAlias = strainAlias;
        }

        public String getStrainCreator() {
            return strainCreator;
        }

        public void setStrainCreator(String strainCreator) {
            this.strainCreator = strainCreator;
        }

        public String getStrainCreatorEmail() {
            return strainCreatorEmail;
        }

        public void setStrainCreatorEmail(String strainCreatorEmail) {
            this.strainCreatorEmail = strainCreatorEmail;
        }

        public CustomChoice getStrainStatus() {
            return strainStatus;
        }

        public void setStrainStatus(CustomChoice strainStatus) {
            this.strainStatus = strainStatus;
        }

        public String getStrainKeywords() {
            return strainKeywords;
        }

        public void setStrainKeywords(String strainKeywords) {
            this.strainKeywords = strainKeywords;
        }

        public String getStrainSummary() {
            return strainSummary;
        }

        public void setStrainSummary(String strainSummary) {
            this.strainSummary = strainSummary;
        }

        public String getStrainNotes() {
            return strainNotes;
        }

        public void setStrainNotes(String strainNotes) {
            this.strainNotes = strainNotes;
        }

        public String getStrainReferences() {
            return strainReferences;
        }

        public void setStrainReferences(String strainReferences) {
            this.strainReferences = strainReferences;
        }

        public CustomChoice getStrainBioSafetyLevel() {
            return strainBioSafetyLevel;
        }

        public void setStrainBioSafetyLevel(CustomChoice strainBioSafetyLevel) {
            this.strainBioSafetyLevel = strainBioSafetyLevel;
        }

        public String getStrainIntellectualProperty() {
            return strainIntellectualProperty;
        }

        public void setStrainIntellectualProperty(String strainIntellectualProperty) {
            this.strainIntellectualProperty = strainIntellectualProperty;
        }

        public String getStrainFundingSource() {
            return strainFundingSource;
        }

        public void setStrainFundingSource(String strainFundingSource) {
            this.strainFundingSource = strainFundingSource;
        }

        public String getStrainPrincipalInvestigator() {
            return strainPrincipalInvestigator;
        }

        public void setStrainPrincipalInvestigator(String strainPrincipalInvestigator) {
            this.strainPrincipalInvestigator = strainPrincipalInvestigator;
        }

        public String getStrainHost() {
            return strainHost;
        }

        public void setStrainHost(String strainHost) {
            this.strainHost = strainHost;
        }

        public String getStrainGenotypePhenotype() {
            return strainGenotypePhenotype;
        }

        public void setStrainGenotypePhenotype(String strainGenotypePhenotype) {
            this.strainGenotypePhenotype = strainGenotypePhenotype;
        }

        public String getStrainPlasmids() {
            return strainPlasmids;
        }

        public void setStrainPlasmids(String strainPlasmids) {
            this.strainPlasmids = strainPlasmids;
        }
    }
}
