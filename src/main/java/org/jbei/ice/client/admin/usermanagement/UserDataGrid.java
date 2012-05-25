package org.jbei.ice.client.admin.usermanagement;

import java.util.ArrayList;
import java.util.Comparator;

import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.ProvidesKey;

public class UserDataGrid extends Composite {

    private final DataGrid<AccountInfo> grid;

    public UserDataGrid(ArrayList<AccountInfo> data) {
        grid = new DataGrid<AccountInfo>(new ProvidesKey<AccountInfo>() {

            @Override
            public String getKey(AccountInfo item) {
                return item.getEmail();
            }
        });

        grid.setWidth("100%");
        grid.setPageSize(30);
        initWidget(grid);

        // message to display when grid is empty
        grid.setEmptyTableWidget(new HTML(
                "<span class=\"font-80em pad-20\">No users available</span>"));

        ListHandler<AccountInfo> sortHandler = new ListHandler<AccountInfo>(data);
        grid.addColumnSortHandler(sortHandler);

        initTableColumns(sortHandler);

        // add data display
    }

    private void initTableColumns(ListHandler<AccountInfo> sortHandler) {

        // First name.
        Column<AccountInfo, String> firstNameColumn = new Column<AccountInfo, String>(
                new EditTextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getFirstName();
            }
        };

        firstNameColumn.setSortable(true);
        sortHandler.setComparator(firstNameColumn, new Comparator<AccountInfo>() {
            public int compare(AccountInfo o1, AccountInfo o2) {
                return o1.getFirstName().compareTo(o2.getFirstName());
            }
        });

        grid.addColumn(firstNameColumn, "First Name");
        firstNameColumn.setFieldUpdater(new FieldUpdater<AccountInfo, String>() {
            public void update(int index, AccountInfo object, String value) {
                // Called when the user changes the value.
                object.setFirstName(value);
                //                ContactDatabase.get().refreshDisplays();
            }
        });
        grid.setColumnWidth(firstNameColumn, 20, Unit.PCT);
    }

}
