package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.vo.IDNASequence;

public class ApeParser extends GenbankParser {
    private static final String APE_PARSER = "ApE";

    @Override
    public String getName() {
        return APE_PARSER;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        return super.parse(textSequence);
    }
}
