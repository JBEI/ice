package org.jbei.ice.client.profile;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.login.RegistrationDetails;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.client.profile.widget.ProfilePanel;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.user.User;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class UserProfilePresenter extends PanelPresenter {

    private final ProfilePanel panel;
    private User user;
    private final String userId;
    private Callback<User> callback;

    public UserProfilePresenter(final RegistryServiceAsync service, HandlerManager eventBus, String uid) {
        super(service, eventBus);
        panel = new ProfilePanel();
        panel.setSendMessageDelegate(new SendMessageDelegate());
        userId = uid;
    }

    public void setNameChangeCallback(Callback<User> accountInfoCallback) {
        this.callback = accountInfoCallback;
    }

    public void setUser(User info) {
        panel.setUser(info);
        user = info;
        checkCanEditProfile();
        checkCanChangePassword();
    }

    @Override
    public IUserProfilePanel getView() {
        return panel;
    }

    private void checkCanEditProfile() {
        new IceAsyncCallback<String>() {

            @Override
            protected void callService(AsyncCallback<String> callback) throws AuthenticationException {
                service.getConfigurationSetting(ConfigurationKey.PROFILE_EDIT_ALLOWED.name(), callback);
            }

            @Override
            public void onSuccess(String result) {
                if ("yes".equalsIgnoreCase(result) || "true".equalsIgnoreCase(result)) {
                    // must be admin or current logged in user
                    String uid = ClientController.account.getId() + "";
                    if (ClientController.account.isAdmin() || uid.equals(userId))
                        panel.setEditProfileButtonHandler(new EditProfileHandler());
                }
            }
        }.go(eventBus);
    }

    private void checkCanChangePassword() {
        new IceAsyncCallback<String>() {

            @Override
            protected void callService(AsyncCallback<String> callback) throws AuthenticationException {
                service.getConfigurationSetting(ConfigurationKey.PASSWORD_CHANGE_ALLOWED.name(), callback);
            }

            @Override
            public void onSuccess(String result) {
                if ("yes".equalsIgnoreCase(result) || "true".equalsIgnoreCase(result)) {
                    // must be currently logged in user to change password
                    String uid = ClientController.account.getId() + "";
                    if (uid.equals(userId))
                        panel.setChangePasswordButtonHandler(new ChangePasswordHandler());
                }
            }
        }.go(eventBus);
    }

    private class EditProfileHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            panel.editProfile(user, new SaveProfileHandler(), new ShowCurrentInfoHandler());
        }
    }

    private class ChangePasswordHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            panel.changePasswordPanel(user, new UpdatePasswordClickHandler(), new ShowCurrentInfoHandler());
        }
    }

    private class ShowCurrentInfoHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            panel.setUser(user);
        }
    }

    private class UpdatePasswordClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            final String password = panel.getUpdatedPassword();
            if (password.isEmpty())
                return;

            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.updateAccountPassword(ClientController.sessionId, user.getEmail(), password, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        panel.setUser(user);
                    }
                }
            }.go(eventBus);
        }
    }

    private class SaveProfileHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            RegistrationDetails details = panel.getUpdatedDetails();
            if (details == null)
                return;

            if (!details.getEmail().equals(user.getEmail())) {
                return;
            }

            user.setDescription(details.getAbout());
            user.setFirstName(details.getFirstName());
            user.setLastName(details.getLastName());
            user.setInitials(details.getInitials());
            user.setInstitution(details.getInstitution());

            new IceAsyncCallback<User>() {

                @Override
                protected void callService(AsyncCallback<User> callback) throws AuthenticationException {
                    service.updateAccount(ClientController.sessionId, user.getEmail(), user, callback);
                }

                @Override
                public void onSuccess(User result) {
                    user = result;
                    panel.setUser(user);
                    if (callback != null)
                        callback.onSuccess(result);
                }
            }.go(eventBus);
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
                    if (!result)
                        panel.warnMessageNotSent();
                }
            }.go(eventBus);
        }
    }
}
