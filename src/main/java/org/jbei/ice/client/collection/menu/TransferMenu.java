package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Top menu item for transferring selected entries to other registries
 *
 * @author Hector Plahar
 */
public class TransferMenu extends Composite {

    interface Style extends CellList.Style {
        String subMenuTransfer();
    }

    interface TransferResource extends CellList.Resources {

        static TransferResource INSTANCE = GWT.create(TransferResource.class);

        @Source("org/jbei/ice/client/resource/css/Transfer.css")
        Style cellListStyle();
    }

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/CollectionMultiSelect.css")
        CellTable.Style cellTableStyle();
    }

    private static final String LABEL = "<i class=\"" + FAIconType.SHARE_SQUARE.getStyleName()
            + "\" style=\"opacity:0.85; color: #0082C0\"></i> "
            + "Transfer <i class=\"" + FAIconType.CARET_DOWN.getStyleName() + "\"></i>";
    private final Button transfer;
    private final CellTable<OptionSelect> table;
    private final Button submitButton;
    private final HTML clear;
    private final ListDataProvider<OptionSelect> dataProvider;
    private final MultiSelectionModel<OptionSelect> model;
    private final PopupHandler addToHandler;

    public TransferMenu() {
        TransferResource.INSTANCE.cellListStyle().ensureInjected();
        transfer = new Button(LABEL);
        transfer.setStyleName(TransferResource.INSTANCE.cellListStyle().subMenuTransfer());
        initWidget(transfer);

        transfer.setEnabled(false);
        table = new CellTable<OptionSelect>(30, SelectionResource.INSTANCE);
        addSelectionColumn();
        addNameColumn();

        table.addCellPreviewHandler(new CellPreviewEvent.Handler<OptionSelect>() {

            @Override
            public void onCellPreview(CellPreviewEvent<OptionSelect> event) {
                boolean clicked = "click".equals(event.getNativeEvent().getType());
                if (!clicked || event.getColumn() == 0)
                    return;

                boolean select = model.isSelected(event.getValue());
                model.setSelected(event.getValue(), !select);
            }
        });

        // message to display when no collections are created
        HTML emptyWidget = new HTML("<i style=\"color: #999\">No registry partners available</i>");
        emptyWidget.addStyleName("font-75em");
        table.setEmptyTableWidget(emptyWidget);
        submitButton = new Button("Submit");
        submitButton.addKeyPressHandler(new EnterClickHandler(submitButton));

        clear = new HTML("Clear");
        clear.setStyleName("font-75em");
        clear.addStyleName("footer_feedback_widget");

        final Widget popup = createPopupWidget();
        addToHandler = new PopupHandler(popup, transfer.getElement(), false);
        transfer.addClickHandler(addToHandler);
        addToHandler.setCloseHandler(new CloseHandler<PopupPanel>() {

            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                model.clear();
            }
        });

        dataProvider = new ListDataProvider<OptionSelect>();
        model = new MultiSelectionModel<OptionSelect>();

        table.setSelectionModel(model, DefaultSelectionEventManager.<OptionSelect>createCheckboxManager());
        this.submitButton.setEnabled(false);

        // logic to enable and disable submission button
        model.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                boolean enable = (model.getSelectedSet().size() > 0);
                submitButton.setEnabled(enable);
            }
        });

        // clear button clickhandler
        this.clear.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                model.clear();
            }
        });
        dataProvider.addDataDisplay(table);
    }

    public void setOptions(List<OptionSelect> options) {
        dataProvider.setList(options);
    }

    public void setHandler(final ClickHandler handler) {
        this.submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                handler.onClick(event);
                hideOptions();
            }
        });
    }

    /**
     * @return widget that is shown when user clicks on the button
     */
    protected Widget createPopupWidget() {
        FlexTable wrapper = new FlexTable();
        wrapper.addStyleName("bg_white");
        wrapper.setWidget(0, 0, table);
        wrapper.getFlexCellFormatter().setColSpan(0, 0, 3);

        wrapper.setHTML(1, 0, "&nbsp;");

        wrapper.setWidget(1, 1, submitButton);
        wrapper.getFlexCellFormatter().setWidth(1, 1, "50px");

        wrapper.setWidget(1, 2, clear);
        wrapper.getFlexCellFormatter().setWidth(1, 2, "40px");

        return wrapper;
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

    public void hideOptions() {
        addToHandler.hidePopup();
        model.clear();
    }

    public ArrayList<OptionSelect> getSelectedItems() {
        return new ArrayList<OptionSelect>(model.getSelectedSet());
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

    public void setEnabled(boolean enable) {
        this.transfer.setEnabled(enable);
    }
}
