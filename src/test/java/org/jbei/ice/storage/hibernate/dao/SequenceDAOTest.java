package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceUtil;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.Strain;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Hector Plahar
 */
public class SequenceDAOTest extends HibernateRepositoryTest {

    private SequenceDAO sequenceDAO = new SequenceDAO();

    @Test
    public void testCreate() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testCreate", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);

        // parse sequence and associate with strain
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(strain);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Sequence result = sequenceDAO.getByEntry(strain);
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);
    }

    @Test
    public void testRetrieve() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testRetrieve", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);

        // parse sequence and associate with strain
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(strain);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Sequence result = sequenceDAO.get(sequence.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);
    }

    @Test
    public void testUpdate() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testUpdate", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);

        // parse sequence and associate with strain
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(strain);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Sequence result = sequenceDAO.get(sequence.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);

        // update
        String newSequence = "gatgatgtggtttctacaggatctgacattattattgttggaagaggactattt";
        sequence.setSequence(newSequence);
        sequence = sequenceDAO.update(sequence);
        Assert.assertNotNull(sequence);

        // get
        result = sequenceDAO.get(sequence.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);
        Assert.assertEquals(newSequence, result.getSequence());
        Assert.assertEquals(newSequence, sequence.getSequence());
    }

    @Test
    public void testDelete() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testDelete", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        Assert.assertNotNull(strain);

        // parse sequence and associate with strain
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(strain);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Sequence result = sequenceDAO.get(sequence.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);

        // delete
        sequenceDAO.delete(result);

        // get
        Assert.assertNull(sequenceDAO.get(sequence.getId()));
        Assert.assertNull(sequenceDAO.getByEntry(strain));
    }

    @Test
    public void testGetByEntry() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testGetByEntry", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Sequence result = sequenceDAO.getByEntry(plasmid);
        Assert.assertNotNull(result);
        Assert.assertEquals(result, sequence);

        Assert.assertNull(sequenceDAO.getByEntry(null));
    }

    @Test
    public void testSequenceString() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testSequenceString", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // get
        Optional<String> result = sequenceDAO.getSequenceString(plasmid);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), dnaSequence.getSequence());
    }

    @Test
    public void testHasSequence() throws Exception {
        Account account1 = AccountCreator.createTestAccount("SequenceDAOTest.testHasSequence1", false);
        Account account2 = AccountCreator.createTestAccount("SequenceDAOTest.testHasSequence2", false);
        Account account3 = AccountCreator.createTestAccount("SequenceDAOTest.testHasSequence3", false);

        Plasmid plasmid1 = TestEntryCreator.createTestPlasmid(account1);
        Plasmid plasmid2 = TestEntryCreator.createTestPlasmid(account2);
        Plasmid plasmid3 = TestEntryCreator.createTestPlasmid(account3);

        // create sequence for plasmid1
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid1);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // create sequence for plasmid3
        dnaSequence = GeneralParser.parse(sequenceString + "atc");
        sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid3);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // test
        Assert.assertTrue(sequenceDAO.hasSequence(plasmid1.getId()));
        Assert.assertFalse(sequenceDAO.hasSequence(plasmid2.getId()));
        Assert.assertTrue(sequenceDAO.hasSequence(plasmid3.getId()));
    }

    @Test
    public void testGetSequenceFilename() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testGetSequenceFilename", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);

        sequence.setFileName("testFile.gb");
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        String filename = sequenceDAO.getSequenceFilename(plasmid);
        Assert.assertEquals("testFile.gb", filename);
    }

    @Test
    public void testHasOriginalSequence() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testHasOriginalSequence", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);

        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // test
        Assert.assertTrue(sequenceDAO.hasOriginalSequence(plasmid.getId()));

        // test with another sequence
        Plasmid plasmid2 = TestEntryCreator.createTestPlasmid(account);
        dnaSequence = GeneralParser.parse(sequenceString + "atc");
        sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid2);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        Assert.assertFalse(sequenceDAO.hasOriginalSequence(plasmid2.getId()));
    }

    @Test
    public void testGetSequence() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testGetSequence", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);

        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        Assert.assertNotNull(sequenceDAO.getSequence(0));
    }

    @Test
    public void testGetSequenceCount() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testGetSequenceCount", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid);

        // parse sequence and associate with plasmid
        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        // create 3 additional sequences
        Plasmid plasmid2 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid2);

        dnaSequence = GeneralParser.parse(sequenceString + "a");
        sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid2);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        Plasmid plasmid3 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid3);
        dnaSequence = GeneralParser.parse(sequenceString + "t");
        sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid3);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        Plasmid plasmid4 = TestEntryCreator.createTestPlasmid(account);
        Assert.assertNotNull(plasmid4);
        dnaSequence = GeneralParser.parse(sequenceString + "c");
        sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        Assert.assertNotNull(sequence);
        sequence.setEntry(plasmid4);
        sequence = sequenceDAO.create(sequence);
        Assert.assertNotNull(sequence);

        Assert.assertTrue(sequenceDAO.getSequenceCount() >= 4);
    }

//    @Test
//    public void testUpdateSequence() throws Exception {
//        // create account and sequence
//        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testUpdateSequence", false);
//        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
//        FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
//        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
//        sequence.setEntry(plasmid);
//        sequence = sequenceDAO.create(sequence);
//        Assert.assertNotNull(sequence);
//
//        // update to add new sequence feature
//        List<SequenceFeature> newFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(plasmid);
//        SequenceFeature sequenceFeature = new SequenceFeature();
//        Feature feature = new Feature();
//        feature.setSequence("atgtcgaaag");
//        feature.setName("test");
//        feature.setHash("hash");
//        sequenceFeature.setFeature(feature);
//        AnnotationLocation location = new AnnotationLocation();
//        location.setGenbankStart(1);
//        location.setEnd(feature.getSequence().length());
//        sequenceFeature.getAnnotationLocations().clear();
//        sequenceFeature.getAnnotationLocations().add(location);
//        newFeatures.add(sequenceFeature);
//
//        sequence = (sequence, new HashSet<>(newFeatures));
//        Assert.assertNotNull(sequence);
//        newFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(plasmid);
//        Assert.assertEquals(2, newFeatures.size());
//        Assert.assertNotEquals(newFeatures.get(0).getFeature(), newFeatures.get(1).getFeature());
//    }

    private static String sequenceString = ">fasta\n" +
            "gatgatgtggtttctacaggatctgacattattattgttggaagaggactatttgcaaag";
}