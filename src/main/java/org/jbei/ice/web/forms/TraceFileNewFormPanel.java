package org.jbei.ice.web.forms;

import java.io.IOException;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.panels.SequenceAnalysisViewPanel;

public class TraceFileNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SequenceAnalysisViewPanel sequenceAnalysisViewPanel;
    private Entry entry;

    public TraceFileNewFormPanel(String id, SequenceAnalysisViewPanel sequenceAnalysisViewPanel,
            Entry entry) {
        super(id);

        this.entry = entry;
        this.sequenceAnalysisViewPanel = sequenceAnalysisViewPanel;

        TraceFileNewForm traceFileNewForm = new TraceFileNewForm("traceFileNewForm");

        add(traceFileNewForm);
        add(new FeedbackPanel("feedback"));
    }

    class TraceFileNewForm extends Form<Object> {
        private static final long serialVersionUID = 1L;

        private FileUpload traceFileInput;

        public TraceFileNewForm(String id) {
            super(id);

            this.setModel(new CompoundPropertyModel<Object>(this));

            setMultiPart(true);

            Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit() {
                    sequenceAnalysisViewPanel.removeForm();
                }
            };

            cancelButton.setDefaultFormProcessing(false);
            add(cancelButton);

            add(new FileUploadField("traceFileInput").setLabel(new Model<String>("File"))
                    .setRequired(true));

            add(new Button("saveTraceSequenceButton", new Model<String>("Save")));
        }

        @Override
        protected void onSubmit() {
            FileUpload fileUpload = getTraceSequenceFileInput();

            assert (fileUpload != null);

            String traceFileName = fileUpload.getClientFileName();

            SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                    IceSession.get().getAccount());

            IDNASequence dnaSequence = null;

            try {
                dnaSequence = sequenceAnalysisController.parse(fileUpload.getBytes());
            } catch (ControllerException e) {
                throw new ViewException(e);
            }

            if (dnaSequence == null || dnaSequence.getSequence() == null) {
                error("Couldn't parse sequence file! Supported formats: Fasta, GenBank, ABI");

                return;
            }

            try {
                sequenceAnalysisController.uploadTraceSequence(entry, traceFileName, IceSession
                        .get().getAccount().getEmail(), dnaSequence.getSequence().toLowerCase(),
                    fileUpload.getInputStream());

                sequenceAnalysisController.rebuildAllAlignments(entry);
            } catch (ControllerException e) {
                throw new ViewException(e);
            } catch (IOException e) {
                throw new ViewException(e);
            }

            setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()
                    + ",1=seqanalysis"));
        }

        public FileUpload getTraceSequenceFileInput() {
            return traceFileInput;
        }
    }
}
