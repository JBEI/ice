package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.common.CommaSeparatedField;

public class StrainNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public StrainNewFormPanel(String id) {
        super(id);

        StrainNewForm form = new StrainNewForm("strainForm");
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    protected class StrainNewForm extends EntrySubmitForm<Strain> {
        private static final long serialVersionUID = 1L;

        // strain only fields
        private String selectionMarkers;
        private String host;
        private String genotypePhenotype;
        private String plasmids;

        public StrainNewForm(String id) {
            super(id);

            setEntry(new Strain());

            add(new TextField<String>("selectionMarkers", new PropertyModel<String>(this,
                    "selectionMarkers")));
            add(new TextField<String>("host", new PropertyModel<String>(this, "host")));
            add(new TextField<String>("genotypePhenotype", new PropertyModel<String>(this,
                    "genotypePhenotype")));
            add(new TextField<String>("plasmids", new PropertyModel<String>(this, "plasmids")));
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();

            Strain strain = getEntry();

            CommaSeparatedField<SelectionMarker> selectionMarkersField = new CommaSeparatedField<SelectionMarker>(
                    SelectionMarker.class, "getName", "setName");
            selectionMarkersField.setString(getSelectionMarkers());
            strain.setSelectionMarkers(selectionMarkersField.getItemsAsSet());

            strain.setHost(getHost());
            strain.setGenotypePhenotype(getGenotypePhenotype());
            strain.setPlasmids(getPlasmids());
        }

        // Getters and setters for StrainForm
        public void setSelectionMarkers(String selectionMarkers) {
            this.selectionMarkers = selectionMarkers;
        }

        public String getSelectionMarkers() {
            return selectionMarkers;
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
}
