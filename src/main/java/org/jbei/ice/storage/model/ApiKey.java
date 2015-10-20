package org.jbei.ice.storage.model;

import org.hibernate.annotations.GenericGenerator;
import org.jbei.ice.lib.access.AccessStatus;
import org.jbei.ice.lib.dto.access.AccessKey;
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

    @Column(name = "owner_email", length = 255, nullable = false)
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

    @Override
    public AccessKey toDataTransferObject() {
        AccessKey accessKey = new AccessKey();
        accessKey.setId(this.id);
        accessKey.setSecret(this.secret);
        accessKey.setClientId(this.clientId);
        accessKey.setCreationTime(this.creationTime.getTime());
        return accessKey;
    }
}
