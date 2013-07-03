package org.jbei.ice.client.search.blast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.client.common.table.HasEntryDataTable;
import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.HasEntryPartIdColumn;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.HasEntryInfo;
import org.jbei.ice.shared.dto.search.SearchResultInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Table for displaying blast results
 *
 * @author Hector Plahar
 */
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
        DataTableColumn<SearchResultInfo, HasEntryInfo> partIdCol = addPartIdColumn(false, 100, Unit.PX);
        columns.add(partIdCol);
        columns.add(super.addNameColumn(120, Unit.PX));
        columns.add(super.addSummaryColumn());
        columns.add(addAlignmentColumn());
        columns.add(addEValueColumn());
        columns.add(addDistributionColumn());

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

    protected DataTableColumn<SearchResultInfo, String> addAlignmentColumn() {
        DataTableColumn<SearchResultInfo, String> alignedCol =
                new DataTableColumn<SearchResultInfo, String>(new TextCell(), ColumnField.ALIGNED_IDENTITY) {

                    @Override
                    public String getValue(SearchResultInfo info) {
                        return info.getAlignment();
                    }
                };
        alignedCol.setSortable(false);
        this.addColumn(alignedCol, "Alignment");
        this.setColumnWidth(alignedCol, 120, Unit.PX);
        return alignedCol;
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
        this.setColumnWidth(col, 85, Unit.PX);
        return col;
    }

    protected DataTableColumn<SearchResultInfo, SearchResultInfo> addDistributionColumn() {

        AbstractCell<SearchResultInfo> cell = new AbstractCell<SearchResultInfo>() {
            @Override
            public void render(Context context, SearchResultInfo value, SafeHtmlBuilder sb) {
                // number of ticks per pixel
                double ptsPerPixel = (double) value.getQueryLength() / 100;
                int start = Integer.MAX_VALUE;
                int end = Integer.MIN_VALUE;
                HashMap<Integer, Integer> stripes = new HashMap<Integer, Integer>();

                boolean started = false;
                for (String line : value.getMatchDetails()) {
                    if (line.startsWith("Query")) {
                        started = true;
                        String[] l = line.split(" ");
                        int tmp = Integer.valueOf(l[2]);
                        if (tmp < start)
                            start = tmp;

                        // check end
                        tmp = Integer.valueOf(l[l.length - 1]);
                        if (tmp > end)
                            end = tmp;
                    } else if (line.trim().startsWith("Score")) { // starting a new line
                        if (started) {
                            stripes.put(start, end);
                            start = Integer.MAX_VALUE;
                            end = Integer.MIN_VALUE;
                        }
                    }
                }

                // generate html;
                String html = "<table cellpadding=0 cellspacing=0><tr>";
                int prevStart = 0;
                String defColor = "#444";
                String stripColor = "orange";
                int fillEnd = 100;

                // for each stripe
                for (Map.Entry<Integer, Integer> entry : stripes.entrySet()) {
                    Integer stripeStart = entry.getKey();
                    Integer stripeEnd = entry.getValue();

                    int stripeBlockLength = (int) (Math.round((stripeEnd - stripeStart) / ptsPerPixel));
                    int fillStart = (int) (Math.round(stripeStart / ptsPerPixel));

                    // if fillstart < prevStart, then we have an overlap

                    // set default color width (FillStart - prev)
                    int width;
                    if (prevStart >= fillStart && prevStart != 0)
                        width = 1;
                    else
                        width = fillStart - prevStart;

                    html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                            + (width) + "px; height: 10px\"></hr></td>";

                    // mark stripe
                    prevStart = (fillStart - prevStart) + stripeBlockLength;
                    html += "<td><hr style=\"background-color: " + stripColor + "; border: 0px; width: "
                            + stripeBlockLength + "px; height: 10px\"></hr></td>";
                    fillEnd = fillStart + stripeBlockLength;
                }

                if (fillEnd < 100)
                    html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                            + (100 - fillEnd) + "px; height: 10px\"></hr></td>";

                html += "</tr></table>";
                sb.append(SafeHtmlUtils.fromTrustedString(html));
            }
        };

        DataTableColumn<SearchResultInfo, SearchResultInfo> col =
                new DataTableColumn<SearchResultInfo, SearchResultInfo>(cell, ColumnField.ALIGNMENT) {
                    @Override
                    public SearchResultInfo getValue(SearchResultInfo info) {
                        return info;
                    }
                };
        col.setSortable(false);
        this.addColumn(col, "Distribution");
        this.setColumnWidth(col, 120, Unit.PX);
        return col;
    }

    public EntryTablePager getPager() {
        return pager;
    }
}
