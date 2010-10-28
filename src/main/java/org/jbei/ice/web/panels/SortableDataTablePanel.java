package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.AbstractSortableColumn;
import org.jbei.ice.web.data.tables.SortableDataTable;
import org.jbei.ice.web.pages.UnprotectedPage;

/**
 * Panel for displaying a data table of specified types, with sortable columns
 * 
 * @author Hector Plahar
 * @param <T>
 *            Data for display in table
 */
public class SortableDataTablePanel<T> extends Panel {

    private static final long serialVersionUID = 1L;
    protected SortableDataProvider<T> dataProvider;
    protected static final int MAX_LONG_FIELD_LENGTH = 100;
    private final List<AbstractSortableColumn<T>> columns;
    protected ArrayList<Entry> entries;

    public SortableDataTablePanel(String id, List<AbstractSortableColumn<T>> columns,
            ArrayList<Entry> entries) {
        super(id);
        if (columns == null)
            this.columns = new ArrayList<AbstractSortableColumn<T>>();
        else
            this.columns = columns;
        this.entries = entries;
    }

    public SortableDataTablePanel(String id, List<AbstractSortableColumn<T>> columns) {
        this(id, columns, null);
    }

    public SortableDataTablePanel(String id) {
        this(id, null, null);
    }

    public void addColumn(AbstractSortableColumn<T> column) {
        this.columns.add(column);
    }
    
    void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    public void renderTable() {
        if (dataProvider == null)
            throw new ViewException("Missing the data provider"); 
        
        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        int size = this.columns.size();
        AbstractSortableColumn<T>[] tableColumns = this.columns
                .toArray(new AbstractSortableColumn[size]);
        SortableDataTable<T> table = new SortableDataTable<T>("data_table", tableColumns,
                dataProvider, 15);
        addOrReplace(table);

        if (entries != null)
            addOrReplace(new DataTableExportOptionsPanel("export_options", entries));
        else
            addOrReplace(new EmptyPanel("export_options"));
    }

    protected String trimLongField(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() > maxLength) {
            return value.substring(0, maxLength) + "...";
        } else {
            return value;
        }
    }

}
