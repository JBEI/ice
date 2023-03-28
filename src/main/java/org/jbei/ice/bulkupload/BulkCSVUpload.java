package org.jbei.ice.bulkupload;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.StorageLocation;
import org.jbei.ice.dto.bulkupload.SampleField;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.dto.sample.PartSample;
import org.jbei.ice.dto.sample.SampleType;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.CustomEntryFieldDAO;
import org.jbei.ice.storage.model.BulkUploadModel;
import org.jbei.ice.storage.model.CustomEntryFieldModel;
import org.jbei.ice.storage.model.Entry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Helper class for dealing with bulk CSV uploads
 *
 * @author Hector Plahar
 */
public class BulkCSVUpload {

    protected final InputStream inputStream;
    protected final String userId;
    final List<EntryFieldLabel> invalidFields; // fields that failed validation
    final long uploadId;
    private final EntryType addType;
    private EntryType subType;    // optional subType'

    BulkCSVUpload(String userId, InputStream inputStream, long uploadId) {
        this.userId = userId;
        this.inputStream = inputStream;
        this.invalidFields = new LinkedList<>();
        this.uploadId = uploadId;
        BulkUploadModel upload = DAOFactory.getBulkUploadDAO().get(uploadId);
        if (upload == null)
            throw new IllegalArgumentException("Invalid bulk upload \"" + uploadId + "\"");
        this.addType = EntryType.nameToType(upload.getImportType());
        if (!StringUtils.isEmpty(upload.getLinkType()))
            this.subType = EntryType.nameToType(upload.getLinkType());
    }

    /**
     * Processes the csv upload
     *
     * @return wrapper around id of created bulk upload or error message
     */
    public ProcessedBulkUpload processUpload() {
        ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();

        try {
            List<PartWithSample> updates = getBulkUploadDataFromFile(inputStream);
            if (updates == null) {
                processedBulkUpload.setSuccess(false);
                processedBulkUpload.setUserMessage("Validation failed");
                for (EntryFieldLabel field : invalidFields) {
                    processedBulkUpload.getHeaders().add(new EntryHeaderValue(field));
                }
                return processedBulkUpload;
            }

            // create actual entries
            BulkUploadEntries creator = new BulkUploadEntries(userId, uploadId);

            // create entries
            if (!creator.createEntries(updates, null)) {
                String errorMsg = "Error creating entries for upload";
                throw new IOException(errorMsg);
            }
            processedBulkUpload.setUploadId(uploadId);
        } catch (IOException e) {
            // validation exception; convert entries to headers
            processedBulkUpload.setSuccess(false);
            processedBulkUpload.setUserMessage("Validation failed");
            for (EntryFieldLabel field : invalidFields) {
                processedBulkUpload.getHeaders().add(new EntryHeaderValue(field));
            }
            Logger.error(e);
            return processedBulkUpload;
        }

        return processedBulkUpload;
    }

    private EntryType detectSubType(String field) {
        String[] fieldNames = field.split("\\s+");
        return EntryType.nameToType(fieldNames[0]);
    }

    private HeaderValue detectHeaderValue(EntryType type, String fieldStr, boolean isSubType) {
        EntryFieldLabel field = EntryFieldLabel.fromString(fieldStr);
        if (field != null) {
            // field header maps as is to EntryField which indicates it is not a sub Type
            return new EntryHeaderValue(field, isSubType, false, fieldStr);
        }

        // check if sample field
        SampleField sampleField = SampleField.fromString(fieldStr);
        if (sampleField != null) {
            return new SampleHeaderValue(sampleField);
        }

        // check custom field
        Optional<CustomEntryFieldModel> optional = DAOFactory.getCustomEntryFieldDAO().getLabelForType(type, fieldStr);
        if (optional.isPresent()) {
            CustomEntryFieldModel model = optional.get();

            // existing field will be null if field type != 'EXISTING'
            return new EntryHeaderValue(model.getExistingField(), isSubType, true, fieldStr);
        }

        return null;
    }

    /**
     * @param headerArray list of column header values representing the entry fields
     * @return mapping of col number to the entry type
     * @throws IOException on exception
     */
    private HashMap<Integer, HeaderValue> processColumnHeaders(String[] headerArray) throws IOException {
        HashMap<Integer, HeaderValue> headers = new HashMap<>();

        for (int i = 0; i < headerArray.length; i += 1) {
            String fieldStr = headerArray[i].replaceAll("[^\\x20-\\x7e]", " ").trim();

            // account for asterisk that indicates a header is required
            fieldStr = fieldStr.replace(FileBulkUpload.ASTERISK_SYMBOL, "");

            // check if a switchover (to the linked fields) has occurred
//            if (subType == null) // todo : "i" should be length of type number of fields
//                subType = detectSubType(fieldStr);

            HeaderValue headerValue;

            if (subType != null && fieldStr.startsWith(subType.getDisplay())) {
                // process subType
                int k = fieldStr.indexOf(subType.getDisplay());
                String subSypeString = fieldStr.substring(k + subType.getDisplay().length());
                headerValue = detectHeaderValue(subType, subSypeString.trim(), true);

                // verify
                if (headerValue == null)
                    headerValue = detectHeaderValue(addType, fieldStr.trim(), false);

//                if (headerValue != null)
//                    subType = null;
            } else {
                // process main add type
                headerValue = detectHeaderValue(addType, fieldStr.trim(), false);
            }

            if (headerValue == null) {
                throw new IllegalArgumentException("Could not process header value \"" + fieldStr + "\"");
            }

            headers.put(i, headerValue);
        }

        if (headers.size() != headerArray.length) {
            throw new IOException("Header size and input header array length differ");
        }
        return headers;
    }

