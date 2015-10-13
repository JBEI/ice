package org.jbei.ice.storage.model;

import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.IDataTransferModel;

import javax.persistence.*;

/**
 * Remote access credentials that allows access to remote entries
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PERMISSION")
@SequenceGenerator(name = "sequence", sequenceName = "permission_id_seq", allocationSize = 1)

public class RemotePermission implements DataModel {

    private Account account;    // local account performing the share

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "remote_partner_id", nullable = false)
    private RemotePartner remotePartner;

    private String userId; // id of user on "remotePartner" who is recipient of share

    private String secret; // private secret used to validate remote secret

    @Enumerated(value = EnumType.STRING)
    private org.jbei.ice.lib.access.AccessType accessType; //read or write

    @Override
    public IDataTransferModel toDataTransferObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RemotePartner getRemotePartner() {
        return remotePartner;
    }

    public void setRemotePartner(RemotePartner remotePartner) {
        this.remotePartner = remotePartner;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public org.jbei.ice.lib.access.AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(org.jbei.ice.lib.access.AccessType accessType) {
        this.accessType = accessType;
    }
}
