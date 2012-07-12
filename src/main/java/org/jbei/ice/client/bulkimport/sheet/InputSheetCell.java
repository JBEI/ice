package org.jbei.ice.client.bulkimport.sheet;

import com.google.gwt.user.client.ui.TextBox;

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
        initWidget(input);
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

    public void setFocus() {
        input.setFocus(true);
    }
}
