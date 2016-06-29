package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }
}
