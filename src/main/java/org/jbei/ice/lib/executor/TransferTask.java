package org.jbei.ice.lib.executor;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteTransfer;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;

import java.util.List;

/**
 * Task for transferring parts to a another registry
 *
 * @author Hector Plahar
 */
public class TransferTask extends Task {

    private final String userId;
    private final long remoteId;
    private final EntrySelection entrySelection;

    public TransferTask(String userId, long remoteId, EntrySelection entrySelection) {
        this.userId = userId;
        this.remoteId = remoteId;
        this.entrySelection = entrySelection;
    }

    public void execute() {
        RemoteTransfer transfer = new RemoteTransfer();
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account.getType() != AccountType.ADMIN)
            return;

        Entries retriever = new Entries(account.getEmail());
        List<Long> entries = retriever.getEntriesFromSelectionContext(entrySelection);
        Logger.info(userId + ": requesting transfer to " + remoteId);
        List<PartData> dataList = transfer.getPartsForTransfer(entries);
        List<Long> remoteIds = transfer.transferEntries(remoteId, dataList);

        // check folder
        if (StringUtils.isEmpty(this.entrySelection.getFolderId()))
            return;

        // create remoteFolder
        long folderId = Long.decode(this.entrySelection.getFolderId());
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        Logger.info("Adding " + remoteIds.size() + " transferred entries to remote folder");
        transfer.transferFolder(remoteId, folder.toDataTransferObject(), remoteIds);
    }
}
