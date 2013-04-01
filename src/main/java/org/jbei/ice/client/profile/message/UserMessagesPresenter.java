package org.jbei.ice.client.profile.message;

import java.util.ArrayList;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.shared.dto.MessageInfo;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;

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
    }

    @Override
    public IUserProfilePanel getView() {
        return panel;
    }

    public void setMessages(ArrayList<MessageInfo> result) {
        messageDataProvider.setMessages(result);
    }

    private class ClickDelegate implements Delegate<MessageInfo> {

        @Override
        public void execute(MessageInfo object) {
            object.setRead(true);
            Window.alert(object.getMessage());
            panel.refresh();
        }
    }
}
