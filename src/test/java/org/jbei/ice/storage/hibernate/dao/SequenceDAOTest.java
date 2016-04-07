package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class SequenceDAOTest {

    @Before
    public void setUp() throws Exception {
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

        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(strain);

        SequenceDAO sequenceDAO = new SequenceDAO();
        Assert.assertNotNull(sequenceDAO.saveSequence(sequence));

        Strain strain2 = TestEntryCreator.createTestStrain(account);
        dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(strain2);
        Assert.assertNotNull(sequenceDAO.saveSequence(sequence));

        SequenceFeatureDAO sequenceFeatureDAO = new SequenceFeatureDAO();
        List<SequenceFeature> list = sequenceFeatureDAO.getAll();
        Assert.assertNotNull(list);

        // separate sequence features
        Assert.assertEquals(2, list.size());

        // same feature
        Assert.assertEquals(list.get(0).getFeature().getId(), list.get(1).getFeature().getId());
    }

    @Test
    public void testUpdateSequence() throws Exception {

    }

    @Test
    public void testDeleteSequence() throws Exception {

    }

    @Test
    public void testSaveFeature() throws Exception {

    }

    @Test
    public void testGetByEntry() throws Exception {

    }

    @Test
    public void testHasSequence() throws Exception {

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
                    "     misc_feature    1..804\n" +
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