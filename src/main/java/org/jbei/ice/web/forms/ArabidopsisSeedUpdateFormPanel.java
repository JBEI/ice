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

public class ArabidopsisSeedUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ArabidopsisSeedUpdateFormPanel(String id, ArabidopsisSeed seed) {
        super(id);

        ArabidopsisSeedForm form = new ArabidopsisSeedForm("arabidopsisSeedForm", seed);
        form.add(form.createDeleteButton(form));
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    class ArabidopsisSeedForm extends EntryUpdateForm<ArabidopsisSeed> {
        private static final long serialVersionUID = 1L;

        private String selectionMarkers;

        // Arabidopsis Seed only fields
        private String homozygosity;
        private String ecotype;
        private String parents;
        private CustomChoice generation;
        private String harvestDate;

        public ArabidopsisSeedForm(String id, ArabidopsisSeed part) {
            super(id, part);

        }

        @Override
        protected void initializeElements() {
            super.initializeElements();

            initializeDatePicker();

            add(new TextField<String>("homozygosity", new PropertyModel<String>(this,
                    "homozygosity")));
            add(new TextField<String>("ecotype", new PropertyModel<String>(this, "ecotype")));
            add(new TextField<String>("parents", new PropertyModel<String>(this, "parents")));
            renderGeneration();
            add(new TextField<String>("selectionMarkers", new PropertyModel<String>(this,
                    "selectionMarkers")));
            add(new TextField<String>("harvestDate", new PropertyModel<String>(this, "harvestDate")));
        }

        @Override
        protected void populateFormElements() {
            super.populateFormElements();

            ArabidopsisSeed seed = getEntry();
            setSelectionMarkers(seed.getSelectionMarkersAsString());
            setHomozygosity(seed.getHomozygosity());
            setEcotype(seed.getEcotype());
            setParents(seed.getParents());

            setGeneration(super.lookupCustomChoice(
                super.customChoicesList(ArabidopsisSeed.getGenerationOptionsMap()),
                String.valueOf(seed.getGeneration())));
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
            if (seed.getHarvestDate() != null) {
                setHarvestDate(dateFormat.format(seed.getHarvestDate()));
            } else {
                setHarvestDate(null);
            }
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();

            ArabidopsisSeed seed = getEntry();
            CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                    SelectionMarker.class, "getName", "setName");
            selectionMarkersField.setString(getSelectionMarkers());
            seed.setSelectionMarkers(selectionMarkersField.getItemsAsSet());
            if (getHomozygosity() != null) {
                seed.setHomozygosity(getHomozygosity());
            } else {
                seed.setHomozygosity("");
            }
            if (getEcotype() != null) {
                seed.setEcotype(getEcotype());
            } else {
                seed.setEcotype("");
            }
            if (getParents() != null) {
                seed.setParents(getParents());
            } else {
                seed.setParents("");
            }
            seed.setGeneration(Generation.valueOf(getGeneration().getValue()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
            try {
                if (getHarvestDate() != null) {
                    seed.setHarvestDate(dateFormat.parse(getHarvestDate()));
                } else {
                    seed.setHarvestDate(null);
                }
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

        // Getters and setters
        public String getSelectionMarkers() {
            return selectionMarkers;
        }

        public void setSelectionMarkers(String selectionMarkers) {
            this.selectionMarkers = selectionMarkers;
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
