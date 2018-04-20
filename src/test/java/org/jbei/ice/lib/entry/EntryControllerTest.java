package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Hector Plahar, Elena Aravina
 */
public class EntryControllerTest {

    private EntryController controller;

    @Before
    public void setUp() {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new EntryController();
    }

    @Test
    public void testUpdatePart() throws Exception {
        Account account = AccountCreator.createTestAccount("testUpdatePart", false);
        String email = account.getEmail();

        long id = TestEntryCreator.createTestPart(email);
        Entry entry = controller.getEntry(Long.toString(id));
        Assert.assertNotNull(entry);
        PartData partData = ModelToInfoFactory.getInfo(entry);
        Assert.assertNotNull(partData);
        partData.setAlias("testUpdatePartAlias");
        long updated = controller.updatePart(email, id, partData);
        Assert.assertEquals(id, updated);
        entry = controller.getEntry(Long.toString(id));
        Assert.assertEquals("testUpdatePartAlias", entry.getAlias());

        // add links
        partData.getLinks().add("a");
        partData.getLinks().add("b");

        controller.updatePart(email, id, partData);
        entry = controller.getEntry(Long.toString(id));
        Assert.assertNotNull(entry);
        Assert.assertEquals(2, entry.getLinks().size());
    }

    @Test
    public void testMoveEntriesToTrash() throws Exception {
        Account account = AccountCreator.createTestAccount("testMoveEntriesToTrash", false);
        String email = account.getEmail();

        long id1 = TestEntryCreator.createTestPart(email);
        Entry entry1 = controller.getEntry(Long.toString(id1));
        Assert.assertNotNull(entry1);
        PartData partData1 = ModelToInfoFactory.getInfo(entry1);
        Assert.assertNotNull(partData1);

        ArrayList<PartData> toTrash1 = new ArrayList<>();
        toTrash1.add(partData1);
        Assert.assertNotNull(toTrash1);
        Assert.assertEquals(toTrash1.size(), 1);

        Assert.assertTrue(controller.moveEntriesToTrash(email, toTrash1));

        long id2 = TestEntryCreator.createTestPart(email);
        Entry entry2 = controller.getEntry(Long.toString(id2));
        Assert.assertNotNull(entry2);
        PartData partData2 = ModelToInfoFactory.getInfo(entry2);
        Assert.assertNotNull(partData2);

        long id3 = TestEntryCreator.createTestPart(email);
        Entry entry3 = controller.getEntry(Long.toString(id3));
        Assert.assertNotNull(entry3);
        PartData partData3 = ModelToInfoFactory.getInfo(entry3);
        Assert.assertNotNull(partData3);

        ArrayList<PartData> toTrash2 = new ArrayList<>();
        toTrash2.add(partData2);
        toTrash2.add(partData3);
        Assert.assertNotNull(toTrash2);
        Assert.assertEquals(toTrash2.size(), 2);

        Assert.assertTrue(controller.moveEntriesToTrash(email, toTrash2));
    }

    @Test
    public void testICE90() throws Exception {
        // create 1 admin accounts and 2 regular user accounts
        Account admin = AccountCreator.createTestAccount("testICE90Admin", true);
        Account user1 = AccountCreator.createTestAccount("testICE90User1", false);
        Account user2 = AccountCreator.createTestAccount("testICE90User2", false);

        // create 10 strains and give read permissions to user1 for five strains
        HashSet<String> ids = new HashSet<>(10);
        HashSet<String> permissions = new HashSet<>(5);
        for (int i = 0; i < 10; i += 1) {
            Strain strain = TestEntryCreator.createTestStrain(admin);
            Assert.assertNotNull(strain);
            Assert.assertTrue(ids.add(strain.getRecordId()));

            if (i > 4)
                continue;

            AccessPermission permission = new AccessPermission();
            permission.setArticle(AccessPermission.Article.ACCOUNT);
            permission.setArticleId(user1.getId());
            permission.setType(AccessPermission.Type.READ_ENTRY);
            permission.setTypeId(strain.getId());

            EntryPermissions entryPermissions = new EntryPermissions(strain.getRecordId(), admin.getEmail());
            AccessPermission result = entryPermissions.add(permission);
            Assert.assertNotNull(result);
            permissions.add(strain.getRecordId());
        }

        // attempt to retrieve

        // admin should retrieve all successfully
        for (String id : ids) {
            PartData data = controller.retrieveEntryDetails(admin.getEmail(), id);
            Assert.assertEquals(data.getRecordId(), id);
        }

        // user 2 should not for all
        for (String id : ids) {
            boolean caught = false;
            try {
                controller.retrieveEntryDetails(user2.getEmail(), id);
            } catch (PermissionException e) {
                caught = true;
            }
            Assert.assertTrue(caught);
        }

        // user 1 should be able to for some
        for (String id : ids) {
            boolean caught = false;
            try {
                PartData data = controller.retrieveEntryDetails(user1.getEmail(), id);
                Assert.assertEquals(data.getRecordId(), id);
            } catch (PermissionException e) {
                caught = true;
            }
            if (permissions.contains(id))
                Assert.assertFalse(caught);
            else
                Assert.assertTrue(caught);
        }
    }

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }
}