    private boolean isEmptyRow(String[] row) {
        StringBuilder sb = new StringBuilder();
        for (String s : row) {
            sb.append(s.trim());
        }
        return sb.toString().isEmpty();
    }

    // NOTE: this also validates the part data (with the exception of the actual files)
    List<PartWithSample> getBulkUploadDataFromFile(InputStream inputStream) throws IOException {
        List<PartWithSample> partDataList = new LinkedList<>();

        HashMap<Integer, HeaderValue> headers = null;
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
        int row = -1;
        for (String[] nextLine : reader) {

            // skip all empty rows
            if (isEmptyRow(nextLine))
                continue;

            row += 1;

            if (row == 0) {
                // parse headers
                headers = processColumnHeaders(nextLine);
                continue;
            }

            // parse data
            PartData partData = new PartData(addType);
            PartSample partSample = null;

            if (subType != null) {
                partData.getLinkedParts().add(new PartData(subType));
            }

            // for each column
            for (int i = 0; i < nextLine.length; i += 1) {
                HeaderValue headerForColumn = headers.get(i);
                String value = nextLine[i].replaceAll("[^\\x20-\\x7e]", " ");

                // process sample information
                if (headerForColumn.isSampleField()) {
                    if (partSample == null)
                        partSample = new PartSample();

                    setPartSampleData(((SampleHeaderValue) headerForColumn).getSampleField(), partSample, value);
                } else if (headerForColumn.isCustomField()) {
                    // process custom field
                    setCustomField(value, partData, (EntryHeaderValue) headerForColumn);
                } else {
                    // process existing field
                    setExistingField(value, partData, (EntryHeaderValue) headerForColumn);
                }
            }

            // validate todo

            partData.setIndex(row);
            PartWithSample partWithSample = new PartWithSample(partSample, partData);
            partDataList.add(partWithSample);
        }

        return partDataList;
    }

    private void setCustomField(String value, PartData partData, EntryHeaderValue headerValue) throws IOException {
        // todo : e.g. {entryField: host} can be existing field or custom field
        CustomEntryFieldDAO fieldDAO = DAOFactory.getCustomEntryFieldDAO();
        EntryType type;
        if (headerValue.isSubType()) {
            type = this.subType;
            partData = partData.getLinkedParts().get(0);
        } else {
            type = this.addType;
        }

        Optional<CustomEntryFieldModel> optional = fieldDAO.getLabelForType(type, headerValue.getLabel());
        if (optional.isEmpty()) {
            Logger.error("Could not retrieve custom field for \"" + headerValue.getLabel() + "\"");
            return;
        }

        CustomEntryFieldModel customEntryFieldModel = optional.get();
        if (customEntryFieldModel.getFieldType() == FieldType.EXISTING) {
            // validate
            if (customEntryFieldModel.getRequired() && StringUtils.isEmpty(value)) {
                // todo : return error
                return;
            }
            setExistingField(value, partData, headerValue);
            return;
        }

        CustomEntryField customEntryField = new CustomEntryField();
        customEntryField.setId(customEntryFieldModel.getId());
        customEntryField.setValue(value); // todo : check if the value is from a pre-selected list of options
        partData.getCustomEntryFields().add(customEntryField);

    }

    private void setExistingField(String value, PartData partData, EntryHeaderValue headerValue) throws IOException {
        EntryFieldLabel field = headerValue.getEntryFieldLabel();
        PartData data;

        boolean isSubType = headerValue.isSubType();

        if (isSubType)
            data = partData.getLinkedParts().get(0);
        else
            data = partData;

        // get the data for the field
        switch (field) {
            case ATT_FILENAME:
                ArrayList<AttachmentInfo> attachments = data.getAttachments();
                if (attachments == null) {
                    attachments = new ArrayList<>();
                    data.setAttachments(attachments);
                }
                attachments.clear();
                attachments.add(new AttachmentInfo(value));
                break;

            case SEQ_FILENAME:
                data.setSequenceFileName(value);
                break;

            case SEQ_TRACE_FILES:
                // todo
                break;

            case EXISTING_PART_NUMBER:
                Entry entry = DAOFactory.getEntryDAO().getByPartNumber(value);
                if (entry == null)
                    throw new IOException("Could not locate part number \"" + value + "\" for linking");
                PartData toLink = entry.toDataTransferObject();
                data.getLinkedParts().add(toLink);
                break;

//            default:
//                EntryUtil.setPartDataFromField(data, value, field);
        }
    }

    private void setPartSampleData(SampleField sampleField, PartSample partSample, String data) {
        switch (sampleField) {
            case LABEL -> partSample.setLabel(data);
            case SHELF -> {
                StorageLocation storageLocation = new StorageLocation();
                storageLocation.setType(SampleType.SHELF);
                storageLocation.setDisplay(data);
                partSample.setLocation(storageLocation);
            }
            case BOX -> {
                StorageLocation childLocation = new StorageLocation();
                childLocation.setDisplay(data);
                childLocation.setType(SampleType.BOX_INDEXED);
                partSample.getLocation().setChild(childLocation);
            }
            case WELL -> {
                StorageLocation grandChildLocation = new StorageLocation();
                grandChildLocation.setType(SampleType.WELL);
                grandChildLocation.setDisplay(data);
                partSample.getLocation().getChild().setChild(grandChildLocation);
            }
            default -> throw new IllegalArgumentException("No handler for sample field " + sampleField);
        }
    }
}
