package org.jbei.ice.lib.folder.collection;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
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
public class CollectionEntriesTest {

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.rollbackTransaction();
    }

    @Test
    public void testGetEntries() throws Exception {
        Account account = AccountCreator.createTestAccount("CollectionEntriesTest.testGetEntries", false);
        Assert.assertNotNull(account);
        CollectionEntries entries = new CollectionEntries(account.getEmail(), CollectionType.AVAILABLE);
        Results<PartData> results = entries.getEntries(ColumnField.CREATED, true, 0, 10);
        Assert.assertNotNull(results);
    }

    @Test
    public void testGetPersonalEntries() throws Exception {

    }

    @Test
    public void testGetAvailableEntries() throws Exception {

    }

    @Test
    public void testGetSharedEntries() throws Exception {

    }

    @Test
    public void testGetEntriesByVisibility() throws Exception {
        Account account = AccountCreator.createTestAccount("CollectionEntriesTest.testGetEntriesByVisibility", false);
        Assert.assertNotNull(account);
        long id = TestEntryCreator.createTestPart(account.getEmail());
        Entry entry = DAOFactory.getEntryDAO().get(id);
        entry.setVisibility(Visibility.DRAFT.getValue());
        DAOFactory.getEntryDAO().update(entry);
        CollectionEntries collectionEntries = new CollectionEntries(account.getEmail(), CollectionType.DRAFTS);
        Results<PartData> results = collectionEntries.getEntries(ColumnField.CREATED, true, 0, 13);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.getData().size());
        Assert.assertEquals(1, results.getResultCount());
    }
}