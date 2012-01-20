package org.jbei.ice.test.parsers;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.parsers.IceGenbankParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.utils.FileUtils;
import org.jbei.ice.lib.vo.IDNASequence;
import org.junit.Test;

public class IceGenbankParserTest {

    private static IceGenbankParser iceGenbankParser = new IceGenbankParser();
    private static String resourceLocation = "/org/jbei/ice/test/parsers/examples/";

    @Test
    public void testGeneArtCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("AcrR_geneart_badlocus_badsequence.gb");
        if (parsed == null) {
            fail();
        }
    }

    @Test
    public void testReversedComplementCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("JBx_000168.gb");
        if (parsed == null) {
            fail();
        }
    }

    public void testMultilineJoinCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("multiline_join_test.gb");
        if (parsed == null) {
            fail();
        }
    }

    @Test
    public void testApeNoLocusNameCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("pcI-LasI_ape_no_locusname.ape");
        if (parsed == null) {
            fail();
        }
    }

    @Test
    public void testPucCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("pUC19.gb");
        if (parsed == null) {
            fail();
        }
    }

    @Test
    public void testSynLocusCase() throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        IDNASequence parsed = tryParsing("SYNPUC19V.gb");
        if (parsed == null) {
            fail();
        }
    }

    private IDNASequence tryParsing(String testFile) throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        testFile = getResourceAsString(resourceLocation + testFile);
        return iceGenbankParser.parse(testFile);
    }

    private String getResourceAsString(String resourceName) throws FileNotFoundException,
            IOException {

        String result = FileUtils.readFileToString(getClass().getResource(resourceName).getFile());

        return result;
    }
}
