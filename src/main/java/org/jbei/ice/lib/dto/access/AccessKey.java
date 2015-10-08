package org.jbei.ice.lib.dto.access;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class AccessKey implements IDataTransferModel {

    private AccountTransfer account;
    private String clientId;
    private String secret;
    private long creationTime;

    public AccountTransfer getAccount() {
        return account;
    }

    public void setAccount(AccountTransfer account) {
        this.account = account;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
