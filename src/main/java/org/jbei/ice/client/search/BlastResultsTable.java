package org.jbei.ice.client.search;

import java.util.ArrayList;

import org.jbei.ice.client.component.table.HasEntryDataTable;
import org.jbei.ice.shared.dto.BlastResultInfo;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.TextColumn;

public class BlastResultsTable extends HasEntryDataTable<BlastResultInfo> {

    public BlastResultsTable() {
        super();
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {

        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();

        columns.add(super.addTypeColumn(true));
        columns.add(super.addPartIdColumn(true));
        columns.add(super.addNameColumn());

        addAlignedColumn();
        addAlignedIdentityColumn();
        addBitScoreColumn();
        addEValueColumn();

        return columns;
    }

    protected void addAlignedColumn() {
        TextColumn<BlastResultInfo> column = new TextColumn<BlastResultInfo>() {

            @Override
            public String getValue(BlastResultInfo info) {
                return ""; // TODO

            }
        };

        this.addColumn(column, "Aligned (BP)");
        this.setColumnWidth(column, 200, Unit.PX);
    }

    protected void addAlignedIdentityColumn() {
        TextColumn<BlastResultInfo> column = new TextColumn<BlastResultInfo>() {

            @Override
            public String getValue(BlastResultInfo info) {
                return ""; // TODO
            }
        };

        this.addColumn(column, "Aligned % Identity");
        this.setColumnWidth(column, 200, Unit.PX);
    }

    protected void addBitScoreColumn() {
        TextColumn<BlastResultInfo> column = new TextColumn<BlastResultInfo>() {

            @Override
            public String getValue(BlastResultInfo info) {
                return String.valueOf(info.getBitScore());
            }
        };

        this.addColumn(column, "Bit Score");
        this.setColumnWidth(column, 200, Unit.PX);
    }

    protected void addEValueColumn() {
        TextColumn<BlastResultInfo> column = new TextColumn<BlastResultInfo>() {

            @Override
            public String getValue(BlastResultInfo info) {
                return String.valueOf(info.geteValue());
            }
        };

        this.addColumn(column, "E-Value");
        this.setColumnWidth(column, 200, Unit.PX);
    }

}
