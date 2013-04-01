package org.jbei.ice.client.login;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * Interface view for decoupling the login view
 *
 * @author Hector Plahar
 */
public interface ILoginView {

    /**
     * @return user entered login
     */
    String getLoginName();

    /**
     * @return user entered login pass
     */
    String getLoginPass();

    /**
     * @return whether user desires to have their session remembered
     *         on this computer for a specified period of time
     */
    boolean rememberUserOnComputer();

    void clearErrorMessages();

    void setInputFieldsEnable(boolean enable);

    void setLoginNameError(String errMsg);

    void setLoginPassError(String errMsg);

    void setSubmitKeyPressHandler(KeyPressHandler handler);

    void setLoginHandler(ClickHandler handler);

    Widget asWidget();

    void addForgotPasswordLinkHandler(ClickHandler forgotPasswordHandler);

    void addRegisterHandler(ClickHandler handler);

    void switchToForgotPasswordMode();

    void switchToRegisterMode(ClickHandler submitHandler, ClickHandler cancelHandler);

    RegistrationDetails getRegistrationDetails();

    void switchToLoginMode();

    void informOfDuplicateRegistrationEmail();

    void setResetPasswordHandler(ClickHandler handler);

    String getForgotPasswordLogin();
}
