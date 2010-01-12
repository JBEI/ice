package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.utils.Emailer;
import org.jbei.ice.web.IceSession;

public class UpdateAccountPage extends ProtectedPage {
	public UpdateAccountPage(PageParameters parameters) {
		super(parameters);

		class UpdateAccountForm extends StatelessForm<Object> {
			private static final long serialVersionUID = 3046351143658183110L;

			private String firstName;
			private String lastName;
			private String initials;
			private String email;
			private String institution;
			private String description;

			Account account;

			public UpdateAccountForm(String id) {
				super(id);

				IceSession session = IceSession.get();

				if (!session.isAuthenticated()) {
					setResponsePage(WelcomePage.class);

					return;
				}

				account = session.getAccount();

				setModel(new CompoundPropertyModel<Object>(this));

				firstName = account.getFirstName();
				lastName = account.getLastName();
				initials = account.getInitials();
				email = account.getEmail();
				institution = account.getInstitution();
				description = account.getDescription();

				add(new TextField<String>("firstName").setRequired(true)
						.setLabel(new Model<String>("Given name")).add(
								new StringValidator.MaximumLengthValidator(50)));
				add(new TextField<String>("lastName").setRequired(true)
						.setLabel(new Model<String>("Family name")).add(
								new StringValidator.MaximumLengthValidator(50)));
				add(new TextField<String>("initials").setLabel(
						new Model<String>("Initials")).add(
						new StringValidator.MaximumLengthValidator(10)));
				add(new TextField<String>("email").setRequired(true).setLabel(
						new Model<String>("Email")).add(
						new StringValidator.MaximumLengthValidator(100)).add(
						EmailAddressValidator.getInstance()));
				add(new TextField<String>("institution")
						.setLabel(new Model<String>("Institution")));
				add(new TextArea<String>("description")
						.setLabel(new Model<String>("Description")));
			}

			@Override
			protected void onSubmit() {
				try {
					assert (account != null);

					if (!email.equals(account.getEmail())) {
						Account testAccount = AccountManager.getByEmail(email);

						if (testAccount != null) {
							error("Account with this email address already registered");

							return;
						}
					}

					account.setFirstName(firstName);
					account.setLastName(lastName);
					account.setInitials(initials);
					account.setEmail(email);
					account.setInstitution(institution);
					account.setDescription(description);

					AccountManager.save(account);

					setResponsePage(UpdateAccountSuccessfulPage.class);

					Emailer
							.send(
									email,
									"Your account information has been updated",
									"Your account information has been updated.\n\nBest regards,\nRegistry Team");
				} catch (ManagerException e) {
					handleException(e);
				} catch (Exception e) {
					handleException(e);
				}
			}
		}

		Form<?> updateAccountForm = new UpdateAccountForm("updateAccountForm");
		updateAccountForm.add(new Button("submitButton", new Model<String>(
				"Update")));

		add(updateAccountForm);
		updateAccountForm.add(new FeedbackPanel("feedback"));
	}
}
