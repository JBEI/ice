package org.jbei.ice.client.profile.message;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.shared.dto.MessageInfo;
import org.jbei.ice.shared.dto.message.MessageList;

import com.google.gwt.event.shared.HandlerManager;

/**
 * @author Hector Plahar
 */
public class UserMessagesPresenter extends PanelPresenter {

    private final UserMessagesPanel panel;
    private final MessageDataProvider messageDataProvider;

    public UserMessagesPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new UserMessagesPanel(new ClickDelegate());
        messageDataProvider = new MessageDataProvider(panel.getDataTable(), service, eventBus);
        panel.setSendMessageDelegate(new SendMessageDelegate());
    }

    @Override
    public IUserProfilePanel getView() {
        return panel;
    }

    public void setMessages(MessageList list) {
        messageDataProvider.setMessages(list);
    }

    private class ClickDelegate implements Delegate<MessageInfo> {

        @Override
        public void execute(MessageInfo object) {
            // TODO : send message to server to mark as read
            object.setRead(true);
            panel.showMessageDetails(object);
            panel.refresh();
        }
    }

    private class SendMessageDelegate implements ServiceDelegate<MessageInfo> {

        @Override
        public void execute(MessageInfo messageInfo) {
            // TODO : send message to user/group (server.sendMessage
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
