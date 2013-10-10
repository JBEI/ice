package org.jbei.ice.server.servlet.helper;

import java.nio.file.Path;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * Bulk CSV Uploader for plasmids
 *
 * @author Hector Plahar
 */
public class PlasmidBulkCSVUpload extends PartBulkCSVUpload {

    static {
        headerFields.add(EntryField.CIRCULAR);
        headerFields.add(EntryField.BACKBONE);
        headerFields.add(EntryField.PROMOTERS);
        headerFields.add(EntryField.REPLICATES_IN);
        headerFields.add(EntryField.ORIGIN_OF_REPLICATION);
        headerFields.add(EntryField.SELECTION_MARKERS);

        // required fields
        requiredFields.add(EntryField.SELECTION_MARKERS);
    }


    public PlasmidBulkCSVUpload(EntryAddType addType, Account account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }
}
