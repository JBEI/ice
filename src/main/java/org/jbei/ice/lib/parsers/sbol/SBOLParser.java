package org.jbei.ice.lib.parsers.sbol;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Parse SBOL files that are imported by the user.
 * As an initial support for version 2, it converts the file to genBank and then the ICE genBank parser
 * is used to convert to the data model
 *
 * @author Hector Plahar
 */
public class SBOLParser extends AbstractParser {

    @Override
    public DNASequence parse(String textSequence) throws InvalidFormatParserException {
        try {
            SBOLDocument sbolDocument = SBOLReader.read(new ByteArrayInputStream(textSequence.getBytes()));
            SBOLValidate.validateSBOL(sbolDocument, true, true, false);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SBOLWriter.write(sbolDocument, out, "GENBANK");
            GenBankParser parser = new GenBankParser();
            return parser.parse(new String(out.toByteArray()));
        } catch (Exception e) {
            Logger.error(e);
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        }
    }
}
