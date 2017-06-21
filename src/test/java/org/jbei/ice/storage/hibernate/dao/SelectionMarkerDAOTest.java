package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.SelectionMarker;
import org.jbei.ice.storage.model.Strain;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class SelectionMarkerDAOTest extends HibernateRepositoryTest {

    private SelectionMarkerDAO dao = new SelectionMarkerDAO();

    @Test
    public void testGetMatchingSelectionMarkers() throws Exception {
        String email = "testGetMatchingSelectionMarkers";
        Account account = AccountCreator.createTestAccount(email, false);
        Assert.assertNotNull(account);
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        strain.setShortDescription("test strain");

        SelectionMarker marker = new SelectionMarker();
        marker.setName("xkcd");
        SelectionMarker marker2 = new SelectionMarker();
        marker2.setName("test");

        Set<SelectionMarker> markerSet = new HashSet<>();
        markerSet.add(marker);
        markerSet.add(marker2);
        strain.setSelectionMarkers(markerSet);
        EntryCreator creator = new EntryCreator();
        strain = (Strain) creator.createEntry(account, strain, null);
        Assert.assertNotNull(strain);

        Assert.assertEquals(2, strain.getSelectionMarkers().size());

        List<String> results = dao.getMatchingSelectionMarkers("xkcd", 5);
        Assert.assertEquals(1, results.size());

        List<String> res = dao.getMatchingSelectionMarkers("tes", 5);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("test", res.get(0));
    }
}