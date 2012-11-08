package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.shared.dto.AccountInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

/**
 * @author Hector Plahar
 */
public class GroupMemberTable extends CellTable<AccountInfo> {

    protected interface TableResources extends Resources {
        static TableResources INSTANCE = GWT.create(TableResources.class);

        @Override
        @Source("org/jbei/ice/client/admin/css/GroupMemberTable.css")
        Style cellTableStyle();
    }

    private SelectionModel<AccountInfo> selectionModel;
    private ServiceDelegate<AccountInfo> deleteDelegate;

    public GroupMemberTable() {
        super(15, TableResources.INSTANCE);

        Label empty = new Label();
        empty.setText("No members for group");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        setSelectionModel();
        createColumns();
    }

    public void setDeleteDelegate(ServiceDelegate<AccountInfo> deleteDelegate) {
        this.deleteDelegate = deleteDelegate;
    }

    /**
     * Adds a selection model so cells can be selected
     */
    private void setSelectionModel() {
        selectionModel = new MultiSelectionModel<AccountInfo>(new ProvidesKey<AccountInfo>() {

            @Override
            public String getKey(AccountInfo item) {
                return String.valueOf(item.getId());
            }
        });

        setSelectionModel(selectionModel, DefaultSelectionEventManager.<AccountInfo>createCheckboxManager());
    }

    protected void createColumns() {
        createIDColumn();
        createNameColumn();
        createEmailColumn();
        createEditRemoveColumn();
    }

    protected void createIDColumn() {
        Column<AccountInfo, String> column = new Column<AccountInfo, String>(new TextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getId() + "";
            }
        };

        addColumn(column, "ID");
    }

    protected void createEditRemoveColumn() {
        DeleteActionCell<AccountInfo> cell = new DeleteActionCell<AccountInfo>(deleteDelegate);
        Column<AccountInfo, AccountInfo> column = new Column<AccountInfo, AccountInfo>(cell) {

            @Override
            public AccountInfo getValue(AccountInfo object) {
                return object;
            }
        };
        addColumn(column, "");

    }

    protected void createSelectionColumn() {
        Column<AccountInfo, Boolean> checkColumn = new Column<AccountInfo, Boolean>(new CheckboxCell(
                true, false)) {
            @Override
            public Boolean getValue(AccountInfo object) {
                return selectionModel.isSelected(object);
            }
        };
        addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        setColumnWidth(checkColumn, 20, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createNameColumn() {
        Column<AccountInfo, String> column = new Column<AccountInfo, String>(new TextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getFullName();
            }
        };

        column.setSortable(false);
        addColumn(column, "Name");
//        setColumnWidth(column, 30, com.google.gwt.dom.client.Style.Unit.PCT);
    }

    protected void createEmailColumn() {
        Column<AccountInfo, String> column = new Column<AccountInfo, String>(new TextCell()) {
            @Override
            public String getValue(AccountInfo object) {
                return object.getEmail();
            }
        };
        column.setSortable(false);
        addColumn(column, "Email");
    }
}
