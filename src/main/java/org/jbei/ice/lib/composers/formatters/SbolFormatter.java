package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;

/**
 * Format to SBOL v 1.1 using libSBOLj
 * 
 * @author Timothy Ham
 * 
 */
public class SbolFormatter extends AbstractFormatter {

    /**
     * Format to SBOL
     */
    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
        DnaComponent dnaComponent = SBOLFactory.createDnaComponent();
        String uriString = JbeirSettings.getSetting("URI_PREFIX");
        Entry entry = sequence.getEntry();

        // Set required properties
        if (entry != null) {
            dnaComponent.setURI(URI.create(uriString + "/Part:"
                    + entry.getOnePartNumber().getPartNumber()));
            dnaComponent.setDisplayId(entry.getOnePartNumber().getPartNumber());
            dnaComponent.setName(entry.getOneName().getName());
            dnaComponent.setDescription(entry.getShortDescription());
        } else {
            dnaComponent.setURI(URI.create(uriString + "/Part:Undefined"));
            dnaComponent.setDisplayId("Undefined");
            dnaComponent.setName("Undefined");
            dnaComponent.setDescription("");
        }

        // Set sequence
        DnaSequence dnaSequence = SBOLFactory.createDnaSequence();
        dnaSequence.setNucleotides(sequence.getSequence());
        dnaSequence.setURI(URI.create(uriString + "/seq#" + sequence.getFwdHash()));
        dnaComponent.setDnaSequence(dnaSequence);

        // populate the annotations
        /* TODO: SBOL requires that annotations MUST have sub DnaComponents. This means ICE's 
            casual feature annotations that are not sub parts cannot be annotated. When ICE parts
            support sub-parts, then populating annotations will make sense.
        */

        SBOLFactory.write(createXmlDocument(dnaComponent), outputStream);
    }

    private SBOLDocument createXmlDocument(DnaComponent dnaComponent) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(dnaComponent);
        return document;
    }
}
