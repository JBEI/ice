package org.jbei.ice.client.common.table;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.client.common.table.cell.EntrySelectionColumnHeaderCell;
import org.jbei.ice.client.common.table.column.DataTableColumn;
import org.jbei.ice.client.common.table.column.ImageColumn;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.entry.EntryInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.DefaultSelectionEventManager;

/**
 * DataTable for view entities that are EntryDatas. Provides selection support via space bar,
 * mouse click, and additional support for range selection using the shift key
 *
 * @param <T>
 * @author Hector Plahar
 */

public abstract class EntryDataTable<T extends EntryInfo> extends DataTable<T> implements IHasEntryId {

    private EntrySelectionModel<T> selectionModel;

    public EntryDataTable(ServiceDelegate<T> delegate) {
        super(delegate);
    }

    @Override
    protected void init() {
        selectionModel = new EntrySelectionModel<T>();
        this.setSelectionModel(selectionModel, DefaultSelectionEventManager.<T>createCheckboxManager());
    }

    protected DataTableColumn<T, Boolean> addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false) {

            @Override
            public void onBrowserEvent(Context context, Element parent, Boolean value,
                    NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
                String type = event.getType();

                boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
                if ("change".equals(type) || enterPressed) {
                    InputElement input = parent.getFirstChild().cast();
                    Boolean isChecked = input.isChecked();

                    if (!isChecked) {
                        selectionModel.setAllSelected(false);
                    }
                }
            }
        };

        DataTableColumn<T, Boolean> selectionColumn = new DataTableColumn<T, Boolean>(columnCell,
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

    public void clearSelection() {
        selectionModel.clear();
    }

    public EntrySelectionModel<T> getSelectionModel() {
        return this.selectionModel;
    }

    protected DataTableColumn<T, String> addTypeColumn(boolean sortable, double width, Unit unit) {
        DataTableColumn<T, String> typeCol = new DataTableColumn<T, String>(new TextCell(), ColumnField.TYPE) {

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

    protected DataTableColumn<T, SafeHtml> addNameColumn(final double width, Unit unit) {

        DataTableColumn<T, SafeHtml> nameColumn = new DataTableColumn<T, SafeHtml>(new SafeHtmlCell(),
                                                                                   ColumnField.NAME) {

            @Override
            public SafeHtml getValue(T object) {
                String name = object.getName();
                if (name == null)
                    return SafeHtmlUtils.EMPTY_SAFE_HTML;

                return SafeHtmlUtils
                        .fromSafeConstant("<div style=\"width: "
                                                  + width + "px; "
                                                  + "white-space: nowrap; overflow: hidden; text-overflow: "
                                                  + "ellipsis;\" title=\"" + name.replaceAll("\"", "'") + "\">"
                                                  + name + "</div>");
            }
        };

        this.addColumn(nameColumn, "Name");
        nameColumn.setSortable(false);
        this.setColumnWidth(nameColumn, width, unit);
        return nameColumn;
    }

    protected DataTableColumn<T, SafeHtml> addSummaryColumn() {
        DataTableColumn<T, SafeHtml> summaryColumn = new DataTableColumn<T, SafeHtml>(
                new SafeHtmlCell(), ColumnField.SUMMARY) {

            @Override
            public SafeHtml getValue(T object) {
                String description = object.getShortDescription();
                if (description == null)
                    return SafeHtmlUtils.EMPTY_SAFE_HTML;

                int size = (Window.getClientWidth() - 850);
                if (size <= 0)
                    size = 200;

                return SafeHtmlUtils
                        .fromSafeConstant("<div style=\"width: "
                                                  + size
                                                  + "px; white-space: nowrap; overflow: hidden; text-overflow: " +
                                                  "ellipsis;\" title=\""
                                                  + description.replaceAll("\"", "'") + "\">"
                                                  + description + "</div>");
            }
        };

        this.addColumn(summaryColumn, "Summary");
        return summaryColumn;
    }

    protected DataTableColumn<T, String> addStatusColumn() {
        DataTableColumn<T, String> statusColumn = new DataTableColumn<T, String>(new TextCell(), ColumnField.STATUS) {

            @Override
            public String getValue(T object) {
                return toUppercaseFully(object.getStatus());
            }
        };

        this.addColumn(statusColumn, "Status");
        statusColumn.setSortable(true);
        this.setColumnWidth(statusColumn, 90, Unit.PX);
        return statusColumn;
    }

    protected String toUppercaseFully(String value) {
        if (value == null || value.isEmpty())
            return "";
        return (value.substring(0, 1).toUpperCase() + value.substring(1));
    }

    protected void addHasAttachmentColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.ATTACHMENT) {
            public boolean showImage(EntryInfo info) {
                return info.isHasAttachment();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSampleColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SAMPLE) {
            public boolean showImage(EntryInfo info) {
                return info.isHasSample();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSequenceColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SEQUENCE) {
            public boolean showImage(EntryInfo info) {
                return info.isHasSequence();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected DataTableColumn<T, String> addCreatedColumn() {
        DataTableColumn<T, String> createdColumn = new DataTableColumn<T, String>(new TextCell(), ColumnField.CREATED) {

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
    // returns the selected entry set
    public Set<Long> getSelectedEntrySet() {
        Set<Long> entrySet = new HashSet<Long>();
        for (EntryInfo info : selectionModel.getSelectedSet()) {
            entrySet.add(info.getId());
        }

        return entrySet;
    }

    private class SelectionColumnHeader extends Header<Boolean> {

        public SelectionColumnHeader() {
            super(new EntrySelectionColumnHeaderCell<T>(EntryDataTable.this, selectionModel, true, false));
        }

        @Override
        public Boolean getValue() {
            if (selectionModel.isAllSelected())
                return true;

            return !(selectionModel.getSelectedSet().isEmpty());
        }
    }
}
