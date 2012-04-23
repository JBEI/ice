package org.jbei.ice.lib.parsers;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLValidationException;

/**
 * Parse SBOL (v 1.1) files.
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
        FeaturedDNASequence featuredDnaSequence = null;
        try {
            SBOLDocument document = SBOLFactory.read(new ByteArrayInputStream(textSequence
                    .getBytes()));
            SBOLRootObject content = document.getContents().get(0);
            if (!(content instanceof DnaComponent)) {
                throw new InvalidFormatParserException("Could not parse SBOL file!");
            }
            DnaComponent dnaComponent = (DnaComponent) content;

            featuredDnaSequence = new FeaturedDNASequence();
            featuredDnaSequence.setName(dnaComponent.getName());
            featuredDnaSequence.setSequence(dnaComponent.getDnaSequence().getNucleotides());
            featuredDnaSequence.setIdentifier(dnaComponent.getDisplayId());
            featuredDnaSequence.setIsCircular(false);

        } catch (SBOLValidationException e) {
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        } catch (IOException e) {
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        }

        return featuredDnaSequence;
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }

}
