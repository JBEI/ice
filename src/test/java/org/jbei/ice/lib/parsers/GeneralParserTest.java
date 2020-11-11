package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.TestInputSequences;
import org.jbei.ice.storage.hibernate.HibernateRepositoryTest;
import org.junit.Assert;
import org.junit.Test;

public class GeneralParserTest extends HibernateRepositoryTest {

    @Test
    public void parse() {
        FeaturedDNASequence sequence = GeneralParser.parse(TestInputSequences.fromAPE);
        Assert.assertNotNull(sequence);
        Assert.assertEquals(sequence.getFeatures().size(), 25);

        Assert.assertTrue(hasCorrectNotes(sequence, "promoter", "AOX1 promoter", 8));
        Assert.assertTrue(hasCorrectNotes(sequence, "CDS", "alpha-factor secretion signal", 10));
//        Assert.assertTrue(hasCorrectNotes(sequence, "misc_feature", "jgi_PenbrAgRF18_1_353857_MatureChain_29_961", 0));
        Assert.assertTrue(hasCorrectNotes(sequence, "source", "source:synthetic DNA construct", 9));
    }

    private boolean hasCorrectNotes(FeaturedDNASequence sequence, String type, String featureName, int notesCount) {
        for (DNAFeature feature : sequence.getFeatures()) {
            if (feature.getName().equals(featureName)) {
                if (feature.getNotes().size() == notesCount && feature.getType().equalsIgnoreCase(type))
                    return true;
            }
        }

        return false;
    }
}
