package org.jbei.ice.client.admin.usermanagement;

import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

public class EditUserPanel extends Composite implements AdminPanel {

    private VerticalPanel panel;
    private final CellTable<AccountInfo> grid;

    public EditUserPanel() {
        panel = new VerticalPanel();
        initWidget(panel);

        grid = new CellTable<AccountInfo>();

        grid.setWidth("100%");
        grid.setPageSize(30);

        // message to display when grid is empty
        grid.setEmptyTableWidget(new HTML(
                "<span class=\"font-80em pad-20\">No users available</span>"));
        initTableColumns();

        panel.add(grid);
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(grid);
        panel.add(pager);
    }

    @Override
    public String getTitle() {
        return "User Management";
    }

    //    public void setData(ArrayList<AccountInfo> data) {
    //        ListHandler<AccountInfo> sortHandler = new ListHandler<AccountInfo>(data);
    //        grid.addColumnSortHandler(sortHandler);
    //        initTableColumns(sortHandler);
    //        grid.setRowCount(data.size(), true);
    //        grid.setRowData(0, data);
    //    }

    @Override
    public Widget getWidget() {
        return this;
    }

    @Override
    public HasData<AccountInfo> getDisplay() {
        return this.grid;
    }

    private void initTableColumns() {

        // First name.
        Column<AccountInfo, String> firstNameColumn = createFirstNameColumn();
        grid.setColumnWidth(firstNameColumn, 20, Unit.PCT);

        // last name
        Column<AccountInfo, String> lastName = new Column<AccountInfo, String>(new EditTextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getLastName();
            }
        };

        lastName.setSortable(false);
        //        sortHandler.setComparator(firstNameColumn, new Comparator<AccountInfo>() {
        //            public int compare(AccountInfo o1, AccountInfo o2) {
        //                return o1.getFirstName().compareTo(o2.getFirstName());
        //            }
        //        });

        grid.addColumn(lastName, "Last Name");
        lastName.setFieldUpdater(new FieldUpdater<AccountInfo, String>() {
            public void update(int index, AccountInfo object, String value) {
                // Called when the user changes the value.
                object.setLastName(value);
                //                ContactDatabase.get().refreshDisplays();
            }
        });
        grid.setColumnWidth(lastName, 20, Unit.PCT);
    }

    private Column<AccountInfo, String> createFirstNameColumn() {
        Column<AccountInfo, String> firstNameColumn = new Column<AccountInfo, String>(
                new EditTextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getFirstName();
            }
        };

        firstNameColumn.setSortable(false);
        //        sortHandler.setComparator(firstNameColumn, new Comparator<AccountInfo>() {
        //            public int compare(AccountInfo o1, AccountInfo o2) {
        //                return o1.getFirstName().compareTo(o2.getFirstName());
        //            }
        //        });

        grid.addColumn(firstNameColumn, "First Name");
        firstNameColumn.setFieldUpdater(new FieldUpdater<AccountInfo, String>() {
            public void update(int index, AccountInfo object, String value) {
                // Called when the user changes the value.
                object.setFirstName(value);
                //                ContactDatabase.get().refreshDisplays();
            }
        });

        return firstNameColumn;
    }
}
