package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.wicket.PageParameters;
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
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Link;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.PartNumber;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.EntryViewPage;

public class PlasmidStrainNewFormPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private Strain strain = null;
	private Plasmid plasmid = null;

	public PlasmidStrainNewFormPanel(String id) {
		super(id);
		strain = new Strain();
		plasmid = new Plasmid();
		populateElements();
	}

	private void populateElements() {
		class PlasmidStrainForm extends StatelessForm<Object> {
			private static final long serialVersionUID = 1L;
			// plasmid entry fields
			private String plasmidLinks;
			private String plasmidNames;
			private String plasmidSelectionMarkers;
			private String plasmidAlias;
			private String plasmidCreator;
			private String plasmidCreatorEmail;
			private CustomChoice plasmidStatus;
			private CustomChoice plasmidVisibility;
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
			private CustomChoice strainVisibility;
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

				// plasmid fields
				String creatorName = IceSession.get().getAccount()
						.getFirstName()
						+ " " + IceSession.get().getAccount().getLastName();
				setPlasmidCreator(creatorName);
				setPlasmidCreatorEmail(IceSession.get().getAccount().getEmail());
				setStrainCreator(creatorName);
				setStrainCreatorEmail(IceSession.get().getAccount().getEmail());
				setPlasmidCircular(true);

				setModel(new CompoundPropertyModel<Object>(this));
				add(new TextField<String>("plasmidNames").setRequired(true)
						.setLabel(new Model<String>("Plasmid Name")));
				add(new TextField<String>("plasmidLinks"));
				add(new TextField<String>("plasmidSelectionMarkers"));
				add(new TextField<String>("plasmidAlias"));
				add(new TextField<String>("plasmidCreator"));
				add(new TextField<String>("plasmidCreatorEmail"));

				CustomChoice planned = new CustomChoice(JbeiConstants
						.getStatus("planned"), "planned");
				CustomChoice complete = new CustomChoice(JbeiConstants
						.getStatus("complete"), "complete");
				CustomChoice inProgress = new CustomChoice(JbeiConstants
						.getStatus("in progress"), "in progress");
				ArrayList<CustomChoice> statusChoices = new ArrayList<CustomChoice>();
				statusChoices.add(planned);
				statusChoices.add(complete);
				statusChoices.add(inProgress);
				setPlasmidStatus(complete);
				add(new DropDownChoice<CustomChoice>("plasmidStatus",
						new PropertyModel<CustomChoice>(this, "plasmidStatus"),
						statusChoices, new ChoiceRenderer<CustomChoice>("name",
								"value")));

				CustomChoice visible9 = new CustomChoice(JbeiConstants
						.getVisibility(9), "9");
				CustomChoice visible5 = new CustomChoice(JbeiConstants
						.getVisibility(5), "5");
				CustomChoice visible0 = new CustomChoice(JbeiConstants
						.getVisibility(0), "0");
				ArrayList<CustomChoice> visibilityChoices = new ArrayList<CustomChoice>();
				visibilityChoices.add(visible9);
				visibilityChoices.add(visible5);
				visibilityChoices.add(visible0);
				setPlasmidVisibility(visible9);
				add(new DropDownChoice<CustomChoice>("plasmidVisibility",
						new PropertyModel<CustomChoice>(this,
								"plasmidVisibility"), visibilityChoices,
						new ChoiceRenderer<CustomChoice>("name", "value")));

				add(new TextField<String>("plasmidKeywords"));
				add(new TextArea<String>("plasmidSummary").setRequired(true)
						.setLabel(new Model<String>("Plasmid Summary")));
				add(new TextArea<String>("plasmidNotes"));
				add(new TextArea<String>("plasmidReferences"));

				CustomChoice level1 = new CustomChoice("Level 1", "1");
				CustomChoice level2 = new CustomChoice("Level 2", "2");
				setPlasmidBioSafetyLevel(level1);
				ArrayList<CustomChoice> bioSafetyChoices = new ArrayList<CustomChoice>();
				bioSafetyChoices.add(level1);
				bioSafetyChoices.add(level2);
				add(new DropDownChoice<CustomChoice>("plasmidBioSafetyLevel",
						new PropertyModel<CustomChoice>(this,
								"plasmidBioSafetyLevel"), bioSafetyChoices,
						new ChoiceRenderer<CustomChoice>("name", "value")));

				add(new TextArea<String>("plasmidIntellectualProperty"));
				add(new TextField<String>("plasmidFundingSource"));
				add(new TextField<String>("plasmidPrincipalInvestigator")
						.setRequired(true).setLabel(
								new Model<String>("Principal Investigator")));
				add(new TextField<String>("plasmidBackbone"));
				add(new TextField<String>("plasmidOriginOfReplication"));
				add(new TextField<String>("plasmidPromoters"));
				add(new CheckBox("plasmidCircular"));

				// shortened strain fields
				setStrainCreator(getPlasmidCreator());
				setStrainCreatorEmail(getPlasmidCreatorEmail());
				add(new TextField<String>("strainNames").setRequired(true)
						.setLabel(new Model<String>("Strain Number")));
				add(new TextField<String>("strainLinks"));
				add(new TextField<String>("strainSelectionMarkers"));
				add(new TextField<String>("strainAlias"));
				add(new TextField<String>("strainKeywords"));
				add(new TextArea<String>("strainSummary").setRequired(true)
						.setLabel(new Model<String>("Strain Summary")));
				add(new TextArea<String>("strainNotes"));
				add(new TextArea<String>("strainReferences"));
				add(new TextArea<String>("strainIntellectualProperty"));
				add(new TextField<String>("strainHost"));
				add(new TextField<String>("strainGenotypePhenotype"));

			}

			protected void onSubmit() {
				// plasmid form processing
				try {
					CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(
							Link.class, "getLink", "setLink");
					linksField.setString(getPlasmidLinks());
					plasmid.setLinks(linksField.getItemsAsSet());
					CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(
							Name.class, "getName", "setName");
					namesField.setString(getPlasmidNames());
					plasmid.setNames(namesField.getItemsAsSet());
					CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
							SelectionMarker.class, "getName", "setName");
					selectionMarkersField
							.setString(getPlasmidSelectionMarkers());
					plasmid.setSelectionMarkers(selectionMarkersField
							.getItemsAsSet());
				} catch (FormException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				plasmid.setCreator(getPlasmidCreator());
				plasmid.setCreatorEmail(getPlasmidCreatorEmail());
				plasmid.setOwner(IceSession.get().getAccount().getFirstName()
						+ " " + IceSession.get().getAccount().getLastName());
				plasmid.setOwnerEmail(IceSession.get().getAccount().getEmail());
				plasmid.setAlias(getPlasmidAlias());
				plasmid.setStatus(getPlasmidStatus().getValue());
				plasmid.setVisibility(Integer.parseInt(getPlasmidVisibility()
						.getValue()));
				plasmid.setKeywords(getPlasmidKeywords());
				plasmid.setShortDescription(getPlasmidSummary());
				plasmid.setLongDescription(getPlasmidNotes());
				plasmid.setReferences(getPlasmidReferences());
				plasmid.setBioSafetyLevel(Integer
						.parseInt(getPlasmidBioSafetyLevel().getValue()));
				plasmid
						.setIntellectualProperty(getPlasmidIntellectualProperty());
				FundingSource fundingSource = new FundingSource();
				fundingSource
						.setFundingSource((getPlasmidFundingSource() != null) ? getPlasmidFundingSource()
								: "");
				fundingSource
						.setPrincipalInvestigator(getPlasmidPrincipalInvestigator());
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
				plasmid.setCircular(isPlasmidCircular());

				// simplified strain form processing
				try {
					CommaSeparatedField<Link> linksField = new CommaSeparatedField<Link>(
							Link.class, "getLink", "setLink");
					linksField.setString(getStrainLinks());
					strain.setLinks(linksField.getItemsAsSet());
					CommaSeparatedField<Name> namesField = new CommaSeparatedField<Name>(
							Name.class, "getName", "setName");
					namesField.setString(getStrainNames());
					strain.setNames(namesField.getItemsAsSet());
					CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
							SelectionMarker.class, "getName", "setName");
					selectionMarkersField
							.setString(getStrainSelectionMarkers());
					strain.setSelectionMarkers(selectionMarkersField
							.getItemsAsSet());
				} catch (FormException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				strain.setCreator(getPlasmidCreator());
				strain.setCreatorEmail(getPlasmidCreatorEmail());
				strain.setOwner(IceSession.get().getAccount().getFirstName()
						+ " " + IceSession.get().getAccount().getLastName());
				strain.setOwnerEmail(IceSession.get().getAccount().getEmail());
				strain.setAlias(getStrainAlias());
				strain.setStatus(getPlasmidStatus().getValue());
				strain.setVisibility(Integer.parseInt(getPlasmidVisibility()
						.getValue()));
				strain.setKeywords(getStrainKeywords());
				strain.setShortDescription(getStrainSummary());
				strain.setLongDescription(getStrainNotes());
				strain.setReferences(getStrainReferences());
				strain.setBioSafetyLevel(Integer
						.parseInt(getPlasmidBioSafetyLevel().getValue()));
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
				Name[] plasmidTempNames = plasmid.getNames().toArray(
						new Name[0]);
				String plasmidNameString = plasmidTempNames[0].getName();
				strain.setPlasmids(plasmidNameString);

				// persist
				try {
					Plasmid newPlasmid = EntryManager.createPlasmid(plasmid);
					String plasmidPartNumberString = "[[jbei:"
							+ plasmid.getPartNumbers().toArray(
									new PartNumber[0])[0].getPartNumber()
							+ "]]";
					strain.setPlasmids(plasmidPartNumberString);
					EntryManager.createStrain(strain);
					setResponsePage(EntryViewPage.class, new PageParameters(
							"0=" + newPlasmid.getId()));
				} catch (ManagerException e) {
					// TODO Auto-generated catch block
					String msg = "System Error: Could not save! ";
					Logger.error(msg + e.getMessage());
					error(msg);
					e.printStackTrace();
				}

			}

			protected void onError() {

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

			public void setPlasmidSelectionMarkers(
					String plasmidSelectionMarkers) {
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

			public CustomChoice getPlasmidVisibility() {
				return plasmidVisibility;
			}

			public void setPlasmidVisibility(CustomChoice plasmidVisibility) {
				this.plasmidVisibility = plasmidVisibility;
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

			public void setPlasmidBioSafetyLevel(
					CustomChoice plasmidBioSafetyLevel) {
				this.plasmidBioSafetyLevel = plasmidBioSafetyLevel;
			}

			public String getPlasmidIntellectualProperty() {
				return plasmidIntellectualProperty;
			}

			public void setPlasmidIntellectualProperty(
					String plasmidIntellectualProperty) {
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

			public void setPlasmidPrincipalInvestigator(
					String plasmidPrincipalInvestigator) {
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

			public void setPlasmidOriginOfReplication(
					String plasmidOriginOfReplication) {
				this.plasmidOriginOfReplication = plasmidOriginOfReplication;
			}

			public String getPlasmidPromoters() {
				return plasmidPromoters;
			}

			public void setPlasmidPromoters(String plasmidPromoters) {
				this.plasmidPromoters = plasmidPromoters;
			}

			public boolean isPlasmidCircular() {
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

			public CustomChoice getStrainVisibility() {
				return strainVisibility;
			}

			public void setStrainVisibility(CustomChoice strainVisibility) {
				this.strainVisibility = strainVisibility;
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

			public void setStrainBioSafetyLevel(
					CustomChoice strainBioSafetyLevel) {
				this.strainBioSafetyLevel = strainBioSafetyLevel;
			}

			public String getStrainIntellectualProperty() {
				return strainIntellectualProperty;
			}

			public void setStrainIntellectualProperty(
					String strainIntellectualProperty) {
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

			public void setStrainPrincipalInvestigator(
					String strainPrincipalInvestigator) {
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

			public void setStrainGenotypePhenotype(
					String strainGenotypePhenotype) {
				this.strainGenotypePhenotype = strainGenotypePhenotype;
			}

			public String getStrainPlasmids() {
				return strainPlasmids;
			}

			public void setStrainPlasmids(String strainPlasmids) {
				this.strainPlasmids = strainPlasmids;
			}
		}

		PlasmidStrainForm form = new PlasmidStrainForm("plasmidStrainForm");
		form.add(new Button("submitButton"));
		add(form);
		add(new FeedbackPanel("feedback"));
	}

	public Plasmid getPlasmid() {
		return plasmid;
	}

	public void setPlasmid(Plasmid plasmid) {
		this.plasmid = plasmid;
	}

	public void setStrain(Strain strain) {
		this.strain = strain;
	}

	public Strain getStrain() {
		return strain;
	}

}
