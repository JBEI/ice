package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.access.AccessStatus;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.ApiKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author Hector Plahar
 */
public class ApiKeyDAOTest extends HibernateRepositoryTest {

    private ApiKeyDAO dao = new ApiKeyDAO();

    @Test
    public void testGet() throws Exception {
        ApiKey key = new ApiKey();
        key.setOwnerEmail("email@example");
        key = dao.create(key);
        Assert.assertNotNull(key);
        ApiKey get = dao.get(key.getId());
        Assert.assertNotNull(get);
        Assert.assertEquals(key.getOwnerEmail(), get.getOwnerEmail());
    }

    @Test
    public void testGetApiKeysForUser() throws Exception {
        Account account = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetApiKeysForUser", false);
        List<ApiKey> keys = dao.getApiKeysForUser(account.getEmail(), "id", 10, 0, false);
        Assert.assertNotNull(keys);
        Assert.assertTrue(keys.isEmpty());

        // create a number of api keys for user
        for (int i = 0; i < 6; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId("client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        keys = dao.getApiKeysForUser(account.getEmail(), "id", 10, 0, false);
        Assert.assertNotNull(keys);
        Assert.assertEquals(6, keys.size());
    }

    @Test
    public void testGetAllApiKeys() throws Exception {
        // create accounts
        Account account1 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetAllApiKeys1", false);
        Account account2 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetAllApiKeys2", false);
        Account account3 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetAllApiKeys3", false);

        Random random = new Random();
        int limit1 = random.nextInt(10);

        // create api keys for each account
        for (int i = 0; i < limit1; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account1.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account1.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        int limit2 = random.nextInt(10);
        for (int i = 0; i < limit2; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account2.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account2.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        int limit3 = random.nextInt(10);
        for (int i = 0; i < limit3; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account3.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account3.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        List<ApiKey> keys = dao.getAllApiKeys("id", 30, 0, false);
        Assert.assertNotNull(keys);
        Assert.assertTrue(limit1 + limit2 + limit3 <= keys.size());
    }

    @Test
    public void testGetApiKeysCount() throws Exception {
        Account account1 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetApiKeysCount1", false);
        Account account2 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetApiKeysCount2", false);
        Account account3 = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetApiKeysCount3", false);

        Random random = new Random();
        int limit1 = random.nextInt(10);

        // create api keys for each account
        for (int i = 0; i < limit1; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account1.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account1.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        int limit2 = random.nextInt(10);
        for (int i = 0; i < limit2; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account2.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account2.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }

        int limit3 = random.nextInt(10);
        for (int i = 0; i < limit3; i += 1) {
            ApiKey apiKey = new ApiKey();
            apiKey.setOwnerEmail(account3.getEmail());
            apiKey.setCreationTime(new Date());
            apiKey.setHashedToken("token" + i);
            apiKey.setStatus(AccessStatus.OK);
            apiKey.setClientId(account3.getEmail() + "_client" + i);
            Assert.assertNotNull(dao.create(apiKey));
        }
        Assert.assertEquals(limit1, dao.getApiKeysCount(account1.getEmail()));
        Assert.assertEquals(limit2, dao.getApiKeysCount(account2.getEmail()));
        Assert.assertEquals(limit3, dao.getApiKeysCount(account3.getEmail()));
    }

    @Test
    public void testGetByClientId() throws Exception {
        Account account = AccountCreator.createTestAccount("ApiKeyDAOTest.testGetByClientId", false);
        ApiKey apiKey = new ApiKey();
        apiKey.setOwnerEmail(account.getEmail());
        apiKey.setCreationTime(new Date());
        apiKey.setHashedToken("token");
        apiKey.setStatus(AccessStatus.OK);
        apiKey.setClientId(account.getEmail() + "_client");
        Assert.assertNotNull(dao.create(apiKey));
        Optional<ApiKey> result = dao.getByClientId(account.getEmail() + "_client");
        Assert.assertTrue(result.isPresent());
    }
}