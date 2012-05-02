package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.bulkimport.sheet.Header;
import org.jbei.ice.client.bulkimport.sheet.ImportTypeHeaders;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;

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

        ArrayList<SheetFieldData[]> getCellData();
    }

    private final View view;
    private HashMap<AutoCompleteField, ArrayList<String>> data;
    private final EntryAddType type;

    public SheetPresenter(View view, EntryAddType type) {
        this.view = view;
        this.type = type;
    }

    public void reset() {
        if (Window.confirm("Clear all data?"))
            view.clear();
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

    public Header[] getTypeHeaders() {
        return ImportTypeHeaders.getHeadersForType(type);
    }

    /**
     * @return size of the field, which also equates to
     *         the number of columns displayed in the sheet. This is based on the number of
     *         headers for the entry type
     */
    public int getFieldSize() {
        return getTypeHeaders().length;
    }
}
