package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.lib.shared.dto.entry.ArabidopsisSeedInfo;

/**
 * Sheet cell for generation field used in importing arabidopsis seeds
 *
 * @author Hector Plahar
 */
public class GenerationSheetCell extends MultiSuggestSheetCell {

    public GenerationSheetCell() {
        super(ArabidopsisSeedInfo.Generation.getDisplayList(), false);
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        if (ArabidopsisSeedInfo.Generation.displayToEnum(data.getValue()) == null) {
            return "[" + getStatusOptions() + "]";
        }

        return "";
    }

    public String getStatusOptions() {
        StringBuilder builder = new StringBuilder();
        int size = ArabidopsisSeedInfo.Generation.getDisplayList().size();
        for (int i = 0; i < size; i += 1) {
            String item = ArabidopsisSeedInfo.Generation.getDisplayList().get(i);
            builder.append(item);
            if (i < size - 1)
                builder.append(", ");
        }

        return builder.toString();
    }

}
