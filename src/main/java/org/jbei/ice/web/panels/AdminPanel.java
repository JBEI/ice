package org.jbei.ice.web.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Job;
import org.jbei.ice.lib.utils.JobCue;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
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
                EntryController entryController = new EntryController(IceSession.get().getAccount());

                EditEntryForm parentForm = (EditEntryForm) getParent();
                String entryId = parentForm.getEntryId();
                Entry entry = null;

                try {
                    entry = entryController.getByIdentifier(entryId);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    // This should never happen. Because user is admin/moderator
                    throw new ViewPermissionException("No permission to view entry!", e);
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

        AjaxButton rebuildBlastButton = new AjaxButton("rebuildBlastButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                JobCue.getInstance().addJob(Job.REBUILD_BLAST_INDEX);
                JobCue.getInstance().processIn(5000L);
            }
        };
        AjaxButton rebuildSearchButton = new AjaxButton("rebuildSearchButton") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                JobCue.getInstance().addJob(Job.REBUILD_SEARCH_INDEX);
                JobCue.getInstance().processIn(5000L);
            }
        };

        EditEntryForm editForm = new EditEntryForm("editForm");
        editForm.add(editButton);
        editForm.add(rebuildBlastButton);
        editForm.add(rebuildSearchButton);

        add(editForm);
        add(new EmptyMessagePanel("editPanel", "").setOutputMarkupId(true));

    }
}
