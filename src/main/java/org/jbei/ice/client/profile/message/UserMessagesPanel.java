package org.jbei.ice.client.profile.message;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.shared.dto.MessageInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Hector Plahar
 */
public class UserMessagesPanel extends Composite implements IUserProfilePanel {

    private final MessageDataTable table;

    public UserMessagesPanel(Delegate<MessageInfo> delegate) {
        FlexTable layout = new FlexTable();
        initWidget(layout);
        this.table = new MessageDataTable(delegate);
        layout.setWidth("100%");
        layout.setWidget(0, 0, table);
    }

    public MessageDataTable getDataTable() {
        return this.table;
    }

    public void refresh() {
        table.redraw();
    }
}
