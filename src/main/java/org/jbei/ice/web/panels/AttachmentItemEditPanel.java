package org.jbei.ice.web.panels;

import java.io.IOException;

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
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
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

                AttachmentController attachmentController = new AttachmentController(IceSession
                        .get().getAccount());

                try {
                    attachmentController.save(attachment, fileUpload.getInputStream());

                    setRedirect(true);

                    setResponsePage(EntryViewPage.class, new PageParameters("0="
                            + attachment.getEntry().getId() + ",1=attachments"));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (IOException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permissions to save attachment!", e);
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
