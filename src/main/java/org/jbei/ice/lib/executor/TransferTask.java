package org.jbei.ice.lib.executor;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteTransfer;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;

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

        Entries retriever = new Entries();
        List<Long> entries = retriever.getEntriesFromSelectionContext(account.getEmail(), entrySelection);
        Logger.info(userId + ": requesting transfer of " + entries.size() + " entries to " + remoteId);
        List<PartData> dataList = transfer.getPartsForTransfer(entries);
        transfer.transferEntries(remoteId, dataList);
    }
}
