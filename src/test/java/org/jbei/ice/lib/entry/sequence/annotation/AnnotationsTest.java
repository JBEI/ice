package org.jbei.ice.lib.entry.sequence.annotation;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.Curation;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureDAO;
import org.jbei.ice.storage.model.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class AnnotationsTest {

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
    public void testCurate() throws Exception {
        Account account = AccountCreator.createTestAccount("AnnotationsTest.testCurate", true);
        Plasmid plasmid = TestEntryCreator.createTestPlasmid(account);

        SequenceDAO sequenceDAO = new SequenceDAO();

        Assert.assertFalse(sequenceDAO.hasSequence(plasmid.getId()));

        DNASequence dnaSequence = GeneralParser.getInstance().parse(sequenceString);
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setEntry(plasmid);
        sequence = sequenceDAO.saveSequence(sequence);
        Assert.assertNotNull(sequence);

        SequenceFeatureDAO sequenceFeatureDAO = new SequenceFeatureDAO();
        List<SequenceFeature> sequenceFeatures = sequenceFeatureDAO.getEntrySequenceFeatures(plasmid);
        Assert.assertEquals(1, sequenceFeatures.size());

        Feature feature = sequenceFeatures.get(0).getFeature();
        DNAFeature dnaFeature = feature.toDataTransferObject();
        Curation curation = new Curation();
        curation.setExclude(true);
        dnaFeature.setCuration(curation);

        List<DNAFeature> features = new ArrayList<>();
        features.add(dnaFeature);

        Annotations annotations = new Annotations(account.getEmail());
        annotations.curate(features);

        sequenceFeatures = sequenceFeatureDAO.getEntrySequenceFeatures(plasmid);
        feature = sequenceFeatures.get(0).getFeature();
        Assert.assertTrue(feature.getCuration().isExclude());
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