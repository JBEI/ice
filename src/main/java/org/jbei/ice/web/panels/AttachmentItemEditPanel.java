package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.utils.Base64String;
import org.jbei.ice.web.pages.EntryViewPage;

public class AttachmentItemEditPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private Attachment attachment = null;

    public AttachmentItemEditPanel(String id, Attachment passedAttachment) {
        super(id);
        attachment = passedAttachment;

        class AttachmentEditForm extends Form<Object> {

            private static final long serialVersionUID = 1L;

            private String description;
            private FileUpload fileInput;

            // private File fileInput;
            // private String fileName;

            public AttachmentEditForm(String id) {
                super(id);
                this.setModel(new CompoundPropertyModel<Object>(this));

                // Always needed for upload forms
                setMultiPart(true);

                Button cancelButton = new Button("cancelButton", new Model<String>("Cancel")) {
                    private static final long serialVersionUID = 1L;

                    public void onSubmit() {
                        setRedirect(true);
                        setResponsePage(EntryViewPage.class, new PageParameters("0="
                                + attachment.getEntry().getId() + ",1=attachments"));
                    }
                };

                cancelButton.setDefaultFormProcessing(false);
                add(cancelButton);
                add(new TextArea<String>("description"));
                add(new Button("saveAttachmentButton", new Model<String>("Save")));
                add(new FileUploadField("fileInput").setRequired(true).setLabel(
                        new Model<String>("File")));
            }

            protected void onSubmit() {
                FileUpload fileUpload = getFileInput();
                AttachmentItemEditPanel thisPanel = (AttachmentItemEditPanel) getParent();
                Attachment attachment = thisPanel.getAttachment();
                attachment.setDescription((getDescription() != null) ? getDescription() : "");
                attachment.setFileName(fileUpload.getClientFileName());
                Base64String data = new Base64String();
                data.putBytes(fileUpload.getBytes());
                attachment.setData(data);

                try {
                    AttachmentManager.create(attachment);
                    setRedirect(true);
                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + attachment.getEntry().getId() + ",1=attachments"));
                } catch (Exception e) {
                    e.printStackTrace();
                    error("Could not upload file: " + e.toString());
                }
            }

            @SuppressWarnings("unused")
            public void setDescription(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }

            @SuppressWarnings("unused")
            public void setFileInput(FileUpload fileInput) {
                this.fileInput = fileInput;
            }

            public FileUpload getFileInput() {
                return fileInput;
            }
        }

        add(new AttachmentEditForm("attachmentEditForm"));
        add(new FeedbackPanel("feedback"));
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public Attachment getAttachment() {
        return attachment;
    }

}
