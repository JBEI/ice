package org.jbei.ice.client.login;

import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
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

    void setLoginNameError(String errMsg);

    void setLoginPassError(String errMsg);

    void clearLoginNameError();

    void clearLoginPassError();

    void setSubmitHandler(KeyPressHandler handler);

    Button getSubmitButton();

    Widget asWidget();
}
