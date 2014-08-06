package org.jbei.ice.lib.bulkupload;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * // TODO : if there should check for link
 * Helper class for dealing with bulk CSV uploads
 *
 * @author Hector Plahar
 */
public abstract class BulkCSVUpload {

    protected final Path csvFilePath;
    protected final String account;
    protected final EntryType addType;
    protected final List<EntryField> headerFields;
    protected final List<EntryField> requiredFields;

    public BulkCSVUpload(EntryType addType, String userId, Path csvFilePath) {
        this.addType = addType;
        this.account = userId;
        this.csvFilePath = csvFilePath;
        this.headerFields = new LinkedList<>();
        this.requiredFields = new LinkedList<>();

        populateHeaderFields();
        populateRequiredFields();
    }

    public List<EntryField> getRequiredFields() {
        return new ArrayList<>(requiredFields);
    }

    abstract String processUpload();

    /**
     * Extract bulk upload information from stream
     *
     * @param inputStream the <code>InputStream</code> to read from
     * @return list of <code>BulkUploadAutoUpdates</code> that resulted from converting the stream
     * @throws Exception
     */
    abstract List<BulkUploadAutoUpdate> getBulkUploadUpdates(InputStream inputStream) throws Exception;

    /**
     * Sets the headers fields that are supported for upload
     */
    abstract void populateHeaderFields();

    /**
     * Sets the header fields that are required at a minimum for upload
     */
    abstract void populateRequiredFields();
}
