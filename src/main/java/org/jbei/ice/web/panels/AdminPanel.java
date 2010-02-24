package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class AdminPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public AdminPanel(String id) {
        super(id);

        class EditEntryForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String entryId;

            public EditEntryForm(String id) {
                super(id);
                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("entryId"));
            }

            @Override
            public void onSubmit() {
                // handled by ajaxbutton
            }

            @SuppressWarnings("unused")
            public void setEntryId(String entryId) {
                this.entryId = entryId;
            }

            public String getEntryId() {
                return entryId;
            }
        }

        AjaxButton editButton = new AjaxButton("editButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                EditEntryForm parentForm = (EditEntryForm) getParent();
                String entryId = parentForm.getEntryId();
                Entry entry = null;
                try {
                    entry = EntryManager.get(Integer.parseInt(entryId));
                } catch (NumberFormatException e) {
                    // It's ok
                } catch (ManagerException e) {
                    // It's ok
                }
                if (entry == null) {
                    Panel answer = new EmptyMessagePanel("editPanel", "invalid entryId");
                    answer.setOutputMarkupId(true);
                    getParent().getParent().replace(answer);
                    target.addComponent(answer);
                } else {
                    Panel answer = null;
                    if (entry instanceof Plasmid) {
                        answer = new PlasmidUpdateFormPanel("editPanel", (Plasmid) entry);
                    } else if (entry instanceof Strain) {
                        answer = new StrainUpdateFormPanel("editPanel", (Strain) entry);
                    } else if (entry instanceof Part) {
                        answer = new PartUpdateFormPanel("editPanel", (Part) entry);
                    }

                    if (answer != null) {
                        answer.setOutputMarkupId(true);
                        getParent().getParent().replace(answer);
                        target.addComponent(answer);
                    }
                }

            }
        };
        EditEntryForm editForm = new EditEntryForm("editForm");
        editForm.add(editButton);

        add(editForm);
        add(new EmptyMessagePanel("editPanel", "").setOutputMarkupId(true));

    }
}
