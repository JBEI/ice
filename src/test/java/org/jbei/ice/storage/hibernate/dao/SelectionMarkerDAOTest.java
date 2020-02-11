package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class SelectionMarkerDAOTest extends HibernateRepositoryTest {

    private SelectionMarkerDAO dao = new SelectionMarkerDAO();

    @Test
    public void testGetMatchingSelectionMarkers() throws Exception {
        // TODO
//        String email = "testGetMatchingSelectionMarkers";
//        Account account = AccountCreator.createTestAccount(email, false);
//        Assert.assertNotNull(account);
//
//        Entry entry = InfoToModelFactory.infoToEntry(new PartData(EntryType.STRAIN));
//        Strain strain = (Strain) entry;
//        strain.setName("sTrain");
//        strain.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
//        strain.setShortDescription("test strain");
//
//        SelectionMarker marker = new SelectionMarker();
//        marker.setName("xkcd");
//        SelectionMarker marker2 = new SelectionMarker();
//        marker2.setName("test");
//
//        Set<SelectionMarker> markerSet = new HashSet<>();
//        markerSet.add(marker);
//        markerSet.add(marker2);
//        strain.setSelectionMarkers(markerSet);
//
//        strain = (Strain) DAOFactory.getEntryDAO().create(strain);
//        Assert.assertNotNull(strain);
//
//        Assert.assertEquals(2, strain.getSelectionMarkers().size());
//
//        List<String> results = dao.getMatchingSelectionMarkers("xkcd", 5);
//        Assert.assertEquals(1, results.size());
//
//        List<String> res = dao.getMatchingSelectionMarkers("tes", 5);
//        Assert.assertEquals(1, res.size());
//        Assert.assertEquals("test", res.get(0));
    }
}