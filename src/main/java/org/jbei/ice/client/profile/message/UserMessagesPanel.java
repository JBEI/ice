package org.jbei.ice.client.profile.message;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.MessageInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
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

    public UserMessagesPanel(Delegate<MessageInfo> delegate) {
        layout = new FlexTable();

        this.table = new MessageDataTable(delegate);
        layout.setWidth("100%");
        layout.setWidget(0, 0, table);
        SimplePager pager = new SimplePager();
        layout.setWidget(1, 0, pager);
        layout.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_CENTER);
        pager.setDisplay(table);
        pager.setPageSize(15);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        createMessage = new Button("<i class=\"blue " + FAIconType.ENVELOPE.getStyleName() + "\"></i>"
                                           + "<i style=\"vertical-align: sub; font-size: 7px;\" class=\""
                                           + FAIconType.PLUS.getStyleName() + "\"></i>&nbsp; Compose");
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(createMessage);
        vPanel.add(new HTML("&nbsp;"));
        vPanel.add(layout);
        initWidget(vPanel);

        createMessagePanel = new CreateMessagePanel();
        addCreateMessageHandler();
    }

    public void setSendMessageDelegate(ServiceDelegate<MessageInfo> delegate) {
        createMessagePanel.setSendMessageDelegate(delegate);
    }

    protected void addCreateMessageHandler() {
        createMessage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createMessagePanel.showDialog();
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
