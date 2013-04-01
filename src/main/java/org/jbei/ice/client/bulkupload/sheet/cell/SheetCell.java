package org.jbei.ice.client.bulkupload.sheet.cell;

import java.util.HashMap;

import org.jbei.ice.client.bulkupload.model.SheetCellData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents cell in a sheet. Note that a single instance is associated with each header (column)
 * so this class keeps track of the data across all rows of the column
 *
 * @author Hector Plahar
 */
public abstract class SheetCell {

    private final HashMap<Integer, SheetCellData> rowValues;
    protected boolean required;

    public SheetCell() {
        this.rowValues = new HashMap<Integer, SheetCellData>();
    }

    /**
     * Set text value of input widget; typically text box base. This is used when focus shifts from
     * displaying the label to the actual widget. This gives the widget the chance to set their text
     * (which is the existing)
     *
     * @param text value to set
     */
    public abstract void setText(String text);

    /**
     * Sets data for row specified in the param, using the user entered value in the input widget
     *
     * @param row current row user is working on
     * @return display for user entered value
     */
    public abstract String setDataForRow(int row);

    /**
     * Give focus to the widget that is wrapped by this cell
     *
     * @param row index of row for focus
     */
    public abstract void setFocus(int row);

    /**
     * This is meant to be overridden in subclasses that have input boxes with multi suggestions
     *
     * @return true if there are multiple suggestions that are presented to the user on input, false
     *         otherwise
     */
    public boolean hasMultiSuggestions() {
        return false;
    }

    public void setWidgetValue(int inputRow, SheetCellData datum) {
        String value;
        String id;

        if (datum == null) {
            value = "";
            id = "";
        } else {
            value = datum.getValue();
            id = datum.getId();
        }

        if (value.trim().isEmpty() && id.trim().isEmpty()) {
            removeDataForRow(inputRow);
            return;
        }

        SheetCellData data = rowValues.get(inputRow);
        if (data == null) {
            data = new SheetCellData();
            rowValues.put(inputRow, data);
        }

        GWT.log("Setting data for row " + inputRow + " value = " + value);
        data.setValue(value);
        data.setId(id);
    }

    public SheetCellData getDataForRow(int row) {
        return rowValues.get(row);
    }

    public SheetCellData removeDataForRow(int row) {
        GWT.log("Removing data for row " + row);
        return rowValues.remove(row);
    }

    /**
     * cell notification for selection. in parent class
     * it does not do anything. subclasses that wish to do some something special
     * when a user selects a cell should sub-class and return true after specialization code
     *
     * @return true if cell handles selection, false otherwise
     */
    public boolean handlesSelection() {
        return false;
    }

    public void reset() {
        this.rowValues.clear();
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Checks if there is data for required fields. Sub-classes should override
     * this and validate their content
     *
     * @param row row for column (which translates to cell) being validated
     * @return error msg if cell data is required and there is data entered
     */
    public String inputIsValid(int row) {
        if (this.required && getDataForRow(row) == null)
            return "Required field";
        return "";
    }

    // get widget for row
    public abstract Widget getWidget(int row, boolean isCurrentSelection, int tabIndex);

    public boolean isRequired() {
        return required;
    }
}
