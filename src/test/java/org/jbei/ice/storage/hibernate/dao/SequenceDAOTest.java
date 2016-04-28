package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class SequenceDAOTest {

    private SequenceDAO sequenceDAO;

    @Before
    public void setUp() throws Exception {
        sequenceDAO = new SequenceDAO();
        HibernateUtil.initializeMock();
        HibernateUtil.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        HibernateUtil.commitTransaction();
    }

    @Test
    public void testSaveSequence() throws Exception {
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testSaveSequence", false);
        Strain strain = TestEntryCreator.createTestStrain(account);

        // parse sequence and associate with strain
        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(strain);

        Assert.assertNotNull(sequenceDAO.saveSequence(sequence));

        // create second strain and associate with same sequence
        Strain strain2 = TestEntryCreator.createTestStrain(account);
        dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(strain2);
        Assert.assertNotNull(sequenceDAO.saveSequence(sequence));

        // same sequence so same number of features
        List<SequenceFeature> sequence1Feature = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(strain);
        List<SequenceFeature> sequence2Feature = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(strain2);
        Assert.assertEquals(sequence1Feature.size(), sequence2Feature.size());

        // same feature
        Assert.assertEquals(sequence1Feature.get(0).getFeature(), sequence2Feature.get(0).getFeature());
    }

    @Test
    public void testUpdateSequence() throws Exception {
        // create account and sequence
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testUpdateSequence", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.saveSequence(sequence);
        Assert.assertNotNull(sequence);

        // update to add new sequence feature
        List<SequenceFeature> newFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(plasmid);
        SequenceFeature sequenceFeature = new SequenceFeature();
        Feature feature = new Feature();
        feature.setSequence("atgtcgaaag");
        feature.setName("test");
        feature.setHash("hash");
        sequenceFeature.setFeature(feature);
        AnnotationLocation location = new AnnotationLocation();
        location.setGenbankStart(1);
        location.setEnd(feature.getSequence().length());
        sequenceFeature.getAnnotationLocations().clear();
        sequenceFeature.getAnnotationLocations().add(location);
        newFeatures.add(sequenceFeature);

        sequence = sequenceDAO.updateSequence(sequence, new HashSet<>(newFeatures));
        Assert.assertNotNull(sequence);
        newFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(plasmid);
        Assert.assertEquals(2, newFeatures.size());
        Assert.assertNotEquals(newFeatures.get(0).getFeature(), newFeatures.get(1).getFeature());
    }

    @Test
    public void testDeleteSequence() throws Exception {

    }

    @Test
    public void testSaveFeature() throws Exception {

    }

    @Test
    public void testGetByEntry() throws Exception {
        // create account and sequence
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testGetByEntry", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);
        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.saveSequence(sequence);
        Assert.assertNotNull(sequence);
        Assert.assertNotNull(sequenceDAO.getByEntry(plasmid));
    }

    @Test
    public void testHasSequence() throws Exception {
        // create account and sequence
        Account account = AccountCreator.createTestAccount("SequenceDAOTest.testHasSequence", false);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);

        Assert.assertFalse(sequenceDAO.hasSequence(plasmid.getId()));

        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.saveSequence(sequence);
        Assert.assertNotNull(sequence);

        Assert.assertTrue(sequenceDAO.hasSequence(plasmid.getId()));
    }

    @Test
    public void testGetSequenceFilename() throws Exception {

    }

    @Test
    public void testHasOriginalSequence() throws Exception {

    }

    @Test
    public void testGetSequence() throws Exception {

    }

    static String sequenceString =
            "LOCUS       pj5_00001                804 bp    dna     circular UNK \n" +
                    "ACCESSION   pj5_00001\n" +
                    "VERSION     pj5_00001.1\n" +
                    "KEYWORDS    .\n" +
                    "FEATURES             Location/Qualifiers\n" +
                    "     misc_feature    156..804\n" +
                    "                     /label=pSMR0100\n" +
                    "ORIGIN      \n" +
                    "        1 atgtcgaaag ctacatataa ggaacgtgct gctactcatc ctagtcctgt tgctgccaag\n" +
                    "       61 ctatttaata tcatgcacga aaagcaaaca aacttgtgtg cttcattgga tgttcgtacc\n" +
                    "      121 accaaggaat tactggagtt agttgaagca ttaggtccca aaatttgttt actaaaaaca\n" +
                    "      181 catgtggata tcttgactga tttttccatg gagggcacag ttaagccgct aaaggcatta\n" +
                    "      241 tccgccaagt acaatttttt actcttcgaa gacagaaaat ttgctgacat tggtaataca\n" +
                    "      301 gtcaaattgc agtactctgc gggtgtatac agaatagcag aatgggcaga cattacgaat\n" +
                    "      361 gcacacggtg tggtgggccc aggtattgtt agcggtttga agcaggcggc agaagaagta\n" +
                    "      421 acaaaggaac ctagaggcct tttgatgtta gcagaattgt catgcaaggg ctccctatct\n" +
                    "      481 actggagaat atactaaggg tactgttgac attgcgaaga gcgacaaaga ttttgttatc\n" +
                    "      541 ggctttattg ctcaaagaga catgggtgga agagatgaag gttacgattg gttgattatg\n" +
                    "      601 acacccggtg tgggtttaga tgacaaggga gatgcattgg gtcaacagta tagaaccgtg\n" +
                    "      661 gatgatgtgg tttctacagg atctgacatt attattgttg gaagaggact atttgcaaag\n" +
                    "      721 ggaagggatg ctaaggtaga gggtgaacgt tacagaaaag caggctggga agcatatttg\n" +
                    "      781 agaagatgcg gccagcaaaa ctaa\n" +
                    "//";
}