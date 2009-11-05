package org.jbei.ice.web.forms;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

public class TextFieldPanel extends FormPanel {
	public TextFieldPanel(String id) {
		super(id);
	}
	
	public TextFieldPanel(String id, PropertyModel propertyModel) {
		super(id);
		formComponent = new TextField("textField", propertyModel);
		add(formComponent);
		
	}
	
	
}
