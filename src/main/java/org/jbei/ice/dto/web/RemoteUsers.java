package org.jbei.ice.dto.web;

import org.jbei.ice.account.Account;
import org.jbei.ice.net.RemoteContact;
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
        Account account = remoteContact.getUser(remotePartner.getUrl(), email, remotePartner.getApiKey());
        if (account == null)
            return null;
        RemoteUser remoteUser = new RemoteUser();
        remoteUser.setPartner(remotePartner.toDataTransferObject());
        remoteUser.setUser(account);
        return remoteUser;
    }
}
