package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the results of a processed upload.
 * If the processing is successful, then the wrapped {@link BulkUploadInfo} contains a reference
 * to the upload otherwise the list of fields that failed validation and/or a user friendly error
 * message is added
 *
 * @author Hector Plahar
 */
public class ProcessedBulkUpload implements IDataTransferModel {

    private boolean success;
    private List<HeaderValue> headers;
    private String userMessage;
    private BulkUploadInfo uploadInfo;

    public ProcessedBulkUpload() {
        headers = new ArrayList<>();
        uploadInfo = new BulkUploadInfo();
        success = true;
    }

    public void setUploadId(long id) {
        uploadInfo.setId(id);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<HeaderValue> getHeaders() {
        return headers;
    }

    public void setHeaders(List<HeaderValue> headers) {
        this.headers = headers;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public BulkUploadInfo getUploadInfo() {
        return uploadInfo;
    }
}
