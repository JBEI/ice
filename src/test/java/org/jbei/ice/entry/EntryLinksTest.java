package org.jbei.ice.entry;

import org.jbei.ice.AccountCreator;
import org.jbei.ice.TestEntryCreator;
import org.jbei.ice.access.PermissionsController;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.storage.hibernate.HibernateConfiguration;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class EntryLinksTest {

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
    public void testAddLink() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testAddLink", false);
        String userId = account.getEmail();
        AccountModel differentAccount = AccountCreator.createTestAccount("testAddLink2", false);

        //create strain and plasmid
        Entry strain = TestEntryCreator.createTestStrain(account);
        Entry plasmid = TestEntryCreator.createTestPlasmid(differentAccount);

        // give each AccountModel read permissions
        AccessPermission accessPermission = new AccessPermission(AccessPermission.Article.ACCOUNT, account.getId(),
            AccessPermission.Type.READ_ENTRY, plasmid.getId(), "");
        PermissionsController permissionsController = new PermissionsController();
        Assert.assertNotNull(permissionsController.addPermission(differentAccount.getEmail(), accessPermission));

        accessPermission = new AccessPermission(AccessPermission.Article.ACCOUNT, differentAccount.getId(),
            AccessPermission.Type.READ_ENTRY, strain.getId(), "");
        Assert.assertNotNull(permissionsController.addPermission(account.getEmail(), accessPermission));

        // add plasmid links to strain
        EntryLinks entryLinks = new EntryLinks(userId, Long.toString(strain.getId()));

        // attempt to add as a parent (expected to fail)
        Assert.assertFalse(entryLinks.addLink(plasmid.toDataTransferObject(), LinkType.PARENT));

        // now add as a child
        Assert.assertTrue(entryLinks.addLink(plasmid.toDataTransferObject(), LinkType.CHILD));

        // add second plasmid
        Entry plasmid2 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertTrue(entryLinks.addLink(plasmid2.toDataTransferObject(), LinkType.CHILD));
    }

    @Test
    public void testGetChildren() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetChildren", false);
        String userId = account.getEmail();

        // create alternate account
        AccountModel account2 = AccountCreator.createTestAccount("testGetChildren2", true);

        // add plasmid links to strain
        Entry strain = TestEntryCreator.createTestStrain(account);
        EntryLinks entryLinks = new EntryLinks(userId, Long.toString(strain.getId()));

        Assert.assertEquals(0, entryLinks.getChildren().size());

        // attempt to add as a parent (expected to fail)
        Entry plasmid1 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertFalse(entryLinks.addLink(plasmid1.toDataTransferObject(), LinkType.PARENT));

        // now add as a child
        Assert.assertTrue(entryLinks.addLink(plasmid1.toDataTransferObject(), LinkType.CHILD));

        // add second plasmid
        Entry plasmid2 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertTrue(entryLinks.addLink(plasmid2.toDataTransferObject(), LinkType.CHILD));

        List<PartData> children = entryLinks.getChildren();
        Assert.assertEquals(2, children.size());

        // create plasmid for alternate account
        Entry alternateStrain = TestEntryCreator.createTestStrain(account2);

        // add as child to account's strain
        EntryLinks alternateLinks = new EntryLinks(account2.getEmail(), Long.toString(strain.getId()));
        Assert.assertTrue(alternateLinks.addLink(alternateStrain.toDataTransferObject(), LinkType.CHILD));

        // account2 should see three children (since it is an admin)
        Assert.assertEquals(3, alternateLinks.getChildren().size());

        // but AccountModel should still see 2 since it does not have read permissions on account2's entry
        Assert.assertEquals(2, entryLinks.getChildren().size());
    }

    @Test
    public void testGetParents() throws Exception {
        AccountModel account = AccountCreator.createTestAccount("testGetParents", false);
        String userId = account.getEmail();

        // add plasmid links to strain
        Entry strain = TestEntryCreator.createTestStrain(account);
        EntryLinks entryLinks = new EntryLinks(userId, Long.toString(strain.getId()));

        Assert.assertEquals(0, entryLinks.getParents().size());

        // attempt to add as a parent (expected to fail)
        Entry plasmid1 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertFalse(entryLinks.addLink(plasmid1.toDataTransferObject(), LinkType.PARENT));

        // now add as a child
        Assert.assertTrue(entryLinks.addLink(plasmid1.toDataTransferObject(), LinkType.CHILD));

        // add second plasmid
        Entry plasmid2 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertTrue(entryLinks.addLink(plasmid2.toDataTransferObject(), LinkType.CHILD));

        List<PartData> children = entryLinks.getChildren();
        Assert.assertEquals(2, children.size());

        EntryLinks plasmidLinks = new EntryLinks(userId, Long.toString(plasmid1.getId()));
        Assert.assertEquals(1, plasmidLinks.getParents().size());

        EntryLinks plasmid2Links = new EntryLinks(userId, Long.toString(plasmid2.getId()));
        Assert.assertEquals(1, plasmid2Links.getParents().size());
    }
}
