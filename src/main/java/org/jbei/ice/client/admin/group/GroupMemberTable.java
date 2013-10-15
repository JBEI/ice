package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

/**
 * Table for displaying the name and email addresses (userId) of members of a group.
 * Also provides the option of removing each member
 *
 * @author Hector Plahar
 */
public class GroupMemberTable extends CellTable<User> {

    protected interface TableResources extends Resources {
        static TableResources INSTANCE = GWT.create(TableResources.class);

        @Override
        @Source("org/jbei/ice/client/admin/css/GroupMemberTable.css")
        Style cellTableStyle();
    }

    private ServiceDelegate<User> deleteDelegate;

    public GroupMemberTable(ServiceDelegate<User> deleteDelegate) {
        super(15, TableResources.INSTANCE);

        Label empty = new Label();
        this.deleteDelegate = deleteDelegate;
        empty.setText("No members selected or available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        setSelectionModel();
        createColumns(deleteDelegate != null);
    }

    /**
     * Adds a selection model so cells can be selected
     */
    private void setSelectionModel() {
        SelectionModel<User> selectionModel = new MultiSelectionModel<User>(
                new ProvidesKey<User>() {

                    @Override
                    public String getKey(User item) {
                        return String.valueOf(item.getId());
                    }
                });

        setSelectionModel(selectionModel, DefaultSelectionEventManager.<User>createCheckboxManager());
    }

    protected void createColumns(boolean createEditColumn) {
        createNameColumn();
        createEmailColumn();
        if (createEditColumn)
            createEditRemoveColumn();
    }

    protected void createEditRemoveColumn() {
        DeleteActionCell<User> cell = new DeleteActionCell<User>(deleteDelegate);
        Column<User, User> column = new Column<User, User>(cell) {

            @Override
            public User getValue(User object) {
                return object;
            }
        };
        addColumn(column, "");
        setColumnWidth(column, 25, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createNameColumn() {
        Column<User, String> column = new Column<User, String>(new TextCell()) {
            @Override
            public String getValue(User object) {
                if (object == null)
                    return "";

                return object.getFullName();
            }
        };

        column.setSortable(false);
        addColumn(column, "Name");
//        setColumnWidth(column, 30, com.google.gwt.dom.client.Style.Unit.PCT);
    }

    protected void createEmailColumn() {
        Column<User, String> column = new Column<User, String>(new TextCell()) {
            @Override
            public String getValue(User object) {
                if (object == null)
                    return "";
                return object.getEmail();
            }
        };
        column.setSortable(false);
        addColumn(column, "Email");
    }
}
