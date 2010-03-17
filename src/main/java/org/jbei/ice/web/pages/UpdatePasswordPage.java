package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class UpdatePasswordPage extends ProtectedPage {
    public UpdatePasswordPage(PageParameters parameters) {
        super(parameters);

        add(new UpdatePasswordForm("updatePasswordForm"));
    }

    @Override
    protected String getTitle() {
        return "Update Password - " + super.getTitle();
    }

    class UpdatePasswordForm extends StatelessForm<Object> {
        private static final long serialVersionUID = 3046351843658183110L;

        private String oldPassword;
        private String newPassword = "";
        private String confirm;

        Account account;

        public UpdatePasswordForm(String id) {
            super(id);

            IceSession session = IceSession.get();

            if (!session.isAuthenticated()) {
                setResponsePage(WelcomePage.class);

                return;
            }

            account = session.getAccount();

            setModel(new CompoundPropertyModel<Object>(this));

            add(new PasswordTextField("oldPassword").setRequired(true).setLabel(
                    new Model<String>("Old Password")).add(
                    new StringValidator.MinimumLengthValidator(6)));
            add(new PasswordTextField("newPassword").setRequired(true).setLabel(
                    new Model<String>("New Password")).add(
                    new StringValidator.MinimumLengthValidator(6)));
            add(new PasswordTextField("confirm").setRequired(true).setLabel(
                    new Model<String>("Confirm"))
                    .add(new StringValidator.MinimumLengthValidator(6)));

            add(new Button("submitButton", new Model<String>("Update")));

            add(new FeedbackPanel("feedback"));
        }

        @Override
        protected void onSubmit() {
            try {
                assert (account != null);

                if (!AccountController.encryptPassword(oldPassword).equals(account.getPassword())) {
                    error("Invalid Old Password");

                    return;
                } else if (!newPassword.equals(confirm)) {
                    error("Password and Confirm doesn't much");

                    return;
                }

                account.setPassword(AccountController.encryptPassword(oldPassword));

                AccountController.save(account);

                setResponsePage(UpdatePasswordSuccessfulPage.class);

                Emailer
                        .send(account.getEmail(), "Your password information has been updated",
                                "Your password information has been updated.\n\nBest regards,\nRegistry Team");
            } catch (ControllerException e) {
                throw new ViewException(e);
            }
        }
    }
}
