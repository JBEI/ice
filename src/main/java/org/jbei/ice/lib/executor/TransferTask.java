package org.jbei.ice.lib.executor;

import java.util.ArrayList;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.net.RemoteTransfer;

/**
 * Task for transferring parts to a another registry
 *
 * @author Hector Plahar
 */
public class TransferTask extends Task {

    private final String userId;
    private final long remoteId;
    private final ArrayList<Long> entries;

    public TransferTask(String userId, long remoteId, ArrayList<Long> entries) {
        this.userId = userId;
        this.remoteId = remoteId;
        this.entries = new ArrayList<>(entries);
    }

    public void execute() {
        RemoteTransfer transfer = new RemoteTransfer();
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (account.getType() != AccountType.ADMIN)
            return;

        Logger.info(userId + ": requesting transfer of " + entries.size() + " entries to " + remoteId);
        transfer.transferEntries(remoteId, entries);
    }
}
