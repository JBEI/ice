package org.jbei.ice.lib.bulkupload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Bulk Upload with zip files. It is expected that the zip contains a csv
 * of the upload with the attachment and sequences files containing
 * the names of the files (which are to be enclosed in the zip)
 *
 * @author Hector Plahar
 */
public class BulkZipUpload {

    private BulkCSVUpload csvUpload;
    private final Path zipFilePath;
    private final EntryType addType;
    private final String userId;

    public BulkZipUpload(String userId, Path path, EntryType addType) {
        this.userId = userId;
        this.zipFilePath = path;
        this.addType = addType;
        this.csvUpload = new BulkCSVUpload(userId, path, addType);
    }

    /**
     * Process the zip file. It expects that there is exactly one file with the .csv extension.
     * This means that a .csv cannot be used as an attachment
     * </p>
     * Also, all dot files are ignored
     *
     * @throws IOException on error processing the file
     */
    public long processUpload() throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath.toFile());
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

        String csvFile = null;
        HashMap<String, InputStream> files = new HashMap<>();

        // go through zip elements
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            // does not go into directories for now
            if (zipEntry.isDirectory())
                continue;

            String name = zipEntry.getName();
            if (name.contains("/"))
                name = name.substring(name.lastIndexOf("/") + 1);

            // ignore all dot files
            if (name.startsWith("."))
                continue;

            // get main csv
            if (name.endsWith(".csv")) {
                if (csvFile != null)
                    throw new IOException("Duplicate csv file in zip archive");

                csvFile = IOUtils.toString(zipFile.getInputStream(zipEntry));
            } else {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                files.put(name, inputStream);
            }
        }

        if (csvFile == null)
            throw new IOException("Could not find a csv file in the zip archive");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvFile.getBytes())) {

            // retrieve the partData and validates
            List<PartData> updates = csvUpload.getBulkUploadDataFromFile(inputStream);

            // validate files to ensure that for each partData with a file, that the file is available
            for (PartData data : updates) {

                // check sequences
                String sequenceFile = data.getSequenceFileName();
                if (StringUtils.isNotBlank(sequenceFile) && files.get(sequenceFile) == null)
                    throw new Exception("Sequence file \"" + sequenceFile + "\" not found in the zip archive");

                // check attachments
                String attachmentFile;
                if (data.getAttachments() != null && !data.getAttachments().isEmpty()) {
                    attachmentFile = data.getAttachments().get(0).getFilename();
                    if (StringUtils.isNotBlank(attachmentFile) && files.get(attachmentFile) == null)
                        throw new Exception("Attachment file \"" + sequenceFile + "\" not found in the zip archive");
                }

                // todo : trace sequences
            }

            // create actual registry parts
            BulkEntryCreator creator = new BulkEntryCreator();
            long uploadId = creator.createBulkUpload(userId, addType);

            // create entries
            if (!creator.createEntries(userId, uploadId, updates, files)) {
                String errorMsg = "Error creating entries for upload";
                Logger.error(errorMsg);
                throw new Exception(errorMsg);
                //todo: delete upload id
            }

            return uploadId;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
