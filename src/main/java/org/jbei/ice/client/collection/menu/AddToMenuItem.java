package org.jbei.ice.client.collection.menu;

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
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.SelectionModel;

public class AddToMenuItem<T extends OptionSelect> extends SubMenuBase implements SubMenuOptionsPresenter.View<T> {

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/CollectionMultiSelect.css")
        CellTable.Style cellTableStyle();
    }

    private final CellTable<T> table;
    private final Button submitButton;
    private final Button clearButton;
    private final SubMenuOptionsPresenter<T> presenter;
    private final Button addWidget;
    private final PopupHandler addToHandler;

    public AddToMenuItem(String label) {
        addWidget = createAddWidget(label);
        initWidget(addWidget);

        table = new CellTable<T>(30, SelectionResource.INSTANCE); // TODO : a pager is needed for when the list size
        // exceeds 30
        addSelectionColumn();
        addNameColumn();

        table.addCellPreviewHandler(new Handler<T>() {

            @Override
            public void onCellPreview(CellPreviewEvent<T> event) {
                boolean clicked = "click".equals(event.getNativeEvent().getType());
                if (!clicked || event.getColumn() == 0)
                    return;

                presenter.optionSelected(event.getValue());
                // we can either trigger a submit when user clicks a single cell
                // or has the check box selected only (user then has to click submit)
                // currently choosing the latter option
                // dispatchSubmitEvent(); 
            }
        });

        // message to display when no collections are created
        table.setEmptyTableWidget(new HTML(
                "<i class=\"font-75em\">No user collections available.</i>"));

        submitButton = new Button("Submit");
        submitButton.setStyleName("saved_draft_button");
        submitButton.addKeyPressHandler(new EnterClickHandler(submitButton));
        submitButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                dispatchSubmitEvent();
                hideOptions();
            }
        });

        clearButton = new Button("Clear");
        clearButton.setStyleName("saved_draft_button");
        clearButton.addKeyPressHandler(new EnterClickHandler(submitButton));

        final Widget popup = createPopupWidget();
        addToHandler = new PopupHandler(popup, addWidget.getElement(), false);
        addWidget.addClickHandler(addToHandler);
        addToHandler.setCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                presenter.clearAllSelected();
            }
        });

        presenter = new SubMenuOptionsPresenter<T>(this);
        presenter.addDisplay(table);
    }

    /**
     * @return widget that is shown when user clicks on the button
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

    /**
     * "Add to" button widget
     *
     * @param label button label
     * @return created button widget
     */
    protected Button createAddWidget(String label) {
        return new Button(label + " <i class=\"" + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
    }

    protected void addNameColumn() {
        TextColumn<T> name = new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                return object.toString();
            }
        };
        table.addColumn(name);
        table.setColumnWidth(name, "200px");
    }

    protected void addSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false);

        Column<T, Boolean> selectionCol = new Column<T, Boolean>(columnCell) {

            @Override
            public Boolean getValue(T object) {
                return presenter.isSelected(object);
            }
        };

        table.addColumn(selectionCol);
        table.setColumnWidth(selectionCol, "5px");
    }

    @Override
    public void setOptions(List<T> options) {
        presenter.setOptions(options);
    }

    @Override
    public void addOption(T option) {
        presenter.addOption(option);
    }

    public void removeOption(T option) {
        presenter.removeOption(option);
    }

    public void updateOption(T option) {
        presenter.updateOption(option);
    }

    @Override
    public void setSelectionModel(SelectionModel<T> selectionModel, Handler<T> selectionEventManager) {
        table.setSelectionModel(selectionModel, selectionEventManager);
    }

    @Override
    public void setSubmitEnable(boolean enable) {
        this.submitButton.setEnabled(enable);
    }

    @Override
    public void setClearEnable(boolean enable) {
        this.clearButton.setEnabled(enable);
    }

    @Override
    public void addClearHandler(ClickHandler handler) {
        this.clearButton.addClickHandler(handler);
    }

    @Override
    public void hideOptions() {
        addToHandler.hidePopup();
        presenter.clearAllSelected();
    }

    public List<T> getSelectedItems() {
        return presenter.getSelectedItems();
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
        this.addWidget.setEnabled(enable);
    }
}
