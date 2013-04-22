package org.jbei.ice.lib.entry;

import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.shared.AutoCompleteField;

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
        HibernateHelper.initializeMock();
        HibernateHelper.beginTransaction();
        controller = new EntryController();
    }

    protected Account createTestAccount() throws Exception {
        String email = "test@TESTER";
        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);
        return account;
    }

    @After
    public void tearDown() {
        HibernateHelper.commitTransaction();
    }

    @Test
    public void testGetMatchingAutoCompleteField() throws Exception {
        for (AutoCompleteField field : AutoCompleteField.values()) {
            Set<String> matches = controller.getMatchingAutoCompleteField(field, "TOKEN", 10);
            Assert.assertNotNull(matches);
        }
    }

    @Test
    public void testGetAllEntryIDs() throws Exception {
        Assert.assertNotNull(controller.getAllEntryIds());
    }

    @Test
    public void testCreateStrainWithPlasmid() throws Exception {
        Account account = createTestAccount();
        try {
            controller.createStrainWithPlasmid(account, null, null, null);
        } catch (ControllerException ce) {
        }

        Strain strain = new Strain();
        setDummyData(strain);
    }

    protected Entry setDummyData(Entry entry) {
        assert (entry != null);
        entry.setAlias("testEntryAlias");
        entry.setBioSafetyLevel(new Integer(1));
        entry.setCreator("tester");
        entry.setIntellectualProperty("no intellectual property");
        return entry;
    }

    @Test
    public void testCreateEntry() throws Exception {
        Account account = createTestAccount();
        Entry strain = new Strain();
        strain = controller.createEntry(account, strain, null);
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getId() > 0);
    }

    @Test
    public void testGet() throws Exception {
        Account account = createTestAccount();
        Entry plasmid = new Plasmid();
        plasmid = controller.createEntry(account, plasmid, null);
        Entry ret = controller.get(account, plasmid.getId());
        Assert.assertNotNull(ret);
    }

    @Test
    public void testGetByRecordId() throws Exception {
        Account account = createTestAccount();
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
