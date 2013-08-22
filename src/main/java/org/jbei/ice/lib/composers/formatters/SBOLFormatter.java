package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;

import org.jbei.ice.lib.models.Sequence;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;

/**
 * Format to SBOL v1.1 using libSBOLj
 *
 * @author Hector Plahar, Timothy Ham
 */
public class SBOLFormatter extends AbstractFormatter {

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        SBOLVisitor visitor = new SBOLVisitor();
        visitor.visit(sequence);
        SBOLFactory.write(createXmlDocument(visitor.getDnaComponent()), outputStream);
    }

    private SBOLDocument createXmlDocument(DnaComponent dnaComponent) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(dnaComponent);
        return document;
    }
}
