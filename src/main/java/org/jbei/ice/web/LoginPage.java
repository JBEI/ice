package org.jbei.ice.web;



import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


/**
 * @author tham
 */
public class LoginPage extends HomePage {
	public LoginPage(PageParameters parameters) {
		super(parameters);
		get("title").replaceWith(new Label("title", "Login - JBEI Registry"));
		
		add(new Label("loginText", "This is some text"));
		
		Form loginForm = new Form("loginForm") {
			@Override
			protected void onSubmit() {
				System.out.println("Ooh, I got something!");
			}
			@Override
			protected void onError() {
				System.out.println("Oops, error");
			}
		};
		
		loginForm.add(new TextField("loginName", new Model(""))
			.setRequired(true));
		loginForm.add(new PasswordTextField("loginPassword", new Model(""))
			.setRequired(true));
		loginForm.add(new Button("logInButton", new Model("Log In")) {
			public void onClick() {
				//button was pushed
			}
		});
		
		add(loginForm);
		
	}
}

