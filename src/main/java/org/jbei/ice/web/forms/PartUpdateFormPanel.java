package org.jbei.ice.web.forms;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.web.common.CustomChoice;

public class PartUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PartUpdateFormPanel(String id, Part part) {
        super(id);

        PartForm form = new PartForm("partForm", part);
        form.add(new Button("submitButton"));
        add(form);
        add(new FeedbackPanel("feedback"));
    }

    class PartForm extends EntryUpdateForm<Part> {
        private static final long serialVersionUID = 1L;

        //part only fields
        private CustomChoice packageFormat;

        public PartForm(String id, Part part) {
            super(id, part);
        }

        @Override
        protected void initializeElements() {
            super.initializeElements();

            renderPackageFormat();
        }

        @Override
        protected void populateFormElements() {
            super.populateFormElements();

            setPackageFormat(lookupCustomChoice(
                    customChoicesList(Part.getPackageFormatOptionsMap()), getEntry()
                            .getPackageFormat()));
        }

        protected void renderPackageFormat() {
            ArrayList<CustomChoice> packageFormats = customChoicesList(Part
                    .getPackageFormatOptionsMap());

            add(new DropDownChoice<CustomChoice>("packageFormat", new PropertyModel<CustomChoice>(
                    this, "packageFormat"), new Model<ArrayList<CustomChoice>>(packageFormats),
                    new ChoiceRenderer<CustomChoice>("name", "value")));

            setPackageFormat(packageFormats.get(0));
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();

            Part part = getEntry();

            part.setPackageFormat(getPackageFormat().getValue());
        }

        // Getters and setters for PartForm
        public CustomChoice getPackageFormat() {
            return packageFormat;
        }

        public void setPackageFormat(CustomChoice packageFormat) {
            this.packageFormat = packageFormat;
        }
    }
}
