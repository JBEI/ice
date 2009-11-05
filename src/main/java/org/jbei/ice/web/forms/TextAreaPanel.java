package org.jbei.ice.web.forms;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class TextAreaPanel extends FormPanel {
	public TextAreaPanel(String id) {
		super(id);
	}
	
	public TextAreaPanel(String id, PropertyModel propertyModel) {
		super(id);
		formComponent = new TextArea("textArea", propertyModel);
		add(formComponent);
		
	}
	
}
