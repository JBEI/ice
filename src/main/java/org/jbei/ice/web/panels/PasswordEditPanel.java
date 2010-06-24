package org.jbei.ice.web.panels;

import java.util.Calendar;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.ProfilePage;

public class PasswordEditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public PasswordEditPanel(String id, Account account) {
        super(id);

        class AccountEditForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private String password;
            private String confirmPassword;
            private Account account;

            public AccountEditForm(String id, Account account) {
                super(id);
                this.account = account;
                setModel(new CompoundPropertyModel<Object>(this));

                add(new PasswordTextField("password").setRequired(true).setLabel(
                    new Model<String>("Password")).add(
                    new StringValidator.MinimumLengthValidator(6)));
                add(new PasswordTextField("confirmPassword").setRequired(true).setLabel(
                    new Model<String>("Confirm"))
                        .add(new StringValidator.MinimumLengthValidator(6)));
                add(new Button("submitButton", new Model<String>("Submit")));
                add(new FeedbackPanel("feedback"));
            }

            @Override
            protected void onSubmit() {
                if (!password.equals(confirmPassword)) {
                    error("Password and Confirm does not match.");
                }

                try {
                    Account sessionAccount = IceSession.get().getAccount();
                    if (!sessionAccount.getEmail().equals(account.getEmail())) {
                        error("Wrong Account!");
                    }
                    if (sessionAccount != null) {
                        sessionAccount.setIsSubscribed(1);
                        sessionAccount.setModificationTime(Calendar.getInstance().getTime());
                        sessionAccount.setPassword(AccountController.encryptPassword(password));

                        AccountController.save(sessionAccount);
                    }
                    setResponsePage(ProfilePage.class, new PageParameters("0=about,1="
                            + sessionAccount.getEmail()));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (Exception e) {
                    throw new ViewException(e);
                }
            }

        }

        add(new AccountEditForm("registrationForm", account));
    }
}
