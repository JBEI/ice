package org.jbei.ice.lib.bulkupload;

import java.io.InputStream;
import java.util.ArrayList;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;

import org.apache.cxf.helpers.IOUtils;

/**
 * Handles sequence and attachment files added parts; specifically those that are uploaded via bulk upload
 *
 * @author Hector Plahar
 */
public class PartFileAdd {

    public static void uploadSequenceToEntry(long entryId, String userId, InputStream inputStream) throws Exception {
        Account account = ControllerFactory.getAccountController().getByEmail(userId);
        Entry entry = ControllerFactory.getEntryController().get(account, entryId);

        // associate with entry  TODO
//        boolean isStrainWithPlasmidPlasmid = (addType == EntryAddType.STRAIN_WITH_PLASMID
//                && type == EntryType.PLASMID);
//        if (isStrainWithPlasmidPlasmid && !entry.getLinkedEntries().isEmpty()) {
//            entry = (Entry) entry.getLinkedEntries().toArray()[0];
//        }

        String sequenceString = IOUtils.readStringFromStream(inputStream);
        ControllerFactory.getSequenceController().parseAndSaveSequence(account, entry, sequenceString);
    }

    public static void uploadAttachmentToEntry(long entryId, String userId, InputStream inputStream, String fileName)
            throws Exception {
        Account account = ControllerFactory.getAccountController().getByEmail(userId);
        Entry entry = ControllerFactory.getEntryController().get(account, entryId);

        AttachmentController attachmentController = ControllerFactory.getAttachmentController();
        ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
        if (attachments != null && !attachments.isEmpty()) {
            for (Attachment attachment : attachments) {
                try {
                    attachmentController.delete(account, attachment);
                } catch (PermissionException e) {
                    Logger.warn(e.getMessage());
                }
            }
        }

        Attachment attachment = new Attachment();
        attachment.setEntry(entry);
        attachment.setDescription("");
        attachment.setFileName(fileName);
        attachmentController.save(account, attachment, inputStream);
    }
}
