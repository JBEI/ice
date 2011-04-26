package org.jbei.ice.web.panels;

import java.io.File;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MarkupAttachmentsPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private class AttachmentsUploadForm extends StatelessForm<Object> {
        private static final long serialVersionUID = 1L;

        private FileUploadField markupFile;
        private String markupFileName;

        public AttachmentsUploadForm(String name) {
            super(name);

            setMultiPart(true);

            add(markupFile = new FileUploadField("markupFile"));
            add(new TextField<String>("markupFileName", new PropertyModel<String>(this,
                    "markupFileName")));

            add(new AjaxButton("ajaxSubmit") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    System.out.print("asdasd");
                }
            });

            //setMaxSize(Bytes.kilobytes(100));
        }

        @Override
        protected void onSubmit() {
            final FileUpload upload = markupFile.getFileUpload();
            if (upload != null) {
                File newFile = new File("", upload.getClientFileName());

                try {
                    // Save to new file
                    newFile.createNewFile();
                    upload.writeTo(newFile);
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to write file");
                }
            }
        }

        @SuppressWarnings("unused")
        public String getMarkupFileName() {
            return markupFileName;
        }

        @SuppressWarnings("unused")
        public void setMarkupFileName(String markupFileName) {
            this.markupFileName = markupFileName;
        }
    }

    private AttachmentsUploadForm attachmentsUploadForm;

    public MarkupAttachmentsPanel(String id) {
        super(id);

        attachmentsUploadForm = new AttachmentsUploadForm("attachmentsUploadForm");
        add(attachmentsUploadForm);

        final FeedbackPanel attachmentsUploadFeedback = new FeedbackPanel(
                "attachmentsUploadFeedback");
        add(attachmentsUploadFeedback);
    }
}