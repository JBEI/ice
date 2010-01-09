package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

public class RegistrationPage extends UnprotectedPage {
	public RegistrationPage(PageParameters parameters) {
		super(parameters);
		
		class RegistrationForm extends StatelessForm<Object> {
			private static final long serialVersionUID = 3046351143658783110L;
			
			private String firstName;
			private String lastName;
			private String initials;
			private String email;
			private String password;
			private String confirmPassword;
			private String institution;
			private String description;
			
			public RegistrationForm(String id) {
				super(id);
				
				setModel(new CompoundPropertyModel<Object>(this));
				
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
				add(new PasswordTextField("password").setRequired(true)
						.setLabel(new Model<String>("Password")).add(
								new StringValidator.MinimumLengthValidator(6)));
				add(new PasswordTextField("confirmPassword").setRequired(true)
						.setLabel(new Model<String>("Confirm")).add(
								new StringValidator.MinimumLengthValidator(6)));
				add(new TextField<String>("institution")
						.setLabel(new Model<String>("Institution")));
				add(new TextArea<String>("description")
						.setLabel(new Model<String>("Description")));
			}
			
			@Override
			protected void onSubmit() {
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OK!");
				
				if(!password.equals(confirmPassword)) {
					error("Password and Confirm doesn't much");
				}
				
				/*
				boolean authenticated = authenticate(getLogin(), getPassword()); 
				if (authenticated) {
					if (getKeepSignedIn()) {
						IceSession iceSession = (IceSession) getSession();
						iceSession.makeSessionPersistent(((WebResponse)getResponse()));
						
					}
					if (!continueToOriginalDestination()) {
						setResponsePage(WorkSpacePage.class);
					}
					
				} else {
					Logger.info("Login failed for user " + getLogin());
					error("Unknown username / password combination");
				}*/
			}
		}
		
		Form<?> registrationForm = new RegistrationForm("registrationForm");
		registrationForm.add(new Button("submitButton", new Model<String>(
				"Submit")));
		
		add(registrationForm);
		registrationForm.add(new FeedbackPanel("feedback"));
	}
}
