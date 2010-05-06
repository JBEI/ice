package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Part.AssemblyStandard;

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

        public PartNewForm(String id) {
            super(id);
            Part part = new Part();
            // default is RAW until sequence is supplied.
            part.setPackageFormat(AssemblyStandard.RAW);
            setEntry(part);
        }

        @Override
        protected void populateEntry() {
            super.populateEntry();
        }

    }
}
