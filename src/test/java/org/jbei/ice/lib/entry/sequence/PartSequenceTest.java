package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.AccountCreator;
import org.jbei.ice.lib.TestEntryCreator;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Strain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

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
        SequenceInfo sequenceInfo = partSequence.parseSequenceFile(inputStream, "testFile.gb", false);
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
        SequenceInfo sequenceInfo = partSequence.parseSequenceFile(inputStream, "fasta.fa", false);
        Assert.assertNotNull(sequenceInfo);
        Assert.assertNotNull(sequenceInfo.getSequence());
    }

    @Test
    public void testSave() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testSave", false);
        PartSequence partSequence = new PartSequence(account.getEmail(), EntryType.PLASMID);
        FeaturedDNASequence sequence = GeneralParser.parse(genbank);
        Assert.assertNotNull(sequence);
        partSequence.save(sequence);

        // compare
        PartSequence existingSequence = new PartSequence(account.getEmail(), partSequence.get().getIdentifier());
        FeaturedDNASequence dnaSequence = existingSequence.get();
        Assert.assertEquals(1, dnaSequence.getFeatures().size());
        Assert.assertEquals(234, dnaSequence.getSequence().length());

        // try to save again
        boolean caught = false;
        try {
            existingSequence.save(sequence);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        Assert.assertTrue(caught);
    }

    private void compareFeatures(DNAFeature feature, DNAFeature compare) {
        Assert.assertEquals(feature.getName(), compare.getName());
        Assert.assertEquals(feature.getType(), compare.getType());
        Assert.assertEquals(feature.getStrand(), compare.getStrand());
        Assert.assertEquals(feature.getSequence(), compare.getSequence());
        Assert.assertEquals(feature.getLocations().get(0).getGenbankStart(), compare.getLocations().get(0).getGenbankStart());
        Assert.assertEquals(feature.getLocations().get(0).getEnd(), compare.getLocations().get(0).getEnd());
    }

    @Test
    public void testUpdate() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testUpdate", false);
        PartSequence partSequence = new PartSequence(account.getEmail(), EntryType.PART);
        Assert.assertNotNull(partSequence);

        // parse sequence
        ByteArrayInputStream inputStream = new ByteArrayInputStream(genbank.getBytes());
        SequenceInfo sequenceInfo = partSequence.parseSequenceFile(inputStream, "testFile2.gb", false);
        Assert.assertNotNull(sequenceInfo);

        // remove all existing features and add new feature
        FeaturedDNASequence dnaSequence = partSequence.get();
        dnaSequence.getFeatures().clear();

        // todo : what happens when location is specified but no sequence (or they differ?)
        DNAFeature feature = new DNAFeature();
        feature.setName("test");
        feature.setType("misc_feature");
        feature.getLocations().add(new DNAFeatureLocation(61, 89));
        dnaSequence.getFeatures().add(feature);
        partSequence.update(dnaSequence);

        // check for correct update
        List<DNAFeature> currentFeatures = partSequence.get().getFeatures();
        Assert.assertEquals(1, currentFeatures.size());
        Assert.assertEquals(sequenceInfo.getSequence().getSequence(), dnaSequence.getSequence());
        compareFeatures(feature, currentFeatures.get(0));

        // change feature location
        feature.getLocations().clear();
        feature.getLocations().add(new DNAFeatureLocation(20, 40));
        dnaSequence.getFeatures().clear();
        dnaSequence.getFeatures().add(feature);
        partSequence.update(dnaSequence);

        // check for correct update
        currentFeatures = partSequence.get().getFeatures();
        Assert.assertEquals(1, currentFeatures.size());
        Assert.assertEquals(sequenceInfo.getSequence().getSequence(), dnaSequence.getSequence());
        compareFeatures(feature, currentFeatures.get(0));

        // add a second feature
        DNAFeature secondFeature = new DNAFeature();
        secondFeature.setName("test2");
        secondFeature.setType("promoter");
        secondFeature.getLocations().add(new DNAFeatureLocation(0, 10));
        dnaSequence.getFeatures().add(secondFeature);
        partSequence.update(dnaSequence);

        // check
        FeaturedDNASequence featuredDNASequence = partSequence.get();
        Assert.assertNotNull(featuredDNASequence);
        currentFeatures = featuredDNASequence.getFeatures();
        Assert.assertEquals(2, currentFeatures.size());
        if (!"test".equalsIgnoreCase(currentFeatures.get(0).getName())) {
            compareFeatures(feature, currentFeatures.get(1));
            compareFeatures(secondFeature, currentFeatures.get(0));
        } else {
            compareFeatures(feature, currentFeatures.get(0));
            compareFeatures(secondFeature, currentFeatures.get(1));
        }
    }

    @Test
    public void testDelete() throws Exception {
        Account account = AccountCreator.createTestAccount("PartSequenceTest.testDelete", false);
        PartSequence partSequence = new PartSequence(account.getEmail(), EntryType.PLASMID);
        FeaturedDNASequence sequence = GeneralParser.parse(genbank);
        Assert.assertNotNull(sequence);
        partSequence.save(sequence);
        Assert.assertNotNull(partSequence.get());
        partSequence.delete();
        Assert.assertNull(partSequence.get());
    }

    private static String genbank =
            "LOCUS       pTrc                     234 bp    DNA     linear\n" +
                    "ACCESSION   pTrc\n" +
                    "VERSION     pTrc.1\n" +
                    "KEYWORDS    .\n" +
                    "FEATURES             Location/Qualifiers\n" +
                    "     promoter        1..234\n" +
                    "                     /label=\"pTrc promoter\"\n" +
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