package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;

/**
 * @author Hector Plahar
 */
public class RemoteUsers {

    public RemoteUsers() {
    }

    public RemoteUser get(long partnerId, String email) {
        RemotePartnerDAO remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        RemotePartner remotePartner = remotePartnerDAO.get(partnerId);
        if (remotePartner == null)
            return null;

        RemoteContact remoteContact = new RemoteContact();
        AccountTransfer accountTransfer = remoteContact.getUser(remotePartner.getUrl(), email, remotePartner.getApiKey());
        if (accountTransfer == null)
            return null;
        RemoteUser remoteUser = new RemoteUser();
        remoteUser.setPartner(remotePartner.toDataTransferObject());
        remoteUser.setUser(accountTransfer);
        return remoteUser;
    }
}
