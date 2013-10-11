package org.jbei.ice.server.servlet.helper;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * Helper class for dealing with bulk CSV uploads
 *
 * @author Hector Plahar
 */
public abstract class BulkCSVUpload {

    protected final Path csvFilePath;
    protected final Account account;
    protected final EntryAddType addType;
    protected final List<EntryField> headerFields;
    protected final List<EntryField> requiredFields;

    public BulkCSVUpload(EntryAddType addType, Account account, Path csvFilePath) {
        this.addType = addType;
        this.account = account;
        this.csvFilePath = csvFilePath;
        this.headerFields = new LinkedList<EntryField>();
        this.requiredFields = new LinkedList<EntryField>();

        populateHeaderFields();
        populateRequiredFields();
    }

    public abstract String processUpload();

    /**
     * Sets the headers fields that are supported for upload
     */
    abstract void populateHeaderFields();

    /**
     * Sets the header fields that are required at a minimum for upload
     */
    abstract void populateRequiredFields();

}
