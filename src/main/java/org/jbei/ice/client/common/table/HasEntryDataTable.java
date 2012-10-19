package org.jbei.ice.client.common.table;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.client.collection.menu.IHasEntryHandlers;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.common.entry.IHasEntryId;
import org.jbei.ice.client.common.table.cell.HasEntryPartIDCell;
import org.jbei.ice.client.common.table.column.ImageColumn;
import org.jbei.ice.client.event.EntryViewEvent;
import org.jbei.ice.client.event.EntryViewEvent.EntryViewEventHandler;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.HasEntryInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.DefaultSelectionEventManager;

/**
 * Table whose elements consists of a type that
 * has a @see EntryDataView
 *
 * @author Hector Plahar
 * @see HasEntryInfo
 */
public abstract class HasEntryDataTable<T extends HasEntryInfo> extends DataTable<T> implements IHasEntryId {

    private final HasEntrySelectionModel<T> selectionModel;

    public HasEntryDataTable() {

        selectionModel = new HasEntrySelectionModel<T>();
        this.setSelectionModel(selectionModel,
                               DefaultSelectionEventManager.<T>createCheckboxManager());
    }

    @Override
    public Set<Long> getSelectedEntrySet() {
        Set<Long> infoSet = new HashSet<Long>();

        for (HasEntryInfo info : selectionModel.getSelectedSet()) {
            infoSet.add(info.getEntryInfo().getId());
        }

        return infoSet;
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

        DataTableColumn<Boolean> selectionColumn = new DataTableColumn<Boolean>(columnCell, ColumnField.SELECTION) {

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

    protected DataTableColumn<String> addTypeColumn(boolean sortable) {
        DataTableColumn<String> typeCol = new DataTableColumn<String>(new TextCell(), ColumnField.TYPE) {

            @Override
            public String getValue(T entry) {
                return toUppercaseFully(entry.getEntryInfo().getType().getDisplay());
            }
        };
        typeCol.setSortable(sortable);
        this.addColumn(typeCol, "Type");
        this.setColumnWidth(typeCol, 100, Unit.PX);
        return typeCol;
    }

    protected void addHasAttachmentColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.ATTACHMENT) {
            public boolean showImage(HasEntryInfo info) {
                return info.getEntryInfo().isHasAttachment();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSampleColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SAMPLE) {
            public boolean showImage(HasEntryInfo info) {
                return info.getEntryInfo().isHasSample();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected void addHasSequenceColumn() {
        ImageColumn<T> column = new ImageColumn<T>(ImageColumn.Type.SEQUENCE) {
            public boolean showImage(HasEntryInfo info) {
                return info.getEntryInfo().isHasSequence();
            }
        };
        this.addColumn(column, column.getHeader());
        this.setColumnWidth(column, 30, Unit.PX);
    }

    protected DataTableColumn<HasEntryInfo> addPartIdColumn(boolean sortable, double width, Unit unit) {
        HasEntryPartIDCell<HasEntryInfo> cell = new HasEntryPartIDCell<HasEntryInfo>(EntryContext.Type.SEARCH);
        cell.addEntryHandler(getHandler());
        DataTableColumn<HasEntryInfo> partIdColumn = new HasEntryDataTable.PartIdColumn(cell);
        this.setColumnWidth(partIdColumn, width, unit);
        partIdColumn.setSortable(sortable);
        this.addColumn(partIdColumn, "Part ID");
        return partIdColumn;
    }

    protected abstract EntryViewEventHandler getHandler();

//    protected DataTableColumn<HasEntryInfo> addPartIdColumn(boolean sortable,
//            EntryViewEventHandler handler, EntryContext.Type mode) {
//
//        DataTableColumn<HasEntryInfo> partIdColumn = new DataTableColumn<HasEntryInfo>(
//                new HasEntryPartIDCell<HasEntryInfo>(mode), ColumnField.PART_ID) { // TODO : see EntryDataTable:108
//
//            @Override
//            public HasEntryInfo getValue(T object) {
//                return object;
//            }
//        };
//
//        this.setColumnWidth(partIdColumn, 100, Unit.PX);
//        this.addColumn(partIdColumn, "Part ID");
//        partIdColumn.setSortable(sortable);
//        return partIdColumn;
//    }

    protected DataTableColumn<SafeHtml> addNameColumn(final double width, Unit unit) {

        DataTableColumn<SafeHtml> nameColumn = new DataTableColumn<SafeHtml>(new SafeHtmlCell(),
                                                                             ColumnField.NAME) {

            @Override
            public SafeHtml getValue(T object) {
                String name = object.getEntryInfo().getName();
                if (name == null)
                    return SafeHtmlUtils.EMPTY_SAFE_HTML;


                return SafeHtmlUtils
                        .fromSafeConstant("<div style=\"width: "
                                                  + width + "px; "
                                                  + "white-space: nowrap; overflow: hidden; text-overflow: " +
                                                  "ellipsis;\" title=\""
                                                  + name.replaceAll("\"", "'") + "\">"
                                                  + name + "</div>");
            }
        };

        this.addColumn(nameColumn, "Name");
        nameColumn.setSortable(false);
        this.setColumnWidth(nameColumn, width, unit);
        return nameColumn;
    }

    protected DataTableColumn<String> addCreatedColumn() {
        DataTableColumn<String> createdColumn = new DataTableColumn<String>(new TextCell(), ColumnField.CREATED) {

            @Override
            public String getValue(HasEntryInfo object) {

                DateTimeFormat format = DateTimeFormat.getFormat("MMM d, yyyy");
                String value = format.format(object.getEntryInfo().getCreationTime());
                if (value.length() >= 13)
                    value = (value.substring(0, 9) + "...");
                return value;
            }
        };

        createdColumn.setSortable(true);
        this.addColumn(createdColumn, "Created");
        this.setColumnWidth(createdColumn, 100, Unit.PX);
        return createdColumn;
    }

    private String toUppercaseFully(String value) {
        if (value == null || value.isEmpty())
            return "";
        return (value.substring(0, 1).toUpperCase() + value.substring(1));
    }

    // inner classes
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
                        HasEntryDataTable.this.redraw();
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

    public class PartIdColumn extends DataTable<HasEntryInfo>.DataTableColumn<HasEntryInfo> implements
                                                                                            IHasEntryHandlers {

        private HandlerManager handlerManager;

        public PartIdColumn(HasEntryPartIDCell<HasEntryInfo> cell) {
            super(cell, ColumnField.PART_ID);
        }

        @Override
        public HasEntryInfo getValue(HasEntryInfo object) {
            return object;
        }

        @Override
        public HandlerRegistration addEntryHandler(EntryViewEventHandler handler) {
            if (handlerManager == null)
                handlerManager = new HandlerManager(this);
            return handlerManager.addHandler(EntryViewEvent.getType(), handler);
        }

        @Override
        public void fireEvent(GwtEvent<?> event) {
            if (handlerManager != null)
                handlerManager.fireEvent(event);
        }
    }

}
