package org.jbei.ice.client.profile.message;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.MessageInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowHoverEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;

/**
 * @author Hector Plahar
 */
public class MessageDataTable extends CellTable<MessageInfo> {

    protected interface MessageTableResources extends Resources {

        static MessageTableResources INSTANCE = GWT.create(MessageTableResources.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/MessageDataTable.css")
        DataTableStyle cellTableStyle();
    }

    public interface DataTableStyle extends Style {
    }

    private MessageSelectionModel selectionModel;
    private Delegate<MessageInfo> cellClickDelegate;

    public MessageDataTable(Delegate<MessageInfo> cellClickDelegate) {
        super(15, MessageTableResources.INSTANCE);
        this.cellClickDelegate = cellClickDelegate;
        this.setWidth("80%");
        Label empty = new Label();
        empty.setText("No data available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);
        setSelectionModel();
        createColumns();

        this.addRowHoverHandler(new RowHoverEvent.Handler() {
            @Override
            public void onRowHover(RowHoverEvent event) {
                GWT.log("" + event.getHoveringRow().getRowIndex());
                event.isUnHover();
            }
        });
    }

    private void setSelectionModel() {
        selectionModel = new MessageSelectionModel();
        setSelectionModel(selectionModel, DefaultSelectionEventManager.<MessageInfo>createCheckboxManager());
    }

    private void createColumns() {
        createSelectionColumn();
        createFromColumn();
        createTitleColumn();
        createDateReceivedColumn();
    }

    private void createSelectionColumn() {
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

        Column<MessageInfo, Boolean> checkColumn = new Column<MessageInfo, Boolean>(columnCell) {

            @Override
            public Boolean getValue(MessageInfo object) {
                return selectionModel.isSelected(object);
            }
        };

        checkColumn.setSortable(false);
        SelectionColumnHeader header = new SelectionColumnHeader();
        addColumn(checkColumn, header);
        setColumnWidth(checkColumn, 15, com.google.gwt.dom.client.Style.Unit.PX);
    }

    protected void createFromColumn() {
        MessageTableCell cell = new MessageTableCell(cellClickDelegate) {

            @Override
            public SafeHtml render(MessageInfo object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (object.isRead())
                    sb.appendEscaped(object.getFrom());
                else {
                    sb.appendHtmlConstant("<b>");
                    sb.appendEscaped(object.getFrom());
                    sb.appendHtmlConstant("</b>");
                }
                return sb.toSafeHtml();
            }
        };

        CanClickColumn column = new CanClickColumn(cell);
        column.setSortable(false);
        addColumn(column, "Inbox");
        setColumnWidth(column, 20, com.google.gwt.dom.client.Style.Unit.PCT);
    }

    protected void createTitleColumn() {
        MessageTableCell cell = new MessageTableCell(cellClickDelegate) {

            @Override
            public SafeHtml render(MessageInfo object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (object.isRead())
                    sb.appendEscaped(object.getTitle());
                else {
                    sb.appendHtmlConstant("<b>");
                    sb.appendEscaped(object.getTitle());
                    sb.appendHtmlConstant("</b>");
                }
                return sb.toSafeHtml();
            }
        };

        CanClickColumn column = new CanClickColumn(cell);
        column.setSortable(false);
        addColumn(column, "");
    }

    protected void createDateReceivedColumn() {
        MessageTableCell cell = new MessageTableCell(cellClickDelegate) {

            @Override
            public SafeHtml render(MessageInfo value) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (value.isRead())
                    sb.appendEscaped(DateUtilities.formatMediumDate(value.getSent()));
                else {
                    sb.appendHtmlConstant("<b>");
                    sb.appendEscaped(DateUtilities.formatMediumDate(value.getSent()));
                    sb.appendHtmlConstant("</b>");
                }
                return sb.toSafeHtml();
            }
        };

        CanClickColumn column = new CanClickColumn(cell);
        column.setSortable(false);
        addColumn(column, "");
        setColumnWidth(column, 10, com.google.gwt.dom.client.Style.Unit.PCT);
    }

    private class SelectionColumnHeader extends Header<Boolean> {

        public SelectionColumnHeader() {
            super(new MessageSelectionColumnHeader(MessageDataTable.this, selectionModel, true, false));
        }

        @Override
        public Boolean getValue() {
            if (selectionModel.isAllSelected())
                return true;

            return !(selectionModel.getSelectedSet().isEmpty());
        }
    }

    private class CanClickColumn extends Column<MessageInfo, MessageInfo> {

        public CanClickColumn(MessageTableCell cell) {
            super(cell);
        }

        @Override
        public MessageInfo getValue(MessageInfo object) {
            return object;
        }
    }
}
