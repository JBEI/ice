package org.jbei.ice.lib.bulkupload;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryFieldLabel;
import org.jbei.ice.lib.dto.entry.PartData;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Bulk Upload with zip files. It is expected that the zip contains a csv
 * of the upload with the attachment and sequences files containing
 * the names of the files (which are to be enclosed in the zip)
 *
 * @author Hector Plahar
 */
public class BulkZipUpload extends BulkCSVUpload {

    BulkZipUpload(String userId, InputStream inputStream, long uploadId) {
        super(userId, inputStream, uploadId);
    }

    /**
     * Process the zip file. It expects that there is exactly one file with the .csv extension.
     * This means that a .csv cannot be used as an attachment
     * </p>
     * Also, all dot files are ignored
     */
    public ProcessedBulkUpload processUpload() {
        ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
        InputStream csvFile = null;
        HashMap<String, InputStream> files = new HashMap<>();

        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {

                if (zipEntry.isDirectory())
                    continue;

                String name = zipEntry.getName();
                if (name.contains("/"))
                    name = name.substring(name.lastIndexOf("/") + 1);

                // ignore all dot files
                if (name.startsWith("."))
                    continue;

                int len;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while ((len = zis.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                zis.closeEntry();

                // get main csv
                if (name.endsWith(".csv")) {
                    if (csvFile != null) {
                        processedBulkUpload.setSuccess(false);
                        processedBulkUpload.setUserMessage("Duplicate csv file in zip archive. It should only contain one.");
                        return processedBulkUpload;
                    } else {
                        csvFile = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    }
                } else {
                    files.put(name, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                }
            }
        } catch (IOException e) {
            processedBulkUpload.setSuccess(false);
            processedBulkUpload.setUserMessage(e.getCause().getMessage());
            return processedBulkUpload;
        }

        if (csvFile == null) {
            processedBulkUpload.setSuccess(false);
            processedBulkUpload.setUserMessage("Could not find a csv file in the zip archive");
            return processedBulkUpload;
        }

        try {
            // retrieve the partData and validates
            List<PartWithSample> updates = super.getBulkUploadDataFromFile(csvFile);
            if (updates == null) {
                processedBulkUpload.setSuccess(false);
                processedBulkUpload.setUserMessage("Validation failed");
                for (EntryFieldLabel field : invalidFields) {
                    processedBulkUpload.getHeaders().add(new EntryHeaderValue(field));
                }
                return processedBulkUpload;
            }

            // validate files to ensure that for each partData with a file, that the file is available
            for (PartWithSample partWithSample : updates) {

                // check sequences
                PartData data = partWithSample.getPartData();
                String sequenceFile = data.getSequenceFileName();
                if (StringUtils.isNotBlank(sequenceFile) && files.get(sequenceFile) == null) {
                    processedBulkUpload.setSuccess(false);
                    processedBulkUpload.setUserMessage("Sequence file \"" + sequenceFile
                            + "\" not found in the zip archive");
                    return processedBulkUpload;
                }

                // check attachments
                String attachmentFile;
                if (data.getAttachments() != null && !data.getAttachments().isEmpty()) {
                    attachmentFile = data.getAttachments().get(0).getFilename();
                    if (StringUtils.isNotBlank(attachmentFile) && files.get(attachmentFile) == null) {
                        processedBulkUpload.setSuccess(false);
                        processedBulkUpload.setUserMessage("Attachment file \"" + sequenceFile
                                + "\" not found in the zip archive");
                        return processedBulkUpload;
                    }
                }

                // todo : trace sequences
            }

            // create actual registry parts
            BulkUploadEntries creator = new BulkUploadEntries(userId, this.uploadId);

            // create entries
            if (!creator.createEntries(updates, files)) {
                String errorMsg = "Error creating entries for upload";
                throw new IOException(errorMsg);
                //todo: delete upload id
            }

            processedBulkUpload.setUploadId(uploadId);
            return processedBulkUpload;
        } catch (IOException e) {
            Logger.error(e);
            processedBulkUpload.setSuccess(false);
            processedBulkUpload.setUserMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return processedBulkUpload;
        }
    }
}
