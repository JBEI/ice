package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.shared.dto.ArabidopsisSeedInfo;

/**
 * @author Hector Plahar
 */
public class PlantTypeSheetCell extends MultiSuggestSheetCell {

    public PlantTypeSheetCell() {
        super(false);
        this.addOracleData(ArabidopsisSeedInfo.PlantType.getDisplayList());
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (ArabidopsisSeedInfo.PlantType.displayToEnum(data.getValue()) == null) {
            return "Status must be one of [" + getStatusOptions() + "]";
        }

        return "";
    }

    public String getStatusOptions() {
        StringBuilder builder = new StringBuilder();
        int size = ArabidopsisSeedInfo.PlantType.getDisplayList().size();
        for (int i = 0; i < size; i += 1) {
            String item = ArabidopsisSeedInfo.PlantType.getDisplayList().get(i);
            builder.append(item);
            if (i < size - 1)
                builder.append(", ");
        }

        return builder.toString();
    }

}
