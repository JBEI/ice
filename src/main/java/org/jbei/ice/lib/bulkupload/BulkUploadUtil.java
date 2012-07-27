package org.jbei.ice.lib.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.server.EntryToInfoFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {

    public static BulkUploadInfo modelToInfo(AttachmentController attachmentController, BulkUpload model) {
        BulkUploadInfo info = new BulkUploadInfo();
        Account draftAccount = model.getAccount();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        info.setAccount(accountInfo);

        info.setId(model.getId());
        info.setLastUpdate(model.getLastUpdateTime());
        info.setCount(model.getContents().size());
        info.setType(EntryAddType.stringToType(model.getImportType()));
        info.setCreated(model.getCreationTime());
        info.setName(model.getName());

        // retrieve the entries associated with the bulk import
        for (Entry entry : model.getContents()) {
            try {
                ArrayList<Attachment> attachments = attachmentController.getByEntry(model.getAccount(), entry);
                SequenceController sequenceController = new SequenceController();
                boolean hasSequence = sequenceController.getByEntry(entry) != null;

                // convert to info object (no samples or trace sequences since bulk import does not have the ui for
                // it yet)
                EntryInfo entryInfo = EntryToInfoFactory.getInfo(model.getAccount(), entry, attachments, null, null,
                                                                 hasSequence);
                if (entryInfo != null)
                    info.getEntryList().add(entryInfo);
            } catch (ControllerException ce) {
                Logger.error(ce);
            }
        }

        return info;
    }
}
