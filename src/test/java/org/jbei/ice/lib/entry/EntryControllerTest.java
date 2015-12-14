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

/**
 * @author Hector Plahar
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

    @After
    public void tearDown() {
        HibernateUtil.commitTransaction();
    }
}
