package org.jbei.ice.web.forms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.panels.SequenceAnalysisViewPanel;

public class TraceFileNewFormPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private SequenceAnalysisViewPanel sequenceAnalysisViewPanel;
    private Entry entry;

    private class ByteHolder implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] bytes = null;
        private String name = null;

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

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

            setModel(new CompoundPropertyModel<Object>(this));

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

            String uploadFileName = fileUpload.getClientFileName();
            SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                    IceSession.get().getAccount());

            IDNASequence dnaSequence = null;

            ArrayList<ByteHolder> byteHolders = new ArrayList<ByteHolder>();

            if ((uploadFileName.endsWith(".zip")) || uploadFileName.endsWith(".ZIP")) {
                try {
                    ZipInputStream zis = new ZipInputStream(fileUpload.getInputStream());
                    ZipEntry zipEntry = null;

                    while (true) {
                        zipEntry = zis.getNextEntry();

                        if (zipEntry != null) {

                            if (!zipEntry.isDirectory()
                                    && !zipEntry.getName().startsWith("__MACOSX")) {

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                int c;
                                while ((c = zis.read()) != -1) {
                                    byteArrayOutputStream.write(c);
                                }
                                ByteHolder byteHolder = new ByteHolder();
                                byteHolder.setBytes(byteArrayOutputStream.toByteArray());
                                byteHolder.setName(zipEntry.getName());
                                byteHolders.add(byteHolder);
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    error("Could not parse zip file.");
                }
            } else {
                ByteHolder byteHolder = new ByteHolder();
                byteHolder.setBytes(fileUpload.getBytes());
                byteHolder.setName(fileUpload.getClientFileName());
                byteHolders.add(byteHolder);
            }
            String currentFileName = "";
            try {
                for (ByteHolder byteHolder : byteHolders) {
                    currentFileName = byteHolder.getName();
                    dnaSequence = sequenceAnalysisController.parse(byteHolder.getBytes());
                    if (dnaSequence == null || dnaSequence.getSequence() == null) {
                        error("Could not parse file: " + currentFileName
                                + ". Only Fasta, GenBank, or ABI files are supported.");
                        return;
                    }
                    sequenceAnalysisController.uploadTraceSequence(entry, byteHolder.getName(),
                        IceSession.get().getAccount().getEmail(), dnaSequence.getSequence()
                                .toLowerCase(), new ByteArrayInputStream(byteHolder.getBytes()));
                }
                sequenceAnalysisController.rebuildAllAlignments(entry);
            } catch (ControllerException e) { // 
                error("Could not parse file: " + currentFileName
                        + ". Only Fasta, GenBank, or ABI files are supported.");
            }

            setResponsePage(EntryViewPage.class, new PageParameters("0=" + entry.getId()
                    + ",1=seqanalysis"));
        }

        public FileUpload getTraceSequenceFileInput() {
            return traceFileInput;
        }
    }
}
