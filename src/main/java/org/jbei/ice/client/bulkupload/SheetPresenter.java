package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;

import org.jbei.ice.client.bulkupload.model.ModelFactory;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.model.SheetModel;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.bulkupload.sheet.header.BulkUploadHeaders;
import org.jbei.ice.client.bulkupload.sheet.header.SampleHeaders;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class SheetPresenter {

    /**
     * View interface for sheet
     */
    public static interface View {

        boolean clear();

        void setCellWidgetForCurrentRow(String value, int row, int col, int tabIndex);

        void removeCellForCurrentRow(int row, int col, int count);

        int getSheetRowCount();

        int getSheetColumnCount(int row);

        void clearErrorCell(int row, int col);

        void setErrorCell(int row, int col, String errMsg);

        void scrollElementToView(int row, int col);

        SampleLocation getSampleSelectionLocation();

        void createHeaderCells();
    }

    private final View view;
    private final EntryAddType type;
    private BulkUploadInfo currentInfo; // used to maintain saved drafts that are loaded
    private final BulkUploadHeaders headers;
    private SampleHeaders sampleHeaders;

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
        if (view.clear())
            this.currentInfo.getEntryList().clear();
    }

    // currently goes through each row and cell and checks to cell value
    public boolean isEmptyRow(int row) {

        for (CellColumnHeader header : headers.getHeaders()) {
            if (header.getCell().getDataForRow(row) != null)
                return false;
        }

        if (sampleHeaders != null) {
            for (CellColumnHeader header : sampleHeaders.getHeaders()) {
                if (header.getCell().getDataForRow(row) != null)
                    return false;
            }
        }

        return true;
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
        if (sampleHeaders != null)
            return headers.getHeaderSize() + sampleHeaders.getHeaderSize();
        return headers.getHeaderSize();
    }

    public void addRow(int row) {
        int headerSize = headers.getHeaderSize();

        for (int i = 0; i < headerSize; i += 1) {
            String value = "";

            if (currentInfo != null && currentInfo.getCount() > row) {
                EntryInfo info = currentInfo.getEntryList().get(row);

                // extractor also sets the header data structure
                CellColumnHeader header = headers.getHeaderForIndex(i);
                SheetCellData data = headers.extractValue(header.getHeaderType(), info);
                header.getCell().setWidgetValue(row, data);
                value = data.getValue();
            }
            view.setCellWidgetForCurrentRow(value, row, i, headers.getHeaderSize());
        }

        addSampleHeaderRows(row);
    }

    public void addSampleHeaderRows(int row) {

        if (sampleHeaders == null)
            return;

        int i = headers.getHeaderSize();   // starting point
        for (CellColumnHeader header : sampleHeaders.getHeaders()) {
            String value = "";

            if (currentInfo != null && currentInfo.getCount() > row) {
                EntryInfo info = currentInfo.getEntryList().get(row);

                // extractor also sets the header data structure
                if (info.isHasSample()) {
                    SheetCellData data = sampleHeaders.extractValue(header.getHeaderType(), info);
                    header.getCell().setWidgetValue(row, data);
                    value = data.getValue();
                }
            }

            view.setCellWidgetForCurrentRow(value, row, i, getFieldSize());
            i += 1;
        }
    }


    protected void removeSampleHeaderRows(int row) {
        int fieldSize = getFieldSize();
        int colCount = view.getSheetColumnCount(row);
        int toRemove = colCount - fieldSize;

        if (toRemove <= 0)
            return;

        view.removeCellForCurrentRow(row, fieldSize - 1, toRemove);
    }

    /**
     * @return true if all cells validate, false otherwise
     */
    public boolean validateCells() {

        boolean isValid = true;
        boolean inView = false;

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
                    col += 1;
                    continue;
                }

                isValid = false;
                view.setErrorCell(row, col, errMsg);
                if (!inView) {
                    view.scrollElementToView(row, col);
                    inView = true;
                }
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

    public ArrayList<CellColumnHeader> getAllHeaders() {
        ArrayList<CellColumnHeader> headers = new ArrayList<CellColumnHeader>();
        headers.addAll(this.headers.getHeaders());

        if (sampleHeaders != null) {
            headers.addAll(sampleHeaders.getHeaders());
        }
        return headers;
    }

    public HandlerRegistration setSampleSelectionHandler(final EntryAddType addType,
            final SingleSelectionModel<SampleInfo> selectionModel) {

        return selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (SheetPresenter.this.type != addType)
                    return;

                SampleInfo info = selectionModel.getSelectedObject();
                ArrayList<String> locationList = view.getSampleSelectionLocation().getListForLocation(
                        info.getLocationId());

                // add sample cols
                sampleHeaders = ImportTypeHeaders.getSampleHeaders(type, locationList);
                if (sampleHeaders == null || sampleHeaders.getHeaders().isEmpty())
                    return;

                view.createHeaderCells();

                int rowCells = view.getSheetColumnCount(0);
                int headerCount = getFieldSize();
                int rowMax = view.getSheetRowCount();

                if (rowCells < headerCount) {

                    for (int row = 0; row < rowMax; row += 1) {
                        addSampleHeaderRows(row);
                    }
                } else {
                    // remove
                    for (int row = 0; row < rowMax; row += 1) {
                        removeSampleHeaderRows(row);
                    }
                }
                // scroll everything into view
                view.scrollElementToView(0, getFieldSize() - 1);
            }
        });
    }
}
