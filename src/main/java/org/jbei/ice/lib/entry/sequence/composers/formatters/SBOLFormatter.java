package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Format to SBOL using libSBOLj
 *
 * @author Hector Plahar
 */
public class SBOLFormatter extends AbstractFormatter {

    public SBOLFormatter() {
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        SBOLVisitor visitor = new SBOLVisitor();
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
