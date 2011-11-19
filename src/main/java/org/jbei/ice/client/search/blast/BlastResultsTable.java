package org.jbei.ice.client.search.blast;

import java.util.ArrayList;

import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.BlastResultInfo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;

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
        columns.add(addAlignedColumn());
        columns.add(addAlignedIdentityColumn());
        columns.add(addBitScoreColumn());
        columns.add(addEValueColumn());

        return columns;
    }

    protected DataTableColumn<String> addAlignedColumn() {

        DataTableColumn<String> alignedCol = new DataTableColumn<String>(new TextCell(),
                ColumnField.ALIGNED_BP) {

            @Override
            public String getValue(BlastResultInfo info) {
                return info.getAlignmentLength() + " / " + info.getQueryLength();
            }
        };
        alignedCol.setSortable(true);
        this.addColumn(alignedCol, "Aligned (BP)");
        this.setColumnWidth(alignedCol, 200, Unit.PX);
        return alignedCol;
    }

    protected DataTableColumn<String> addAlignedIdentityColumn() {
        DataTableColumn<String> alignedCol = new DataTableColumn<String>(new TextCell(),
                ColumnField.ALIGNED_IDENTITY) {

            @Override
            public String getValue(BlastResultInfo info) {
                return String.valueOf(info.getPercentId());
            }
        };
        alignedCol.setSortable(true);
        this.addColumn(alignedCol, "Aligned % Identity");
        this.setColumnWidth(alignedCol, 200, Unit.PX);
        return alignedCol;
    }

    protected DataTableColumn<String> addBitScoreColumn() {
        DataTableColumn<String> col = new DataTableColumn<String>(new TextCell(),
                ColumnField.BIT_SCORE) {

            @Override
            public String getValue(BlastResultInfo info) {
                return String.valueOf(info.getBitScore());
            }
        };
        col.setSortable(true);
        this.addColumn(col, ColumnField.BIT_SCORE.getName());
        this.setColumnWidth(col, 200, Unit.PX);
        return col;
    }

    protected DataTableColumn<String> addEValueColumn() {
        DataTableColumn<String> col = new DataTableColumn<String>(new TextCell(),
                ColumnField.E_VALUE) {

            @Override
            public String getValue(BlastResultInfo info) {
                return String.valueOf(info.geteValue());
            }
        };
        col.setSortable(true);
        this.addColumn(col, ColumnField.E_VALUE.getName());
        this.setColumnWidth(col, 200, Unit.PX);
        return col;
    }
}
