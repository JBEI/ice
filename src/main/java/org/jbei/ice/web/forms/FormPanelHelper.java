package org.jbei.ice.web.forms;

import java.util.LinkedHashMap;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;

public class FormPanelHelper {
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
