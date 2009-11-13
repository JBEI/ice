package org.jbei.ice.web.pages;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author tham
 */
public class RegisterPage extends HomePage {
	public RegisterPage (PageParameters parameters) {
		super(parameters);
		add(new Label("registerText", "This is some text"));
	}
}

