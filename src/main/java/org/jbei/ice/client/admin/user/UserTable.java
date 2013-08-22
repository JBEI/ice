package org.jbei.ice.client.admin.user;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.table.cell.UrlCell;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Table for displaying list of users
 *
 * @author Hector Plahar
 */
public class UserTable extends CellTable<User> {

    protected interface UserTableResources extends Resources {

        static UserTableResources INSTANCE = GWT.create(UserTableResources.class);

        @Override
        @Source("org/jbei/ice/client/admin/css/UserTable.css")
        Style cellTableStyle();
    }

    private UserTableSelectionModel selectionModel;

    public UserTable() {
        super(15, UserTableResources.INSTANCE);
        Label empty = new Label();
        empty.setText("No users available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        setSelectionModel();
        createColumns();
    }

    /**
     * Adds a selection model so cells can be selected
     */
    private void setSelectionModel() {
        selectionModel = new UserTableSelectionModel();
        setSelectionModel(selectionModel, DefaultSelectionEventManager.<User>createCheckboxManager());
    }

    private void createColumns() {
//        createSelectionColumn();
        createFirstNameColumn();
        createLastNameColumn();
        createEmailColumn();
        createAccountTypeColumn();
        createEntryCountColumn();
        createActionColumn();
    }

    private void createActionColumn() {
    }

    private void createSelectionColumn() {

        Column<User, Boolean> checkColumn = new Column<User, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(User object) {
                return selectionModel.isSelected(object);
            }
        };
        addColumn(checkColumn, new SelectionColumnHeader());
        setColumnWidth(checkColumn, 15, Unit.PX);
    }

    private void createFirstNameColumn() {
        Column<User, String> firstNameColumn = new Column<User, String>(
                new EditTextCell()) {
            @Override
            public String getValue(User object) {
                return object.getFirstName();
            }
        };

        firstNameColumn.setSortable(false);
        //        sortHandler.setComparator(firstNameColumn, new Comparator<User>() {
        //            public int compare(User o1, User o2) {
        //                return o1.getFirstName().compareTo(o2.getFirstName());
        //            }
        //        });

        addColumn(firstNameColumn, "First Name");
        firstNameColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            public void update(int index, User object, String value) {
                // Called when the user changes the value.
                object.setFirstName(value);
                //                ContactDatabase.get().refreshDisplays();
            }
        });

        setColumnWidth(firstNameColumn, 20, Unit.PCT);
    }

    private void createLastNameColumn() {

        Column<User, String> lastName = new Column<User, String>(new EditTextCell()) {
            @Override
            public String getValue(User object) {
                return object.getLastName();
            }
        };

        lastName.setSortable(false);
        //        sortHandler.setComparator(firstNameColumn, new Comparator<User>() {
        //            public int compare(User o1, User o2) {
        //                return o1.getFirstName().compareTo(o2.getFirstName());
        //            }
        //        });

        addColumn(lastName, "Last Name");
        lastName.setFieldUpdater(new FieldUpdater<User, String>() {
            public void update(int index, User object, String value) {
                // Called when the user changes the value.
                object.setLastName(value);
                //                ContactDatabase.get().refreshDisplays(); // TODO
            }
        });
        setColumnWidth(lastName, 20, Unit.PCT);
    }

    private void createEmailColumn() {
        EmailCell cell = new EmailCell();
        Column<User, User> email = new Column<User, User>(cell) {

            @Override
            public User getValue(User object) {
                return object;
            }
        };
        addColumn(email, "Email");
        setColumnWidth(email, 30, Unit.PT);
    }

    private void createAccountTypeColumn() {
        Column<User, String> accountType = new Column<User, String>(new TextCell()) {
            @Override
            public String getValue(User object) {
                return object.getAccountType().toString();
            }
        };
        addColumn(accountType, "Account Type");
        setColumnWidth(accountType, 130, Unit.PX);
    }

    private void createEntryCountColumn() {
        Column<User, String> entryCount = new Column<User, String>(new TextCell()) {

            @Override
            public String getValue(User object) {
                return object.getUserEntryCount() + "";
            }
        };
        addColumn(entryCount, "# Entries");
        setColumnWidth(entryCount, 50, Unit.PX);
    }

    //
    // inner classes
    //
    protected class EmailCell extends UrlCell<User> {

        @Override
        protected String getCellValue(User object) {
            return object.getEmail();
        }

        @Override
        protected void onClick(User object) {
            History.newItem(Page.PROFILE.getLink() + ";id=" + object.getId() + ";s=profile");
        }
    }

    private class SelectionColumnHeader extends Header<String> {

        public SelectionColumnHeader() {
            super(new TextCell());
        }

        @Override
        public String getValue() {
            return Integer.toString(selectionModel.getSelectedSet().size());
        }
    }

    private class UserTableSelectionModel extends MultiSelectionModel<User> {

        private boolean allSelected;

        public UserTableSelectionModel() {

            super(new ProvidesKey<User>() {

                @Override
                public String getKey(User item) {
                    return item.getEmail();
                }
            });
        }

        public boolean isAllSelected() {
            return allSelected;
        }

        public void setAllSelected(boolean allSelected) {
            this.allSelected = allSelected;
        }
    }
}
