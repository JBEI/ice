package org.jbei.ice.client.profile.message;

import java.util.List;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class UserMessagesPanel extends Composite implements IUserProfilePanel {

    private final MessageDataTable table;
    private final FlexTable layout;
    private MessageDetailView detailView;
    private Button createMessage;
    private final CreateMessagePanel createMessagePanel;
    private final SimplePager pager;
    private final FlexTable vPanel;

    public UserMessagesPanel(Delegate<MessageInfo> delegate) {
        layout = new FlexTable();

        this.table = new MessageDataTable(delegate);

        layout.setWidth("100%");

        pager = new SimplePager();
        pager.setDisplay(table);
        pager.setPageSize(15);

        createMessage = new Button("<i class=\"blue " + FAIconType.ENVELOPE.getStyleName() + "\"></i>"
                                           + "<i style=\"vertical-align: sub; font-size: 7px;\" class=\""
                                           + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Compose");

        layout.setWidget(0, 0, table);
        layout.setWidget(1, 0, pager);
        layout.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);

        vPanel = new FlexTable();
        vPanel.setWidth("100%");

        vPanel.setHTML(0, 0, "&nbsp;");
        vPanel.getFlexCellFormatter().setColSpan(0, 0, 2);

        vPanel.setWidget(1, 0, createMessage);
        vPanel.getFlexCellFormatter().setWidth(1, 0, "620px");
        FeedbackPanel panel = new FeedbackPanel("340px");
        panel.setFailureMessage("Could not sent message to one or more recipients");
        vPanel.setWidget(1, 1, panel);
        vPanel.getFlexCellFormatter().setVisible(1, 1, false);

        vPanel.setHTML(2, 0, "&nbsp;");
        vPanel.getFlexCellFormatter().setColSpan(2, 0, 2);

        vPanel.setWidget(3, 0, layout);
        vPanel.getFlexCellFormatter().setColSpan(3, 0, 2);

        initWidget(vPanel);

        createMessagePanel = new CreateMessagePanel();
        addCreateMessageHandler();
    }

    public void setPagerVisibility(boolean visible) {
        pager.setVisible(visible);
    }

    public void setSendMessageDelegate(ServiceDelegate<MessageInfo> delegate) {
        createMessagePanel.setSendMessageDelegate(delegate);
    }

    public void setPrivateGroupOptions(List<OptionSelect> list) {
        createMessagePanel.setToDropDownOptions(list);
    }

    protected void addCreateMessageHandler() {
        createMessage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createMessagePanel.showDialog(true);
            }
        });
    }

    public MessageDataTable getDataTable() {
        return this.table;
    }

    public void showMessageDetails(MessageInfo messageInfo) {
        if (detailView == null) {
            detailView = new MessageDetailView(messageInfo);
            detailView.setBackIconHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    layout.setWidget(0, 0, table);
                    layout.getFlexCellFormatter().setVisible(1, 0, true);
                }
            });
        } else {
            detailView.setInfo(messageInfo);
        }

        layout.setWidget(0, 0, detailView);
        layout.getFlexCellFormatter().setVisible(1, 0, false);
    }

    public void refresh() {
        table.redraw();
    }

    public void showErrorMessage(boolean show) {
        vPanel.getFlexCellFormatter().setVisible(1, 1, show);
    }

    /**
     * Shows a detail view of a particular message
     */
    private class MessageDetailView extends Composite {

        private final FlexTable layout;
        private final Icon backIcon;
        private MessageInfo info;

        public MessageDetailView(MessageInfo info) {
            layout = new FlexTable();
            layout.setCellPadding(0);
            layout.setCellSpacing(0);
            layout.setWidth("80%");
            initWidget(layout);

            backIcon = new Icon(FAIconType.CIRCLE_ARROW_LEFT);
            backIcon.addStyleName("display-inline");
            backIcon.removeStyleName("font-awesome");
            backIcon.addStyleName("edit_icon");

            this.info = info;

            display();
        }

        protected void display() {
            HTMLPanel headerPanel = new HTMLPanel(
                    "<span id=\"back_to_messages\"></span>&nbsp; " + info.getTitle()
                            + "<span style=\"float: right\" id=\"email_header_nav\"></span>");
//            headerPanel.add(createNav(), "email_header_nav");
            headerPanel.add(backIcon, "back_to_messages");
            headerPanel.setStyleName("profile_message_detail_header");
            layout.setWidget(0, 0, headerPanel);

            // from
            String html = "<span style=\"opacity: 0.6\">From:</span> <b>" + info.getFrom()
                    + "</b><span style=\"float: right;opacity: 0.6\">"
                    + DateUtilities.formatDate(info.getSent()) + "</span>";
            HTMLPanel fromPanel = new HTMLPanel(html);
            fromPanel.setStyleName("font-80em");
            layout.setWidget(1, 0, fromPanel);
            layout.getCellFormatter().setHeight(1, 0, "35px");

            // actual message
            layout.setWidget(2, 0, createMessageDisplay(info.getMessage()));
        }

        public void setBackIconHandler(ClickHandler handler) {
            this.backIcon.addClickHandler(handler);
        }

        protected Widget createNav() {
            String html = "<span id=\"left_chevron\"></span> &nbsp; <span id=\"right_chevron\"></span>";
            HTMLPanel panel = new HTMLPanel(html);
            Icon left = new Icon(FAIconType.CHEVRON_LEFT);
            left.addStyleName("display-inline");
            left.removeStyleName("font-awesome");
            left.addStyleName("edit_icon");

            Icon right = new Icon(FAIconType.CHEVRON_RIGHT);
            right.addStyleName("display-inline");
            right.removeStyleName("font-awesome");
            right.addStyleName("edit_icon");

            panel.add(left, "left_chevron");
            panel.add(right, "right_chevron");
            return panel;
        }

        protected Widget createMessageDisplay(String message) {
            HTML html = new HTML("<br>" + message);
            html.setStyleName("font-85em");
            return html;
        }

        public void setInfo(MessageInfo info) {
            this.info = info;
            display();
        }
    }
}
