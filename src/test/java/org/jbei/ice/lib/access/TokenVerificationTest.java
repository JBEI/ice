package org.jbei.ice.lib.access;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.account.UserApiKeys;
import org.jbei.ice.lib.dto.access.AccessKey;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.junit.*;

/**
 * @author Hector Plahar
 */
public class TokenVerificationTest {

    private TokenVerification verification;

    @BeforeClass
    public static void init() {
        HibernateUtil.initializeMock();
    }

    @Before
    public void setUp() throws Exception {
        HibernateUtil.beginTransaction();
        verification = new TokenVerification();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testVerifyAPIKey() throws Exception {
        Account account = AccountCreator.createTestAccount("testVerifyAPIKey", true);
        String userId = account.getEmail();
        UserApiKeys keys = new UserApiKeys(userId);
        AccessKey key = keys.requestKey("test.jbei.org");
        String verified = verification.verifyAPIKey(key.getToken(), key.getClientId(), userId);
        Assert.assertEquals(verified, userId);

        // verify for another user
        Account account1 = AccountCreator.createTestAccount("testVerifyAPIKey2", false);
        String userId1 = account1.getEmail();
        Assert.assertEquals(userId1, verification.verifyAPIKey(key.getToken(), key.getClientId(), userId1));
    }
}