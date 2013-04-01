package org.jbei.ice.client.login;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Hector Plahar
 */
public class ForgotPasswordPanel extends Composite {

    private TextBox loginInput;
    private Button submitButton;
    private HandlerRegistration submitRegistration;
    private Label cancel;

    public ForgotPasswordPanel() {
        loginInput = new TextBox();
        loginInput.setStyleName("login_input");
        loginInput.getElement().setAttribute("placeHolder", "Username");

        submitButton = new Button("Reset Password");
        submitButton.setStyleName("login_btn");
        submitButton.setWidth("120px");

        cancel = new Label("Cancel");
        cancel.setStyleName("footer_feedback_widget");
        cancel.addStyleName("font-80em");
        cancel.addStyleName("display-inline");

        String html = "<br><img src=\"static/images/logo.png\" /><br><br><br>"
                + "<span id=\"user_login_input\"></span><br>"
                + "<span id=\"login_button\"></span> &nbsp; <span id=\"cancel_reset_password\"></span>";
        HTMLPanel htmlPanel = new HTMLPanel(html);
        initWidget(htmlPanel);
        htmlPanel.add(loginInput, "user_login_input");
        htmlPanel.add(submitButton, "login_button");
        htmlPanel.add(cancel, "cancel_reset_password");

        loginInput.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (loginInput.getText().isEmpty() || loginInput.getText().length() > 1)
                    return;

                if (loginInput.getStyleName().contains("login_input_error"))
                    loginInput.removeStyleName("login_input_error");
            }
        });
    }

    public void setSubmitClickHandler(final ClickHandler handler) {
        if (submitRegistration != null)
            submitRegistration.removeHandler();

        submitRegistration = submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (loginInput.getText().trim().isEmpty()) {
                    loginInput.addStyleName("login_input_error");
                    return;
                }

                loginInput.removeStyleName("login_input_error");
                handler.onClick(event);
            }
        });
    }

    public void reset() {
        loginInput.setText("");
        loginInput.removeStyleName("login_input_error");
    }

    public void setCancelHandler(ClickHandler handler) {
        cancel.addClickHandler(handler);
    }

    public String getLogin() {
        return loginInput.getText();
    }
}