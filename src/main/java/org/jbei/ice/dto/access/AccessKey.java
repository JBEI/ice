package org.jbei.ice.dto.access;

import org.jbei.ice.account.Account;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class AccessKey implements IDataTransferModel {

    private long id;
    private Account account;
    private String clientId;
    private String secret;
    private String token;
    private long creationTime;
    private boolean allowDelegate;
    private boolean readOnly;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
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

    public void setAllowDelegate(boolean allowDelegate) {
        this.allowDelegate = allowDelegate;
    }

    public boolean isAllowDelegate() {
        return this.allowDelegate;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
