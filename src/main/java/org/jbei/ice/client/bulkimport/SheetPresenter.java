package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

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

        HashMap<Integer, ArrayList<String>> getCellData();
    }

    private final View view;

    public SheetPresenter(View view) {
        this.view = view;
    }

    public void reset() {
        if (Window.confirm("Clear all data?"))
            view.clear();
    }
}
