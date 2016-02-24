package org.jbei.ice.lib.net;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

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
        Strain strain = createStrainObject(account);
        Plasmid plasmid = createPlasmidObject(account);

        // link the two
        PartData strainData = strain.toDataTransferObject();
        PartData plasmidData = plasmid.toDataTransferObject();
        strainData.getLinkedParts().add(plasmidData);

        strainData = parts.receiveTransferredEntry(strainData);
        Assert.assertNotNull(strainData);

        strain = (Strain) DAOFactory.getEntryDAO().get(strainData.getId());
        Assert.assertNotNull(strain);
        Assert.assertTrue(strain.getVisibility() == Visibility.TRANSFERRED.getValue());

        plasmid = (Plasmid) DAOFactory.getEntryDAO().get(strainData.getLinkedParts().get(0).getId());
        Assert.assertNotNull(plasmid);
        Assert.assertTrue(plasmid.getVisibility() == Visibility.TRANSFERRED.getValue());

        Assert.assertTrue(strain.getLinkedEntries().contains(plasmid));

        // create new plasmid
        Plasmid plasmid2 = createPlasmidObject(account);
        strainData.getLinkedParts().add(plasmid2.toDataTransferObject());
        Assert.assertEquals(2, strainData.getLinkedParts().size());

        // transfer strain (with 2 linked plasmids)
        PartData received = parts.receiveTransferredEntry(strainData);
        Assert.assertNotNull(received);

        // check return
        strain = (Strain) DAOFactory.getEntryDAO().get(strainData.getId());
        Assert.assertNotNull(strain);

        plasmid = (Plasmid) DAOFactory.getEntryDAO().get(strainData.getLinkedParts().get(0).getId());
        Assert.assertNotNull(plasmid);
        Assert.assertTrue(strain.getLinkedEntries().contains(plasmid));

        plasmid2 = (Plasmid) DAOFactory.getEntryDAO().get(strainData.getLinkedParts().get(1).getId());
        Assert.assertNotNull(plasmid2);
        Assert.assertTrue(strain.getLinkedEntries().contains(plasmid2));
    }

    private Plasmid createPlasmidObject(Account owner) {
        Plasmid plasmid = new Plasmid();
        plasmid.setBackbone("plasmid backone");
        plasmid.setOriginOfReplication("None");
        plasmid.setBioSafetyLevel(1);
        plasmid.setShortDescription("plasmid description");
        plasmid.setName("pLasmid");
        plasmid.setOwner(owner.getFullName());
        plasmid.setOwnerEmail(owner.getEmail());
        plasmid.setCreator(plasmid.getOwner());
        plasmid.setCreatorEmail(plasmid.getOwnerEmail());
        plasmid.setCreationTime(new Date());
        return plasmid;
    }

    private Strain createStrainObject(Account owner) {
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain.setOwner(owner.getFullName());
        strain.setOwnerEmail(owner.getEmail());
        strain.setCreator(strain.getOwner());
        strain.setCreatorEmail(strain.getOwnerEmail());
        strain.setCreationTime(new Date());
        return strain;
    }
}