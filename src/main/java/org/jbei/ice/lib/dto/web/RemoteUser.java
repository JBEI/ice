package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.storage.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class RemoteUser implements IDataTransferModel {

    private RegistryPartner partner;
    private AccountTransfer user;

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public AccountTransfer getUser() {
        return user;
    }

    public void setUser(AccountTransfer user) {
        this.user = user;
    }
}
