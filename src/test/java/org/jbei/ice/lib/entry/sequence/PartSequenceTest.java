package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

/**
 * @author Hector Plahar
 */
public class PartSequenceTest {

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
    public void testGet() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testGet", false);
        Strain strain = TestEntryCreator.createTestStrain(account);
        PartSequence partSequence = new PartSequence(account.getEmail(), strain.getRecordId());
        FeaturedDNASequence sequence = partSequence.get();
        Assert.assertNull(sequence);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(genbank.getBytes());
        SequenceInfo sequenceInfo = partSequence.parseSequenceFile(inputStream, "testFile.gb");
        Assert.assertNotNull(sequenceInfo);
        FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) sequenceInfo.getSequence();
        Assert.assertNotNull(featuredDNASequence);
        Assert.assertEquals(1, featuredDNASequence.getFeatures().size());

        sequence = partSequence.get();
        Assert.assertNotNull(sequence);
        Assert.assertEquals(234, sequence.getSequence().length());
        Assert.assertEquals(1, sequence.getFeatures().size());
    }

    @Test
    public void testParseSequence() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testParseSequence", false);
        PartSequence partSequence = new PartSequence(account.getEmail(), EntryType.PART);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fasta.getBytes());
        SequenceInfo sequenceInfo = partSequence.parseSequenceFile(inputStream, "fasta.fa");
        Assert.assertNotNull(sequenceInfo);
        Assert.assertNotNull(sequenceInfo.getSequence());
    }

    private static String genbank =
            "LOCUS       pTrc                     234 bp    DNA     linear\n" +
                    "ACCESSION   pTrc\n" +
                    "VERSION     pTrc.1\n" +
                    "KEYWORDS    .\n" +
                    "FEATURES             Location/Qualifiers\n" +
                    "     promoter        1..234\n" +
                    "                     /label=pTrc promoter\n" +
                    "                     /vntifkey=\"30\"\n" +
                    "ORIGIN\n" +
                    "        1 cgactgcacg gtgcaccaat gcttctggcg tcaggcagcc atcggaagct gtggtatggc\n" +
                    "       61 tgtgcaggtc gtaaatcact gcataattcg tgtcgctcaa ggcgcactcc cgttctggat\n" +
                    "      121 aatgtttttt gcgccgacat cataacggtt ctggcaaata ttctgaaatg agctgttgac\n" +
                    "      181 aattaatcat ccggctcgta taatgtgtgg aattgtgagc ggataacaat ttca\n" +
                    "//";

    private static String fasta =
            ">org.jbei|test.1| \n" +
                    "ccggcttatcggtcagtttcacttcttcataaaacccgcttcggcgggtttttgcttttacagggcggcaggatgaatga\n" +
                    "ctgtccacgacgctatacccaaaagaaa";
}