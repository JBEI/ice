package org.jbei.ice.client.bulkupload.sheet.cell;

import org.jbei.ice.client.bulkupload.model.SheetCellData;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * Sheet cell for data fields
 *
 * @author Hector Plahar
 */
public class DateInputCell extends SheetCell {

    private DateBox dateBox;
    private int currentRow;

    public DateInputCell() {
        dateBox = new DateBox();
        dateBox.setStyleName("cell_input");
        dateBox.getDatePicker().setStyleName("font-70em");

        DateTimeFormat dateFormat = DateTimeFormat.getFormat("MM/dd/yyyy");
        dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));

        dateBox.getTextBox().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                String s = setDataForRow(currentRow);
                dateBox.getTextBox().setText(s);
            }
        });

        dateBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                int code = event.getNativeKeyCode();
                if (KeyCodes.KEY_TAB != code && KeyCodes.KEY_ENTER != code)
                    return;

                dateBox.getTextBox().setFocus(false);
            }
        });
    }

    /**
     * Set text value of input widget; typically text box base. This is used when focus shifts from
     * displaying the label to the actual widget. This gives the widget the chance to set their text
     * (which is the existing)
     *
     * @param text value to set
     */
    @Override
    public void setText(String text) {
    }

    /**
     * Sets data for row specified in the param, using the user entered value in the input widget
     *
     * @param row current row user is working on
     * @return display for user entered value
     */
    @Override
    public String setDataForRow(int row) {
        String ret = dateBox.getTextBox().getText();
        SheetCellData data = new SheetCellData();
        data.setId(ret);
        data.setValue(ret);
        setWidgetValue(row, data);
        dateBox.getTextBox().setText("");
        return ret;
    }

    /**
     * Give focus to the widget that is wrapped by this cell
     *
     * @param row index of row for focus
     */
    @Override
    public void setFocus(int row) {
        dateBox.showDatePicker();
        currentRow = row;
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection, int tabIndex) {
        dateBox.setTabIndex(tabIndex);
        return dateBox;
    }
}
