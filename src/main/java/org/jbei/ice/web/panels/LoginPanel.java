package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.SessionData;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.IceSession.IceSessionException;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.ForgotPasswordPage;
import org.jbei.ice.web.pages.HomePage;
import org.jbei.ice.web.pages.RegistrationPage;

public class LoginPanel extends Panel {
    private static final long serialVersionUID = -3410412725639985716L;

    public LoginPanel(String id) {
        super(id);
        class LoginForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String loginName;
            private String loginPassword;
            private boolean keepSignedIn;

            public LoginForm(String id) {
                super(id);

                setModel(new CompoundPropertyModel<Object>(this));

                add(new TextField<String>("loginName").setRequired(true).setLabel(
                    new Model<String>("Login")));
                add(new PasswordTextField("loginPassword").setRequired(true).setLabel(
                    new Model<String>("Password")));
                add(new CheckBox("keepSignedIn"));
                BookmarkablePageLink<RegistrationPage> bookmarkablePageLink = new BookmarkablePageLink<RegistrationPage>(
                        "registrationLink", RegistrationPage.class);
                if (!JbeirSettings.getSetting("NEW_REGISTRATION_ALLOWED").equals("yes")) {
                    bookmarkablePageLink.setVisible(false);
                }
                add(bookmarkablePageLink);
                BookmarkablePageLink<ForgotPasswordPage> forgotPasswordLink = new BookmarkablePageLink<ForgotPasswordPage>(
                        "forgotPasswordLink", ForgotPasswordPage.class);
                if (!JbeirSettings.getSetting("PASSWORD_CHANGE_ALLOWED").equals("yes")) {
                    forgotPasswordLink.setVisible(false);
                }
                add(forgotPasswordLink);
                add(new Button("logInButton", new Model<String>("Log In")));

            }

            // overridden methods
            @Override
            protected void onSubmit() {
                IceSession iceSession = (IceSession) getSession();

                try {
                    SessionData sessionData = iceSession
                            .authenticateUser(getLogin(), getPassword());

                    if (getKeepSignedIn()) {

                        try {
                            iceSession.makeSessionPersistent(((WebResponse) getResponse()),
                                sessionData);
                        } catch (ManagerException e) {
                            throw new ViewException(e);
                        }
                    }

                    if (!continueToOriginalDestination()) {
                        setResponsePage(HomePage.class);
                    }
                } catch (IceSessionException e) {
                    throw new ViewException(e);
                } catch (InvalidCredentialsException e) {
                    Logger.info("Login failed for user " + getLogin());

                    error(e.getMessage());
                }
            }

            @Override
            protected void onError() {
            }

            // setters and getters
            public String getLogin() {
                return loginName;
            }

            public String getPassword() {
                return loginPassword;
            }

            public boolean getKeepSignedIn() {
                return keepSignedIn;
            }
        }

        Form<?> loginForm = new LoginForm("loginForm");

        add(loginForm);
        add(new FeedbackPanel("feedback"));
    }
}
