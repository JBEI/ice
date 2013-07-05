package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.lib.shared.BioSafetyOption;

/**
 * @author Hector Plahar
 */
public class BioSafetySheetCell extends MultiSuggestSheetCell {

    public BioSafetySheetCell() {
        super(false);
        this.setOracleData(BioSafetyOption.getDisplayList());
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (BioSafetyOption.displayToEnum(data.getValue()) == null) {
            return "Valid values: [" + getStatusOptions() + "]";
        }

        return "";
    }

    public String getStatusOptions() {
        StringBuilder builder = new StringBuilder();
        int size = BioSafetyOption.getDisplayList().size();
        for (int i = 0; i < size; i += 1) {
            String item = BioSafetyOption.getDisplayList().get(i);
            builder.append(item);
            if (i < size - 1)
                builder.append(", ");
        }

        return builder.toString();
    }
}
