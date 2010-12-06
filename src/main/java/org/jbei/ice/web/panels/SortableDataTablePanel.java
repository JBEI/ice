package org.jbei.ice.web.panels;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.AbstractSortableColumn;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.data.tables.SortableDataTable;
import org.jbei.ice.web.pages.UnprotectedPage;

/**
 * Panel for displaying a data table of specified types, with sortable columns
 * 
 * @author Hector Plahar
 * @param <T>
 *            Type of data for display in table
 */
public class SortableDataTablePanel<T> extends Panel {

    private static final long serialVersionUID = 1L;
    protected SortableDataProvider<T> dataProvider;
    protected static final int MAX_LONG_FIELD_LENGTH = 100;
    private final List<AbstractSortableColumn<T>> columns;
    protected ArrayList<Entry> entries;

    protected ResourceReference blankImage;
    protected ResourceReference hasAttachmentImage;
    protected ResourceReference hasSequenceImage;
    protected ResourceReference hasSampleImage;

    protected SortableDataTable<T> table;

    public SortableDataTablePanel(String id, List<AbstractSortableColumn<T>> columns,
            ArrayList<Entry> entries) {
        super(id);

        blankImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
        hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
        hasSequenceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
        hasSampleImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

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

    // COLUMNS
    protected void addIndexColumn() {
        addColumn(new LabelHeaderColumn<T>("#") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, final T t, int index) {               
                return new Label(componentId, String.valueOf(table.getPageCount()
                        * table.getCurrentPage() + index + 1));
            }
        });
    }

    protected void addTypeColumn(String propertyExpression, boolean sort) {
        String sortProperty = null;
        if (sort) {
            sortProperty = propertyExpression;
        }
        addLabelHeaderColumn("Type", sortProperty, propertyExpression);
    }

    // COLUMNS

    void setEntries(ArrayList<Entry> entries) {
        this.entries = entries;
    }

    protected void addLabelHeaderColumn(String header, String sortProperty,
            String propertyExpression) {
        addColumn(new LabelHeaderColumn<T>(header, sortProperty, propertyExpression));
    }

    public void renderTable() {
        if (dataProvider == null)
            throw new ViewException("Missing the data provider");

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.cluetip.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
            UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.cluetip.css"));

        int size = this.columns.size();
        @SuppressWarnings("unchecked")
        AbstractSortableColumn<T>[] tableColumns = this.columns
                .toArray(new AbstractSortableColumn[size]);
        table = new SortableDataTable<T>("data_table", tableColumns, dataProvider, 15);
        addOrReplace(table);

        if (entries != null && !entries.isEmpty())
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
