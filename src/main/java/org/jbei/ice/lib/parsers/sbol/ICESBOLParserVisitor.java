package org.jbei.ice.lib.parsers.sbol;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jbei.ice.lib.bulkupload.DNAFeatureComparator;
import org.jbei.ice.lib.composers.formatters.IceSequenceOntology;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;
import org.sbolstandard.core.util.SBOLBaseVisitor;

/**
 * Implementation of the ICESBOLParserVisitor
 *
 * @author Hector Plahar
 */
public class ICESBOLParserVisitor extends SBOLBaseVisitor<RuntimeException> {

    private FeaturedDNASequence featuredDNASequence;
    private BulkUploadAutoUpdate update;

    public ICESBOLParserVisitor() {
    }

    public ICESBOLParserVisitor(EntryType type) {
        update = new BulkUploadAutoUpdate(type);
    }

    public BulkUploadAutoUpdate getUpdate() {
        return this.update;
    }

    public FeaturedDNASequence getFeaturedDNASequence() {
        Collections.sort(featuredDNASequence.getFeatures(), new DNAFeatureComparator());
        return featuredDNASequence;
    }

    @Override
    public void visit(DnaComponent component) {
        if (featuredDNASequence == null) {
            featuredDNASequence = new FeaturedDNASequence();

            if (update != null) {
                String name = component.getName();
                if (name == null || name.trim().isEmpty())
                    update.getKeyValue().put(EntryField.NAME, name);
                else {
                    update.getKeyValue().put(EntryField.NAME, name);
                    update.getKeyValue().put(EntryField.ALIAS, component.getDisplayId());
                }
                update.getKeyValue().put(EntryField.SUMMARY, component.getDescription());
            }

            featuredDNASequence.setName(component.getName());
            featuredDNASequence.setIdentifier(component.getDisplayId());
            featuredDNASequence.setDescription(component.getDescription());

            featuredDNASequence.setDcUri(component.getURI().toString());

            DnaSequence sequence = component.getDnaSequence();
            if (sequence != null) {
                featuredDNASequence.setSequence(sequence.getNucleotides());
                featuredDNASequence.setUri(sequence.getURI().toString());
            }
        }

        List<SequenceAnnotation> annotations = component.getAnnotations();
        Logger.debug("Encountered DC " + component.getDisplayId());

        if (!annotations.isEmpty()) {
            // iterate sorted annotations for top level
            for (SequenceAnnotation sequenceAnnotation : annotations) {
                int strand = sequenceAnnotation.getStrand() == StrandType.POSITIVE ? 1 : -1;
                Pair relative = new Pair(sequenceAnnotation.getBioStart(), sequenceAnnotation.getBioEnd(), strand);

                walkTree(sequenceAnnotation, relative);

                DNAFeature feature = createDNAFeature(sequenceAnnotation, relative);
                DNAFeatureLocation location = feature.getLocations().get(0);
                featuredDNASequence.getFeatures().add(feature);

                Logger.debug("Adding feature [" + location.getGenbankStart() + ", " + location.getEnd() + "] for "
                                     + feature.getIdentifier());
            }
        }
    }

    private void walkTree(SequenceAnnotation parent, Pair relativePair) {
        List<SequenceAnnotation> annotations = parent.getSubComponent().getAnnotations();
        if (!annotations.isEmpty()) {
            for (SequenceAnnotation sequenceAnnotation : annotations) {
                int strand = sequenceAnnotation.getStrand() == StrandType.POSITIVE ? (relativePair
                        .getStrand()) : (relativePair.getStrand() * -1);

                Pair newRelativePair;
                if (strand > 0)
                    newRelativePair = calculatePairForPositiveStrand(sequenceAnnotation, relativePair);
                else
                    newRelativePair = calculatePairForNegativeStrand(sequenceAnnotation, relativePair);

                walkTree(sequenceAnnotation, newRelativePair);

                DNAFeature feature = createDNAFeature(sequenceAnnotation, newRelativePair);
                DNAFeatureLocation location = feature.getLocations().get(0);
                featuredDNASequence.getFeatures().add(feature);

                Logger.debug("Adding feature " + strand + "[" + location.getGenbankStart() + ", " + location
                        .getEnd() + "] for " + feature.getIdentifier());
            }
        }
    }

    static Pair calculatePairForPositiveStrand(SequenceAnnotation sequenceAnnotation, Pair relativePair) {
        int childStart = sequenceAnnotation.getBioStart();

        // generate new pair relative to parent
        int length = childStart == 1 ? sequenceAnnotation.getBioEnd() :
                sequenceAnnotation.getBioEnd() - sequenceAnnotation.getBioStart();
        int start;

        // get relative current start
        if (childStart == 1)
            start = relativePair.getFirst(); // start at same location as parent e.g. p:[3, 10] c:[1,5]
        else if (relativePair.getFirst() == 1)
            start = childStart;             // start at child location e.g. p:[1,10] c:[3,5]
        else
            start = childStart + relativePair.getFirst();  // p:[3,10] c:[3,5] (start at 8)

        int extra = start == 1 ? length : start + length;
        if (extra > relativePair.getSecond())
            extra = relativePair.getSecond();

        return new Pair(start, extra, 1);
    }

    static Pair calculatePairForNegativeStrand(SequenceAnnotation sequenceAnnotation, Pair relativePair) {
        int childStart = sequenceAnnotation.getBioStart();
        int childEnd = sequenceAnnotation.getBioEnd();

        int end = relativePair.getSecond() - (childStart);
        int start = relativePair.getSecond() - (childEnd);
        if (start < relativePair.getFirst()) {
            start = relativePair.getFirst();
        }
        return new Pair(start, end, -1);
    }

    static DNAFeature createDNAFeature(SequenceAnnotation annotation, Pair pair) {
        DNAFeature feature = new DNAFeature();

        // set feature location
        DNAFeatureLocation location = new DNAFeatureLocation();

        location.setGenbankStart(pair.getFirst());
        location.setEnd(pair.getSecond());

        // get sequence strand type and uri
        feature.setStrand(pair.getStrand());
        feature.setUri(annotation.getURI().toString());

        // get sequence annotation type and start and end sequence
        DnaComponent subComponent = annotation.getSubComponent();

        if (subComponent != null) {
            location.setUri(subComponent.getURI().toString());
            String name = subComponent.getName();
            if (name == null || name.trim().isEmpty())
                name = subComponent.getDisplayId();
            feature.setName(name);
            feature.setIdentifier(subComponent.getDisplayId());

            String featureType = getFeatureType(subComponent.getTypes());
            feature.setType(featureType);
        }

        feature.getLocations().add(location);
        return feature;
    }

    private static String getFeatureType(Collection<URI> types) {
        if (types == null || types.isEmpty())
            return "";

        URI typesURI = (URI) types.toArray()[0];
        if (typesURI != null && typesURI.getRawPath().contains("SO_")) {
            String[] s = typesURI.getRawPath().split("SO_");
            if (s != null && s.length == 2) {
                return (IceSequenceOntology.getFeatureType("SO_" + s[1]));
            }
        }
        return "";
    }

    private static class Pair {
        private final int first;
        private final int second;
        private final int strand;

        private Pair(int first, int second, int strand) {
            this.first = first;
            this.second = second;
            this.strand = strand;
        }

        @Override
        public String toString() {
            return strand + " [" + first + ", " + second + "]";
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        public int getStrand() {
            return strand;
        }
    }
}
