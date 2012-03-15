package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.shared.AutoCompleteField;

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

    public SheetPresenter(View view) {
        this.view = view;
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
}
