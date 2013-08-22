package org.jbei.ice.client.profile.message;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.view.OptionSelect;
import org.jbei.ice.client.common.header.HeaderView;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.message.MessageList;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
        retrievePrivateGroups();
    }

    protected void retrievePrivateGroups() {
        new IceAsyncCallback<ArrayList<UserGroup>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<UserGroup>> callback) throws AuthenticationException {
                service.retrieveGroups(ClientController.sessionId, GroupType.PRIVATE, callback);
            }

            @Override
            public void onSuccess(ArrayList<UserGroup> result) {
                List<OptionSelect> options = new ArrayList<OptionSelect>();
                for (UserGroup user : result) {
                    OptionSelect option = new OptionSelect(user.getId(), user.getLabel());
                    options.add(option);
                }
                panel.setPrivateGroupOptions(options);
            }
        }.go(eventBus);
    }

    @Override
    public IUserProfilePanel getView() {
        return panel;
    }

    public void setMessages(MessageList list) {
        messageDataProvider.setMessages(list);
        panel.setPagerVisibility(!list.getList().isEmpty());
    }

    private class ClickDelegate implements Delegate<MessageInfo> {

        @Override
        public void execute(final MessageInfo object) {
            if (!object.isRead()) {
                new IceAsyncCallback<Integer>() {

                    @Override
                    protected void callService(AsyncCallback<Integer> callback) throws AuthenticationException {
                        service.markMessageRead(ClientController.sessionId, object.getId(), callback);
                    }

                    @Override
                    public void onSuccess(Integer result) {
                        HeaderView.getInstance().setNewMessages(result.intValue());
                    }
                }.go(eventBus);
            }
            panel.showMessageDetails(object);
            panel.refresh();
        }
    }

    private class SendMessageDelegate implements ServiceDelegate<MessageInfo> {

        @Override
        public void execute(final MessageInfo messageInfo) {

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.sendMessage(ClientController.sessionId, messageInfo, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    panel.refresh();
                }
            }.go(eventBus);
        }
    }
}
