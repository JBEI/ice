package org.jbei.ice.lib.access;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ApiKey;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.Optional;

/**
 * Verifies the different tokens that ICE handles including <code>API</code> token,
 * <code>Web of registries token</code>
 *
 * @author Hector Plahar
 */
public class TokenVerification {

    private final TokenHash tokenHash;

    public TokenVerification() {
        this.tokenHash = new TokenHash();
    }

    public String verifyAPIKey(String token, String clientId, String userId) {
        // hash = (token, client + salt + client)

        Optional<ApiKey> optionalKey = DAOFactory.getApiKeyDAO().getByClientId(clientId);
        if (!optionalKey.isPresent())
            throw new PermissionException("Invalid client Id " + clientId);

        ApiKey key = optionalKey.get();
        String hash_token = tokenHash.encrypt(token, clientId + key.getSecret() + clientId);
        if (!hash_token.equalsIgnoreCase(key.getHashedToken()))
            throw new PermissionException("Invalid token");

        // if the api belongs to an admin, accept whatever user id they present
        AccountDAO accountDAO = DAOFactory.getAccountDAO();
        Account account = accountDAO.getByEmail(key.getOwnerEmail());
        if (userId == null)
            userId = account.getEmail();

        if (account.getType() == AccountType.ADMIN) {
            if (account.getEmail().equalsIgnoreCase(userId))
                return userId;
            if (accountDAO.getByEmail(userId) == null)
                throw new PermissionException("Invalid user id");
            return userId;
        }

        return key.getOwnerEmail();
    }

    public RegistryPartner verifyPartnerToken(String url, String token) {
        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(url);
        if (remotePartner == null)
            return null;

        String hash = this.tokenHash.encrypt(token + url, remotePartner.getSalt());
        if (!hash.equals(remotePartner.getAuthenticationToken()))
            return null;
        return remotePartner.toDataTransferObject();
    }
}
