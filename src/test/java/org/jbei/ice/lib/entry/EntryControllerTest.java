package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {
    private EntryController controller;

    @Before
    public void setUp() {
//        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new EntryController();
    }

    @After
    public void tearDown() {
        HibernateHelper.rollbackTransaction();
    }

    @Test
    public void testCreateEntry() throws Exception {
    }

    @Test
    public void testGet() throws Exception {
        String email = "testGet@TESTER.org";
        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        Entry plasmid = new Plasmid();
        plasmid = controller.createEntry(account, plasmid, null);
        Entry ret = controller.get(account, plasmid.getId());
        Assert.assertNotNull(ret);
    }

    @Test
    public void testGetByRecordId() throws Exception {
        String email = "testGetByRecordId@TESTER.org";
        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        ArabidopsisSeed seed = new ArabidopsisSeed();
        seed.setEcotype("ecotype");
        seed.setGeneration(ArabidopsisSeed.Generation.M0);
        seed.setParents("parents");
        seed.setPlantType(ArabidopsisSeed.PlantType.OVER_EXPRESSION);
        seed.setHomozygosity("homozygo");
        controller.createEntry(account, seed, null);
        ArabidopsisSeed ret = (ArabidopsisSeed) controller.getByRecordId(account, seed.getRecordId());
        Assert.assertNotNull(ret);

        Assert.assertEquals("ecotype", ret.getEcotype());
        Assert.assertEquals(ArabidopsisSeed.Generation.M0, ret.getGeneration());
        Assert.assertEquals("parents", ret.getParents());
        Assert.assertEquals(ArabidopsisSeed.PlantType.OVER_EXPRESSION, ret.getPlantType());
        Assert.assertEquals("homozygo", ret.getHomozygosity());
    }

    @Test
    public void testGetByPartNumber() throws Exception {
    }

    @Test
    public void testUpdate() throws Exception {
    }

    @Test
    public void testGetByName() throws Exception {

    }

    @Test
    public void testHasReadPermission() throws Exception {

    }

    @Test
    public void testHasWritePermission() throws Exception {

    }

    @Test
    public void testHasAttachments() throws Exception {

    }

    @Test
    public void testGetAllVisibleEntryIDs() throws Exception {

    }

    @Test
    public void testGetAllEntryIDs() throws Exception {

    }

    @Test
    public void testGetNumberOfVisibleEntries() throws Exception {
    }

    @Test
    public void testGetEntryIdsByOwner() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testGetAllEntryCount() throws Exception {
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail("haplahar@lbl.gov");
        long count = controller.getNumberOfVisibleEntries(account);
        System.out.println(count);

        account = accountController.getByEmail("mjdougherty@lbl.gov");
        count = controller.getNumberOfVisibleEntries(account);
        System.out.println(count);

    }

    @Test
    public void testGetOwnerEntryCount() throws Exception {

    }

    @Test
    public void testGetAllEntries() throws Exception {

    }

    @Test
    public void testRetrieveEntriesByIdSetSort() throws Exception {

    }

    @Test
    public void testGetEntriesByIdSet() throws Exception {

    }

    @Test
    public void testSortList() throws Exception {

    }

    @Test
    public void testRetrieveEntryByType() throws Exception {

    }

    @Test
    public void testRetrieveStrainsForPlasmid() throws Exception {

    }
}
