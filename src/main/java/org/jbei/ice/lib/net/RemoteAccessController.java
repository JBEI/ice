package org.jbei.ice.lib.net;

import java.util.UUID;

import org.jbei.ice.lib.access.RemotePermission;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.RemotePartnerDAO;
import org.jbei.ice.lib.dao.hibernate.RemotePermissionDAO;
import org.jbei.ice.lib.dto.permission.RemoteAccessPermission;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.RestClient;

import org.apache.commons.lang.StringUtils;

/**
 * @author Hector Plahar
 */
public class RemoteAccessController {

    private final RemotePermissionDAO dao;
    private final WoRController webController;
    private final RemotePartnerDAO remotePartnerDAO;

    public RemoteAccessController() {
        this.dao = DAOFactory.getRemotePermissionDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        webController = new WoRController();
    }

    public void addPermission(String requester, RemoteAccessPermission permission) {
        RestClient client = RestClient.getInstance();
        WebOfRegistries registries = webController.getRegistryPartners(true);

        // search for partners with user
        for (RegistryPartner partner : registries.getPartners()) {
            String url = partner.getUrl();

            try {
                Object result = client.get(url, "/rest/users/" + permission.getUserId(), AccountTransfer.class);
                if (result == null)
                    continue;

                // user found, create new permission user secret sauce
                String privateSecret = Utils.encryptSha256(UUID.randomUUID().toString());
                String secretSource = Utils.encryptSha256(permission.getUserId() + privateSecret);
                if (StringUtils.isEmpty(secretSource))
                    continue;

                // local record
                RemotePermission remotePermission = new RemotePermission();
                RemotePartner remotePartner = remotePartnerDAO.getByUrl(url);
                remotePermission.setRemotePartner(remotePartner);
                remotePermission.setUserId(permission.getUserId());
                remotePermission.setSecret(privateSecret);
                remotePermission.setAccessType(permission.getAccessType());
                dao.create(remotePermission);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }
}
