package org.jbei.ice.client.collection.table;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.cell.UrlCell;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.History;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Table for displaying meta data about the list of collections.
 * 
 * @see CollectionEntriesDataTable which is responsible for displaying the contents of each
 *      collection
 * @author Hector Plahar
 */
public class CollectionListTable extends DataTable<FolderDetails> {

    private DataProvider provider;

    public CollectionListTable() {
        super();
        this.setPageSize(5);
        provider = new DataProvider();
        provider.addDataDisplay(this);
    }

    @Override
    protected ArrayList<DataTableColumn<?>> createColumns() {
        ArrayList<DataTableColumn<?>> columns = new ArrayList<DataTableColumn<?>>();
        columns.add(this.createNameColumn());
        columns.add(this.createDescriptionColumn());
        columns.add(this.createCountColumn());
        return columns;
    }

    protected DataTableColumn<FolderDetails> createNameColumn() {
        UrlCell<FolderDetails> nameCell = new UrlCell<FolderDetails>() {

            @Override
            protected String getCellValue(FolderDetails object) {
                return object.getName();
            }

            @Override
            protected void onClick(FolderDetails object) {
                History.newItem(Page.COLLECTIONS.getLink() + ";id=" + object.getId());
            }
        };

        DataTableColumn<FolderDetails> nameColumn = new DataTableColumn<FolderDetails>(nameCell,
                ColumnField.NAME) {

            @Override
            public FolderDetails getValue(FolderDetails object) {
                return object;
            }
        };

        this.addColumn(nameColumn);
        nameColumn.setSortable(true);
        this.setColumnWidth(nameColumn, 250, Unit.PX);
        return nameColumn;
    }

    protected DataTableColumn<String> createDescriptionColumn() {
        DataTableColumn<String> descriptionColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.DESCRIPTION) {

            @Override
            public String getValue(FolderDetails object) {
                String description = object.getDescription();
                if (description == null)
                    description = "";
                return description;
            }
        };

        this.addColumn(descriptionColumn);
        return descriptionColumn;
    }

    protected DataTableColumn<String> createCountColumn() {
        DataTableColumn<String> countColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.COUNT) {

            @Override
            public String getValue(FolderDetails object) {
                return object.getCount() + " entries.";
            }
        };

        this.addColumn(countColumn);
        this.setColumnWidth(countColumn, 100, Unit.PX);
        return countColumn;
    }

    public void setData(ArrayList<FolderDetails> data) {
        this.provider.setData(data);
    }

    //
    // data provider
    //

    protected class DataProvider extends AbstractDataProvider<FolderDetails> {

        private LinkedList<FolderDetails> data;

        public DataProvider() {
            data = new LinkedList<FolderDetails>();
        }

        public void setData(ArrayList<FolderDetails> data) {
            this.data.clear();
            this.data.addAll(data);

            final Range range = CollectionListTable.this.getVisibleRange();
            final int rangeStart = range.getStart();
            final int rangeEnd;
            if ((rangeStart + range.getLength()) > this.data.size())
                rangeEnd = this.data.size();
            else
                rangeEnd = (rangeStart + range.getLength());

            LinkedList<FolderDetails> show = new LinkedList<FolderDetails>();
            show.addAll(data.subList(rangeStart, rangeEnd));
            updateRowCount(this.data.size(), true);
            updateRowData(rangeStart, show);
        }

        @Override
        protected void onRangeChanged(HasData<FolderDetails> display) {
            if (data.isEmpty())
                return;

            final Range range = display.getVisibleRange();
            final ColumnSortList sortList = CollectionListTable.this.getColumnSortList();
            int start = range.getStart();
            int end = range.getLength() + start;
            if (end > data.size())
                end = data.size();

            if (sortList != null && sortList.size() > 0) {
                sortByColumn(this.getSortField(), sortList.get(0).isAscending());
            }
            CollectionListTable.this.setRowData(start, data.subList(start, end));
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
            ColumnSortList sortList = CollectionListTable.this.getColumnSortList();
            int colIndex = CollectionListTable.this.getColumns().indexOf(
                sortList.get(0).getColumn());
            if (colIndex == -1)
                return null; // TODO : this will be pretty unusual

            ColumnField field = CollectionListTable.this.getColumns().get(colIndex).getField();
            return field;
        }
    }
}
