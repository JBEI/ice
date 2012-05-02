package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.shared.dto.EntryInfo;

// model for strain, plasmid, seed, part to avoid code duplication
public abstract class SingleInfoSheetModel extends SheetModel {

    public EntryInfo setCommonFields(EntryInfo info) {

        return info;
    }
}
