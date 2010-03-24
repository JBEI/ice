package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.panels.SequenceViewPanel;

public class SequenceUpdateFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SequenceViewPanel sequenceViewPanel;
    private Entry entry;

    class SequenceUpdateForm extends Form<Object> {
        private static final long serialVersionUID = 1L;

        private String sequenceUser;
        private FileUpload sequenceFileInput;

        public SequenceUpdateForm(String id, Sequence sequence) {
            super(id);

            sequenceUser = sequence.getSequenceUser();

            this.setModel(new CompoundPropertyModel<Object>(this));

            setMultiPart(true);

            Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    sequenceViewPanel.clearForm();
                }
            };

            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);

            add(new TextArea<String>("sequenceUser",
                    new PropertyModel<String>(this, "sequenceUser")));
            add(new Button("saveSequenceButton", new Model<String>("Save")));
            add(new FileUploadField("sequenceFileInput").setLabel(new Model<String>("File")));
        }

        @Override
        protected void onSubmit() {
            FileUpload fileUpload = getSequenceFileInput();

            if (!(fileUpload != null || (sequenceUser != null && !sequenceUser.trim().isEmpty()))) {
                error("Please provide either File or paste Sequence!");

                return;
            }

            if (fileUpload != null && sequenceUser != null && !sequenceUser.trim().isEmpty()) {
                error("Please provide either File or paste Sequence! Not both!");

                return;
            }

            if (fileUpload != null) {
                sequenceUser = new String(fileUpload.getBytes());
            }

            SequenceController sequenceController = new SequenceController(IceSession.get()
                    .getAccount());

            Sequence newSequence = sequenceController.parse(sequenceUser);

            if (newSequence == null) {
                error("Couldn't parse sequence file! Supported formats: "
                        + GeneralParser.getInstance().availableParsersToString() + ".");
                error("If you are using ApE, try opening and re-saving using a recent version.");

                return;
            }

            try {
                newSequence.setEntry(entry);

                sequenceController.update(newSequence);
            } catch (ControllerException e) {
                throw new ViewException(e);
            } catch (PermissionException e) {
                throw new ViewPermissionException("No permissions to update sequence!", e);
            }

            sequenceViewPanel.updateView(newSequence);
        }

        public String getSequenceUser() {
            return sequenceUser;
        }

        public FileUpload getSequenceFileInput() {
            return sequenceFileInput;
        }
    }

    public SequenceUpdateFormPanel(String id, SequenceViewPanel sequenceViewPanel, Entry entry) {
        super(id);

        this.entry = entry;
        this.sequenceViewPanel = sequenceViewPanel;

        SequenceController sequenceController = new SequenceController(IceSession.get()
                .getAccount());

        SequenceUpdateForm sequenceForm;
        try {
            sequenceForm = new SequenceUpdateForm("sequenceNewForm", sequenceController
                    .getByEntry(entry));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        add(sequenceForm);
        add(new FeedbackPanel("feedback"));
    }
}
