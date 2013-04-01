package org.jbei.ice.test.parsers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.genbank.IceGenbankParser;
import org.jbei.ice.lib.utils.FileUtils;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IceGenbankParserTest {

    private static IceGenbankParser iceGenbankParser = new IceGenbankParser();
    private static String resourceLocation = "/org/jbei/ice/test/parsers/examples/";

    @Test
    public void testGeneArtCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence parsed = tryParsing("AcrR_geneart_badlocus_badsequence.gb");
        parsed.getSequence();
    }

    @Test
    public void testReversedComplementCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence parsed = tryParsing("JBx_000168.gb");
        parsed.getSequence();
    }

    @Test
    public void testMultilineJoinCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence parsed = tryParsing("multiline_join_test.gb");
        List<DNAFeature> features = parsed.getFeatures();
        assertEquals(5, features.get(3).getLocations().size());
    }

    @Test
    public void testApeNoLocusNameCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence parsed = tryParsing("pcI-LasI_ape_no_locusname.ape");
        parsed.getSequence();
    }

    @Test
    public void testPucCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence parsed = tryParsing("pUC19.gb");
        parsed.getSequence();
    }

    @Test
    public void testSynLocusCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("SYNPUC19V.gb");
        parsed.getSequence();
    }

    private FeaturedDNASequence tryParsing(String testFile) throws FileNotFoundException,
            IOException, InvalidFormatParserException {
        testFile = getResourceAsString(resourceLocation + testFile);
        FeaturedDNASequence parsed = (FeaturedDNASequence) iceGenbankParser.parse(testFile);
        if (parsed == null) {
            fail();
        }
        return parsed;
    }

    private String getResourceAsString(String resourceName) throws FileNotFoundException,
            IOException {

        String result = FileUtils.readFileToString(getClass().getResource(resourceName).getFile());

        return result;
    }
}
