package org.jbei.ice.lib.account;

import org.jbei.ice.lib.access.AccessStatus;
import org.jbei.ice.lib.dto.access.AccessKey;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ApiKeyDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ApiKey;

import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class UserApiKeys {

    private final String userId;
    private final ApiKeyDAO apiKeyDAO;

    public UserApiKeys(String userId) {
        this.userId = userId;
        this.apiKeyDAO = DAOFactory.getApiKeyDAO();
    }

    public AccessKey requestKey(String clientId) {
        TokenHash hash = new TokenHash();
        String token = hash.generateRandomToken(32);
        String salt = hash.generateSalt();
        String hash_token = hash.encryptPassword(token, clientId + salt + clientId);

        ApiKey apiKey = new ApiKey();
        apiKey.setCreationTime(new Date());
        apiKey.setOwnerEmail(userId);
        apiKey.setClientId(clientId);
        apiKey.setSecret(salt);
        apiKey.setStatus(AccessStatus.OK);
        apiKey.setHashedToken(hash_token);

        apiKey = apiKeyDAO.create(apiKey);
        AccessKey key = apiKey.toDataTransferObject();
        key.setToken(token);
        return key;
    }

    public Results<AccessKey> getKeys(int limit, int offset, String sortField, boolean asc) {
        Results<AccessKey> accessKeyResults = new Results<>();
        AccountController accountController = new AccountController();
        boolean isAdmin = accountController.isAdministrator(this.userId);

        List<ApiKey> results = DAOFactory.getApiKeyDAO().getApiKeysForUser(userId, sortField, limit, offset, asc);
        for (ApiKey key : results) {
            AccessKey accessKey = key.toDataTransferObject();
            if (isAdmin) {
                Account account = accountController.getByEmail(key.getOwnerEmail());
                accessKey.setAccount(account.toDataTransferObject());
            }
            accessKeyResults.getData().add(accessKey);
        }
        return accessKeyResults;
    }

    public boolean deleteKey(long id, String secret) {
        ApiKey key = apiKeyDAO.get(id);
        if (!key.getSecret().equalsIgnoreCase(secret))
            return false;

        apiKeyDAO.delete(key);
        return true;
    }
}
