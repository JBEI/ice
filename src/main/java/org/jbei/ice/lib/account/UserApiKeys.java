package org.jbei.ice.lib.account;

import org.jbei.ice.lib.access.AccessStatus;
import org.jbei.ice.lib.access.PermissionException;
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

    /**
     * Retrieves either list of available keys for current user or all keys.
     * If requesting all keys then user must be an administrator
     *
     * @param limit        maximum number of keys to retrieve
     * @param offset       paging parameter start
     * @param sortField    field to sort on
     * @param asc          whether the retrieve order is in ascending order
     * @param getAvailable whether to retrieve all available keys or restrict by current user
     * @return wrapper around list of retrieved keys including number available
     */
    public Results<AccessKey> getKeys(int limit, int offset, String sortField, boolean asc, boolean getAvailable) {
        Results<AccessKey> accessKeyResults = new Results<>();
        List<ApiKey> results;
        AccountController accountController = new AccountController();
        boolean isAdmin = accountController.isAdministrator(this.userId);

        if (getAvailable) {
            if (!isAdmin)
                throw new PermissionException("Cannot retrieve all api keys without admin privileges");

            results = apiKeyDAO.getAllApiKeys(sortField, limit, offset, asc);
        } else {
            results = apiKeyDAO.getApiKeysForUser(userId, sortField, limit, offset, asc);
        }

        for (ApiKey key : results) {
            AccessKey accessKey = key.toDataTransferObject();
            Account account = accountController.getByEmail(key.getOwnerEmail());
            accessKey.setAccount(account.toDataTransferObject());
            accessKeyResults.getData().add(accessKey);
        }

        // get count
        String user = getAvailable ? null : this.userId;
        long count = apiKeyDAO.getApiKeysCount(user);
        accessKeyResults.setResultCount(count);
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
