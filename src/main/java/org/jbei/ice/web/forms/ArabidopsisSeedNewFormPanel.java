package org.jbei.ice.web.forms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.lib.models.ArabidopsisSeed.Generation;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.web.common.CommaSeparatedField;
import org.jbei.ice.web.common.CustomChoice;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.panels.MarkupAttachmentsPanel;

public class ArabidopsisSeedNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private MarkupAttachmentsPanel markupAttachmentsPanel;

    public ArabidopsisSeedNewFormPanel(String id) {
        super(id);

        ArabidopsisSeedForm form = new ArabidopsisSeedForm("arabidopsisSeedForm");
        form.add(new Button("submitButton"));
        add(form);

        add(new FeedbackPanel("feedback"));

        //renderMarkupAttachmentsPanel();
    }

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

    @SuppressWarnings("unused")
    private class ArabidopsisSeedForm extends EntrySubmitForm<ArabidopsisSeed> {
        private static final long serialVersionUID = 1L;

        private String selectionMarkers;

        // Arabidopsis Seed only fields
        private String homozygosity;
        private String ecotype;
        private String parents;
        private CustomChoice generation;
        private String harvestDate;

        public ArabidopsisSeedForm(String id) {
            super(id);

            setEntry(new ArabidopsisSeed());

            add(new TextField<String>("homozygosity", new PropertyModel<String>(this,
                    "homozygosity")));
            add(new TextField<String>("ecotype", new PropertyModel<String>(this, "ecotype")));
            add(new TextField<String>("parents", new PropertyModel<String>(this, "parents")));
            renderGeneration();
            add(new TextField<String>("selectionMarkers", new PropertyModel<String>(this,
                    "selectionMarkers")));
            add(new TextField<String>("harvestDate", new PropertyModel<String>(this, "harvestDate")));
            initializeDatePicker();

        }

        @Override
        protected void populateEntry() {
            super.populateEntry();

            ArabidopsisSeed arabidopsisSeed = getEntry();

            CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                    SelectionMarker.class, "getName", "setName");
            selectionMarkersField.setString(getSelectionMarkers());
            arabidopsisSeed.setSelectionMarkers(selectionMarkersField.getItemsAsSet());
            if (getHomozygosity() != null) {
                arabidopsisSeed.setHomozygosity(getHomozygosity());
            } else {
                arabidopsisSeed.setHomozygosity("");
            }
            if (getEcotype() != null) {
                arabidopsisSeed.setEcotype(getEcotype());
            } else {
                arabidopsisSeed.setEcotype("");
            }
            if (getParents() != null) {
                arabidopsisSeed.setParents(getParents());
            } else {
                arabidopsisSeed.setParents("");
            }
            arabidopsisSeed.setGeneration(Generation.valueOf(getGeneration().getValue()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
            try {
                arabidopsisSeed.setHarvestDate(dateFormat.parse(getHarvestDate()));
            } catch (ParseException e) {
                error("Could not interpret Harvest Date");
            }

        }

        protected void renderGeneration() {
            ArrayList<CustomChoice> generationChoices = generateCustomChoicesList(ArabidopsisSeed
                    .getGenerationOptionsMap());
            add(new DropDownChoice<CustomChoice>("generation", new PropertyModel<CustomChoice>(
                    this, "generation"), new Model<ArrayList<CustomChoice>>(generationChoices),
                    new ChoiceRenderer<CustomChoice>("name", "value")).setRequired(true));
            setGeneration(generationChoices.get(0));

        }

        private ArrayList<CustomChoice> generateCustomChoicesList(Map<String, String> map) {
            ArrayList<CustomChoice> results = new ArrayList<CustomChoice>();

            for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                results.add(new CustomChoice(mapEntry.getValue(), mapEntry.getKey()));
            }

            return results;
        }

        protected void initializeDatePicker() {

            add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "ui/ui.datepicker.css"));
            add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery-ui-1.7.2.custom.min.js"));

            add(new Label("initializeDatePickerScript", "$('#harvestDate').datepicker();")
                    .setEscapeModelStrings(false));
        }

        // Getters and setters for ArabidopsisSeedForm
        public void setSelectionMarkers(String selectionMarkers) {
            this.selectionMarkers = selectionMarkers;
        }

        public String getSelectionMarkers() {
            return selectionMarkers;
        }

        public String getHomozygosity() {
            return homozygosity;
        }

        public void setHomozygosity(String homozygosity) {
            this.homozygosity = homozygosity;
        }

        public String getEcotype() {
            return ecotype;
        }

        public void setEcotype(String ecotype) {
            this.ecotype = ecotype;
        }

        public String getParents() {
            return parents;
        }

        public void setParents(String parents) {
            this.parents = parents;
        }

        public CustomChoice getGeneration() {
            return generation;
        }

        public void setGeneration(CustomChoice generation) {
            this.generation = generation;
        }

        public String getHarvestDate() {
            return harvestDate;
        }

        public void setHarvestDate(String harvestDate) {
            this.harvestDate = harvestDate;
        }
    }
}
