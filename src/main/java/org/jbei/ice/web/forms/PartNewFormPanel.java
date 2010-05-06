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
import org.jbei.ice.lib.models.Part.AssemblyStandard;
import org.jbei.ice.web.common.CustomChoice;

public class PartNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PartNewFormPanel(String id) {
        super(id);

        PartNewForm form = new PartNewForm("partForm");
        form.add(new Button("submitButton"));
        add(form);

        add(new FeedbackPanel("feedback"));
    }

    private class PartNewForm extends EntrySubmitForm<Part> {
        private static final long serialVersionUID = 1L;

        // part only fields
        private CustomChoice packageFormat;

        public PartNewForm(String id) {
            super(id);

            setEntry(new Part());

            renderPackageFormat();
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

            part.setPackageFormat(AssemblyStandard.valueOf(getPackageFormat().getValue()));
        }

        // Getters and setters for PartForm
        public CustomChoice getPackageFormat() {
            return this.packageFormat;
        }

        public void setPackageFormat(CustomChoice packageFormat) {
            this.packageFormat = packageFormat;
        }
    }
}
