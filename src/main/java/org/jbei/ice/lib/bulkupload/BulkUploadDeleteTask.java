package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.model.BulkUpload;
import org.jbei.ice.storage.model.Entry;


/**
 * Task to delete bulk uploads with a status of draft and their contents.
 *
 * @author Hector Plahar
 */
public class BulkUploadDeleteTask extends Task {

    private final long bulkUploadId;
    private final String userId;

    public BulkUploadDeleteTask(final String userId, final long id) {
        this.bulkUploadId = id;
        this.userId = userId;
    }

    @Override
    public void execute() {
        BulkUploadDAO dao = DAOFactory.getBulkUploadDAO();

        BulkUpload upload = dao.get(bulkUploadId);
        if (upload == null) {
            Logger.error("Could not locate bulk upload " + bulkUploadId + " for deletion");
            return;
        }

        BulkUploadAuthorization authorization = new BulkUploadAuthorization();
        authorization.expectWrite(userId, upload);

        if (upload.getStatus() != BulkUploadStatus.IN_PROGRESS)
            return;

        // delete all associated entries that have a status of draft
        for (Entry entry : upload.getContents()) {
            for (Entry linkedEntry : entry.getLinkedEntries()) {
                if (linkedEntry.getVisibility() != Visibility.DRAFT.getValue())
                    continue;

                DAOFactory.getEntryDAO().fullDelete(linkedEntry);
//                entryController.fullDelete(userId, linkedEntry.getId());
            }

            if (entry.getVisibility() == Visibility.DRAFT.getValue()) {
                DAOFactory.getEntryDAO().fullDelete(entry);
//                entryController.fullDelete(userId, entry.getId());
            }
        }

        dao.delete(upload);
    }
}
