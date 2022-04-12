package org.jbei.ice.storage.model;

import org.hibernate.annotations.GenericGenerator;
import org.jbei.ice.access.AccessStatus;
import org.jbei.ice.account.Account;
import org.jbei.ice.dto.access.AccessKey;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Hector Plahar
 */
@Entity
@Table(name = "APIKey")
public class ApiKey implements DataModel {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private long id;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "expires_on")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "secret", unique = true)
    private String secret;

    @Column(name = "hashed_token")
    private String hashedToken;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private AccessStatus status;

    @Column(name = "allow_delegate")
    private Boolean allowDelegate;          // allow other user id actions. this is only applicable if user is admin

    @Column(name = "read_only")
    private Boolean readOnly;

    public long getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getHashedToken() {
        return hashedToken;
    }

    public void setHashedToken(String hashedToken) {
        this.hashedToken = hashedToken;
    }

    public AccessStatus getStatus() {
        return status;
    }

    public void setStatus(AccessStatus status) {
        this.status = status;
    }

    public Boolean getAllowDelegate() {
        return this.allowDelegate;
    }

    public void setAllowDelegate(boolean allow) {
        this.allowDelegate = allow;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public AccessKey toDataTransferObject() {
        AccessKey accessKey = new AccessKey();
        accessKey.setId(this.id);
        accessKey.setSecret(this.secret);
        accessKey.setClientId(this.clientId);
        accessKey.setCreationTime(this.creationTime.getTime());
        Account account = new Account();
        account.setEmail(this.ownerEmail);
        if (this.allowDelegate != null)
            accessKey.setAllowDelegate(this.allowDelegate);

        if (this.readOnly != null)
            accessKey.setReadOnly(this.readOnly);
        accessKey.setAccount(account);
        return accessKey;
    }
}
