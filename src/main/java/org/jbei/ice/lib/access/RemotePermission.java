package org.jbei.ice.lib.access;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.net.RemotePartner;

/**
 * Remote access credentials that allows access to remote entries
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PERMISSION")
@SequenceGenerator(name = "sequence", sequenceName = "permission_id_seq", allocationSize = 1)

public class RemotePermission implements IDataModel {

    private static final long serialVersionUID = 1L;

    private Account account;    // local account performing the share

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "remote_partner_id", nullable = false)
    private RemotePartner remotePartner;

    private String userId; // id of user on "remotePartner" who is recipient of share

    private String secret; // private secret used to validate remote secret

    @Enumerated(value = EnumType.STRING)
    private AccessType accessType; //read or write

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

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }
}
