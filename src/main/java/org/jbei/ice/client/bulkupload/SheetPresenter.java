package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.ModelFactory;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.model.SheetModel;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.bulkupload.sheet.header.BulkUploadHeaders;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;

public class SheetPresenter {

    /**
     * View interface for sheet
     */
    public static interface View {

        void clear();

        void setCellWidgetForCurrentRow(String value, int row, int col, int tabIndex);

        int getSheetRowCount();

        void clearErrorCell(int row, int col);

        void setErrorCell(int row, int col, String errMsg);
    }

    private final View view;
    private final EntryAddType type;
    private BulkUploadInfo currentInfo; // used to maintain saved drafts that are loaded
    private final BulkUploadHeaders headers;

    public SheetPresenter(View view, EntryAddType type) {
        this.view = view;
        this.type = type;
        this.headers = ImportTypeHeaders.getHeadersForType(type);
    }

    public SheetPresenter(View view, EntryAddType type, BulkUploadInfo info) {
        this(view, type);
        this.currentInfo = info;
    }

    public void reset() {
        if (Window.confirm("Clear all data?")) {
            this.currentInfo.getEntryList().clear();
            view.clear();
        }
    }

    public EntryAddType getType() {
        return this.type;
    }

    public void setCurrentInfo(BulkUploadInfo info) {
        this.currentInfo = info;
    }

    public BulkUploadHeaders getTypeHeaders() {
        return headers;
    }

    public ArrayList<EntryInfo> getCellEntryList(String ownerEmail, String owner) {

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
            for (CellColumnHeader header : headers.getHeaders()) {
                SheetCellData data = header.getCell().getDataForRow(i);
                if (data == null)
                    continue;

                rowHasData = true;
                data.setType(header.getHeaderType());
                existing = model.setInfoField(data, existing);
            }

            // skip no data rows
            if (!rowHasData)
                continue;

            if (existing != null) {
                if (ownerEmail != null && owner != null) {
                    existing.setOwnerEmail(ownerEmail);
                    existing.setOwner(owner);
                    existing.setCreator(owner);
                    existing.setCreatorEmail(ownerEmail);

                    if (existing.getInfo() != null) {
                        existing.getInfo().setOwnerEmail(ownerEmail);
                        existing.getInfo().setOwner(owner);
                        existing.getInfo().setCreator(owner);
                        existing.getInfo().setCreatorEmail(ownerEmail);
                    }
                }

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
        return headers.getHeaderSize();
    }

    public void addRow(int row) {
        int index = row; // row includes the headers but this is 0-indexed

        int i = 0;
        for (CellColumnHeader header : getTypeHeaders().getHeaders()) {
            SheetCellData data = null;

            if (currentInfo != null && currentInfo.getCount() > index) {
                EntryInfo info = currentInfo.getEntryList().get(index);

                // extractor also sets the header data structure
                data = getTypeHeaders().extractValue(header.getHeaderType(), info, index);
                header.getCell().setWidgetValue(index, data);
            }

            String value = data == null ? "" : data.getValue();
            view.setCellWidgetForCurrentRow(value, row, i, headers.getHeaderSize());
            i += 1;
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

            int col = 0;
            for (CellColumnHeader header : headers.getHeaders()) {
                SheetCell cell = header.getCell();
                view.clearErrorCell(row, col);
                atLeastOneCellHasRowData = (cell.getDataForRow(row) != null);
                if (atLeastOneCellHasRowData)
                    break;
                col += 1;
            }

            if (!atLeastOneCellHasRowData)
                continue;

            // for each header (col)
            col = 0;
            for (CellColumnHeader header : headers.getHeaders()) {
                SheetCell cell = header.getCell();
                String errMsg = cell.inputIsValid(row);
                if (errMsg.isEmpty()) {
                    view.clearErrorCell(row, col);
                    continue;
                }

                isValid = false;
                view.setErrorCell(row, col, errMsg);
                col += 1;
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
        SheetCell newSelection = headers.getHeaderForIndex(currentIndex).getCell();
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
