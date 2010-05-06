package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Part;

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

        public PartForm(String id, Part part) {
            super(id, part);
        }

        @Override
        protected void initializeElements() {
            super.initializeElements();
        }

        @Override
        protected void populateFormElements() {
            super.populateFormElements();
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();
        }
    }
}
