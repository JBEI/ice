package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;

/**
 * Format to SBOL v 1.1 using libSBOLj
 *
 * @author Hector Plahar, Timothy Ham
 */
public class SbolFormatter extends AbstractFormatter {

    /**
     * Format to SBOL
     */
    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        DnaComponent dnaComponent = SBOLFactory.createDnaComponent();
        String uriString = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        Entry entry = sequence.getEntry();

        // Set required properties
        String partId = entry.getOnePartNumber().getPartNumber();
        dnaComponent.setURI(URI.create(uriString + "/dc#" + partId));
        dnaComponent.setDisplayId(partId);
        dnaComponent.setName(entry.getOneName().getName());
        dnaComponent.setDescription(entry.getShortDescription());

        // Set sequence
        DnaSequence dnaSequence = SBOLFactory.createDnaSequence();
        dnaSequence.setNucleotides(sequence.getSequence());
        dnaSequence.setURI(URI.create(uriString + "/ds#" + sequence.getFwdHash()));
        dnaSequence.setNucleotides(sequence.getSequence());
        dnaComponent.setDnaSequence(dnaSequence);

        // populate the annotations
        Set<SequenceFeature> features = sequence.getSequenceFeatures();
        dnaComponent = addAnnotations(uriString, dnaComponent, features);
        SBOLFactory.write(createXmlDocument(dnaComponent), outputStream);
    }

    private DnaComponent addAnnotations(String uriString, DnaComponent component, Set<SequenceFeature> features) {
        if (features == null || features.isEmpty())
            return component;

        HashSet<String> uriSet = new HashSet<>();

        for (SequenceFeature feature : features) {
            SequenceAnnotation annotation = SBOLFactory.createSequenceAnnotation();
            String annotationHash = feature.getFeature().getHash();
            annotation.setURI(URI.create(uriString + "/sa#" + annotationHash));
            String dcUri = null;

            if (feature.getAnnotationLocations() != null && !feature.getAnnotationLocations().isEmpty()) {
                AnnotationLocation location = (AnnotationLocation) feature.getAnnotationLocations().toArray()[0];
                annotation.setBioStart(location.getGenbankStart());
                annotation.setBioEnd(location.getEnd());
                try {
                    String sequence = feature.getSequence().getSequence().substring(location.getGenbankStart(),
                                                                                    location.getEnd());
                    dcUri = SequenceUtils.calculateSequenceHash(sequence);
                    if (uriSet.contains(dcUri))
                        dcUri = UUID.randomUUID().toString();
                } catch (IndexOutOfBoundsException ioe) {
                    dcUri = UUID.randomUUID().toString();
                }
            }

            annotation.setStrand(feature.getStrand() == 1 ? StrandType.POSITIVE : StrandType.NEGATIVE);
            DnaComponent dnaComponent = SBOLFactory.createDnaComponent();
            dnaComponent.setURI(URI.create(uriString + "/dc#" + dcUri));
            dnaComponent.setDisplayId(feature.getName());
            dnaComponent.setName(feature.getName());
            dnaComponent.addType(IceSequenceOntology.getURI(feature.getGenbankType()));
            annotation.setSubComponent(dnaComponent);
            component.addAnnotation(annotation);
        }

        return component;
    }

    private SBOLDocument createXmlDocument(DnaComponent dnaComponent) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(dnaComponent);
        return document;
    }
}
