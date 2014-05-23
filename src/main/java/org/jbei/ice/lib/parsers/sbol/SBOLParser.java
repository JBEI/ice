package org.jbei.ice.lib.parsers.sbol;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.vo.DNASequence;

import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLValidationException;

/**
 * Parse SBOL (v 1.1) files that are imported by the user
 *
 * @author Hector Plahar, Timothy Ham
 */
public class SBOLParser extends AbstractParser {

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
    public DNASequence parse(String textSequence) throws InvalidFormatParserException {
        try {
            SBOLDocument document = SBOLFactory.read(new ByteArrayInputStream(textSequence.getBytes()));
            ICESBOLParserVisitor visitor = new ICESBOLParserVisitor();

            // walk top level object
            // TODO : throw exception on multiple top level or ask user
            for (SBOLRootObject rootObject : document.getContents()) {
                rootObject.accept(visitor);
                break;
            }

            return visitor.getFeaturedDNASequence();
        } catch (SBOLValidationException | IOException e) {
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        }
    }

    @Override
    public DNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }
}
