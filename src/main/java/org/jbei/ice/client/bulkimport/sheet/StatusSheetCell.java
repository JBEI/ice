package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.shared.StatusType;

/**
 * Sheet cell for status column
 *
 * @author Hector Plahar
 */
public class StatusSheetCell extends MultiSuggestSheetCell {

    public StatusSheetCell() {
        super();
        this.addOracleData(StatusType.getDisplayList());
    }
}
