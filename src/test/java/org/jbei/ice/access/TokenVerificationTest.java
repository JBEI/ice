package org.jbei.ice.access;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.account.TokenHash;
import org.jbei.ice.account.UserApiKeys;
import org.jbei.ice.dto.access.AccessKey;
import org.jbei.ice.dto.web.RemotePartnerStatus;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.RemotePartner;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author Hector Plahar
 */
public class TokenVerificationTest extends HibernateRepositoryTest {

    private TokenVerification verification;

    public TokenVerificationTest() {
        verification = new TokenVerification();
    }

    @Test
    public void testVerifyAPIKey() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testVerifyAPIKey", true);
        String userId = account.getEmail();
        UserApiKeys keys = new UserApiKeys(userId);
        AccessKey key = keys.requestKey("test.jbei.org");
        String verified = verification.verifyAPIKey(key.getToken(), key.getClientId(), userId);
        Assert.assertEquals(verified, userId);

        // verify for another user (cannot use someone else's token)
        AccountModel account1 = AccountCreator.createTestAccount("testVerifyAPIKey2", false);
        String userId1 = account1.getEmail();

        // will fail because delegation not set
        boolean caught = false;
        try {
            verification.verifyAPIKey(key.getToken(), key.getClientId(), userId1);
        } catch (PermissionException e) {
            caught = true;
        }
        Assert.assertTrue(caught);

        // set delegation and try again
        key.setAllowDelegate(true);
        keys.update(key.getId(), key);
        Assert.assertNotNull(key);
        Assert.assertEquals(userId1, verification.verifyAPIKey(key.getToken(), key.getClientId(), userId1));
    }

    @Test
    public void testVerifyPartnerToken() throws Exception {
        RemotePartner remotePartner = new RemotePartner();
        remotePartner.setName("jbei-test");
        remotePartner.setUrl("test.jbei.org");
        remotePartner.setPartnerStatus(RemotePartnerStatus.APPROVED);
        TokenHash tokenHash = new TokenHash();
        remotePartner.setSalt(tokenHash.generateSalt());
        String token = tokenHash.generateRandomToken();
        String hash = tokenHash.encrypt(token + remotePartner.getUrl(), remotePartner.getSalt());
        remotePartner.setAuthenticationToken(hash);
        remotePartner.setApiKey("foo");
        remotePartner.setAdded(new Date());
        Assert.assertNotNull(DAOFactory.getRemotePartnerDAO().create(remotePartner));
        Assert.assertNotNull(verification.verifyPartnerToken(remotePartner.getUrl(), token));
    }
}
