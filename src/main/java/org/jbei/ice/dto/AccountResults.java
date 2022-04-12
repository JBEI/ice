package org.jbei.ice.dto;

import org.jbei.ice.account.Account;
import org.jbei.ice.storage.IDataTransferModel;

import java.util.LinkedList;

/**
 * Wrapper around a list of user accounts
 *
 * @author Hector Plahar
 */
public class AccountResults implements IDataTransferModel {

    private long resultCount;
    private final LinkedList<Account> users;

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

    public LinkedList<Account> getResults() {
        return this.users;
    }
}

