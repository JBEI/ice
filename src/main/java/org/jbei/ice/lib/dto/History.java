package org.jbei.ice.lib.dto;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class History implements IDataTransferModel {

    private long id;
    private String action;
    private String userId;
    private boolean localUser;
    private AccountTransfer account;
    private long time;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLocalUser() {
        return localUser;
    }

    public void setLocalUser(boolean localUser) {
        this.localUser = localUser;
    }

    public AccountTransfer getAccount() {
        return account;
    }

    public void setAccount(AccountTransfer account) {
        this.account = account;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
