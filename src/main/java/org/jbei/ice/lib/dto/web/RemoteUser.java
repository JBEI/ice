package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * @author Hector Plahar
 */
public class RemoteUser implements IDataTransferModel {

    private RegistryPartner partner;
    private String userId;

    public RegistryPartner getPartner() {
        return partner;
    }

    public void setPartner(RegistryPartner partner) {
        this.partner = partner;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
