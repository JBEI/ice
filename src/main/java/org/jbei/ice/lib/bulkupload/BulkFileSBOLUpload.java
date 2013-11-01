package org.jbei.ice.lib.bulkupload;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.lib.composers.formatters.IceSequenceOntology;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLRootObject;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.StrandType;
import org.sbolstandard.core.util.SBOLBaseVisitor;

/**
 * Bulk upload with multiple components in a single SBOL document
 *
 * @author Hector Plahar
 */
public class BulkFileSBOLUpload {

    private final Path filePath;
    private final EntryAddType addType;
    private final String userId;

    public BulkFileSBOLUpload(String userId, Path path, EntryAddType addType) {
        this.userId = userId;
        this.filePath = path;
        this.addType = addType;
    }

    public long processUpload() throws IOException {
        if (addType == EntryAddType.STRAIN_WITH_PLASMID)
            throw new IOException("No support for SBOL strain with plasmid upload");

        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        long bulkUploadId = 0;

        SBOLDocument document = SBOLFactory.read(new FileInputStream(filePath.toFile()));
        try {
            // walk top level object
            for (SBOLRootObject rootObject : document.getContents()) {
                Visitor visitor = new Visitor(EntryAddType.addTypeToType(addType));
                rootObject.accept(visitor);
                IDNASequence sequence = visitor.featuredDNASequence;
                BulkUploadAutoUpdate update = visitor.update;
                update.setBulkUploadId(bulkUploadId);
                Logger.info(userId + ": " + update.toString());

                update = controller.autoUpdateBulkUpload(userId, update, addType);
                if (bulkUploadId == 0)
                    bulkUploadId = update.getBulkUploadId();

                long entryId = update.getEntryId();
                PartFileAdd.uploadSequenceToEntry(entryId, userId, sequence);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        return bulkUploadId;
    }

    private static class Visitor extends SBOLBaseVisitor<RuntimeException> {

        private FeaturedDNASequence featuredDNASequence;
        private BulkUploadAutoUpdate update;

        public Visitor(EntryType type) {
            featuredDNASequence = new FeaturedDNASequence();
            update = new BulkUploadAutoUpdate(type);
        }

        @Override
        public void visit(DnaComponent component) {
            String name = component.getName().trim();
            if (name == null || name.isEmpty())
                update.getKeyValue().put(EntryField.NAME, name);
            else {
                update.getKeyValue().put(EntryField.NAME, name);
                update.getKeyValue().put(EntryField.ALIAS, component.getDisplayId());
            }

            featuredDNASequence.setName(component.getName());
            featuredDNASequence.setIdentifier(component.getDisplayId());
            featuredDNASequence.setIsCircular(false);
            featuredDNASequence.setDescription(component.getDescription());
            String description = component.getDescription();
            update.getKeyValue().put(EntryField.SUMMARY, description);
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
                feature.setIdentifier(subComponent.getDisplayId());
                location.setUri(subComponent.getURI().toString());
            }

            location.setGenbankStart(annotation.getBioStart());
            location.setEnd(annotation.getBioEnd());
            feature.getLocations().add(location);
            featuredDNASequence.getFeatures().add(feature);
        }
    }
}
