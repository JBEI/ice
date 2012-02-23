package org.jbei.ice.client.common.table;

import java.util.Set;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.entry.IHasEntry;
import org.jbei.ice.client.common.table.cell.PartIDCell;
import org.jbei.ice.client.common.table.cell.UrlCell;
import org.jbei.ice.client.common.table.column.ImageColumn;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.History;
import com.google.gwt.view.client.DefaultSelectionEventManager;

/**
 * DataTable for view entities that are EntryDatas. Provides selection support via space bar,
 * mouse click, and additional support for range selection using the shift key
 * 
 * @author Hector Plahar
 * 
 * @param <T>
 */

public abstract class EntryDataTable<T extends EntryInfo> extends DataTable<T> implements
        IHasEntry<T> {

    private final EntrySelectionModel<T> selectionModel;

    public EntryDataTable() {
        super();
        selectionModel = new EntrySelectionModel<T>();
        this.setSelectionModel(selectionModel,
            DefaultSelectionEventManager.<T> createCheckboxManager());
    }

    protected DataTableColumn<Boolean> addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false) {
            @Override
            public void onBrowserEvent(Context context, Element parent, Boolean value,
                    NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
                String type = event.getType();

                boolean enterPressed = "keydown".equals(type)
                        && event.getKeyCode() == KeyCodes.KEY_ENTER;
                if ("change".equals(type) || enterPressed) {
                    InputElement input = parent.getFirstChild().cast();
                    Boolean isChecked = input.isChecked();

                    if (!isChecked) {
                        selectionModel.setAllSelected(false);
                    }
                }
            }
        };

        DataTableColumn<Boolean> selectionColumn = new DataTableColumn<Boolean>(columnCell,
                ColumnField.SELECTION) {

            @Override
            public Boolean getValue(T object) {
                // returns column value from underlying data object (EntryDataView in this instance)
                return selectionModel.isSelected(object);
            }
        };
        selectionColumn.setSortable(false);
        SelectionColumnHeader header = new SelectionColumnHeader();

        this.addColumn(selectionColumn, header);
        this.setColumnWidth(selectionColumn, 30, Unit.PX);
        return selectionColumn;
    }

    protected DataTableColumn<String> addTypeColumn(boolean sortable, double width, Unit unit) {
        DataTableColumn<String> typeCol = new DataTableColumn<String>(new TextCell(),
                ColumnField.TYPE) {

            @Override
            public String getValue(T entry) {
                return toUppercaseFully(entry.getType().getDisplay());
            }
        };
        typeCol.setSortable(sortable);
        this.addColumn(typeCol, "Type");
        this.setColumnWidth(typeCol, width, unit);
        return typeCol;
    }

    protected DataTableColumn<EntryInfo> addPartIdColumn(boolean sortable, double width, Unit unit) {

        DataTableColumn<EntryInfo> partIdColumn = new DataTableColumn<EntryInfo>(
                new PartIDCell<EntryInfo>(), ColumnField.PART_ID) {

            @Override
            public EntryInfo getValue(T object) {
                return object;
            }
        };

        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected DataTableColumn<String> addNameColumn(double width, Unit unit) {
        DataTableColumn<String> nameColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.NAME) {

            @Override
            public String getValue(T object) {
                return object.getName();
            }
        };

        this.addColumn(nameColumn, "Name");
        nameColumn.setSortable(true);
        this.setColumnWidth(nameColumn, width, unit);
        return nameColumn;
    }

    protected DataTableColumn<String> addSummaryColumn() {
        DataTableColumn<String> summaryColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.SUMMARY) {

            @Override
            public String getValue(T object) {
                // TODO : limit length of returned string
                return object.getShortDescription();
            }
        };

        this.addColumn(summaryColumn, "Summary");
        return summaryColumn;
    }

    protected DataTableColumn<EntryInfo> addOwnerColumn() {
        UrlCell<EntryInfo> cell = new UrlCell<EntryInfo>() {

            @Override
            protected String getCellValue(EntryInfo object) {
                return object.getOwner();
            }

            @Override
            protected void onClick(EntryInfo object) {
                History.newItem(Page.PROFILE.getLink() + ";id=" + object.getOwnerEmail());
            }
        };

        DataTableColumn<EntryInfo> ownerColumn = new DataTableColumn<EntryInfo>(cell,
                ColumnField.OWNER) {

            @Override
            public EntryInfo getValue(T object) {
                return object;
            }
        };

        this.addColumn(ownerColumn, "Owner");
        ownerColumn.setSortable(true);
        this.setColumnWidth(ownerColumn, 110, Unit.PX);
        return ownerColumn;
    }

    protected DataTableColumn<String> addStatusColumn() {
        DataTableColumn<String> statusColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.STATUS) {

            @Override
            public String getValue(T object) {
                return toUppercaseFully(object.getStatus());
            }
        };

        this.addColumn(statusColumn, "Status");
        statusColumn.setSortable(true);
        this.setColumnWidth(statusColumn, 110, Unit.PX);
        return statusColumn;
    }

    protected String toUppercaseFully(String value) {
        if (value == null || value.isEmpty())
            return "";
        return (value.substring(0, 1).toUpperCase() + value.substring(1));
    }

    protected void addHasAttachmentColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.ATTACHMENT);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSampleColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SAMPLE);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSequenceColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SEQUENCE);
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected DataTableColumn<String> addCreatedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(),
                ColumnField.CREATED) {

            @Override
            public String getValue(EntryInfo object) {
                DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMM d, yyyy");
                return dateTimeFormat.format(object.getCreationTime());
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 100, Unit.PX);
        return createdColumn;
    }

    @Override
    public Set<T> getEntries() {
        return selectionModel.getSelectedSet();
    }

    //
    // inner classes
    //

    protected CheckboxCell createHeaderCell() {
        return new CheckboxCell(true, false) {
            @Override
            public void onBrowserEvent(Context context, Element parent, Boolean value,
                    NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
                String type = event.getType();

                boolean enterPressed = "keydown".equals(type)
                        && event.getKeyCode() == KeyCodes.KEY_ENTER;
                if ("change".equals(type) || enterPressed) {
                    InputElement input = parent.getFirstChild().cast();
                    Boolean isChecked = input.isChecked();

                    if (isChecked) {
                        selectionModel.setAllSelected(true);
                        EntryDataTable.this.redraw();
                    } else {
                        selectionModel.clear();
                        selectionModel.setAllSelected(false);
                    }
                }
            }
        };
    }

    private class SelectionColumnHeader extends Header<Boolean> {

        public SelectionColumnHeader() {
            super(createHeaderCell());
        }

        @Override
        public Boolean getValue() {
            if (selectionModel.isAllSelected())
                return true;

            return !(selectionModel.getSelectedSet().isEmpty());
        }
    }
}
