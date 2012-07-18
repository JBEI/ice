package org.jbei.ice.client.admin.group;

import org.jbei.ice.shared.dto.GroupInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

/**
 * Table for displaying Group details
 *
 * @author Hector Plahar
 */
class GroupTable extends CellTable<GroupInfo> {
    protected interface EntryResources extends Resources {

        static EntryResources INSTANCE = GWT.create(EntryResources.class);

        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/EntryTable.css")
        Style cellTableStyle();
    }

    private SelectionModel<GroupInfo> selectionModel;

    public GroupTable() {
        super(15, EntryResources.INSTANCE);
        setStyleName("data_table");
        Label empty = new Label();
        empty.setText("No data available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        setSelectionModel();
        createColumns();

        /* 
         * ListHandler<ContactInfo> sortHandler =
        new ListHandler<ContactInfo>(ContactDatabase.get().getDataProvider().getList());
        dataGrid.addColumnSortHandler(sortHandler);
         */
    }

    /**
     * Adds a selection model so cells can be selected
     */
    private void setSelectionModel() {
        selectionModel = new MultiSelectionModel<GroupInfo>(new ProvidesKey<GroupInfo>() {

            @Override
            public String getKey(GroupInfo item) {
                return String.valueOf(item.getId());
            }
        });

        setSelectionModel(selectionModel,
                          DefaultSelectionEventManager.<GroupInfo>createCheckboxManager());
    }

    private void createColumns() {
//        createSelectionColumn();
        createIDColumn();
        createLabelColumn();
        createDescriptionColumn();
        createParentColumn();
        createUUIDColumn();
    }

    private void createIDColumn() {
        Column<GroupInfo, String> column = new Column<GroupInfo, String>(new TextCell()) {
            @Override
            public String getValue(GroupInfo object) {
                return object.getId() + "";
            }
        };

        column.setSortable(false);
        addColumn(column, "ID");
//        setColumnWidth(column, 30, Unit.PCT);

    }

    private void createSelectionColumn() {
        Column<GroupInfo, Boolean> checkColumn = new Column<GroupInfo, Boolean>(new CheckboxCell(
                true, false)) {
            @Override
            public Boolean getValue(GroupInfo object) {
                return selectionModel.isSelected(object);
            }
        };
        addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        setColumnWidth(checkColumn, 20, Unit.PX);
    }

    private void createLabelColumn() {
        Column<GroupInfo, String> column = new Column<GroupInfo, String>(new EditTextCell()) {
            @Override
            public String getValue(GroupInfo object) {
                return object.getLabel();
            }
        };

        column.setSortable(false);
        //        sortHandler.setComparator(firstNameColumn, new Comparator<AccountInfo>() {
        //            public int compare(AccountInfo o1, AccountInfo o2) {
        //                return o1.getFirstName().compareTo(o2.getFirstName());
        //            }
        //        });

        addColumn(column, "Label");
        column.setFieldUpdater(new FieldUpdater<GroupInfo, String>() {
            public void update(int index, GroupInfo object, String value) {
                // Called when the user changes the value.
                // TODO ContactDatabase.get().refreshDisplays();
            }
        });

        setColumnWidth(column, 30, Unit.PCT);
    }

    private void createDescriptionColumn() {
        Column<GroupInfo, String> column = new Column<GroupInfo, String>(new EditTextCell()) {
            @Override
            public String getValue(GroupInfo object) {
                return object.getDescription();
            }
        };
        column.setSortable(false);
        addColumn(column, "Description");
        column.setFieldUpdater(new FieldUpdater<GroupInfo, String>() {
            public void update(int index, GroupInfo object, String value) {
                // Called when the user changes the value.
                // TODO ContactDatabase.get().refreshDisplays();
            }
        });
    }

    private void createUUIDColumn() {
        Column<GroupInfo, String> column = new Column<GroupInfo, String>(new TextCell()) {
            @Override
            public String getValue(GroupInfo object) {
                return object.getUuid();
            }
        };
        addColumn(column, "UUID");
    }

    private void createParentColumn() {
        Column<GroupInfo, String> column = new Column<GroupInfo, String>(new TextCell()) {
            @Override
            public String getValue(GroupInfo object) {
                return object.getParentId() + "";
            }
        };
        addColumn(column, "Parent ID");
    }
}
