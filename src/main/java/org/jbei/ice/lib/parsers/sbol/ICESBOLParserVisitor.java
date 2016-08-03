package org.jbei.ice.lib.parsers.sbol;

import org.jbei.ice.lib.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.bulkupload.DNAFeatureComparator;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.sequence.composers.formatters.IceSequenceOntology;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.DnaSequence;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;
import org.sbolstandard.core.util.SBOLBaseVisitor;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
                int strand;

                if (sequenceAnnotation.getStrand() == null) {
                    strand = 1;
                } else {
                    strand = sequenceAnnotation.getStrand() == StrandType.POSITIVE ? 1 : -1;
                }

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
                int strand;

                if (sequenceAnnotation.getStrand() == null) {
                    strand = relativePair.getStrand();
                } else {
                    strand = sequenceAnnotation.getStrand() == StrandType.POSITIVE ? relativePair
                            .getStrand() : relativePair.getStrand() * -1;
                }

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
        int childEnd = sequenceAnnotation.getBioEnd();

        int length = childEnd - (childStart - 1);

        if (childStart == 1) {
            int start = relativePair.getFirst();
            return new Pair(start, (start + length) - 1, 1);
        }

        int start = (relativePair.getFirst() - 1) + childStart;
        return new Pair(start, (start + length) - 1, 1);
    }

    static Pair calculatePairForNegativeStrand(SequenceAnnotation sequenceAnnotation, Pair relativePair) {
        int childStart = sequenceAnnotation.getBioStart();
        int childEnd = sequenceAnnotation.getBioEnd();

        int end;
        int sLength = childEnd - (childStart - 1);

        if (childStart == 1) {
            end = relativePair.getSecond();
            return new Pair((end - sLength) + 1, end, -1);
        } else {
            end = (relativePair.getSecond() - childStart) + 1;
            return new Pair((end - sLength) + 1, end, -1);
        }
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
            if (s.length == 2) {
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
