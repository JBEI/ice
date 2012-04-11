package org.jbei.ice.lib.parsers;

import org.jbei.ice.lib.vo.IDNASequence;

/**
 * Parse and generate SBOL (v 1.1) files.
 * 
 * @author Timothy Ham
 * 
 */
public class SbolParser extends AbstractParser {

    private static final String SBOL_PARSER = "SBOL";
    @Override
    public String getName() {
        return SBOL_PARSER;
    }

    @Override
    public Boolean hasErrors() {
        // This parser cannot succeed with errors, so always return false, or fail.
        return false;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }

}
