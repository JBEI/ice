package org.jbei.ice.lib.parsers.sbol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.jbei.ice.lib.composers.formatters.IceSequenceOntology;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SBOLValidationException;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;
import org.sbolstandard.core.util.SBOLBaseVisitor;

/**
 * Parse SBOL (v 1.1) files that are imported by the user
 *
 * @author Hector Plahar, Timothy Ham
 */
public class SbolParser extends AbstractParser {

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

            return visitor.sequenceWithFeatures;
        } catch (SBOLValidationException | IOException e) {
            throw new InvalidFormatParserException("Could not parse SBOL file!", e);
        }
    }

    private class Visitor extends SBOLBaseVisitor<RuntimeException> {

        private FeaturedDNASequence sequenceWithFeatures;

        public Visitor() {
            sequenceWithFeatures = new FeaturedDNASequence();
        }

        @Override
        public void visit(DnaComponent component) {
            sequenceWithFeatures.setName(component.getName());
            sequenceWithFeatures.setIdentifier(component.getDisplayId());
            sequenceWithFeatures.setIsCircular(false);
            sequenceWithFeatures.setDescription(component.getDescription());

            if (component.getDnaSequence() != null) {
                sequenceWithFeatures.setSequence(component.getDnaSequence().getNucleotides());
            }

            java.util.Collection<SequenceAnnotation> annotations = component.getAnnotations();
            if (!annotations.isEmpty()) {
                for (SequenceAnnotation sequenceAnnotation : annotations) {
                    visit(sequenceAnnotation);
                }
            }
        }

        @Override
        public void visit(SequenceAnnotation annotation) {
            DNAFeature feature = new DNAFeature();
            feature.setStrand(annotation.getStrand() == StrandType.NEGATIVE ? -1 : 1);

            DnaComponent subComponent = annotation.getSubComponent();
            if (subComponent != null && !subComponent.getTypes().isEmpty()) {
                URI uri = (URI) subComponent.getTypes().toArray()[0];
                if (uri != null) {
                    String[] s = uri.getRawPath().split("SO_");
                    if (s != null && s.length == 2) {
                        feature.setType(IceSequenceOntology.getFeatureType("SO_" + s[1]));
                    }
                }
                feature.setName(subComponent.getDisplayId());
            }

            DNAFeatureLocation location = new DNAFeatureLocation();
            location.setGenbankStart(annotation.getBioStart());
            location.setEnd(annotation.getBioEnd());
            feature.getLocations().add(location);
            sequenceWithFeatures.getFeatures().add(feature);
        }
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }
}
