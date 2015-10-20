package org.jbei.ice.lib.access;

import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ApiKey;

/**
 * Verifies the different tokens that ICE handles including <code>API</code> token,
 * <code>Web of registries token</code>
 *
 * @author Hector Plahar
 */
public class TokenVerification {

    public String verifyAPIKey(String token, String clientId, String userId) {
        // hash = (token, client + salt + client)

        ApiKey key = DAOFactory.getApiKeyDAO().getByClientId(clientId);
        if (key == null)
            throw new PermissionException("Invalid client Id " + clientId);

        TokenHash tokenHash = new TokenHash();
        String hash_token = tokenHash.encryptPassword(token, clientId + key.getSecret() + clientId);
        if (!hash_token.equalsIgnoreCase(key.getHashedToken()))
            throw new PermissionException("Invalid token");

        // if the api belongs to an admin, accept whatever user id they present
        Account account = DAOFactory.getAccountDAO().getByEmail(key.getOwnerEmail());
        if (userId == null)
            userId = account.getEmail();

        if (account.getType() == AccountType.ADMIN)
            return userId;

        return key.getOwnerEmail();
    }
}
