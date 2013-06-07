package org.jbei.ice.lib.parsers.sbol;

import org.jbei.ice.lib.composers.formatters.IceSequenceOntology;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.sbolstandard.core.*;
import org.sbolstandard.core.util.SBOLBaseVisitor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Parse SBOL (v 1.1) files that are imported by the user
 *
 * @author Hector Plahar, Timothy Ham
 */
public class SBOLParser extends AbstractParser {

    private static final String SBOL_PARSER = "SBOL";

    @Override
    public String getName() {
        return SBOL_PARSER;
    }

    @Override
    public Boolean hasErrors() {
        // This parser cannot succeed with errors, so always return false, or fail.
        return false;
    }

    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        try {
            SBOLDocument document = SBOLFactory.read(new ByteArrayInputStream(textSequence.getBytes()));
            Visitor visitor = new Visitor();

            // walk top level objects
            for (SBOLRootObject rootObject : document.getContents()) {
                rootObject.accept(visitor);
            }

            return visitor.featuredDNASequence;
        } catch (SBOLValidationException | IOException e) {
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        }
    }

    private class Visitor extends SBOLBaseVisitor<RuntimeException> {

        private FeaturedDNASequence featuredDNASequence;

        public Visitor() {
            featuredDNASequence = new FeaturedDNASequence();
        }

        @Override
        public void visit(DnaComponent component) {
            featuredDNASequence.setName(component.getName());
            featuredDNASequence.setIdentifier(component.getDisplayId());
            featuredDNASequence.setIsCircular(false);
            featuredDNASequence.setDescription(component.getDescription());
            featuredDNASequence.setDcUri(component.getURI().toString());

            if (component.getDnaSequence() != null) {
                featuredDNASequence.setSequence(component.getDnaSequence().getNucleotides());
                featuredDNASequence.setUri(component.getDnaSequence().getURI().toString());
            }

            List<SequenceAnnotation> annotations = component.getAnnotations();
            if (!annotations.isEmpty()) {

                Collections.sort(annotations, new Comparator<SequenceAnnotation>() {
                    @Override
                    public int compare(SequenceAnnotation o1, SequenceAnnotation o2) {
                        if (o1.getBioStart().intValue() == o2.getBioStart().intValue())
                            return o1.getBioEnd().compareTo(o2.getBioEnd());
                        return o1.getBioStart().compareTo(o2.getBioStart());
                    }
                });

                for (SequenceAnnotation sequenceAnnotation : annotations) {
                    visit(sequenceAnnotation);
                }
            }
        }

        @Override
        public void visit(SequenceAnnotation annotation) {
            DNAFeature feature = new DNAFeature();
            DNAFeatureLocation location = new DNAFeatureLocation();

            feature.setStrand(annotation.getStrand() == StrandType.NEGATIVE ? -1 : 1);
            feature.setUri(annotation.getURI().toString());

            DnaComponent subComponent = annotation.getSubComponent();
            if (subComponent != null && !subComponent.getTypes().isEmpty()) {
                URI typesURI = (URI) subComponent.getTypes().toArray()[0];
                if (typesURI != null) {
                    String[] s = typesURI.getRawPath().split("SO_");
                    if (s != null && s.length == 2) {
                        feature.setType(IceSequenceOntology.getFeatureType("SO_" + s[1]));
                    }
                }
                String name = subComponent.getName();
                if (name == null || name.trim().isEmpty())
                    name = subComponent.getDisplayId();
                feature.setName(name);
                location.setUri(subComponent.getURI().toString());
            }

            location.setGenbankStart(annotation.getBioStart());
            location.setEnd(annotation.getBioEnd());
            feature.getLocations().add(location);
            featuredDNASequence.getFeatures().add(feature);
        }
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }
}
