package org.jbei.ice.client.bulkupload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.bulkupload.model.ModelFactory;
import org.jbei.ice.client.bulkupload.model.SheetCellData;
import org.jbei.ice.client.bulkupload.model.SheetModel;
import org.jbei.ice.client.bulkupload.sheet.CellColumnHeader;
import org.jbei.ice.client.bulkupload.sheet.Header;
import org.jbei.ice.client.bulkupload.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkupload.sheet.cell.SheetCell;
import org.jbei.ice.client.bulkupload.sheet.header.BulkUploadHeaders;
import org.jbei.ice.client.bulkupload.sheet.header.SampleHeaders;
import org.jbei.ice.client.bulkupload.sheet.header.StrainWithPlasmidHeaders;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.shared.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.entry.EntryType;

import com.google.gwt.core.client.GWT;
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

        void createHeaderCells(ServiceDelegate<PreferenceInfo> lockUnlockDelegate);
    }

    private final View view;
    private final EntryAddType type;
    private BulkUploadInfo currentInfo; // used to maintain saved drafts that are loaded
    private final BulkUploadHeaders headers;
    private SampleHeaders sampleHeaders;
    private String currentSampleLocationId;
    private final HashMap<Integer, BitSet> rowBitSet;
    private ServiceDelegate<BulkUploadAutoUpdate> autoUpdateDelegate;
    private final ServiceDelegate<PreferenceInfo> serviceDelegate;
    private final HashMap<Integer, EntryInfo> rowInfoMap;
    private static final HashMap<EntryAddType, SheetModel<? extends EntryInfo>> sheetModelCache =
            new HashMap<EntryAddType, SheetModel<? extends EntryInfo>>();

    public SheetPresenter(View view, EntryAddType type, BulkUploadInfo info, ServiceDelegate<PreferenceInfo>
            serviceDelegate) {
        this.view = view;
        this.type = type;
        this.serviceDelegate = serviceDelegate;
        this.rowBitSet = new HashMap<Integer, BitSet>();
        this.rowInfoMap = new HashMap<Integer, EntryInfo>();

        this.currentInfo = info;
        if (info != null && info.getEntryList() != null) {
            for (int i = 0; i < info.getEntryList().size(); i += 1) {
                rowInfoMap.put(i, info.getEntryList().get(i));
            }
            currentInfo.getEntryList().clear();
        }

        HashMap<String, String> preferences = new HashMap<String, String>();
        if (currentInfo != null && currentInfo.getPreferences() != null) {
            for (PreferenceInfo preferenceInfo : currentInfo.getPreferences()) {
                preferences.put(preferenceInfo.getKey().toUpperCase(), preferenceInfo.getValue());
            }
        }
        this.headers = ImportTypeHeaders.getHeadersForType(type, createInfoDelegate(), preferences);
    }

    /**
     * @param preferences mapping of header (uppercase) to a preference
     */
    public void setPreferences(HashMap<String, PreferenceInfo> preferences) {
        for (CellColumnHeader header : this.headers.getHeaders()) {
            if (!header.isCanLock() || header.isLocked())
                continue;

            PreferenceInfo info = preferences.get(header.toString().toUpperCase());
            if (info == null)
                continue;

            header.setDefaultValue(info.getValue());
            header.setLocked(true);
            // TODO : update lock view
        }
    }

    protected EntryInfoDelegate createInfoDelegate() {
        return new EntryInfoDelegate() {

            @Override
            public EntryInfo getInfoForRow(int row, boolean isStrainWithPlasmidPlasmid) {
                EntryInfo info = rowInfoMap.get(row);
                if (isStrainWithPlasmidPlasmid && info != null)
                    return info.getInfo();
                return info;
            }
        };
    }

    public void reset() {
        if (view.clear()) {
            this.rowInfoMap.clear();
            headers.reset();
            this.rowBitSet.clear();
            if (sampleHeaders != null)
                sampleHeaders.reset();
        }
    }

    public int getEntryRowCount() {
        return rowInfoMap.size();
    }

    public boolean isEmptyRow(int row) {
        return rowBitSet.get(row).hasSetBit();
    }

    public EntryAddType getType() {
        return this.type;
    }

    public BulkUploadInfo setUpdateBulkUploadId(long id) {
        if (currentInfo == null)
            currentInfo = new BulkUploadInfo();
        currentInfo.setId(id);
        return currentInfo;
    }

    /**
     * sets the entry that was updated . TODO : getCell is not cell for the header
     * <p/>
     * // todo sample code for setting cell
     * CellColumnHeader header = headers.getHeaderForIndex(i);
     * SheetCellData data = headers.extractValue(header.getHeaderType(), info);
     * header.getCell().setWidgetValue(row, data);
     * if (data != null)
     * value = data.getValue();
     *
     * @param autoUpdate info from auto update
     * @return bulk upload info with updated stats based on returned values
     */
    public BulkUploadInfo setUpdateEntry(BulkUploadAutoUpdate autoUpdate) {
        if (currentInfo == null) {
            currentInfo = new BulkUploadInfo();
            currentInfo.setType(type);
        }
        currentInfo.setId(autoUpdate.getBulkUploadId());

        EntryInfo info = rowInfoMap.get(autoUpdate.getRow());

        // display all fields that were added
        for (Map.Entry<String, String> set : autoUpdate.getKeyValue().entrySet()) {
            String field = set.getKey();
            String value = set.getValue();
            Header header = Header.stringToHeader(field);
            SheetCellData data = new SheetCellData(header, "", value);
            info = getModelForCurrentType().setInfoField(data, info);
        }

        info.setId(autoUpdate.getEntryId());
        rowInfoMap.put(autoUpdate.getRow(), info);

        currentInfo.setCount(rowInfoMap.size());
        return currentInfo;
    }

    public SheetCell getCellForIndex(int newCol) {
        if (newCol < headers.getHeaderSize())
            return headers.getHeaderForIndex(newCol).getCell();

        int index = newCol - headers.getHeaderSize();
        return sampleHeaders.getHeaderForIndex(index).getCell();
    }

    private SheetModel<? extends EntryInfo> getModelForCurrentType() {
        SheetModel<? extends EntryInfo> model = sheetModelCache.get(type);
        if (model == null) {
            model = ModelFactory.getModelForType(type);
            sheetModelCache.put(type, model);
        }
        return model;
    }

    public void setAutoUpdateDelegate(ServiceDelegate<BulkUploadAutoUpdate> delegate) {
        autoUpdateDelegate = delegate;
    }

    /**
     * Auto updates cell in specified index and row
     *
     * @param inputIndex sheet index (column)
     * @param inputRow   sheet row
     */
    public void autoUpdate(int inputIndex, int inputRow) {
        if (inputIndex == -1 || inputRow == -1)
            return;

        CellColumnHeader header = getHeaderForIndex(inputIndex);

        // do not auto update locked headers since that is done when another cell in the same row is updated
        if (header.isLocked()) {
            GWT.log("skipping update of locked cell (" + inputRow + ", " + inputIndex + ")");
            return;
        }

        // check particular row being updated (which narrows it down to a cell since we are looking at the column)
        SheetCellData data = header.getCell().getDataForRow(inputRow);
        String value = "";
        if (data != null) {
            value = data.getValue();
        }

        // determine if the entry exists and we are updating based on the entryid
        long entryId = 0;
        EntryInfo rowInfo = rowInfoMap.get(inputRow);
        EntryType entryType;
        if (type == EntryAddType.STRAIN_WITH_PLASMID) {
            boolean isPlasmid = StrainWithPlasmidHeaders.isPlasmidHeader(header.getHeaderType());

            if (isPlasmid) {
                // if updating plasmid portion of strain with one plasmid
                entryType = EntryType.PLASMID;
                if (rowInfo != null && rowInfo.getInfo() != null)
                    entryId = rowInfo.getInfo().getId();
            } else {
                entryType = EntryType.STRAIN;
                if (rowInfo != null) {
                    entryId = rowInfo.getId();
                }
            }
        } else {
            entryType = EntryAddType.addTypeToType(type);
            if (rowInfo != null)
                entryId = rowInfo.getId();
        }

        // submit for auto update
        BulkUploadAutoUpdate update = new BulkUploadAutoUpdate();
        update.setEntryId(entryId);
        update.getKeyValue().put(header.toString(), value);
        update.setType(entryType);
        long bulkUpload = currentInfo == null ? 0 : currentInfo.getId();
        update.setBulkUploadId(bulkUpload);
        update.setRow(inputRow);

        // check locked rows
        int i = -1;
        for (CellColumnHeader columnHeader : this.headers.getHeaders()) {
            i += 1;
            if (!columnHeader.isLocked())
                continue;

            SheetCellData cellData = columnHeader.getCell().getDataForRow(inputRow);
            if (cellData != null)
                continue;

            update.getKeyValue().put(columnHeader.toString(), columnHeader.getDefaultValue());

            // update cell display
            view.setCellWidgetForCurrentRow(columnHeader.getDefaultValue(), inputRow, i, -1);
        }

        // successful execution calls setUpdateEntry(autoUpdate) above
        autoUpdateDelegate.execute(update);
    }

    public ServiceDelegate<PreferenceInfo> getPreferenceDelegate() {
        return this.serviceDelegate;
    }

