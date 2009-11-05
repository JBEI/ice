package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class FormPanel extends Panel {

	protected FormComponent formComponent = null;

	public FormPanel(String id) {
		super(id);
	}

	public FormPanel(String id, IModel<?> model) {
		super(id, model);
	}

    //some comment to test hg plugin
	public FormComponent getFormComponent() {
		return formComponent;
	}

}