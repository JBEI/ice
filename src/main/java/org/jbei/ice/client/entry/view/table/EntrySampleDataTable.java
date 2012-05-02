package org.jbei.ice.client.entry.view.table;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class EntrySampleDataTable extends DataTable<SampleStorage> {

    private DataProvider provider;

    public EntrySampleDataTable() {
        provider = new DataProvider();
        provider.addDataDisplay(this);
        this.setStyleName("entry_sample_table");
        this.setPageSize(5);
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();
        columns.add(this.createLabelColumn());
        columns.add(this.createStorageColumn());
        columns.add(this.createAddedColumn());
        columns.add(this.createEditColumn()); // TODO : add if only admins
        return columns;
    }

    protected DataTableColumn<SampleStorage> createStorageColumn() {
        AbstractCell<SampleStorage> cell = new AbstractCell<SampleStorage>() {

            @Override
            public void render(Context context, SampleStorage value, SafeHtmlBuilder sb) {
                LinkedList<StorageInfo> list = value.getStorageList();
                if (list == null || list.isEmpty())
                    return;

                Tree tree = new Tree();
                Hyperlink rootLink = new Hyperlink(list.get(0).getDisplay(), ";id="
                        + list.get(0).getId());
                TreeItem root = new TreeItem(rootLink);
                tree.addItem(root);
                TreeItem tmp;

                if (list.size() > 1) {
                    for (int i = 1; i < list.size(); i += 1) {
                        StorageInfo info = list.get(i);
                        // TODO 
                        Hyperlink infoLink = new Hyperlink(info.getDisplay(), "");
                        tmp = new TreeItem(infoLink);
                        root.addItem(tmp);
                        root = tmp;
                    }
                }

                sb.appendHtmlConstant(tree.getElement().getInnerHTML());
            }
        };

        SampleInfoDataColumn labelColumn = new SampleInfoDataColumn(cell, ColumnField.LABEL);
        this.addColumn(labelColumn);
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, 250, Unit.PX);
        return labelColumn;
    }

    protected DataTableColumn<SampleStorage> createEditColumn() {
        AbstractCell<SampleStorage> cell = new AbstractCell<SampleStorage>() {

            @Override
            public void render(Context context, SampleStorage value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>edit | delete");
                sb.appendHtmlConstant("</span>");
            }
        };

        SampleInfoDataColumn labelColumn = new SampleInfoDataColumn(cell, ColumnField.LABEL);
        this.addColumn(labelColumn);
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, 250, Unit.PX);
        return labelColumn;
    }

    protected DataTableColumn<SampleStorage> createLabelColumn() {
        AbstractCell<SampleStorage> cell = new AbstractCell<SampleStorage>() {

            @Override
            public void render(Context context, SampleStorage value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span class=\"font-bold\">");
                sb.appendEscaped(value.getSample().getLabel());
                sb.appendHtmlConstant("</span><br><span style=\"color: #999\" class=\"font-85em\">");
                sb.appendEscaped(value.getSample().getNotes() == null ? "" : value.getSample()
                        .getNotes());
                sb.appendHtmlConstant("</span>");
            }
        };

        SampleInfoDataColumn labelColumn = new SampleInfoDataColumn(cell, ColumnField.LABEL);
        this.addColumn(labelColumn);
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, 250, Unit.PX);
        return labelColumn;
    }

    protected DataTableColumn<?> createAddedColumn() {
        AbstractCell<SampleStorage> cell = new AbstractCell<SampleStorage>() {

            @Override
            public void render(Context context, SampleStorage value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>");
                sb.appendEscaped(DateUtilities.formatDate(value.getSample().getCreationTime()));

                Hyperlink link = new Hyperlink(value.getSample().getDepositor(),
                        Page.PROFILE.getLink() + ";id=" + value.getSample().getDepositor());

                sb.appendHtmlConstant("</span><br /><span>");
                sb.appendHtmlConstant("by " + link.getElement().getInnerHTML());
                sb.appendHtmlConstant("</span>");
            }
        };

        SampleInfoDataColumn labelColumn = new SampleInfoDataColumn(cell, ColumnField.LABEL);
        this.addColumn(labelColumn);
        labelColumn.setSortable(true);
        this.setColumnWidth(labelColumn, 20, Unit.PCT);
        return labelColumn;
    }

    public void setData(ArrayList<SampleStorage> data) {
        this.provider.setData(data);
    }

    //
    // inner classes
    //
    protected class DataProvider extends AbstractDataProvider<SampleStorage> {

        private ArrayList<SampleStorage> data;

        public DataProvider() {
            data = new ArrayList<SampleStorage>();
        }

        public void setData(ArrayList<SampleStorage> data) {
            this.data.clear();
            this.data.addAll(data);

            final Range range = EntrySampleDataTable.this.getVisibleRange();
            final int rangeStart = range.getStart();
            final int rangeEnd;
            if ((rangeStart + range.getLength()) > this.data.size())
                rangeEnd = this.data.size();
            else
                rangeEnd = (rangeStart + range.getLength());

            List<SampleStorage> show = new ArrayList<SampleStorage>();
            show.addAll(data.subList(rangeStart, rangeEnd));
            updateRowCount(this.data.size(), true);
            updateRowData(rangeStart, show);
        }

        @Override
        protected void onRangeChanged(HasData<SampleStorage> display) {
            if (data.isEmpty())
                return;

            final Range range = display.getVisibleRange();
            final ColumnSortList sortList = EntrySampleDataTable.this.getColumnSortList();
            int start = range.getStart();
            int end = range.getLength() + start;
            if (end > data.size())
                end = data.size();

            sortByColumn(this.getSortField(), sortList.get(0).isAscending());
            EntrySampleDataTable.this.setRowData(start, data.subList(start, end));
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
            ColumnSortList sortList = EntrySampleDataTable.this.getColumnSortList();
            int colIndex = EntrySampleDataTable.this.getColumns().indexOf(
                sortList.get(0).getColumn());
            if (colIndex == -1)
                return null; // TODO : this will be pretty unusual

            ColumnField field = EntrySampleDataTable.this.getColumns().get(colIndex).getField();
            return field;
        }
    }

    private class SampleInfoDataColumn extends DataTableColumn<SampleStorage> {

        public SampleInfoDataColumn(Cell<SampleStorage> cell, ColumnField field) {
            super(cell, field);
        }

        @Override
        public SampleStorage getValue(SampleStorage object) {
            return object;
        }
    }

}