//    public ArrayList<EntryInfo> getCellEntryList(String ownerEmail, String owner, String creator,
// String creatorEmail) {
//        int rowCount = rowBitSet.size();
//        SheetModel<? extends EntryInfo> model = getModelForCurrentType();
//        if (model == null)
//            return null;
//
//        ArrayList<EntryInfo> infoList = new ArrayList<EntryInfo>();
//
//        // for each row
//        for (int i = 0; i < rowCount; i += 1) {
//            // does it have new data (both new records and existing)
//            boolean rowHasNewData = rowBitSet.get(i).hasSetBit();
//            if (!rowHasNewData)
//                continue;
//
//            // is row associated with a saved entry?
//            EntryInfo existing;
//            if (currentInfo != null && currentInfo.getEntryList().size() > i)
//                existing = currentInfo.getEntryList().get(i);
//            else
//                existing = model.createInfo();
//
//            // go through headers (column) for data
//            ArrayList<Integer> dataRows = rowBitSet.get(i).getSetBits();
//            for (Integer dataRow : dataRows) {
//                CellColumnHeader header = getHeaderForIndex(dataRow);
//                SheetCellData data = header.getCell().getDataForRow(i);
//                if (data == null) {
//                    // clear the data associated with header
//                    data = new SheetCellData(header.getHeaderType(), "", "");
//                    model.setInfoField(data, existing);
//                    continue;
//                }
//                data.setType(header.getHeaderType());
//                existing = model.setInfoField(data, existing);
//            }
//
//            if (existing != null) {
//                if (ownerEmail != null && owner != null) {
//                    existing.setOwnerEmail(ownerEmail);
//                    existing.setOwner(owner);
//
//                    if (existing.getInfo() != null) {
//                        existing.getInfo().setOwnerEmail(ownerEmail);
//                        existing.getInfo().setOwner(owner);
//                    }
//                }
//
//                // set creator information
//                existing.setCreator(creator);
//                existing.setCreatorEmail(creatorEmail);
//                if (existing.getInfo() != null) {
//                    existing.getInfo().setCreator(creator);
//                    existing.getInfo().setCreatorEmail(creatorEmail);
//                }
//
//                // set sample location
//                if (existing.isHasSample()) {
//                    SampleStorage sampleStorage = existing.getOneSampleStorage();
//                    sampleStorage.getSample().setLocationId(currentSampleLocationId);
//                    sampleStorage.getSample().setDepositor(owner);
//                }
//
//                infoList.add(existing);
//            }
//        }
//
//        if (currentInfo != null) {
//            currentInfo.getEntryList().clear();
//            currentInfo.getEntryList().addAll(infoList);
//        }
//        return infoList;
//    }
//

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
        // bit set
        int size = sampleHeaders == null ? headerSize : (headerSize + sampleHeaders.getHeaderSize());
        rowBitSet.put(row, new BitSet(size));

        for (int i = 0; i < headerSize; i += 1) {
            String value = "";

            // check for existing infos
            EntryInfo info = rowInfoMap.get(row);
            if (info != null) {
                // extractor also sets the header data structure
                CellColumnHeader header = headers.getHeaderForIndex(i);
                SheetCellData data = headers.extractValue(header.getHeaderType(), info);
                header.getCell().setWidgetValue(row, data);
                if (data != null)
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
            EntryInfo info = rowInfoMap.get(row);
            if (info != null) {
                // extractor also sets the header data structure
                if (info.isHasSample()) {
                    SheetCellData data = sampleHeaders.extractValue(header.getHeaderType(), info);
                    header.getCell().setWidgetValue(row, data);

                    if (data != null)
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
        SheetCell newSelection = getHeaderForIndex(currentIndex).getCell();
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

    public CellColumnHeader getHeaderForIndex(int index) {
        int headerSize = this.headers.getHeaderSize();
        if (index < headerSize)
            return headers.getHeaderForIndex(index);

        // check samples
        if (sampleHeaders != null)
            return sampleHeaders.getHeaderForIndex(index - headerSize);

        return null;
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

                // get selected sample info and retrieve storage list options
                SampleInfo info = selectionModel.getSelectedObject();
                if (selectSample(addType, info.getLocationId())) {
                    // scroll everything into view
                    view.scrollElementToView(0, getFieldSize() - 1);
                }
            }
        });
    }

    public boolean selectSample(final EntryAddType addType, String locationId) {
        if (SheetPresenter.this.type != addType)
            return false;

        // get selected sample info and retrieve storage list options
        ArrayList<String> locationList = view.getSampleSelectionLocation().getListForLocation(locationId);
        locationList.add(0, "Name");

        // add sample cols
        sampleHeaders = ImportTypeHeaders.getSampleHeaders(type, locationList);
        if (sampleHeaders == null || sampleHeaders.getHeaders().isEmpty())
            return false;

        currentSampleLocationId = locationId;
        view.createHeaderCells(this.serviceDelegate);

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

        return true;
    }

    //
    // inner classes
    //

    private static class BitSet {

        private Boolean[] set;

        public BitSet(int size) {
            set = new Boolean[size];
        }

        public boolean hasSetBit() {
            for (int i = 0; i < set.length; i += 1) {
                if (set[i])
                    return true;
            }
            return false;
        }
    }
}
