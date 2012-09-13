package org.jbei.ice.lib.entry;

import java.util.ArrayList;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.EntryFundingSource;
import org.jbei.ice.lib.entry.model.PartNumber;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.shared.dto.Visibility;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**/

/**
 * @author Hector Plahar
 */
public class EntryControllerTest {
    private EntryController controller;

    @Before
    public void setUp() {
        HibernateHelper.initializeMock();
        controller = new EntryController();
    }

    @Test
    public void testCreateEntry() throws Exception {
        String email = "testCreateEntry@TESTER.org";

        AccountController accountController = new AccountController();
        accountController.createNewAccount("", "", "", email, "", "");
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        Entry entry = new Strain();
        entry.setStatus("Complete");
        entry.setBioSafetyLevel(new Integer(1));
        entry = controller.createEntry(account, entry, false, null);
        Assert.assertNotNull(entry);

        // account should only have a single entry
        ArrayList<Long> list = controller.getEntryIdsByOwner(email);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());

        long id = list.get(0);
        Assert.assertEquals(entry.getId(), id);
        Assert.assertEquals(Visibility.OK.getValue(), entry.getVisibility().intValue());
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
        String email = "testGetByPartNumber@TESTER.org";
        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        Strain strain = new Strain();
        Assert.assertNotNull(controller.createEntry(account, strain, false, null));
        PartNumber number = strain.getOnePartNumber();
        Assert.assertNotNull(number);

        Strain ret = (Strain) controller.getByPartNumber(account, number.getPartNumber());
        Assert.assertNotNull(ret);
    }

    @Test
    public void testUpdate() throws Exception {

        // create account
        String email = "testUpdate@TESTER.org";

        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        // create entry
        Plasmid plasmid = new Plasmid();
        EntryFundingSource entryFundingSource = new EntryFundingSource();
        FundingSource fundingSource = new FundingSource();
        fundingSource.setFundingSource("USA");
        fundingSource.setPrincipalInvestigator("PI");
        entryFundingSource.setFundingSource(fundingSource);
        plasmid.getEntryFundingSources().add(entryFundingSource);
        entryFundingSource.setEntry(plasmid);
        Assert.assertNotNull(controller.createEntry(account, plasmid, false, null));

        // retrieve
        plasmid = (Plasmid) controller.get(account, plasmid.getId());
        Assert.assertNotNull(plasmid);
        Assert.assertEquals(1, plasmid.getEntryFundingSources().size());
        entryFundingSource = (EntryFundingSource) plasmid.getEntryFundingSources().toArray()[0];
        Assert.assertNotNull(entryFundingSource);
        Assert.assertEquals("USA", entryFundingSource.getFundingSource().getFundingSource());
        Assert.assertEquals("PI", entryFundingSource.getFundingSource().getPrincipalInvestigator());

        // update
        entryFundingSource.getFundingSource().setFundingSource("NEW");
        plasmid = (Plasmid) controller.update(account, plasmid, false, null);
        Assert.assertNotNull(plasmid);

        entryFundingSource = (EntryFundingSource) plasmid.getEntryFundingSources().toArray()[0];
        Assert.assertEquals("NEW", entryFundingSource.getFundingSource().getFundingSource());
        Assert.assertEquals("PI", entryFundingSource.getFundingSource().getPrincipalInvestigator());
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

        String email = "testGetNumberOfVisibleEntries@TESTER.org";

        AccountController accountController = new AccountController();
        String pass = accountController.createNewAccount("", "TEST", "T", email, null, "");
        Assert.assertNotNull(pass);
        Account account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        long count = controller.getNumberOfVisibleEntries(account);
        Assert.assertEquals("New account has entry", 0, count);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.create("delete_NUMBERPUBLIC", "TEST", null);
        account.getGroups().add(publicGroup);
        accountController.save(account);

        count = controller.getNumberOfVisibleEntries(account);
        Assert.assertEquals("New account has entries", 0, count);

        // create some entries
        Strain strain = new Strain();
        controller.createEntry(account, strain, false, publicGroup);
        Assert.assertNotNull(strain);

        count = controller.getNumberOfVisibleEntries(account);
        Assert.assertEquals(1, count);

        // add write permission
        PermissionsController permissionsController = new PermissionsController();
        permissionsController.addPermission(account, PermissionInfo.PermissionType.WRITE_GROUP, strain,
                                            publicGroup.getId());

        // same entry so it should be one
        count = controller.getNumberOfVisibleEntries(account);
        Assert.assertEquals(1, count);

        Assert.assertEquals(count, controller.getAllVisibleEntryIDs(account).size());
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
