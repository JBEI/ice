package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Format to SBOL using libSBOLj
 *
 * @author Hector Plahar
 */
public class SBOL1Formatter extends AbstractFormatter {

    public SBOL1Formatter() {
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        SBOL1Visitor visitor = new SBOL1Visitor();
        visitor.visit(sequence);
        SBOLDocument sbolDocument = createXmlDocument(visitor.getDnaComponent());
        SBOLFactory.write(sbolDocument, outputStream);
    }

    private SBOLDocument createXmlDocument(DnaComponent dnaComponent) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(dnaComponent);
        return document;
    }
}
