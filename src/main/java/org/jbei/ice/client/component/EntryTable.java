package org.jbei.ice.client.component;

import java.util.Date;

import org.jbei.ice.shared.EntryDataView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;

public class EntryTable extends CellTable<EntryDataView> {

    protected interface EntryResources extends Resources {
        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/EntryTable.css")
        Style cellTableStyle();
    }

    private static EntryResources resource = GWT.create(EntryResources.class);

    public EntryTable() {

        super(15, resource);
        createTableColumns();
        setStyleName("data_table");

        Label empty = new Label();
        empty.setText("Empty");
        this.setEmptyTableWidget(empty);
    }

    protected void createTableColumns() {

        //Type
        this.addTypeColumn();

        // Part ID
        this.addPartIdColumn();

        // Name
        this.addNameColumn();

        // summary column
        this.addSummaryColumn();

        // owner column
        this.addOwnerColumn();

        // Status
        this.addStatusColumn();

        // Has Attachment    
        this.addHasAttachmentColumn();

        // Has Sample
        this.addHasSampleColumn();

        // Has Sequence
        this.addHasSequenceColumn();

        // Created
        this.addCreatedColumn();
    }

    protected void addTypeColumn() {
        TextColumn<EntryDataView> typeCol = new TextColumn<EntryDataView>() {

            @Override
            public String getValue(EntryDataView entry) {
                return entry.getType();
            }
        };
        this.addColumn(typeCol, "Type");
        this.setColumnWidth(typeCol, 100, Unit.PX);
    }

    protected void addPartIdColumn() {
        PartIDColumn column = new PartIDColumn();
        this.addColumn(column, "Part ID");
        this.setColumnWidth(column, 150, Unit.PX);
    }

    protected void addNameColumn() {
        TextColumn<EntryDataView> column = new TextColumn<EntryDataView>() {

            @Override
            public String getValue(EntryDataView object) {
                return object.getName();
            }
        };

        this.addColumn(column, "Name");
        this.setColumnWidth(column, 200, Unit.PX);
    }

    protected void addSummaryColumn() {
        this.addColumn(new TextColumn<EntryDataView>() {
            @Override
            public String getValue(EntryDataView entry) {
                return entry.getSummary();
            }
        }, "Summary");
    }

    protected void addOwnerColumn() {

        URLColumn column = new URLColumn();

        this.addColumn(column, "Owner");
        this.setColumnWidth(column, 180, Unit.PX);
    }

    protected void addStatusColumn() {
        TextColumn<EntryDataView> column = new TextColumn<EntryDataView>() {

            @Override
            public String getValue(EntryDataView object) {
                return object.getStatus();
            }
        };

        this.addColumn(column, "Status");
        this.setColumnWidth(column, 110, Unit.PX);
    }

    protected void addHasAttachmentColumn() {
        ImageColumn column = new ImageColumn(ImageColumn.Type.ATTACHMENT);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 35, Unit.PX);
    }

    protected void addHasSampleColumn() {
        ImageColumn column = new ImageColumn(ImageColumn.Type.SAMPLE);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 35, Unit.PX);
    }

    protected void addHasSequenceColumn() {
        ImageColumn column = new ImageColumn(ImageColumn.Type.SEQUENCE);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 35, Unit.PX);
    }

    protected void addCreatedColumn() {
        final int WIDTH = 120;
        TextColumn<EntryDataView> created = new TextColumn<EntryDataView>() {

            @Override
            public String getValue(EntryDataView object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                Date date = new Date(object.getCreated());
                String value = format.format(date);
                if (value.length() >= 13)
                    value = (value.substring(0, 9) + "...");
                return value;
            }
        };

        this.addColumn(created, "Created");
        this.setColumnWidth(created, WIDTH, Unit.PX);
    }
}
