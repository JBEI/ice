package org.jbei.ice.client.bulkimport.sheet;

import org.jbei.ice.client.bulkimport.model.SheetCellData;
import org.jbei.ice.shared.StatusType;

import com.google.common.base.Joiner;

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

    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (StatusType.displayToEnum(data.getValue()) == null) {
            return "Status must be one of [" + Joiner.on(", ").join(StatusType.getDisplayList()) + "]";
        }

        return "";
    }
}
