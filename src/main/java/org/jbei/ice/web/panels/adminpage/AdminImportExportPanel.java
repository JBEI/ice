package org.jbei.ice.web.panels.adminpage;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.xerces.impl.dv.util.Base64;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.IceXmlSerializer.AttachmentData;
import org.jbei.ice.lib.utils.IceXmlSerializer.CompleteEntry;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.DownloadPage;

import edu.emory.mathcs.backport.java.util.Arrays;

public class AdminImportExportPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private ImportExportForm importExportForm = null;
    private Panel selectionPanel = null;

    class ImportExportForm extends Form<Object> {
        private static final long serialVersionUID = 1L;

        private String exportList = null;
        private String importXml = null;
        private FileUpload importXmlFile = null;
        private String ownerEmail = null;
        private String ownerName = null;
        private boolean applyDefaultPermission = false;

        public ImportExportForm(String id) {
            super(id);
            setModel(new CompoundPropertyModel<Object>(this));
            setMultiPart(true);

            add(new FeedbackPanel("feedback"));

            // export fields
            add(new TextArea<String>("exportList"));
            add(new AjaxButton("submitExportListButton") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ImportExportForm parentForm = (ImportExportForm) form;
                    target.addComponent(parentForm);
                    Page page = parentForm.processExportList();
                    if (page != null) {
                        setResponsePage(page);
                    }

                }

            });

            // import fields
            add(new TextArea<String>("importXml"));
            add(new FileUploadField("importXmlFile"));
            add(new TextArea<String>("ownerEmail"));
            add(new TextArea<String>("ownerName"));
            add(new CheckBox("applyDefaultPermission"));
            add(new AjaxButton("submitImportXmlButton") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ImportExportForm parentForm = (ImportExportForm) form;
                    target.addComponent(parentForm);
                    parentForm.processImportXml();
                }

            });
            selectionPanel = new EmptyPanel("selectionPanel");
            selectionPanel.setOutputMarkupId(true);
            add(selectionPanel);
        }

        @Override
        protected void onSubmit() {
            // Ajaxbuttons handle the submission
        }

        @SuppressWarnings("unchecked")
        public Page processExportList() {
            String exportList = getExportList();
            ArrayList<String> startingSet = new ArrayList<String>();
            String[] commaSepList = exportList.split(",");
            for (String item : commaSepList) {
                String[] whitespaceSepList = item.trim().split("\\s+");
                startingSet.addAll(Arrays.asList(whitespaceSepList));
            }

            ArrayList<String> entryRecordIds = new ArrayList<String>();
            ArrayList<String> failedIds = new ArrayList<String>();
            for (String identifier : startingSet) {
                System.out.println(identifier);
                Entry item = null;

                try {
                    item = EntryManager.getByRecordId(identifier);
                    if (item == null) {
                        item = EntryManager.getByPartNumber(identifier);
                    }
                    if (item == null) {
                        try {
                            item = EntryManager.get(Long.parseLong(identifier));
                        } catch (NumberFormatException e) {
                            // pass
                        }
                    }
                    if (item == null) { // failed ones go here.
                        failedIds.add(identifier);
                    }
                    if (item != null) { //success go here.
                        entryRecordIds.add(item.getRecordId());
                        item = null;
                    }

                } catch (ManagerException e) {
                    failedIds.add(identifier);
                }
            }

            if (failedIds.size() != 0) {
                importExportForm.error("Following identifiers were not recognized: " + failedIds);
                return null;
            } else {
                ArrayList<Entry> entries = new ArrayList<Entry>();

                for (String recordId : entryRecordIds) {
                    try {
                        entries.add(EntryManager.getByRecordId(recordId));

                    } catch (ManagerException e) {
                        error("Exception retrieving " + recordId);
                    }
                }
                String xmlString;
                try {
                    xmlString = IceXmlSerializer.serializeToJbeiXml(entries);
                } catch (UtilityException e) {
                    throw new ViewException(e);
                }
                return new DownloadPage("ice.xml", "text/xml", xmlString);
            }
        }

        public Page processImportXml() {
            IceXmlSerializer iceXmlSerializer = new IceXmlSerializer();
            List<CompleteEntry> completeEntries = null;
            try {
                String xmlString = null;
                if (getImportXmlFile() != null) {
                    xmlString = new String(getImportXmlFile().getBytes());
                } else {
                    xmlString = getImportXml();
                }
                if (xmlString != null) {
                    completeEntries = iceXmlSerializer.deserializeJbeiXml(xmlString);
                } else {
                    error("Please upload a file or paste in the xml content");
                }
            } catch (UtilityException e) {
                error("Exception parsing xml.");
            }

            /* TODO: actual checking implementation.
            ArrayList<CompleteEntry> existingEntries = new ArrayList<CompleteEntry>();
            ArrayList<CompleteEntry> partNumberConflicts = new ArrayList<CompleteEntry>();

            for (CompleteEntry completeEntry : completeEntries) {
                try {
                    Entry temp = null;
                    temp = EntryManager.getByRecordId(completeEntry.getEntry().getRecordId());
                    if (temp != null) {
                        existingEntries.add(completeEntry);
                        completeEntries.remove(completeEntry);
                        continue;
                    }
                    for (PartNumber partNumber : completeEntry.getEntry().getPartNumbers()) {
                        temp = EntryManager.getByPartNumber(partNumber.getPartNumber());
                        if (temp != null) {
                            partNumberConflicts.add(completeEntry);
                            completeEntries.remove(completeEntry);
                            break;
                        }
                    }
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }
            }
            */

            // new owner, if specified
            if (getOwnerEmail() != null && getOwnerEmail().length() != 0) {
                //pass
            } else {
                for (CompleteEntry completeEntry : completeEntries) {
                    completeEntry.getEntry().setOwnerEmail(getOwnerEmail().trim());
                    completeEntry.getEntry().setOwner(getOwnerName().trim());
                }
            }

            EntryController entryController = new EntryController(IceSession.get().getAccount());
            SequenceController sequenceController = new SequenceController(IceSession.get()
                    .getAccount());
            AttachmentController attachmentController = new AttachmentController(IceSession.get()
                    .getAccount());

            for (CompleteEntry completeEntry : completeEntries) {
                Entry newEntry = null;
                Logger.debug("saving " + completeEntry.getEntry().getRecordId());
                try {
                    newEntry = entryController.createEntry(completeEntry.getEntry(), true, true);
                } catch (ControllerException e) {
                    error("Controller error saving " + completeEntry.getEntry().getRecordId());
                }
                if (newEntry != null) {
                    // set permissions
                    if (isApplyDefaultPermission()) {

                    }
                    // add sequence
                    if (completeEntry.getSequence() != null) {
                        completeEntry.getSequence().setEntry(newEntry);
                        try {
                            sequenceController.save(completeEntry.getSequence());
                        } catch (ControllerException e) {
                            error("Controller error saving sequence  for " + newEntry.getRecordId());
                        } catch (PermissionException e) {
                            error("Permission error saving sequence  for " + newEntry.getRecordId());
                        }
                    }

                    // add attachments
                    for (AttachmentData attachmentData : completeEntry.getAttachments()) {
                        Attachment attachment = new Attachment();
                        attachment.setDescription(attachmentData.getDescription());
                        attachment.setFileId(attachmentData.getFileId());
                        attachment.setFileName(attachmentData.getFileId());
                        attachment.setEntry(newEntry);
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                                Base64.decode(attachmentData.getBase64Data()));
                        try {
                            attachmentController.save(attachment, inputStream);
                        } catch (ControllerException e) {
                            error("Controller error saving attachment for "
                                    + newEntry.getRecordId());
                        } catch (PermissionException e) {
                            error("Permission Error saving attachment for "
                                    + newEntry.getRecordId());
                        }
                    }
                }

            }
            return null;
        }

        public String getExportList() {
            return exportList;
        }

        public String getImportXml() {
            return importXml;
        }

        public FileUpload getImportXmlFile() {
            return importXmlFile;
        }

        public String getOwnerEmail() {
            return ownerEmail;
        }

        public void setOwnerEmail(String ownerEmail) {
            this.ownerEmail = ownerEmail;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public boolean isApplyDefaultPermission() {
            return applyDefaultPermission;
        }

        public void setApplyDefaultPermission(boolean applyDefaultPermission) {
            this.applyDefaultPermission = applyDefaultPermission;
        }
    }

    public AdminImportExportPanel(String id) {
        super(id);
        importExportForm = new ImportExportForm("importExportForm");
        add(importExportForm);

    }

}
