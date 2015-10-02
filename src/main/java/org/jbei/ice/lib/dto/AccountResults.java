package org.jbei.ice.lib.dto;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;

/**
 * Wrapper around a list of user accounts
 *
 * @author Hector Plahar
 */
public class AccountResults implements IDataTransferModel {

    private long resultCount;
    private LinkedList<AccountTransfer> users;

    public AccountResults() {
        users = new LinkedList<>();
    }

    public void setResultCount(long count) {
        this.resultCount = count;
    }

    /**
     * @return total query result count. not just the count of results returned
     */
    public long getResultCount() {
        return this.resultCount;
    }

    public LinkedList<AccountTransfer> getResults() {
        return this.users;
    }
}

