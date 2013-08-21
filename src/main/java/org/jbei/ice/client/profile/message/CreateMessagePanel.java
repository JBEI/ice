package org.jbei.ice.client.profile.message;

import java.util.List;
import java.util.Set;

import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Panel for creating a new message
 *
 * @author Hector Plahar
 */
public class CreateMessagePanel extends Composite {

    private DialogBox dialogBox;
    private TextBox subjectBox;
    private TextBox toBox;
    private TextArea messageArea;
    private Button sendMessageButton;
    private Label cancel;
    private RecipientWidget recipientWidget;
    private ServiceDelegate<MessageInfo> sendMessageDelegate;
    private final FlexTable layout;

    public CreateMessagePanel() {
        layout = new FlexTable();
        initWidget(layout);
        initComponents();

        layout.setWidth("100%");
        layout.setCellPadding(0);
        layout.setCellSpacing(5);

        layout.setWidget(0, 0, recipientWidget);
        layout.setWidget(0, 1, toBox);

        layout.setHTML(1, 0, "Subject");
        layout.setWidget(1, 1, subjectBox);

        layout.setWidget(2, 0, messageArea);
        layout.getFlexCellFormatter().setColSpan(2, 0, 2);

        layout.setWidget(3, 0, createActionWidget());
        layout.getCellFormatter().setHorizontalAlignment(3, 0, HasAlignment.ALIGN_RIGHT);
        layout.getFlexCellFormatter().setColSpan(3, 0, 2);

        // cancel handler
        cancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });

        // submit handler
        sendMessageButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (sendMessageDelegate == null)
                    return;

                if (!validates())
                    return;

                MessageInfo info = new MessageInfo();
                for (String email : toBox.getText().split(",")) {
                    User user = new User();
                    user.setEmail(email);
                    info.getAccounts().add(user);
                }

                // set groups
                if (recipientWidget.getSelected() != null) {
                    for (OptionSelect select : recipientWidget.getSelected()) {
                        UserGroup userGroup = new UserGroup();
                        userGroup.setId(select.getId());
                        info.getUserGroups().add(userGroup);
                    }
                }

                info.setMessage(messageArea.getText().trim());
                info.setTitle(subjectBox.getText().trim());

                sendMessageDelegate.execute(info);
                dialogBox.hide();
            }
        });
    }

    public void setTo(String email) {
        toBox.setText(email);
        toBox.setEnabled(false);
        layout.getFlexCellFormatter().setVisible(0, 0, false);
        layout.getFlexCellFormatter().setVisible(0, 1, false);
    }

    protected void initComponents() {
        // to
        toBox = new TextBox();
        toBox.getElement().setAttribute("placeHolder",
                                        "Enter comma separated list of user ids (emails) and/or select available " +
                                                "group(s)");
        toBox.setStyleName("input_box");
        toBox.setWidth("500px");
        recipientWidget = new RecipientWidget();

        // subject widget
        subjectBox = new TextBox();
        subjectBox.setStyleName("input_box");
        subjectBox.setWidth("500px");

        // dialog box
        dialogBox = new DialogBox(new Caption());
        dialogBox.setWidth("600px");
        dialogBox.setGlassEnabled(true);
        dialogBox.setGlassStyleName("dialog_box_glass");
        dialogBox.setWidget(this);

        // save button
        sendMessageButton = new Button("Send");

        // cancel button
        cancel = new Label("Cancel");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-80em");
        cancel.addStyleName("display-inline");

        // message area
        messageArea = new TextArea();
        messageArea.setStyleName("input_box");
        messageArea.setVisibleLines(12);
        messageArea.setWidth("100%");
    }

    public void setToDropDownOptions(List<OptionSelect> options) {
        recipientWidget.setOptions(options);
    }

    protected boolean validates() {
        boolean isValid = true;

        if (recipientWidget.getSelected().isEmpty() && toBox.getText().trim().isEmpty()) {
            toBox.setStyleName("input_box_error");
            isValid = false;
        } else
            toBox.setStyleName("input_box");

        // message body is favored over subject if both are empty
        if (messageArea.getText().trim().isEmpty()) {
            messageArea.setStyleName("input_box_error");
            isValid = false;
        } else
            messageArea.setStyleName("input_box");

        return isValid;
    }

    protected Widget createActionWidget() {
        String html = "<span id=\"submit_message\"></span> &nbsp; <span id=\"cancel_reset_password\"></span>";
        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.add(sendMessageButton, "submit_message");
        htmlPanel.add(cancel, "cancel_reset_password");
        return htmlPanel;
    }

    public void showDialog(boolean show) {
        if (show)
            dialogBox.center();
        else
            dialogBox.hide();
    }

    public void setSendMessageDelegate(ServiceDelegate<MessageInfo> sendMessageDelegate) {
        this.sendMessageDelegate = sendMessageDelegate;
    }

    /**
     * Caption for the dialog box.
     */
    private class Caption extends HTML implements DialogBox.Caption {
    }

    /**
     * Widget that allows selection of message recipients via clicking "To"
     */

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/CollectionMultiSelect.css")
        CellTable.Style cellTableStyle();
    }

    private static class RecipientWidget extends Composite {

        private final HTML toLabel;
        private final CellTable<OptionSelect> table;
        private final ListDataProvider<OptionSelect> dataProvider;
        private final MultiSelectionModel<OptionSelect> model;
        private final PopupHandler addToHandler;

        public RecipientWidget() {
            toLabel = new HTML("To <i class=\"" + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
            toLabel.setStyleName("cursor_pointer");
            initWidget(toLabel);

            // table
            table = new CellTable<OptionSelect>(30, SelectionResource.INSTANCE);
            table.addStyleName("bg_white");
            table.setEmptyTableWidget(new HTML("<i class=\"font-75em\">No private groups available</i>"));
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
                    // we can either trigger a submit when user clicks a single cell
                    // or has the check box selected only (user then has to click submit)
                    // currently choosing the latter option
                    // dispatchSubmitEvent();
                }
            });

            addToHandler = new PopupHandler(table, toLabel.getElement(), false);
            toLabel.addClickHandler(addToHandler);
            addToHandler.setCloseHandler(new CloseHandler<PopupPanel>() {

                @Override
                public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
                    model.clear();
                }
            });

            dataProvider = new ListDataProvider<OptionSelect>();
            model = new MultiSelectionModel<OptionSelect>();
            table.setSelectionModel(model, DefaultSelectionEventManager.<OptionSelect>createCheckboxManager());
            dataProvider.addDataDisplay(table);
        }

        public Set<OptionSelect> getSelected() {
            return model.getSelectedSet();
        }

        public void setOptions(List<OptionSelect> options) {
            dataProvider.setList(options);
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

//        /**
//         * @return widget that is shown when user clicks on the button
//         */
//        protected Widget createPopupWidget() {
//            FlexTable wrapper = new FlexTable();
//            wrapper.addStyleName("bg_white");
//            wrapper.setWidget(0, 0, table);
//            wrapper.getFlexCellFormatter().setColSpan(0, 0, 2);
//
//            wrapper.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
//            wrapper.getFlexCellFormatter().setWidth(1, 1, "46px");
//            return wrapper;
//        }

    }
}
