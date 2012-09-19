package org.jbei.ice.client.bulkupload.sheet.cell;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.SheetCellData;

/**
 * Sheet cell that accepts Yes/No or True/False as values
 *
 * @author Hector Plahar
 */
public class BooleanSheetCell extends MultiSuggestSheetCell {

    public BooleanSheetCell() {
        super(false);
        ArrayList<String> data = new ArrayList<String>();
        data.add("Yes");
        data.add("No");
        data.add("True");
        data.add("False");

        this.addOracleData(data);
    }

    @Override
    public String inputIsValid(int row) {
        String errMsg = super.inputIsValid(row);
        if (errMsg != null && !errMsg.isEmpty())
            return errMsg;

        SheetCellData data = getDataForRow(row);
        boolean valid = "Yes".equalsIgnoreCase(data.getValue()) ||
                "No".equalsIgnoreCase(data.getValue()) ||
                "True".equalsIgnoreCase(data.getValue()) ||
                "False".equalsIgnoreCase(data.getValue());

        if (!valid) {
            return "Valid values: [Yes, No, True, False]";
        }
        return "";
    }

    public static Boolean getBooleanValue(String value) {
        if (value.isEmpty() || (!"Yes".equalsIgnoreCase(value)
                && !"True".equalsIgnoreCase(value)
                && !"False".equalsIgnoreCase(value)
                && !"No".equalsIgnoreCase(value))) {
            return null;
        }

        return "Yes".equalsIgnoreCase(value) || "True".equalsIgnoreCase(value);
    }
}
