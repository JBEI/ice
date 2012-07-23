package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.ModelFactory;
import org.jbei.ice.client.bulkimport.model.SheetCellData;
import org.jbei.ice.client.bulkimport.model.SheetModel;
import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.client.bulkimport.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkimport.sheet.InfoValueExtractorFactory;
import org.jbei.ice.client.bulkimport.sheet.SheetCell;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;

public class SheetPresenter {

    /**
     * View interface for sheet
     */
    public static interface View {

        void clear();

        void setCellWidgetForCurrentRow(Header header, String value, int row, int col);

        int getSheetRowCount();

        void clearErrorCell(int row, int col);

        void setErrorCell(int row, int col, String errMsg);
    }

    private final View view;
    private HashMap<AutoCompleteField, ArrayList<String>> data;
    private final EntryAddType type;
    private BulkImportInfo currentInfo; // used to maintain saved drafts that are loaded
    private final Header[] headers;

    public SheetPresenter(View view, EntryAddType type) {
        this.view = view;
        this.type = type;
        this.headers = ImportTypeHeaders.getHeadersForType(type);

        for (Header header : headers) {
            header.getCell().reset();
        }
    }

    public SheetPresenter(View view, EntryAddType type, BulkImportInfo info) {
        this(view, type);
        this.currentInfo = info;
    }

    public void reset() {
        if (Window.confirm("Clear all data?")) {
            this.currentInfo.getEntryList().clear();
            view.clear();
        }
    }

    public void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data) {
        this.data = data;
    }

    public ArrayList<String> getAutoCompleteData(AutoCompleteField field) {
        if (data == null)
            return null;

        return data.get(field);
    }

    public EntryAddType getType() {
        return this.type;
    }

    public void setCurrentInfo(BulkImportInfo info) {
        this.currentInfo = info;
    }

    public Header[] getTypeHeaders() {
        return headers;
    }

    public ArrayList<EntryInfo> getCellEntryList(String ownerEmail, String owner) {

        Header[] headers = getTypeHeaders();
        int rowCount = view.getSheetRowCount();
        SheetModel<? extends EntryInfo> model = ModelFactory.getModelForType(type);
        if (model == null)
            return null;

        ArrayList<EntryInfo> infoList = new ArrayList<EntryInfo>();

        // for each row
        for (int i = 0; i < rowCount; i += 1) {

            // is row associated with a saved entry?
            EntryInfo existing;
            if (currentInfo != null && currentInfo.getEntryList().size() > i)
                existing = currentInfo.getEntryList().get(i);
            else
                existing = model.createInfo();

            // for each header
            boolean rowHasData = false;

            // go through headers (column) for data
            for (Header header : headers) {
                SheetCellData data = header.getCell().getDataForRow(i);
                if (data == null)
                    continue;

                rowHasData = true;
                data.setType(header);
                existing = model.setInfoField(data, existing);
            }

            // skip no data rows
            if (!rowHasData)
                continue;

            if (existing != null) {
                existing.setOwnerEmail(ownerEmail);
                existing.setOwner(owner);
                existing.setCreator(owner);
                existing.setCreatorEmail(ownerEmail);
                infoList.add(existing);
            }
        }

        if (currentInfo != null) {
            currentInfo.getEntryList().clear();
            currentInfo.getEntryList().addAll(infoList);
        }
        return infoList;
    }

    /**
     * @return size of the field, which also equates to
     *         the number of columns displayed in the sheet. This is based on the number of
     *         headers for the entry type
     */
    public int getFieldSize() {
        return getTypeHeaders().length;
    }

    public void addRow(int row) {
        int index = row; // row includes the headers but this is 0-indexed

        // type is already set in the constructor
        Header[] headers = getTypeHeaders();
        int headersSize = headers.length;

        for (int i = 0; i < headersSize; i += 1) {

            String value = "";

            if (currentInfo != null && currentInfo.getCount() > index) {
                EntryInfo info = currentInfo.getEntryList().get(index);

                // extractor also sets the header data structure
                value = InfoValueExtractorFactory.extractEntryValue(this.type, headers[i], info, index);
            }

            view.setCellWidgetForCurrentRow(headers[i], value, row, i);
        }
    }

    /**
     * @return true if all cells validate, false otherwise
     */
    public boolean validateCells() {

        boolean isValid = true;

        // for each row
        for (int row = 0; row < view.getSheetRowCount(); row += 1) {

            boolean atLeastOneCellHasRowData = false;

            for (int col = 0; col < headers.length; col += 1) {
                SheetCell cell = headers[col].getCell();
                atLeastOneCellHasRowData = (cell.getDataForRow(row) != null);
                if (atLeastOneCellHasRowData)
                    break;
            }

            if (!atLeastOneCellHasRowData)
                continue;

            // for each header (col)
            for (int col = 0; col < headers.length; col += 1) {
                SheetCell cell = headers[col].getCell();
                String errMsg = cell.inputIsValid(row);
                if (errMsg.isEmpty()) {
                    view.clearErrorCell(row, col);
                    continue;
                }

                isValid = false;
                view.setErrorCell(row, col, errMsg);
            }
        }

        return isValid;
    }

    /**
     * sets the text for the current cell
     *
     * @param currentRow   row of cell
     * @param currentIndex index of current cell
     * @return the current cell for the row being interacted with or null if non exist
     */
    public SheetCell setCellInputFocus(int currentRow, int currentIndex) {
        // get cell for selection and set it to existing
        SheetCell newSelection = headers[currentIndex].getCell();
        if (newSelection == null)
            return null;

        // get already existing data in cell
        String text = "";
        SheetCellData data = newSelection.getDataForRow(currentRow);
        if (data != null)
            text = data.getValue();
        newSelection.setText(text);

        return newSelection;
    }
}
