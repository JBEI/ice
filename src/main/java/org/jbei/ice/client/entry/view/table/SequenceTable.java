package org.jbei.ice.client.entry.view.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class SequenceTable extends DataTable<SequenceAnalysisInfo> { // TODO : create a new parent class for tables that do not look like tables

    private final DataProvider provider;

    public SequenceTable() {
        super();
        provider = new DataProvider();
        provider.addDataDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();
        columns.add(this.createNameColumn());
        columns.add(this.createAddedColumn());
        return columns;
    }

    protected DataTableColumn<?> createNameColumn() {
        AbstractCell<SequenceAnalysisInfo> cell = new AbstractCell<SequenceAnalysisInfo>() {

            @Override
            public void render(Context context, SequenceAnalysisInfo value, SafeHtmlBuilder sb) {
                if (value == null)
                    return;

                sb.appendEscaped(value.getName());
            }
        };

        DataTableColumn<SequenceAnalysisInfo> labelColumn = new DataTableColumn<SequenceAnalysisInfo>(
                cell, ColumnField.NAME) {

            @Override
            public SequenceAnalysisInfo getValue(SequenceAnalysisInfo info) {
                return info;
            }
        };

        this.addColumn(labelColumn);
        labelColumn.setSortable(false);
        return labelColumn;
    }

    protected DataTableColumn<SequenceAnalysisInfo> createAddedColumn() {
        AbstractCell<SequenceAnalysisInfo> cell = new AbstractCell<SequenceAnalysisInfo>() {

            @Override
            public void render(Context context, SequenceAnalysisInfo value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>");
                sb.appendEscaped(formatDate(value.getCreated()));
                sb.appendHtmlConstant("</span><br /><span>");
                //                sb.appendEscaped(value.getDepositor());

                if (value.getDepositor() == null) {
                    sb.appendHtmlConstant("</span>");
                } else {
                    String name = value.getDepositor().getFirstName() + " "
                            + value.getDepositor().getLastName();
                    String email = value.getDepositor().getEmail();
                    if (value.getDepositor().getFirstName() == null || name.trim().isEmpty())
                        name = email;
                    sb.appendHtmlConstant("by <a href='" + email + "'>" + name + "</a></span>");
                }
            }
        };

        DataTableColumn<SequenceAnalysisInfo> labelColumn = new DataTableColumn<SequenceAnalysisInfo>(
                cell, ColumnField.NAME) {

            @Override
            public SequenceAnalysisInfo getValue(SequenceAnalysisInfo info) {
                return info;
            }
        };

        this.addColumn(labelColumn);
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, 20, Unit.PCT);
        return labelColumn;
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }

    public void setData(ArrayList<SequenceAnalysisInfo> data) {
        this.provider.setData(data);
    }

    //
    // inner classes
    //
    protected class DataProvider extends AbstractDataProvider<SequenceAnalysisInfo> {

        private LinkedList<SequenceAnalysisInfo> data;

        public DataProvider() {
            data = new LinkedList<SequenceAnalysisInfo>();
        }

        public void setData(ArrayList<SequenceAnalysisInfo> data) {
            this.data.clear();
            this.data.addAll(data);

            final Range range = SequenceTable.this.getVisibleRange();
            final int rangeStart = range.getStart();
            final int rangeEnd;
            if ((rangeStart + range.getLength()) > this.data.size())
                rangeEnd = this.data.size();
            else
                rangeEnd = (rangeStart + range.getLength());

            LinkedList<SequenceAnalysisInfo> show = new LinkedList<SequenceAnalysisInfo>();
            show.addAll(data.subList(rangeStart, rangeEnd));
            updateRowCount(this.data.size(), true);
            updateRowData(rangeStart, show);
        }

        @Override
        protected void onRangeChanged(HasData<SequenceAnalysisInfo> display) {
            if (data.isEmpty())
                return;

            final Range range = display.getVisibleRange();
            final ColumnSortList sortList = SequenceTable.this.getColumnSortList();
            int start = range.getStart();
            int end = range.getLength() + start;
            if (end > data.size())
                end = data.size();

            if (sortList != null && sortList.size() > 0)
                sortByColumn(this.getSortField(), sortList.get(0).isAscending());
            SequenceTable.this.setRowData(start, data.subList(start, end));
        }

        /**
         * Sorts the data based on params
         * 
         * @param field
         * @param asc
         */
        private void sortByColumn(ColumnField field, boolean asc) {
            // TODO : collections.sort(data...)
        }

        protected ColumnField getSortField() {
            ColumnSortList sortList = SequenceTable.this.getColumnSortList();

            ColumnSortInfo sortInfo = sortList.get(0);
            int colIndex = SequenceTable.this.getColumns().indexOf(sortInfo.getColumn());
            if (colIndex == -1)
                return null; // TODO : this will be pretty unusual

            ColumnField field = SequenceTable.this.getColumns().get(colIndex).getField();
            return field;
        }
    }
}
