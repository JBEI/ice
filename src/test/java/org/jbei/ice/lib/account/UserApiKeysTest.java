package org.jbei.ice.lib.account;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.access.AccessKey;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class UserApiKeysTest {

    @Before
    public void setUp() throws Exception {
        HibernateConfiguration.initializeMock();
        HibernateConfiguration.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateConfiguration.commitTransaction();
    }

    @Test
    public void testRequestKey() throws Exception {
        Account account = AccountCreator.createTestAccount("UserApiKeysTest.testRequestKey", false);
        UserApiKeys apiKeys = new UserApiKeys(account.getEmail());
        AccessKey accessKey = apiKeys.requestKey("app.test");
        Assert.assertNotNull(accessKey);
        Assert.assertNotNull(accessKey.getToken());

        AccessKey accessKey2 = apiKeys.requestKey("app.test2");
        Assert.assertNotNull(accessKey2);
        Assert.assertNotNull(accessKey2.getToken());
        Assert.assertNotEquals(accessKey.getToken(), accessKey2.getToken());
    }

    @Test
    public void testGetKeys() throws Exception {
        Account account = AccountCreator.createTestAccount("UserApiKeysTest.testGetKeys", false);
        Account admin = AccountCreator.createTestAccount("UserApiKeysTest.testGetKeys.Admin", true);

        UserApiKeys apiKeys = new UserApiKeys(account.getEmail());
        AccessKey accessKey = apiKeys.requestKey("app.test.3");
        Assert.assertNotNull(accessKey);
        Assert.assertNotNull(accessKey.getToken());

        AccessKey accessKey2 = apiKeys.requestKey("app.test.4");
        Assert.assertNotNull(accessKey2);
        Assert.assertNotNull(accessKey2.getToken());

        UserApiKeys adminApiKeys = new UserApiKeys(admin.getEmail());
        AccessKey adminKey = adminApiKeys.requestKey("admin.app.test");
        Assert.assertNotNull(adminKey);

        Results<AccessKey> keys = apiKeys.getKeys(15, 0, "creationTime", true, false);
        Assert.assertEquals(2, keys.getResultCount());
        Assert.assertTrue(adminApiKeys.getKeys(15, 0, "creationTime", true, true).getResultCount() >= 3);
    }

    @Test
    public void testDeleteKey() throws Exception {
        Account account = AccountCreator.createTestAccount("UserApiKeysTest.testDeleteKey", false);

        UserApiKeys apiKeys = new UserApiKeys(account.getEmail());
        AccessKey accessKey = apiKeys.requestKey("app.test.6");
        Assert.assertNotNull(accessKey);
        Assert.assertNotNull(accessKey.getToken());

        Assert.assertFalse(apiKeys.deleteKey(accessKey.getId(), "secret"));
        Assert.assertTrue(apiKeys.deleteKey(accessKey.getId(), accessKey.getSecret()));

        Results<AccessKey> keys = apiKeys.getKeys(15, 0, "creationTime", true, false);
        Assert.assertEquals(0, keys.getResultCount());
    }
}
