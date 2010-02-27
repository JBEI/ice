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
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.ForgotPasswordPage;
import org.jbei.ice.web.pages.RegistrationPage;
import org.jbei.ice.web.pages.UserPage;

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
                add(new BookmarkablePageLink<RegistrationPage>("registrationLink",
                        RegistrationPage.class));
                add(new BookmarkablePageLink<ForgotPasswordPage>("forgotPasswordLink",
                        ForgotPasswordPage.class));
                add(new Button("logInButton", new Model<String>("Log In")));

            }

            // overridden methods
            @Override
            protected void onSubmit() {
                boolean authenticated = authenticate(getLogin(), getPassword());
                if (authenticated) {
                    if (getKeepSignedIn()) {
                        IceSession iceSession = (IceSession) getSession();
                        try {
                            iceSession.makeSessionPersistent(((WebResponse) getResponse()));
                        } catch (ManagerException e) {
                            String msg = "Could not save authentication to db: " + e.toString();
                            Logger.error(msg, e);
                            error("System Error: " + msg);
                        }
                    }

                    if (!continueToOriginalDestination()) {
                        setResponsePage(UserPage.class);
                    }

                } else {
                    Logger.info("Login failed for user " + getLogin());
                    error("Unknown username / password combination");
                }
            }

            @Override
            protected void onError() {
            }

            // specific method
            protected boolean authenticate(String username, String password) {
                IceSession icesession = (IceSession) getSession();

                return icesession.authenticateUser(username, password);
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
