package org.jbei.ice.lib.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.comment.UserComment;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;
import org.jbei.ice.shared.dto.entry.PlasmidInfo;
import org.jbei.ice.shared.dto.entry.StrainInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

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

    protected Account createTestAccount(String testName, boolean admin) throws Exception {
        String email = testName + "@TESTER";
        AccountController accountController = new AccountController();
        Account account = accountController.getByEmail(email);
        if (account != null)
            throw new Exception("duplicate account");

        AccountInfo info = new AccountInfo();
        info.setFirstName("");
        info.setLastName("TEST");
        info.setEmail(email);
        String pass = accountController.createNewAccount(info, false);
        Assert.assertNotNull(pass);
        account = accountController.getByEmail(email);
        Assert.assertNotNull(account);

        if (admin) {
            account.setType(AccountType.ADMIN);
            accountController.save(account);
        }
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
        Account account = createTestAccount("testCreateStrainWithPlasmid", false);
        try {
            controller.createStrainWithPlasmid(account, null, null, null);
        } catch (ControllerException ce) {
            // expecting ce
        }

        StrainInfo strainInfo = new StrainInfo();
        strainInfo.setAlias("testStrainAlias");
        strainInfo.setBioSafetyLevel(new Integer(1));
        strainInfo.setGenotypePhenotype("genPhenTest");
        strainInfo.setHost("testHost");
        strainInfo.setName("sTrain");
        Strain strain = (Strain) InfoToModelFactory.infoToEntry(strainInfo);
        Assert.assertNotNull(strain);

        PlasmidInfo plasmidInfo = new PlasmidInfo();
        plasmidInfo.setName("pLasmid");
        plasmidInfo.setCircular(true);
        plasmidInfo.setOriginOfReplication("repOrigin");
        plasmidInfo.setPromoters("None");
        plasmidInfo.setBackbone("backbone");
        Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(plasmidInfo);
        Assert.assertNotNull(plasmid);

        HashSet<Entry> results = controller.createStrainWithPlasmid(account, strain, plasmid, null);
        Assert.assertNotNull(results);
        Assert.assertEquals("Strain with plasmid creation returned wrong entry count", 2, results.size());
    }

    @Test
    public void testCreateEntry() throws Exception {
        Account account = createTestAccount("testCreateEntry", false);
        Entry strain = new Strain();
        strain = controller.createEntry(account, strain, null);
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getId() > 0);
    }

    @Test
    public void testGet() throws Exception {
        Account account = createTestAccount("testGet", false);
        Entry plasmid = new Plasmid();
        plasmid = controller.createEntry(account, plasmid, null);
        Entry ret = controller.get(account, plasmid.getId());
        Assert.assertNotNull(ret);
    }

    @Test
    public void testGetByRecordId() throws Exception {
        Account account = createTestAccount("testGetByRecordId", false);
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
        Account creator = createTestAccount("testGetByPartNumber", false);

        PlasmidInfo info = new PlasmidInfo();
        info.setType(EntryType.PLASMID);
        info.setBioSafetyLevel(1);
        info.setOriginOfReplication("kanamycin");
        info.setCircular(false);
        info.setOwnerEmail(info.getCreatorEmail());
        info.setOwner(info.getCreator());
        info.setShortDescription("testing");
        info.setStatus("Complete");
        info.setName("pSTC100");

        Entry plasmid = InfoToModelFactory.infoToEntry(info);
        plasmid = controller.createEntry(creator, plasmid);
        String partNumber = plasmid.getOnePartNumber().getPartNumber();
        Entry result = controller.getByPartNumber(creator, partNumber);
        Assert.assertNotNull(result);
        Assert.assertEquals(EntryType.PLASMID.toString().toLowerCase(), result.getRecordType().toLowerCase());
        result = controller.getByPartNumber(creator, "fake");
        Assert.assertNull(result);
    }

    @Test
    public void testUpdate() throws Exception {
        Account creator = createTestAccount("testUpdate1", false);
        Account account = createTestAccount("testUpdate2", false);

        // create entry
        PlasmidInfo info = new PlasmidInfo();
        info.setType(EntryType.PLASMID);
        info.setBioSafetyLevel(1);
        info.setCreatorEmail(creator.getEmail());
        info.setCreator(creator.getFullName());
        info.setOriginOfReplication("kanamycin");
        info.setCircular(false);
        info.setOwnerEmail(info.getCreatorEmail());
        info.setOwner(info.getCreator());
        info.setShortDescription("testing");
        info.setStatus("Complete");
        info.setName("pSTC100");

        Plasmid plasmid = (Plasmid) InfoToModelFactory.infoToEntry(info);
        Assert.assertNotNull(plasmid);

        // add Write permission for account
        ArrayList<PermissionInfo> permissions = new ArrayList<>();
        PermissionInfo permissionInfo = new PermissionInfo();
        permissionInfo.setArticle(PermissionInfo.Article.ACCOUNT);
        permissionInfo.setType(PermissionInfo.Type.WRITE_ENTRY);
        permissionInfo.setArticleId(account.getId());
        permissions.add(permissionInfo);
        plasmid = (Plasmid) controller.createEntry(creator, plasmid, permissions);
        Assert.assertNotNull(plasmid);
        Assert.assertTrue(plasmid.getId() > 0);

        // expect two permissions, write for owner and write for account
        Assert.assertEquals("Unexpected number of permissions", 2, plasmid.getPermissions().size());


        // update with account
        info.setCircular(true);
        info.setRecordId(plasmid.getRecordId());
        Entry existing = controller.getByRecordId(account, info.getRecordId());
        ArrayList<PermissionInfo> p = ControllerFactory.getPermissionController()
                                                       .retrieveSetEntryPermissions(account, plasmid);
        info.setPermissions(p);

        Entry entry = InfoToModelFactory.infoToEntry(info, existing);

        Entry updated = controller.update(account, entry);
        Assert.assertNotNull(updated);
    }

    @Test
    public void testGetByName() throws Exception {
        Account creator = createTestAccount("testGetByName", false);

        PlasmidInfo info = new PlasmidInfo();
        info.setType(EntryType.PLASMID);
        info.setBioSafetyLevel(1);
        info.setOriginOfReplication("kanamycin");
        info.setCircular(false);
        info.setOwnerEmail(info.getCreatorEmail());
        info.setOwner(info.getCreator());
        info.setShortDescription("testing");
        info.setStatus("Complete");
        info.setName("pSTC1000");

        Entry plasmid = InfoToModelFactory.infoToEntry(info);
        plasmid = controller.createEntry(creator, plasmid);
        String name = plasmid.getOneName().getName();
        Entry result = controller.getByName(creator, name);
        Assert.assertNotNull(result);
        Assert.assertEquals(EntryType.PLASMID.toString().toLowerCase(), result.getRecordType().toLowerCase());
        String partNumber = plasmid.getOnePartNumber().getPartNumber();
        result = controller.getByName(creator, partNumber);
        Assert.assertNull(result);
    }

    @Test
    public void testAddComment() throws Exception {
        Account creator = createTestAccount("testAddComment", false);

        PlasmidInfo info = new PlasmidInfo();
        info.setType(EntryType.PLASMID);
        info.setBioSafetyLevel(1);
        info.setOriginOfReplication("kanamycin");
        info.setCircular(false);
        info.setOwnerEmail(info.getCreatorEmail());
        info.setOwner(info.getCreator());
        info.setShortDescription("testing");
        info.setStatus("Complete");
        info.setName("pSTC1000");

        Entry plasmid = InfoToModelFactory.infoToEntry(info);
        plasmid = controller.createEntry(creator, plasmid);
        Assert.assertNotNull(plasmid);
        UserComment comment = new UserComment("This is a test");
        comment.setEntryId(plasmid.getId());
        comment = controller.addCommentToEntry(creator, comment);
        Assert.assertNotNull(comment);
        EntryInfo entryInfo = controller.retrieveEntryDetails(creator, plasmid);
        Assert.assertNotNull(entryInfo);
        Assert.assertEquals(1, entryInfo.getComments().size());
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
    public void testRetrieveEntryByType() throws Exception {

    }

    @Test
    public void testRetrieveStrainsForPlasmid() throws Exception {

    }
}
