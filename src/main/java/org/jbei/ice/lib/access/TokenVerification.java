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
 * Verifies the different tokens that ICE handles including
 * <ul>
 * <li><code>API</code> token</li>
 * <li><code>Web of registries token</code></li>
 * </ul>
 *
 * @author Hector Plahar
 */
public class TokenVerification {

    private final TokenHash tokenHash;

    public TokenVerification() {
        this.tokenHash = new TokenHash();
    }

    /**
     * Verify the API token and return the user id that this validates.
     * Note that the returned the validated user might be different from the owner of the token
     * if the api key allows delegation and the owner of the token is an administrator
     *
     * @param token    unique token identifier
     * @param clientId client identifier for the token
     * @param userId   user id that is the purported owner of the token or the being used to delegate the task
     * @return user id according to api keys validation
     */
    public String verifyAPIKey(String token, String clientId, String userId) {
        // hash = (token, client + salt + client)

        Optional<ApiKey> optionalKey = DAOFactory.getApiKeyDAO().getByClientId(clientId);
        if (!optionalKey.isPresent())
            throw new PermissionException("Invalid client Id " + clientId);

        ApiKey key = optionalKey.get();
        String hash_token = tokenHash.encrypt(token, clientId + key.getSecret() + clientId);
        if (!hash_token.equalsIgnoreCase(key.getHashedToken()))
            throw new PermissionException("Invalid token");

        // validate owner; must have a valid account on this instance
        AccountDAO accountDAO = DAOFactory.getAccountDAO();
        Account account = accountDAO.getByEmail(key.getOwnerEmail());
        if (account == null)
            throw new PermissionException("Invalid token owner");   // this really shouldn't happen

        // return owner if none specified
        if (userId == null || key.getOwnerEmail().equalsIgnoreCase(userId))
            return account.getEmail();

        // must be admin
        if (account.getType() != AccountType.ADMIN)
            throw new PermissionException("Invalid API key request.");

        // check if validation is allowed
        if (key.getAllowDelegate() == null || !key.getAllowDelegate())
            throw new PermissionException("Invalid API key request. Delegation not permitted.");

        return userId;
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
