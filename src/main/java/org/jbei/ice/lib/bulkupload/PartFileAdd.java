package org.jbei.ice.lib.bulkupload;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Attachment;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Handles sequence and attachment files added parts; specifically those that are uploaded via bulk upload
 *
 * @author Hector Plahar
 */
public class PartFileAdd {

    public static void uploadSequenceToEntry(long entryId, String userId, InputStream inputStream, boolean getLinkEntry)
            throws Exception {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);

        // associate with entry
        if (getLinkEntry) {
            if (entry.getLinkedEntries().isEmpty())
                throw new Exception("Could not retrieve associated part");
            entry = (Entry) entry.getLinkedEntries().toArray()[0];
        }

        String sequenceString = IOUtils.toString(inputStream);
        new SequenceController().parseAndSaveSequence(userId, entry.getId(), sequenceString);
    }

    public static void uploadSequenceToEntry(long entryId, String userId, DNASequence dnaSequence,
            String sequenceUser) throws Exception {
        Entry entry = DAOFactory.getEntryDAO().get(entryId);

        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(entry);
        if (sequenceUser != null)
            sequence.setSequenceUser(sequenceUser);
        new SequenceController().save(userId, sequence);
    }

    public static void uploadAttachmentToEntry(long entryId, String userId, InputStream inputStream, String fileName,
            boolean getLinkEntry) throws Exception {
        Account account = new AccountController().getByEmail(userId);
        Entry entry = DAOFactory.getEntryDAO().get(entryId);

        // associate with entry
        if (getLinkEntry) {
            if (entry.getLinkedEntries().isEmpty())
                throw new Exception("Could not retrieve associated part");
            entry = (Entry) entry.getLinkedEntries().toArray()[0];
        }

        AttachmentController attachmentController = new AttachmentController();
        ArrayList<Attachment> attachments = attachmentController.getByEntry(account.getEmail(), entry);
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
