package org.jbei.ice.client.bulkupload.sheet.cell;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Default cell for the import sheet
 *
 * @author Hector Plahar
 */
public class InputSheetCell extends SheetCell {

    private final TextBox input;

    public InputSheetCell() {
        super();
        input = new TextBox();
        input.setStyleName("cell_input");
    }

    @Override
    public void setText(String text) {
        input.setText(text);
    }

    /**
     * Sets data for row specified in the param
     *
     * @param row current row user is working on
     * @return display for user entered value
     */
    @Override
    public String setDataForRow(int row) {
        String ret = input.getText();
        setWidgetValue(row, ret, ret);
        input.setText("");
        return ret;
    }

    public void setFocus(int row) {
        input.setFocus(true);
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection) {
        return input;
    }
}
