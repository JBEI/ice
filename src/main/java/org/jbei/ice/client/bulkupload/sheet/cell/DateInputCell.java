package org.jbei.ice.client.bulkupload.sheet.cell;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author Hector Plahar
 */
public class DateInputCell extends SheetCell {

    private DateBox dateBox;

    public DateInputCell() {

        dateBox = new DateBox();
        dateBox.setStyleName("input_box");

        DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
        dateBox.setWidth("205px");
        dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets data for row specified in the param, using the user entered value in the input widget
     *
     * @param row current row user is working on
     * @return display for user entered value
     */
    @Override
    public String setDataForRow(int row) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Give focus to the widget that is wrapped by this cell
     *
     * @param row index of row for focus
     */
    @Override
    public void setFocus(int row) {
        dateBox.showDatePicker();
    }

    @Override
    public Widget getWidget(int row, boolean isCurrentSelection) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
