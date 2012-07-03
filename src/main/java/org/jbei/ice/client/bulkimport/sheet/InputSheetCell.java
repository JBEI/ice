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
        input = new TextBox();
        input.setStyleName("cell_input");
        initWidget(input);
    }

    @Override
    public void setText(String text) {
        input.setText(text);
    }

    @Override
    public String getWidgetText() {
        String ret = input.getText();
        input.setText("");
        return ret;
    }

    public void setFocus() {
        input.setFocus(true);
    }
}
