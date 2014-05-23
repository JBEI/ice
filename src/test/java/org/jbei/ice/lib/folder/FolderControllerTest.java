package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.HibernateUtil;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.ColumnField;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class FolderControllerTest {

    private FolderController controller;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        controller = new FolderController();
    }

    @Test
    public void testCreateNewFolder() throws Exception {
        Account account = AccountCreator.createTestAccount("testCreateNewFolder", false);
        FolderDetails folder = controller.createNewFolder(account, "test", "testing folder creation", null);
        Assert.assertNotNull(folder);
        Folder f = DAOFactory.getFolderDAO().get(folder.getId());
        Assert.assertNotNull(f);
        Assert.assertEquals("test", f.getName());
        Assert.assertEquals("testing folder creation", f.getDescription());
    }

    @Test
    public void testRetrieveFolderContents() throws Exception {
        Account account = AccountCreator.createTestAccount("testRetrieveFolderContents", false);

        // create folder
        FolderDetails folder = controller.createNewFolder(account, "test", "testing folder creation", null);
        Assert.assertNotNull(folder);
        final short size = 105;

        // create 100 test strains
        HashMap<String, Entry> parts = new HashMap<>();
        for (int i = 0; i < size; i += 1) {
            Strain strain = TestEntryCreator.createTestStrain(account);
            Assert.assertNotNull(strain);
            parts.put(strain.getPartNumber(), strain);
        }
        Assert.assertEquals(size, parts.size());

        // add to folder
        Folder added = controller.addFolderContents(account, folder.getId(), new ArrayList<Entry>(parts.values()));
        Assert.assertNotNull(added);

        // keep track to find duplicates
        HashSet<Long> set = new HashSet<>();

        // retrieve (supported sort types created, status, name, part_id, type)
        FolderDetails details = controller.retrieveFolderContents(account.getEmail(), folder.getId(),
                                                                  ColumnField.PART_ID, false,
                                                                  0, 15);
        Assert.assertNotNull(details);

        short pageSize = 15;

        int it = 1;
        while (!details.getEntries().isEmpty()) {
            Assert.assertEquals(pageSize, details.getEntries().size());
            for (PartData partData : details.getEntries()) {
                Assert.assertNotNull(parts.remove(partData.getPartId()));
                Assert.assertFalse(set.contains(partData.getId()));
                set.add(partData.getId());
            }
            // check remaining
            Assert.assertEquals((size - (it * pageSize)), parts.size());
            details = controller.retrieveFolderContents(account.getEmail(), folder.getId(), ColumnField.PART_ID, false,
                                                        pageSize * it, pageSize);
            it += 1;
        }
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }
}
