package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.lib.shared.dto.entry.PlantType;

/**
 * @author Hector Plahar
 */
public class PlantTypeSheetCell extends MultiSuggestSheetCell {

    public PlantTypeSheetCell() {
        super(false);
        this.setOracleData(PlantType.getDisplayList());
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (PlantType.fromString(data.getValue()) == null) {
            return "Status must be one of [" + getStatusOptions() + "]";
        }

        return "";
    }

    public String getStatusOptions() {
        StringBuilder builder = new StringBuilder();
        int size = PlantType.getDisplayList().size();
        for (int i = 0; i < size; i += 1) {
            String item = PlantType.getDisplayList().get(i);
            builder.append(item);
            if (i < size - 1)
                builder.append(", ");
        }

        return builder.toString();
    }

}
