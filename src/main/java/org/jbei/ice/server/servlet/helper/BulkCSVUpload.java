package org.jbei.ice.server.servlet.helper;

import java.nio.file.Path;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

/**
 * Helper class for dealing with bulk CSV uploads
 *
 * @author Hector Plahar
 */
public abstract class BulkCSVUpload {

    protected final Path csvFilePath;
    protected final Account account;
    protected final EntryAddType addType;

    public BulkCSVUpload(EntryAddType addType, Account account, Path csvFilePath) {
        this.addType = addType;
        this.account = account;
        this.csvFilePath = csvFilePath;
    }

    public abstract String processUpload();

    // TODO : combine with StrainWithPlasmidHeaders::isPlasmidHeader()
    private EntryType toEntryType(EntryAddType type, EntryField field) {
        EntryType entryType;
        if (type == EntryAddType.STRAIN_WITH_PLASMID) {
            boolean isPlasmid = isPlasmidHeader(field);

            if (isPlasmid) {
                // if updating plasmid portion of strain with one plasmid
                entryType = EntryType.PLASMID;
            } else {
                entryType = EntryType.STRAIN;
            }
        } else {
            entryType = EntryAddType.addTypeToType(type);
        }
        return entryType;
    }

    protected boolean isPlasmidHeader(EntryField entryField) {
        return (entryField == EntryField.PLASMID_NAME
                || entryField == EntryField.PLASMID_ALIAS
                || entryField == EntryField.PLASMID_LINKS
                || entryField == EntryField.PLASMID_SELECTION_MARKERS
                || entryField == EntryField.CIRCULAR
                || entryField == EntryField.PLASMID_BACKBONE
                || entryField == EntryField.PLASMID_PROMOTERS
                || entryField == EntryField.PLASMID_REPLICATES_IN
                || entryField == EntryField.PLASMID_ORIGIN_OF_REPLICATION
                || entryField == EntryField.PLASMID_KEYWORDS
                || entryField == EntryField.PLASMID_SUMMARY
                || entryField == EntryField.PLASMID_NOTES
                || entryField == EntryField.PLASMID_REFERENCES
                || entryField == EntryField.PLASMID_SEQ_FILENAME
                || entryField == EntryField.PLASMID_ATT_FILENAME);
    }
}
