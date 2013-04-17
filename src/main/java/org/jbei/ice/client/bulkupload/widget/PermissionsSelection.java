package org.jbei.ice.client.bulkupload.widget;

import java.util.HashSet;

import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Widget that allows user to select the global readable permissions for
 * groups
 *
 * @author Hector Plahar
 */
public class PermissionsSelection implements IsWidget {

    private final FocusPanel parent;
    private final CellTable<OptionSelect> table;
    private final HashSet<PermissionInfo> permissions;
    private final MultiSelectionModel<OptionSelect> model;
    private final ListDataProvider<OptionSelect> dataProvider;
    private final Button submitButton;
    private final Button clearButton;
    private final PopupHandler addToHandler;

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/CollectionMultiSelect.css")
        CellTable.Style cellTableStyle();
    }

    public PermissionsSelection() {
        Icon icon = new Icon(FAIconType.KEY);
        icon.setTitle("Click to set permissions");
        icon.addStyleName("display-inline");
        icon.removeStyleName("font-awesome");

        HTMLPanel panel = new HTMLPanel("<span id=\"creator_icon\"></span> Permissions <i class=\""
                                                + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
        panel.add(icon, "creator_icon");
        panel.setStyleName("display-inline");
        panel.setTitle("Set read permissions for entries");

        parent = new FocusPanel(panel);
        parent.setStyleName("bulk_upload_visibility");
        parent.addStyleName("opacity_hover");

        table = new CellTable<OptionSelect>(30, SelectionResource.INSTANCE);
        addSelectionColumn();
        addNameColumn();
        table.setEmptyTableWidget(new HTML("<i class=\"font-75em\">No groups available.</i>"));
        table.addCellPreviewHandler(new CellPreviewEvent.Handler<OptionSelect>() {

            @Override
            public void onCellPreview(CellPreviewEvent<OptionSelect> event) {
                boolean clicked = "click".equals(event.getNativeEvent().getType());
                if (!clicked || event.getColumn() == 0)
                    return;

                OptionSelect selected = event.getValue();
                boolean select = model.isSelected(selected);
                model.setSelected(selected, !select);
                // we can either trigger a submit when user clicks a single cell
                // or has the check box selected only (user then has to click submit)
                // currently choosing the latter option dispatchSubmitEvent();
            }
        });

        permissions = new HashSet<PermissionInfo>();
        dataProvider = new ListDataProvider<OptionSelect>();
        dataProvider.addDataDisplay(table);

        model = new MultiSelectionModel<OptionSelect>();

        submitButton = new Button("Submit");
        submitButton.addKeyPressHandler(new EnterClickHandler(submitButton));
        submitButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
//                dispatchSubmitEvent(); TODO
                addToHandler.hidePopup();
                model.clear();
            }
        });

        clearButton = new Button("Clear");
        clearButton.addKeyPressHandler(new EnterClickHandler(clearButton));

        final Widget popup = createPopupWidget();
        addToHandler = new PopupHandler(popup, parent.getElement(), false);
        parent.addClickHandler(addToHandler);
        addToHandler.setCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                model.clear();
            }
        });
    }

    /**
     * @return pop widget displayed when user clicks permissions along with selection options
     */
    protected Widget createPopupWidget() {
        FlexTable wrapper = new FlexTable();
        wrapper.addStyleName("bg_white");
        wrapper.setWidget(0, 0, table);
        wrapper.getFlexCellFormatter().setColSpan(0, 0, 2);

        wrapper.setWidget(1, 0, submitButton);
        wrapper.setWidget(1, 1, clearButton);
        wrapper.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
        wrapper.getFlexCellFormatter().setWidth(1, 1, "46px");
        return wrapper;
    }

    @Override
    public Widget asWidget() {
        return parent;
    }

    protected void addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false);

        Column<OptionSelect, Boolean> selectionCol = new Column<OptionSelect, Boolean>(columnCell) {

            @Override
            public Boolean getValue(OptionSelect object) {
                return model.isSelected(object);
            }
        };

        table.addColumn(selectionCol);
        table.setColumnWidth(selectionCol, "5px");
    }

    protected void addNameColumn() {
        TextColumn<OptionSelect> name = new TextColumn<OptionSelect>() {

            @Override
            public String getValue(OptionSelect object) {
                return object.toString();
            }
        };
        table.addColumn(name);
        table.setColumnWidth(name, "200px");
    }

    // inner classes
    private static class EnterClickHandler implements KeyPressHandler {

        private final Button hasClick;

        public EnterClickHandler(Button hasClick) {
            this.hasClick = hasClick;
        }

        @Override
        public void onKeyPress(KeyPressEvent event) {
            int code = event.getNativeEvent().getKeyCode();
            if (code != KeyCodes.KEY_ENTER)
                return;
            hasClick.click();
        }
    }
}
