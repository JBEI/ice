package org.jbei.ice.web.pages;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.web.IceSession;


/**
 * @author tham
 */
public class LoginPage extends HomePage {
	public LoginPage(PageParameters parameters) {
		super(parameters);
		get("title").replaceWith(new Label("title", "Login - JBEI Registry"));
		
		add(new Label("loginText", "This is some text"));
		
		class LoginForm extends StatelessForm<Object> {
			private static final long serialVersionUID = 1L;
			private String loginName;
			private String loginPassword;
			
			public LoginForm(String id) {
				super(id);
				setModel(new CompoundPropertyModel<Object>(this));
				add(new TextField<String>("loginName").setRequired(true)
						.setLabel(new Model<String>("Login")));
				add(new PasswordTextField("loginPassword").setRequired(true).setLabel(new Model<String>("Password")));
			}
			
			public String getLogin() {
				return loginName;
			}
			public String getPassword() {
				return loginPassword;
			}
		}
		
		Form<?> loginForm = new LoginForm("loginForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				if (authenticate(getLogin(), getPassword())) {
					if (!continueToOriginalDestination()) {
						setResponsePage(getApplication().getHomePage());
					}
				} else {
					Logger.info("Login failed for user " + getLogin());
					error("Unknown username / password combination");
				}
			}
			
			@Override
			protected void onError() {
				
			}
			
			protected boolean authenticate(String username, String password) {
				IceSession session = IceSession.get();
				return session.authenticateUser(username, password);
			}
		};
		
		loginForm.add(new Button("logInButton", new Model<String>("Log In")) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		});
		
		add(loginForm);
		add(new FeedbackPanel("feedback"));
		
	}
}

