package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.ModelFactory;
import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.bulkimport.model.SheetModel;
import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.client.bulkimport.sheet.ImportTypeHeaders;
import org.jbei.ice.client.bulkimport.sheet.InfoValueExtractorFactory;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;

public class SheetPresenter {

    /**
     * interface that the equivalent view
     * for this presenter must interface
     */
    public static interface View {

        void highlightHeaders(int row, int col);

        void clear();

        boolean isEmptyRow(int row);

        void setRow(int row);

        int getRow();

        void setCellWidgetForCurrentRow(Header header, String display, String title, int col);

        int getSheetRowCount();
    }

    private final View view;
    private HashMap<AutoCompleteField, ArrayList<String>> data;
    private final EntryAddType type;
    private BulkImportDraftInfo currentInfo; // used to maintain saved drafts that are loaded
    private final Header[] headers;

    public SheetPresenter(View view, EntryAddType type) {
        this.view = view;
        this.type = type;
        this.headers = ImportTypeHeaders.getHeadersForType(type);

        for (Header header : headers) {
            header.getCell().reset();
        }
    }

    public SheetPresenter(View view, EntryAddType type, BulkImportDraftInfo info) {
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

    public void setCurrentInfo(BulkImportDraftInfo info) {
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

            if (view.isEmptyRow(i)) {
                continue;
            }

            // sheet includes the headers so the actual rows start from 1
            int index = i - 1;

            // is row associated with a saved entry?
            EntryInfo existing;
            if (currentInfo != null && currentInfo.getEntryList().size() > index)
                existing = currentInfo.getEntryList().get(index);
            else
                existing = model.createInfo();

            // for each header
            for (Header header : headers) {
                String text = header.getCell().getValueForRow(index);
                String id = header.getCell().getIdForRow(index);
                existing = model.setInfoField(new SheetFieldData(header, id, text), existing);
            }

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

            String display = "";
            String title = "";
            if (currentInfo != null && currentInfo.getCount() >= view.getRow()) {

                EntryInfo primaryInfo = currentInfo.getEntryList().get(index);
                String value = InfoValueExtractorFactory.extractValue(getType(), headers[i],
                    primaryInfo, primaryInfo.getInfo(), index);
                if (value == null)
                    value = "";

                display = value;
                title = value;
                if (value.length() > 15)
                    display = (value.substring(0, 13) + "...");
            }

            view.setCellWidgetForCurrentRow(headers[i], display, title, i);
        }
        view.setRow(view.getRow() + 1);
    }
}
