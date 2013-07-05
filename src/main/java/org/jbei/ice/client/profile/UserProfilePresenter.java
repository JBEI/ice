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
import org.jbei.ice.lib.shared.dto.AccountInfo;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.MessageInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class UserProfilePresenter extends PanelPresenter {

    private final ProfilePanel panel;
    private AccountInfo accountInfo;
    private final String userId;
    private Callback<AccountInfo> callback;

    public UserProfilePresenter(final RegistryServiceAsync service, HandlerManager eventBus, String uid) {
        super(service, eventBus);
        panel = new ProfilePanel();
        panel.setSendMessageDelegate(new SendMessageDelegate());
        userId = uid;
    }

    public void setNameChangeCallback(Callback<AccountInfo> accountInfoCallback) {
        this.callback = accountInfoCallback;
    }

    public void setAccountInfo(AccountInfo info) {
        panel.setAccountInfo(info);
        accountInfo = info;
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
            panel.editProfile(accountInfo, new SaveProfileHandler(), new ShowCurrentInfoHandler());
        }
    }

    private class ChangePasswordHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            panel.changePasswordPanel(accountInfo, new UpdatePasswordClickHandler(), new ShowCurrentInfoHandler());
        }
    }

    private class ShowCurrentInfoHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            panel.setAccountInfo(accountInfo);
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
                    service.updateAccountPassword(ClientController.sessionId, accountInfo.getEmail(), password,
                                                  callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        panel.setAccountInfo(accountInfo);
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

            if (!details.getEmail().equals(accountInfo.getEmail())) {
                return;
            }

            accountInfo.setDescription(details.getAbout());
            accountInfo.setFirstName(details.getFirstName());
            accountInfo.setLastName(details.getLastName());
            accountInfo.setInitials(details.getInitials());
            accountInfo.setInstitution(details.getInstitution());

            new IceAsyncCallback<AccountInfo>() {

                @Override
                protected void callService(AsyncCallback<AccountInfo> callback) throws AuthenticationException {
                    service.updateAccount(ClientController.sessionId, accountInfo.getEmail(), accountInfo, callback);
                }

                @Override
                public void onSuccess(AccountInfo result) {
                    accountInfo = result;
                    panel.setAccountInfo(accountInfo);
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
                }
            }.go(eventBus);
        }
    }
}
