package org.jbei.ice.web.forms;

import java.util.ArrayList;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;
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

            add(new FileUploadField("traceFileInput").setLabel(new Model<String>("File")));

            add(new Button("saveTraceSequenceButton", new Model<String>("Save")));
        }

        @Override
        protected void onSubmit() {
            FileUpload fileUpload = getTraceSequenceFileInput();

            String traceSequence = "";

            if (fileUpload != null) {
                traceSequence = new String(fileUpload.getBytes());
            }

            Sequence sequence = GeneralParser.getInstance().parse(traceSequence);

            if (sequence == null) {
                error("Couldn't parse sequence file! Supported formats: Fasta");

                return;
            }

            if (entry.getSequence() == null) {
                return;
            }

            Blast blast = new Blast();
            String bl2seqAlignment = null;
            try {
                bl2seqAlignment = blast.runBl2Seq(entry.getSequence().getSequence(), sequence
                        .getSequence());

            } catch (BlastException e) {
                Logger.error("bl2seq failed on trace file submit", e);

                return;
            } catch (ProgramTookTooLongException e) {
                Logger.error("bl2seq failed on trace file submit", e);

                return;
            }

            ArrayList<Bl2SeqResult> bl2seqAlignments;
            try {
                bl2seqAlignments = Bl2SeqParser.parse(bl2seqAlignment);
            } catch (Bl2SeqException e) {
                Logger.error("bl2seq parser failed on trace file submit", e);

                return;
            }

            for (Bl2SeqResult bl2SeqResult : bl2seqAlignments) {
                System.out.println(bl2SeqResult.getScore());
            }
        }

        public FileUpload getTraceSequenceFileInput() {
            return traceFileInput;
        }
    }
}
