package org.jbei.ice.client.admin.web;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.web.RegistryPartner;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

/**
 * CellTable for displaying information about Web of Registries partners
 *
 * @author Hector Plahar
 */
public class PartnerTable extends CellTable<RegistryPartner> {

    protected interface TableResources extends Resources {
        static TableResources INSTANCE = GWT.create(TableResources.class);

        @Override
        @Source("org/jbei/ice/client/admin/css/GroupMemberTable.css")
        Style cellTableStyle();
    }

    private ListDataProvider<RegistryPartner> dataProvider;

    public PartnerTable() {
        super(15, TableResources.INSTANCE);

        dataProvider = new ListDataProvider<RegistryPartner>();
        dataProvider.addDataDisplay(this);

        Label empty = new Label();
        empty.setText("No registry partners available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        createColumns();
    }

    protected void createColumns() {
        createNameColumn();
        createURLColumn();
        createStatusColumn();
        createActionColumn();
    }

    protected void createNameColumn() {
        Column<RegistryPartner, String> column = new Column<RegistryPartner, String>(new TextCell()) {
            @Override
            public String getValue(RegistryPartner object) {
                if (object == null)
                    return "";

                return object.getName();
            }
        };

        column.setSortable(false);
        addColumn(column, "Name");
        setColumnWidth(column, 180, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createURLColumn() {
        Column<RegistryPartner, String> column = new Column<RegistryPartner, String>(new TextCell()) {
            @Override
            public String getValue(RegistryPartner object) {
                if (object == null)
                    return "";

                return object.getUrl();
            }
        };

        column.setSortable(false);
        addColumn(column, "URL");
        setColumnWidth(column, 180, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createStatusColumn() {
        Column<RegistryPartner, String> column = new Column<RegistryPartner, String>(new TextCell()) {
            @Override
            public String getValue(RegistryPartner object) {
                return object.getStatus().name();
            }
        };

        column.setSortable(false);
        addColumn(column, "Status");
        setColumnWidth(column, 180, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createActionColumn() {
        ActionCell cell = new ActionCell(null);
        Column<RegistryPartner, RegistryPartner> column = new Column<RegistryPartner, RegistryPartner>(cell) {
            @Override
            public RegistryPartner getValue(RegistryPartner object) {
                return object;
            }
        };

        column.setSortable(false);
        addColumn(column, "");
        setColumnWidth(column, 50, com.google.gwt.dom.client.Style.Unit.PX);
    }

    public void setData(ArrayList<RegistryPartner> data) {
        this.dataProvider.setList(data);
    }
}
