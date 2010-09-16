package org.jbei.ice.web.panels;

import java.util.Calendar;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.ProfilePage;

public class ProfileEditPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ProfileEditPanel(String id, Account account) {
        super(id);

        class AccountEditForm extends StatelessForm<Object> {
            private static final long serialVersionUID = 1L;
            private final String firstName;
            private final String lastName;
            private String initials;
            private final String email;
            private String institution;
            private String description;

            public AccountEditForm(String id, Account account) {
                super(id);

                firstName = account.getFirstName();
                lastName = account.getLastName();
                initials = account.getInitials();
                email = account.getEmail();
                institution = account.getInstitution();
                description = account.getDescription();

                setModel(new CompoundPropertyModel<Object>(this));
                add(new TextField<String>("firstName", new PropertyModel<String>(this, "firstName"))
                        .setRequired(true).setLabel(new Model<String>("Given name"))
                        .add(new StringValidator.MaximumLengthValidator(50)));
                add(new TextField<String>("lastName", new PropertyModel<String>(this, "lastName"))
                        .setRequired(true).setLabel(new Model<String>("Family name"))
                        .add(new StringValidator.MaximumLengthValidator(50)));
                add(new TextField<String>("initials", new PropertyModel<String>(this, "initials"))
                        .setLabel(new Model<String>("Initials")).add(
                            new StringValidator.MaximumLengthValidator(10)));
                add(new TextField<String>("email", new PropertyModel<String>(this, "email"))
                        .setRequired(true).setLabel(new Model<String>("Email"))
                        .add(new StringValidator.MaximumLengthValidator(100))
                        .add(EmailAddressValidator.getInstance()).setEnabled(false));
                add(new TextField<String>("institution", new PropertyModel<String>(this,
                        "institution")).setLabel(new Model<String>("Institution")));
                add(new TextArea<String>("description", new PropertyModel<String>(this,
                        "description")).setLabel(new Model<String>("Description")));
                add(new Button("submitButton", new Model<String>("Submit")));
                add(new BookmarkablePageLink<Object>("cancelLink", ProfilePage.class,
                        new PageParameters("0=about,1=" + email)));
                add(new FeedbackPanel("feedback"));
            }

            @Override
            protected void onSubmit() {
                if (initials == null) {
                    initials = "";
                }
                if (institution == null) {
                    institution = "";
                }
                if (description == null) {
                    description = "";
                }

                try {
                    Account account = IceSession.get().getAccount();
                    if (account != null) {
                        account.setIsSubscribed(1);
                        account.setModificationTime(Calendar.getInstance().getTime());

                        account.setFirstName(firstName);
                        account.setLastName(lastName);
                        account.setInitials(initials);
                        account.setInstitution(institution);
                        account.setDescription(description);

                        AccountController.save(account);
                    }
                    setResponsePage(ProfilePage.class,
                        new PageParameters("0=about,1=" + account.getEmail()));
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
