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
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.panels.SequenceViewPanel;

public class SequenceNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SequenceViewPanel sequenceViewPanel;
    private Entry entry;

    class SequenceNewEditForm extends Form<Object> {
        private static final long serialVersionUID = 1L;

        private String sequenceUser;
        private FileUpload sequenceFileInput;

        public SequenceNewEditForm(String id) {
            super(id);
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

            add(new TextArea<String>("sequenceUser"));
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

            IDNASequence dnaSequence = sequenceController.parse(sequenceUser);

            if (dnaSequence == null) {
                error("Couldn't parse sequence file! Supported formats: "
                        + GeneralParser.getInstance().availableParsersToString() + ".");

                return;
            }

            Sequence sequence = null;

            try {
                sequence = sequenceController.dnaSequenceToSequence(dnaSequence);

                sequence.setSequenceUser(sequenceUser);
                sequence.setEntry(entry);

                sequenceController.save(sequence);
            } catch (ControllerException e) {
                throw new ViewException(e);
            } catch (PermissionException e) {
                throw new ViewPermissionException("No permissions to save sequence!", e);
            }

            sequenceViewPanel.updateView(sequence);
        }

        public String getSequenceUser() {
            return sequenceUser;
        }

        public FileUpload getSequenceFileInput() {
            return sequenceFileInput;
        }
    }

    public SequenceNewFormPanel(String id, SequenceViewPanel sequenceViewPanel, Entry entry) {
        super(id);

        this.entry = entry;
        this.sequenceViewPanel = sequenceViewPanel;

        SequenceNewEditForm sequenceForm = new SequenceNewEditForm("sequenceNewForm");

        add(sequenceForm);
        add(new FeedbackPanel("feedback"));
    }
}
