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
public class SBOLFormatter extends AbstractFormatter {

    private final boolean isVersion1;

    public SBOLFormatter(boolean version1) {
        this.isVersion1 = version1;
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        SBOLVisitor visitor = new SBOLVisitor();
        visitor.visit(sequence);
        SBOLDocument sbolDocument = createXmlDocument(visitor.getDnaComponent());
        if (isVersion1) {
            SBOLFactory.write(sbolDocument, outputStream);
            return;
        }

        // convert to sbol 2
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SBOLFactory.write(sbolDocument, out);

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(out.toByteArray());
            org.sbolstandard.core2.SBOLDocument v2 = SBOLReader.read(byteArrayInputStream);
            SBOLWriter.write(v2, outputStream);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private SBOLDocument createXmlDocument(DnaComponent dnaComponent) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(dnaComponent);
        return document;
    }
}
