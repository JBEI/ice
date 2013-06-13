package org.jbei.ice.lib.composers.formatters;

import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.sbolstandard.core.*;

import java.net.URI;
import java.util.*;

/**
 * @author Hector Plahar
 */
public class SBOLVisitor {

    private final DnaComponent dnaComponent;
    private final String uriString;

    public SBOLVisitor() {
        dnaComponent = SBOLFactory.createDnaComponent();
        uriString = Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/entry";
    }

    public void visit(Sequence sequence) {
        // ice data model conflates the sequence and component
        Entry entry = sequence.getEntry();

        // Set required properties
        String partId = entry.getOnePartNumber().getPartNumber();
        String dcUri = sequence.getComponentUri();
        if (dcUri == null) {
            dnaComponent.setURI(URI.create(uriString + "/dc#" + partId));
            dnaComponent.setDisplayId(partId);
        } else {
            dnaComponent.setURI(URI.create(dcUri));
            String displayId = dcUri.substring(dcUri.lastIndexOf("#") + 1);
            dnaComponent.setDisplayId(displayId);
        }
        dnaComponent.setName(entry.getOneName().getName());
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

        Collections.sort(features, new Comparator<SequenceFeature>() {
            @Override
            public int compare(SequenceFeature o1, SequenceFeature o2) {
                if (o1.getUniqueGenbankStart() == null || o2.getUniqueGenbankStart() == null)
                    return 0;

                if (o1.getUniqueGenbankStart().intValue() == o2.getUniqueGenbankStart().intValue()) {
                    if (o1.getUniqueEnd() == null || o2.getUniqueEnd() == null)
                        return 0;

                    return o1.getUniqueEnd().compareTo(o2.getUniqueEnd());
                }
                return o1.getUniqueGenbankStart().compareTo(o2.getUniqueGenbankStart());
            }
        });
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
        } else
            annotation.setURI(URI.create(uri));

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
            subComponent.setURI(URI.create(dcUri));
            String displayId = dcUri.substring(dcUri.lastIndexOf("#") + 1);
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
            builder.append(sequence.substring(location.getGenbankStart() - 1, sequence.length()));
            builder.append(sequence.substring(0, location.getEnd()));
            subComponentSequence.setNucleotides(builder.toString());
            subComponentSequence.setURI(URI.create(uriString + "/ds#" + UUID.randomUUID().toString()));
            subComponent.setDnaSequence(subComponentSequence);
        }

        dnaComponent.addAnnotation(annotation);
    }
}
