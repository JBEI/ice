package org.jbei.ice.lib.account;

import org.jbei.ice.lib.access.AccessStatus;
import org.jbei.ice.lib.dto.access.AccessKey;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ApiKeyDAO;
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
        return apiKey.toDataTransferObject();
    }

    public Results<AccessKey> getKeys(int limit, int offset, String sortField, boolean asc) {
        Results<AccessKey> accessKeyResults = new Results<>();
        List<ApiKey> results = DAOFactory.getApiKeyDAO().getApiKeysForUser(userId, limit, offset, asc);
        for (ApiKey key : results) {
            accessKeyResults.getData().add(key.toDataTransferObject());
        }
        return accessKeyResults;
    }
}
