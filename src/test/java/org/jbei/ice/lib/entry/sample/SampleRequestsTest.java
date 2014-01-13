package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.EntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestStatus;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class SampleRequestsTest {

    private SampleRequests requests;

    @Before
    public void setUp() throws Exception {
        requests = new SampleRequests();
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testPlaceSampleInCart() throws Exception {
        Account account = AccountCreator.createTestAccount("testPlaceSampleInCart", false);
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();
        SampleRequest request = requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);
        request = requests.placeSampleInCart(account, id, SampleRequestType.STREAK_ON_AGAR_PLATE);
        // can only have one entry per account placed in cart. In this case, it is null because
        // liquid culture has already been requested
        Assert.assertNull(request);

        // create a new account and place same entry in cart
        Account account2 = AccountCreator.createTestAccount("testPlaceSampleInCart2", false);

        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setType(AccessPermission.Type.READ_ENTRY);
        accessPermission.setTypeId(entry.getId());
        accessPermission.setArticle(AccessPermission.Article.ACCOUNT);
        accessPermission.setArticleId(account2.getId());
        new PermissionsController().addPermission(account, accessPermission);

        request = requests.placeSampleInCart(account2, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);
    }

    @Test
    public void testGetSampleRequestsInCart() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetSampleRequestsInCart", false);
        ArrayList<SampleRequest> inCart = requests.getSampleRequestsInCart(account);
        Assert.assertTrue(inCart.isEmpty());
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();
        SampleRequest request = requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);
        inCart = requests.getSampleRequestsInCart(account);
        Assert.assertTrue(inCart.size() == 1);

        // create another entry and place that in the cart
        entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id2 = entry.getId();
        request = requests.placeSampleInCart(account, id2, SampleRequestType.STREAK_ON_AGAR_PLATE);
        Assert.assertNotNull(request);
        inCart = requests.getSampleRequestsInCart(account);
        Assert.assertTrue(inCart.size() == 2);
    }

    @Test
    public void testGetPendingRequests() throws Exception {
        Account account = AccountCreator.createTestAccount("testGetPendingRequests", false);
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();

        SampleRequest request = requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);

        // check pending requests. although there is a sample in the cart, it is not pending
        ArrayList<SampleRequest> pending = requests.getPendingRequests(account);
        Assert.assertTrue(pending.isEmpty());

        pending.add(request);

        // submit
        Assert.assertTrue(requests.request(account, pending));

        pending.clear();
        pending = requests.getPendingRequests(account);
        Assert.assertEquals(1, pending.size());
    }

    @Test
    public void testRemoveSampleFromCart() throws Exception {
        Account account = AccountCreator.createTestAccount("testRemoveSampleFromCart", false);
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();
        Assert.assertNotNull(requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE));
        Assert.assertNotNull(requests.removeSampleFromCart(account, id));
    }

    @Test
    public void testUpdateRequest() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdateRequest", false);
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();
        SampleRequest request = requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);
        request.setStatus(SampleRequestStatus.FULFILLED);
        Assert.assertNotNull(requests.updateRequest(account, request));
    }

    @Test
    public void testRequest() throws Exception {
        Account account = AccountCreator.createTestAccount("testRequest", false);
        Entry entry = EntryCreator.createTestStrain(account);
        Assert.assertNotNull(entry);
        long id = entry.getId();

        SampleRequest request = requests.placeSampleInCart(account, id, SampleRequestType.LIQUID_CULTURE);
        Assert.assertNotNull(request);

        ArrayList<SampleRequest> pending = new ArrayList<>();
        pending.add(request);

        // submit
        Assert.assertTrue(requests.request(account, pending));
    }
}
