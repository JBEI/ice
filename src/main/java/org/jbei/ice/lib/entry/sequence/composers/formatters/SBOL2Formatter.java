package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class SBOL2Formatter extends AbstractFormatter {

    public SBOL2Formatter() {
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {

        SBOLDocument doc = new SBOLDocument();

        try {
            SBOL2Visitor visitor = new SBOL2Visitor(doc);
            visitor.visit(sequence);
            doc.write(outputStream);
        } catch (SBOLValidationException | SBOLConversionException | URISyntaxException e) {
            throw new FormatterException(e);
        }
    }
}