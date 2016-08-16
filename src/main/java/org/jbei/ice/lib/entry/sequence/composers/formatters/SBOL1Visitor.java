package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.model.AnnotationLocation;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;
import org.sbolstandard.core.*;

import java.net.URI;
import java.util.*;

/**
 * @author Hector Plahar
 */
public class SBOL1Visitor {

    private final DnaComponent dnaComponent;
    private final String uriString;
    private Set<String> uris;

    public SBOL1Visitor() {
        dnaComponent = SBOLFactory.createDnaComponent();
        uriString = Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/entry";
        uris = new HashSet<>();
    }

    public void visit(Sequence sequence) {
        // ice data model conflates the sequence and component
        Entry entry = sequence.getEntry();

        // Set required properties
        String partId = entry.getPartNumber();
        String dcUri = sequence.getComponentUri();
        if (dcUri == null) {
            dnaComponent.setURI(URI.create(uriString + "/dc#" + partId));
            dnaComponent.setDisplayId(partId);
        } else {
            dnaComponent.setURI(URI.create(dcUri));
            String displayId = StringUtils.isBlank(sequence.getIdentifier()) ?
                    dcUri.substring(dcUri.lastIndexOf("/") + 1) : sequence.getIdentifier();
            dnaComponent.setDisplayId(displayId);
        }
        dnaComponent.setName(entry.getName());
        dnaComponent.setDescription(entry.getShortDescription());

        DnaSequence dnaSequence = SBOLFactory.createDnaSequence();
        dnaSequence.setNucleotides(sequence.getSequence());

        String dsUri = sequence.getUri();
        if (dsUri == null || dsUri.isEmpty()) {
            dsUri = sequence.getFwdHash();
            dnaSequence.setURI(URI.create(uriString + "/ds#" + dsUri));
        } else {
            dnaSequence.setURI(URI.create(dsUri));
        }

        dnaSequence.setNucleotides(sequence.getSequence());
        dnaComponent.setDnaSequence(dnaSequence);

        List<SequenceFeature> features = new ArrayList<>(sequence.getSequenceFeatures());

        Collections.sort(features, new SequenceFeatureComparator());

        for (SequenceFeature feature : features)
            visit(feature);
    }

    public DnaComponent getDnaComponent() {
        return dnaComponent;
    }

    public void visit(SequenceFeature feature) {
        SequenceAnnotation annotation = SBOLFactory.createSequenceAnnotation();
        String uri = feature.getUri();

        if (uri == null || uri.isEmpty()) {
            uri = UUID.randomUUID().toString();
            annotation.setURI(URI.create(uriString + "/sa#" + uri));
        } else {
            if (uris.contains(uri))
                return;

            uris.add(uri);
            annotation.setURI(URI.create(uri));
        }

        AnnotationLocation location = null;
        if (feature.getAnnotationLocations() != null && !feature.getAnnotationLocations().isEmpty()) {
            location = (AnnotationLocation) feature.getAnnotationLocations().toArray()[0];

            if (location.getGenbankStart() <= location.getEnd()) {
                annotation.setBioStart(location.getGenbankStart());
                annotation.setBioEnd(location.getEnd());
            }
        }

        annotation.setStrand(feature.getStrand() == 1 ? StrandType.POSITIVE : StrandType.NEGATIVE);

        DnaComponent subComponent = SBOLFactory.createDnaComponent();
        String dcUri = feature.getFeature().getUri();
        if (dcUri == null || dcUri.isEmpty()) {
            dcUri = UUID.randomUUID().toString();
            subComponent.setURI(URI.create(uriString + "/dc#" + dcUri));
            subComponent.setDisplayId(dcUri);
        } else {
            if (uris.contains(dcUri))
                return;

            uris.add(dcUri);
            subComponent.setURI(URI.create(dcUri));
            String displayId = StringUtils.isBlank(feature.getFeature().getIdentification()) ?
                    dcUri.substring(dcUri.lastIndexOf("/") + 1) : feature.getFeature().getIdentification();
            subComponent.setDisplayId(displayId);
        }

        subComponent.setName(feature.getName());
        subComponent.addType(IceSequenceOntology.getURI(feature.getGenbankType()));
        annotation.setSubComponent(subComponent);

        // add a dna sequence for cases where the feature wraps around the origin
        if (location != null && location.getGenbankStart() > location.getEnd()) {
            DnaSequence subComponentSequence = SBOLFactory.createDnaSequence();
            String sequence = location.getSequenceFeature().getSequence().getSequence();
            StringBuilder builder = new StringBuilder();
            int start = location.getGenbankStart() - 1;
            if (start < sequence.length()) {
                builder.append(sequence.substring(start, sequence.length()));
            } else {
                Logger.warn("Encountered feature with start " + location
                        .getGenbankStart() + " and sequence length of " + sequence.length());
                return;
            }
            builder.append(sequence.substring(0, location.getEnd()));
            subComponentSequence.setNucleotides(builder.toString());
            subComponentSequence.setURI(URI.create(uriString + "/ds#" + UUID.randomUUID().toString()));
            subComponent.setDnaSequence(subComponentSequence);
        }

        dnaComponent.addAnnotation(annotation);
    }
}
