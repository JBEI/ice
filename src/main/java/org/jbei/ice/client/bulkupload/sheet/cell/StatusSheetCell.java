package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.shared.StatusType;

/**
 * Sheet cell for status column
 *
 * @author Hector Plahar
 */
public class StatusSheetCell extends MultiSuggestSheetCell {

    public StatusSheetCell() {
        super(false);
        this.addOracleData(StatusType.getDisplayList());
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (StatusType.displayToEnum(data.getValue()) == null) {
            return "Status must be one of [" + getStatusOptions() + "]";
        }

        return "";
    }

    public String getStatusOptions() {
        StringBuilder builder = new StringBuilder();
        int size = StatusType.getDisplayList().size();
        for (int i = 0; i < size; i += 1) {
            String item = StatusType.getDisplayList().get(i);
            builder.append(item);
            if (i < size - 1)
                builder.append(", ");
        }

        return builder.toString();
    }
}
