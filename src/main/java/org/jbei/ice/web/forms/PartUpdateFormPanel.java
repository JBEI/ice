package org.jbei.ice.web.forms;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class PartUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PartUpdateFormPanel(String id, Part part) {
        super(id);

        PartForm form = new PartForm("partForm", part);
        AjaxButton deleteButton = new AjaxButton("deletePartButton", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                @SuppressWarnings("rawtypes")
                Entry entry = ((EntryUpdateForm) getParent()).getEntry();
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    entryController.delete(entry);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewException(e);
                }
            }
        };

        deleteButton.add(new JavascriptEventConfirmation("onclick", "Delete this Entry?"));
        form.add(deleteButton);
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
