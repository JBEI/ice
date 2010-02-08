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
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Feature;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.FastaParser;
import org.jbei.ice.lib.parsers.GenbankParser;
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

            AbstractParser parser = new GenbankParser();
            Sequence sequence = parser.parse(sequenceUser);

            if (sequence == null) {
                parser = new FastaParser();
                sequence = parser.parse(sequenceUser);

                if (sequence == null) {
                    error("Couldn't parse sequence file! Sequence should be either in FASTA or GenBank formats.");

                    return;
                }
            }

            sequence.setEntry(entry);

            try {
                Sequence currentSequence = entry.getSequence();
                entry.setSequence(null);

                SequenceManager.delete(currentSequence);

                if (sequence.getSequenceFeatures() != null) {
                    for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                        Feature feature = sequenceFeature.getFeature();

                        Feature saveResultFeature = SequenceManager.save(feature);

                        if (saveResultFeature != feature) {
                            sequenceFeature.setFeature(saveResultFeature);
                        }
                    }
                }

                entry.setSequence(sequence);
                EntryManager.save(entry);
            } catch (ManagerException e) {
                e.printStackTrace();

                return;
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

    public SequenceUpdateFormPanel(String id, SequenceViewPanel sequenceViewPanel, Entry entry) {
        super(id);

        this.entry = entry;
        this.sequenceViewPanel = sequenceViewPanel;

        SequenceUpdateForm sequenceForm = new SequenceUpdateForm("sequenceNewForm", entry
                .getSequence());

        add(sequenceForm);
        add(new FeedbackPanel("feedback"));
    }
}
