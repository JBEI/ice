package org.jbei.ice.web.forms;

import java.util.ArrayList;
import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.BlastController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.TraceSequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.models.TraceSequenceAlignment;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
import org.jbei.ice.web.IceSession;
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

            String uploadedTraceSequence = new String(fileUpload.getBytes());
            String traceFileName = fileUpload.getClientFileName();

            Sequence sequence = GeneralParser.getInstance().parse(uploadedTraceSequence);

            if (sequence == null || sequence.getSequence() == null) {
                error("Couldn't parse sequence file! Supported formats: Fasta");

                return;
            }

            TraceSequence traceSequence = new TraceSequence(entry, traceFileName, IceSession.get()
                    .getAccount().getEmail(), sequence.getSequence(), uploadedTraceSequence, null,
                    new Date());

            try {
                TraceSequenceManager.create(traceSequence);
            } catch (ManagerException e) {
                Logger.error("Failed to create TraceSequence", e);

                error("Failed to create TraceSequence");

                return;
            }

            if (entry.getSequence() == null) {
                setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()
                        + ",1=seqanalysis"));

                return;
            }

            ArrayList<Bl2SeqResult> bl2seqAlignments = null;
            try {
                bl2seqAlignments = BlastController.alignSequencesAndParse(entry.getSequence()
                        .getSequence(), sequence.getSequence());
            } catch (ProgramTookTooLongException e) {
                Logger.error("Prgoram took to long to align and parse sequences", e);
            }

            if (bl2seqAlignments != null && bl2seqAlignments.size() > 0) {
                Bl2SeqResult maxBl2SeqResult = null;
                int maxScore = -1;
                for (Bl2SeqResult bl2SeqResult : bl2seqAlignments) {
                    if (bl2SeqResult.getScore() > maxScore) {
                        maxScore = bl2SeqResult.getScore();
                        maxBl2SeqResult = bl2SeqResult;
                    }
                }

                TraceSequenceAlignment traceSequenceAlignment = new TraceSequenceAlignment(
                        traceSequence, maxBl2SeqResult.getScore(), "1,1,1,1,1,1,1,0,0,0,0,0",
                        maxBl2SeqResult.getQueryStart(), maxBl2SeqResult.getQueryEnd(),
                        maxBl2SeqResult.getSubjectStart(), maxBl2SeqResult.getSubjectEnd(),
                        maxBl2SeqResult.getQuerySequence(), maxBl2SeqResult.getSubjectSequence(),
                        new Date());

                try {
                    TraceSequenceManager.saveAlignment(traceSequenceAlignment);
                    traceSequence.setAlignment(traceSequenceAlignment);
                    TraceSequenceManager.save(traceSequence);
                } catch (ManagerException e) {
                    Logger.error("Failed to Save TraceSequenceAlignment", e);
                }
            }

            setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()
                    + ",1=seqanalysis"));
        }

        public FileUpload getTraceSequenceFileInput() {
            return traceFileInput;
        }
    }
}
