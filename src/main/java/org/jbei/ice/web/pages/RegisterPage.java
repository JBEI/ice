package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

public class RegisterPage extends UnprotectedPage {
	public RegisterPage(PageParameters parameters) {
		super(parameters);
		
		add(new Label("registerText", "This is some text"));
	}
}
