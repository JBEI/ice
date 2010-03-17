package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.pages.EntryViewPage;

public class SampleItemEditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Sample sample = null;

    public SampleItemEditPanel(String id, Sample passedSample, boolean isEditAction) {
        super(id);

        sample = passedSample;

        class SampleEditForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;

            private String label;
            private String depositor;
            private String notes;

            public SampleEditForm(String id, boolean isEditAction) {
                super(id);

                setLabel(sample.getLabel());
                if (isEditAction) {
                    if (sample.getDepositor() == null || sample.getDepositor().isEmpty()) {
                        setDepositor(sample.getEntry().getOwnerEmail());
                    } else {
                        setDepositor(sample.getDepositor());
                    }
                } else {
                    setDepositor(IceSession.get().getAccount().getEmail());
                }
                setNotes(sample.getNotes());

                setModel(new CompoundPropertyModel<Object>(this));

                Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit() {
                        setRedirect(true);
                        setResponsePage(EntryViewPage.class, new PageParameters("0="
                                + sample.getEntry().getId() + ",1=samples"));
                    }
                };

                cancelButton.setDefaultFormProcessing(false);
                add(cancelButton);

                add(new TextField<String>("label").setRequired(true).setLabel(
                        new Model<String>("Label")));
                add(new TextField<String>("depositor").setRequired(true).setLabel(
                        new Model<String>("Depositor")));
                add(new TextArea<String>("notes"));
                add(new Button("saveSampleButton", new Model<String>("Save")));
            }

            @Override
            protected void onSubmit() {
                SampleItemEditPanel sampleItemEditPanel = (SampleItemEditPanel) getParent();
                Sample sample = sampleItemEditPanel.getSample();
                sample.setLabel(getLabel());
                sample.setDepositor(getDepositor());
                sample.setNotes(getNotes());

                SampleController sampleController = new SampleController(IceSession.get()
                        .getAccount());

                try {
                    sampleController.saveSample(sampleItemEditPanel.getSample());

                    setRedirect(true);
                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + sample.getEntry().getId() + ",1=samples"));
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to save sample!", e);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getDepositor() {
                return depositor;
            }

            public void setDepositor(String depositor) {
                this.depositor = depositor;
            }

            public String getNotes() {
                return notes;
            }

            public void setNotes(String notes) {
                this.notes = notes;
            }

        }
        add(new SampleEditForm("sampleEditForm", isEditAction));
        add(new FeedbackPanel("feedback"));
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Sample getSample() {
        return sample;
    }
}
