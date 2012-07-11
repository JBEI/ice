package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.ModelFactory;
import org.jbei.ice.client.bulkimport.model.SheetCellData;
import org.jbei.ice.client.bulkimport.model.SheetModel;
import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.client.bulkimport.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkimport.sheet.InfoValueExtractorFactory;
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

        void highlightHeaders(int row, int col);

        void clear();

        void setRow(int row);

        int getRow();

        void setCellWidgetForCurrentRow(Header header, String value, int col);

        int getSheetRowCount();
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
            SheetCellData data = null;
            boolean rowHasData = false;

            // go through headers (column) for data
            for (Header header : headers) {
                data = header.getCell().getDataForRow(i);
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

    public void addRow() {
        int index = view.getRow() - 1; // row includes the headers but this is 0-indexed

        // type is already set in the constructor
        Header[] headers = getTypeHeaders();
        int headersSize = headers.length;

        for (int i = 0; i < headersSize; i += 1) {

            String value = "";

            if (currentInfo != null && currentInfo.getCount() >= view.getRow()) {
                EntryInfo info = currentInfo.getEntryList().get(index);

                // extractor also sets the header data structure
                value = InfoValueExtractorFactory.extractValue(getType(), headers[i], info, index);
            }

            view.setCellWidgetForCurrentRow(headers[i], value, i);
        }
        view.setRow(view.getRow() + 1);
    }
}
