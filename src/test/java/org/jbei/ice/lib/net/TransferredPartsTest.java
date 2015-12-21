package org.jbei.ice.lib.net;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class TransferredPartsTest {

    private TransferredParts parts;

    @Before
    public void setUp() throws Exception {
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
        parts = new TransferredParts();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testReceiveTransferredEntry() throws Exception {
        Account account = AccountCreator.createTestAccount("testReceivedTransferredEntry", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        PartData data = strain.toDataTransferObject();
        data.setRecordId(account.getEmail()); // faking record id since this is stored on "this instance"
        PartData createdData = parts.receiveTransferredEntry(data);
        Assert.assertNotNull(createdData);
        // transferring with same record id. should return null
        Assert.assertNull(parts.receiveTransferredEntry(data));

        // retrieve list of transferred
        List<Entry> entries = DAOFactory.getEntryDAO().getByVisibility(null, Visibility.TRANSFERRED, ColumnField.CREATED, false, 0, 1000, null);
        Assert.assertNotNull(entries);
        Assert.assertTrue(entries.size() == 1);
    }
}