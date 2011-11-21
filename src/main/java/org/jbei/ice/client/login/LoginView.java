package org.jbei.ice.client.login;

import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.header.HeaderView;

import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Hector Plahar
 */
public class LoginView extends Composite implements ILoginView {

    private Button submitButton;
    private TextBox loginInput;
    private VerticalPanel loginPanel;
    private Label loginErrorLabel;

    private TextBox passwordInput;
    private VerticalPanel passwordPanel;
    private Label passwordErrorLabel;
    private CheckBox remember;

    public LoginView() {
        HeaderPanel layout = new HeaderPanel();
        layout.setWidth("100%");
        layout.setHeight("100%");
        initWidget(layout);

        layout.setHeaderWidget(createHeader());
        layout.setContentWidget(createContents());
        layout.setFooterWidget(createFooter());
    }

    protected Widget createHeader() {
        HeaderView header = new HeaderView();
        header.setWidth("100%");
        return header;
    }

    protected Widget createContents() {

        //        VerticalPanel panel = new VerticalPanel();
        FlowPanel panel = new FlowPanel();

        // login text
        Label loginLabel = new Label("LOG IN");
        loginLabel.setStyleName("login_header_label");
        panel.add(loginLabel);

        // line 
        HorizontalPanel underline = new HorizontalPanel();
        underline.setStyleName("blue_underline");
        underline.setWidth("100%");
        panel.add(underline);

        // username text
        Label usernameLabel = new Label("Username");
        usernameLabel.addStyleName("login_text");
        panel.add(usernameLabel);

        // username input
        loginInput = new TextBox();
        loginInput.setStyleName("login_input");
        loginPanel = new VerticalPanel();
        loginPanel.add(loginInput);
        panel.add(loginPanel);

        // password text
        Label passwordLabel = new Label("Password");
        passwordLabel.addStyleName("login_text");
        panel.add(passwordLabel);

        // password input
        passwordInput = new PasswordTextBox();
        passwordInput.setStyleName("login_input");
        passwordPanel = new VerticalPanel();
        passwordPanel.add(passwordInput);
        panel.add(passwordPanel);

        // login button and "remember me link"
        HorizontalPanel submitPanel = new HorizontalPanel();
        submitButton = new Button("Login");
        submitButton.setStyleName("login_button");
        submitPanel.add(submitButton);

        remember = new CheckBox();
        submitPanel.add(remember);

        Label rememberLabel = new Label("Remember me on this computer");
        submitPanel.add(rememberLabel);

        panel.add(submitPanel);
        return panel;
    }

    protected Widget createFooter() {
        return Footer.getInstance();
    }

    @Override
    public void setSubmitHandler(KeyPressHandler handler) {
        submitButton.addKeyPressHandler(handler);
        passwordInput.addKeyPressHandler(handler);
        loginInput.addKeyPressHandler(handler);
    }

    @Override
    public String getLoginName() {
        return loginInput.getText();
    }

    @Override
    public String getLoginPass() {
        return passwordInput.getText();
    }

    @Override
    public boolean rememberUserOnComputer() {
        return remember.getValue();
    }

    @Override
    public void setLoginNameError(String errorMsg) {
        if (loginErrorLabel == null)
            loginErrorLabel = new Label();

        loginErrorLabel.setText(errorMsg);
        loginErrorLabel.setStyleName("login_error_msg");
        loginPanel.add(loginErrorLabel);
        loginInput.setStyleName("login_input_error");
    }

    @Override
    public void setLoginPassError(String errorMsg) {
        if (passwordErrorLabel == null)
            passwordErrorLabel = new Label();

        passwordErrorLabel.setText(errorMsg);
        passwordErrorLabel.setStyleName("login_error_msg");
        passwordPanel.add(passwordErrorLabel);
        passwordInput.setStyleName("login_input_error");
    }

    @Override
    public void clearLoginNameError() {
        if (loginErrorLabel == null)
            return;

        loginPanel.remove(loginErrorLabel);
        loginInput.setStyleName("login_input");
    }

    @Override
    public void clearLoginPassError() {
        if (passwordErrorLabel == null)
            return;

        passwordPanel.remove(passwordErrorLabel);
        passwordInput.setStyleName("login_input");
    }

    @Override
    public Button getSubmitButton() {
        return this.submitButton;
    }
}
