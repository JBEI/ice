package org.jbei.ice.dto.web;

import org.jbei.ice.account.Account;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class RemoteUser implements IDataTransferModel {

    private RegistryPartner partner;
    private Account user;

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public Account getUser() {
        return user;
    }

    public void setUser(Account user) {
        this.user = user;
    }
}
