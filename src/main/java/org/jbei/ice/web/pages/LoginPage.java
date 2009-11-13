package org.jbei.ice.web.pages;


import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.web.IceSession;


/**
 * @author tham
 */
public class LoginPage extends HomePage {
	public LoginPage(PageParameters parameters) {
		super(parameters);
		get("title").replaceWith(new Label("title", "Login - JBEI Registry"));
		
		add(new Label("loginText", "This is some text"));
		
		class LoginForm extends StatelessForm {
			private String loginName;
			private String loginPassword;
			
			public LoginForm(String id) {
				super(id);
				setModel(new CompoundPropertyModel(this));
				add(new TextField<String>("loginName").setRequired(true));
				add(new PasswordTextField("loginPassword").setRequired(true));
			}
			
			public String getLogin() {
				return loginName;
			}
			public String getPassword() {
				return loginPassword;
			}
		}
		
		Form loginForm = new LoginForm("loginForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				System.out.println("User: " + getLogin());
				System.out.println("Pass: " + getPassword());
				
				if (authenticate(getLogin(), getPassword())) {
					if (!continueToOriginalDestination()) {
						setResponsePage(getApplication().getHomePage());
					}
				} else {
					error("unknown username / password");
				}
			}
			
			@Override
			protected void onError() {
				System.out.println("Oops, error");
			}
			
			protected boolean authenticate(String username, String password) {
				IceSession session = IceSession.get();
				return session.authenticateUser(username, password);
			}
		};
		
		/*
		loginForm.add(new TextField("loginName", new Model(""))
			.setRequired(true));
		loginForm.add(new PasswordTextField("loginPassword", new Model(""))
			.setRequired(true));
		*/
		
		loginForm.add(new Button("logInButton", new Model<String>("Log In")) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void onClick() {
				//button was pushed
			}
		});
		
		add(loginForm);
		
	}
}

