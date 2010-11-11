package org.jbei.ice.web.data.tables;

import java.util.HashSet;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

public class SortableDataTable<T> extends DataTable<T> {

    private static final long serialVersionUID = 1L;
    private HashSet<String> selected = new HashSet<String>();
    private boolean selectAllColumns;

    public SortableDataTable(String id, IColumn<T>[] columns, SortableDataProvider<T> dataProvider,
            int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);

        setOutputMarkupId(true);
        setVersioned(false);

        addTopToolbar(new AjaxFallbackHeadersToolbar(this, dataProvider));
        addBottomToolbar(new RegistryTableNavigationToolbar(this));
    }

    @Override
    protected Item<T> newRowItem(String id, int index, IModel<T> model) {
        return new OddEvenItem<T>(id, index, model);
    }

    public boolean isSelectAllColumns() {
        return this.selectAllColumns;
    }

    public void setSelectAllColumns(boolean selectAllColumns) {
        this.selectAllColumns = selectAllColumns;
    }

    public boolean isSelected(String entryId) {
        return (this.isSelectAllColumns() || selected.contains(entryId));
    }

    public void addSelection(String entryId) {
        selected.add(entryId);
    }

    public void removeSelection(String entryId) {
        selected.remove(entryId);
    }
}
