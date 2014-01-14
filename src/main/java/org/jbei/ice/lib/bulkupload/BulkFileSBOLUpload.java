package org.jbei.ice.lib.bulkupload;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.parsers.sbol.ICESBOLParserVisitor;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.vo.DNASequence;

import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.SBOLRootObject;

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

        BulkUploadController controller = new BulkUploadController();
        long bulkUploadId = 0;

        SBOLDocument document = SBOLFactory.read(new FileInputStream(filePath.toFile()));
        try {
            // walk top level object
            for (SBOLRootObject rootObject : document.getContents()) {
                ICESBOLParserVisitor visitor = new ICESBOLParserVisitor(EntryAddType.addTypeToType(addType));
                rootObject.accept(visitor);
                DNASequence sequence = visitor.getFeaturedDNASequence();
                BulkUploadAutoUpdate update = visitor.getUpdate();
                update.setBulkUploadId(bulkUploadId);
                Logger.info(userId + ": " + update.toString());

                update = controller.autoUpdateBulkUpload(userId, update, addType);
                if (bulkUploadId == 0)
                    bulkUploadId = update.getBulkUploadId();

                // get "user sequence"
                String user = getSequenceDocument(rootObject);

                long entryId = update.getEntryId();
                PartFileAdd.uploadSequenceToEntry(entryId, userId, sequence, user);
            }
        } catch (Exception e) {
            Logger.error(e);
            throw new IOException(e);
        }

        return bulkUploadId;
    }

    protected String getSequenceDocument(SBOLRootObject component) {
        SBOLDocument document = SBOLFactory.createDocument();
        document.addContent(component);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            SBOLFactory.write(document, stream);
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }
        return stream.toString();
    }
}
