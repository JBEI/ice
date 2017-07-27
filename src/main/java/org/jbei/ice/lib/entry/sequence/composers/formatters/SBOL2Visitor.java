package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.model.*;
import org.jbei.ice.storage.model.Sequence;
import org.sbolstandard.core2.*;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SBOL2Visitor {

    static final String ICE_NS = "http://ice.jbei.org#";
    static final String ICE_PREFIX = "ice";

    private ComponentDefinition componentDefinition;
    private String uriString;
    private SBOLDocument doc;
    private int annotationCount;

    public SBOL2Visitor(SBOLDocument doc) throws SBOLValidationException, URISyntaxException {
        this.doc = doc;
        uriString = Utils.getConfigValue(ConfigurationKey.URI_PREFIX) + "/entry";

        /* libSBOLj complains if there's no prefix on the URI
         */
        if (!uriString.contains("://")) {
            uriString = "http://" + uriString;
        }

        doc.addNamespace(new URI(ICE_NS), ICE_PREFIX);
    }

    public void visit(Sequence sequence) throws SBOLValidationException, URISyntaxException {
        // ice data model conflates the sequence and component
        Entry entry = sequence.getEntry();

        // Set required properties
        String partId = entry.getPartNumber();
        componentDefinition = doc.createComponentDefinition(uriString, partId, "1", ComponentDefinition.DNA);
        componentDefinition.setName(entry.getName());
        componentDefinition.setDescription(entry.getShortDescription());

        String dsUri = "sequence_" + sequence.getFwdHash().replaceAll("[\\s\\-()]", "");
        org.sbolstandard.core2.Sequence dnaSequence = doc.createSequence(
                uriString, dsUri, "1", sequence.getSequence(), org.sbolstandard.core2.Sequence.IUPAC_DNA);

        dnaSequence.setElements(sequence.getSequence());
        componentDefinition.addSequence(dnaSequence);
        List<SequenceFeature> features = new ArrayList<>(sequence.getSequenceFeatures());
        Collections.sort(features, new SequenceFeatureComparator());

        for (SequenceFeature feature : features) {
            visit(feature);
        }

        componentDefinition.createAnnotation(new QName(ICE_NS, "id", ICE_PREFIX), entry.getId());

        if (entry.getRecordId() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "recordId", ICE_PREFIX), entry.getRecordId());

        if (entry.getVersionId() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "versionId", ICE_PREFIX), entry.getVersionId());

        if (entry.getRecordType() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "recordType", ICE_PREFIX), entry.getRecordType());

        if (entry.getOwner() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "owner", ICE_PREFIX), entry.getOwner());

        if (entry.getOwnerEmail() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "ownerEmail", ICE_PREFIX), entry.getOwnerEmail());

        if (entry.getCreator() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "creator", ICE_PREFIX), entry.getCreator());

        if (entry.getCreatorEmail() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "creatorEmail", ICE_PREFIX), entry.getCreatorEmail());

        if (entry.getStatus() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "status", ICE_PREFIX), entry.getStatus());

        if (entry.getAlias() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "alias", ICE_PREFIX), entry.getAlias());

        for (SelectionMarker selectionMarker : entry.getSelectionMarkers()) {
            componentDefinition.createAnnotation(new QName(ICE_NS, "selectionMarker", ICE_PREFIX), selectionMarker.getName());
        }

        if (entry.getLinks() != null) {
            for (Link link : entry.getLinks()) {
                if (StringUtils.isNotEmpty(link.getLink()) && StringUtils.isNotEmpty(link.getUrl()))
                    componentDefinition.createAnnotation(new QName(ICE_NS, link.getLink(), ICE_PREFIX), link.getUrl());
            }
        }

        if (entry.getKeywords() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "keywords", ICE_PREFIX), entry.getKeywords());

        if (entry.getShortDescription() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "shortDescription", ICE_PREFIX), entry.getShortDescription());

        if (entry.getLongDescription() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "longDescription", ICE_PREFIX), entry.getLongDescription());

        if (entry.getLongDescriptionType() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "longDescriptionType", ICE_PREFIX), entry.getLongDescriptionType());

        if (entry.getReferences() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "references", ICE_PREFIX), entry.getReferences());

        if (entry.getCreationTime() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "creationTime", ICE_PREFIX), entry.getCreationTime().toString());

        if (entry.getModificationTime() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "modificationTime", ICE_PREFIX), entry.getModificationTime().toString());

        if (entry.getBioSafetyLevel() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "bioSafetyLevel", ICE_PREFIX), entry.getBioSafetyLevel());

        if (entry.getIntellectualProperty() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "intellectualProperty", ICE_PREFIX), entry.getIntellectualProperty());

        if (entry.getVisibility() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "visibility", ICE_PREFIX), entry.getVisibility());

        if (entry.getParameters() != null) {
            for (Parameter parameter : entry.getParameters()) {
                if (StringUtils.isNotEmpty(parameter.getKey()) && StringUtils.isNotEmpty(parameter.getValue()))
                    componentDefinition.createAnnotation(new QName(ICE_NS, parameter.getKey(), ICE_PREFIX), parameter.getValue());
            }
        }

        if (entry.getFundingSource() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "fundingSource", ICE_PREFIX), entry.getFundingSource());

        if (entry.getPrincipalInvestigator() != null)
            componentDefinition.createAnnotation(new QName(ICE_NS, "principalInvestigator", ICE_PREFIX), entry.getPrincipalInvestigator());

        // TODO: samples
        // TODO: attachments
    }

    public void visit(SequenceFeature feature) throws SBOLValidationException, URISyntaxException {
        annotationCount++;

        if (feature.getAnnotationLocations() != null && !feature.getAnnotationLocations().isEmpty()) {
            AnnotationLocation location = (AnnotationLocation) feature.getAnnotationLocations().toArray()[0];
            SequenceAnnotation annotation;
            OrientationType orientation = feature.getStrand() == 1 ? OrientationType.INLINE : OrientationType.REVERSECOMPLEMENT;

            if (location.getEnd() < location.getGenbankStart()) {
                annotation = componentDefinition.createSequenceAnnotation(
                        "annotation" + annotationCount, "locationStart", location.getGenbankStart(),
                        feature.getSequence().getSequence().length(),
                        orientation
                );
                annotation.addRange("locationEnd" + annotationCount, 1, location.getEnd(), orientation);
            } else {
                annotation = componentDefinition.createSequenceAnnotation(
                        "annotation" + annotationCount,
                        "location",
                        location.getGenbankStart(), location.getEnd(),
                        orientation);
            }

            annotation.addRole(IceSequenceOntology.getURI(feature.getGenbankType()));
            annotation.setName(feature.getName());
        }
    }
}