package org.jbei.ice.client.bulkimport.sheet;

import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.SheetCellData;

import com.google.gwt.user.client.ui.Composite;

/**
 * Represents cell in a sheet. Not that a single instance is associated with each header (column)
 * so this class keeps track of the data across all rows of the column
 *
 * @author Hector Plahar
 */
public abstract class SheetCell extends Composite {

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
     */
    public abstract void setFocus();

    /**
     * This is meant to be overridden in subclasses that have input boxes with multi suggestions
     *
     * @return true if there are multiple suggestions that are presented to the user on input, false
     *         otherwise
     */
    public boolean hasMultiSuggestions() {
        return false;
    }

    protected void setWidgetValue(int inputRow, String value, String id) {
        SheetCellData data = rowValues.get(inputRow);
        if (data == null) {
            data = new SheetCellData();
            rowValues.put(inputRow, data);
        }

        data.setValue(value);
        data.setId(id);
    }

    public SheetCellData getDataForRow(int row) {
        return rowValues.get(row);
    }

    /**
     * This attempts to get around the issue of fileinput setting their own data
     * on file upload and inputcell relying on Sheet.java to set the data when a user clicks on another cell
     *
     * @return true if sub-classes handle setting their data, false otherwise
     */
    public boolean handlesDataSet() {
        return false;
    }

    /**
     * cell notification for selection. in parent class
     * it does not do anything. subclasses that wish to do some something special
     * when a user selects a cell should sub-class and return true after specialization code
     *
     * @param row selection row
     * @param col selection column
     * @return true if cell handles selection, false otherwise
     */
    public boolean cellSelected(int row, int col) {
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
}
