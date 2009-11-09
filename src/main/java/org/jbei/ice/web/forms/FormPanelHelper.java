package org.jbei.ice.web.forms;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.FormComponent;

public class FormPanelHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String name;
	protected FormPanel panel;
	
	public String getName() {
		return name;
	}

	public FormPanel getPanel() {
		return panel;
	}
	
	public FormComponent getFormComponent() {
		return panel.getFormComponent();
	}
	
	public FormPanelHelper(String name, FormPanel panel) {
		this.name = name;
		this.panel = panel;
	}
	
	
}
