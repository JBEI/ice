package org.jbei.ice.client.search.blast;

import java.util.ArrayList;

import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.HasEntryPartIdColumn;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;
import org.jbei.ice.shared.dto.search.SearchResultInfo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;

public abstract class BlastResultsTable extends HasEntryDataTable<SearchResultInfo> {

    private final EntryTablePager pager;

    public BlastResultsTable() {
        super();
        this.pager = new EntryTablePager();
        pager.setDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<SearchResultInfo, ?>> createColumns() {
        ArrayList<DataTableColumn<SearchResultInfo, ?>> columns = new ArrayList<DataTableColumn<SearchResultInfo, ?>>();
        columns.add(super.addSelectionColumn());
        columns.add(super.addTypeColumn(false));
        DataTableColumn<SearchResultInfo, HasEntryInfo> partIdCol = addPartIdColumn(false, 120, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(addAlignedColumn());
        columns.add(addAlignedIdentityColumn());
        columns.add(addBitScoreColumn());
        columns.add(addEValueColumn());

        return columns;
    }

    protected DataTableColumn<SearchResultInfo, HasEntryInfo> addPartIdColumn(boolean sortable,
            double width, Unit unit) {
        HasEntryPartIDCell<HasEntryInfo> cell = new HasEntryPartIDCell<HasEntryInfo>(EntryContext.Type.SEARCH);
        cell.addEntryHandler(getHandler());
        DataTableColumn<SearchResultInfo, HasEntryInfo> partIdColumn = new HasEntryPartIdColumn<SearchResultInfo>(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected DataTableColumn<SearchResultInfo, String> addAlignedColumn() {

        DataTableColumn<SearchResultInfo, String> alignedCol =
                new DataTableColumn<SearchResultInfo, String>(new TextCell(), ColumnField.ALIGNED_BP) {

                    @Override
                    public String getValue(SearchResultInfo info) {
                        return info.getAlignmentLength() + " / " + info.getQueryLength();
                    }
                };
        alignedCol.setSortable(false);
        this.addColumn(alignedCol, "Aligned (BP)");
        this.setColumnWidth(alignedCol, 200, Unit.PX);
        return alignedCol;
    }

    protected DataTableColumn<SearchResultInfo, String> addAlignedIdentityColumn() {
        DataTableColumn<SearchResultInfo, String> alignedCol =
                new DataTableColumn<SearchResultInfo, String>(new TextCell(), ColumnField.ALIGNED_IDENTITY) {

                    @Override
                    public String getValue(SearchResultInfo info) {
                        return String.valueOf(info.getPercentId());
                    }
                };
        alignedCol.setSortable(false);
        this.addColumn(alignedCol, "Aligned % Identity");
        this.setColumnWidth(alignedCol, 200, Unit.PX);
        return alignedCol;
    }

    protected DataTableColumn<SearchResultInfo, String> addBitScoreColumn() {
        DataTableColumn<SearchResultInfo, String> col =
                new DataTableColumn<SearchResultInfo, String>(new TextCell(), ColumnField.BIT_SCORE) {

                    @Override
                    public String getValue(SearchResultInfo info) {
                        return String.valueOf(info.getBitScore());
                    }
                };
        col.setSortable(false);
        this.addColumn(col, ColumnField.BIT_SCORE.getName());
        this.setColumnWidth(col, 200, Unit.PX);
        return col;
    }

    protected DataTableColumn<SearchResultInfo, String> addEValueColumn() {
        DataTableColumn<SearchResultInfo, String> col =
                new DataTableColumn<SearchResultInfo, String>(new TextCell(), ColumnField.E_VALUE) {

                    @Override
                    public String getValue(SearchResultInfo info) {
                        return String.valueOf(info.geteValue());
                    }
                };
        col.setSortable(false);
        this.addColumn(col, ColumnField.E_VALUE.getName());
        this.setColumnWidth(col, 200, Unit.PX);
        return col;
    }

    public EntryTablePager getPager() {
        return pager;
    }
}
