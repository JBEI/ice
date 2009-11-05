package org.jbei.ice.web.forms;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class DropDownChoicePanel extends FormPanel {
		
	public DropDownChoicePanel(String id, PropertyModel model, ChoiceRendererHelper choices) {
		super(id);
		formComponent = new DropDownChoice("choiceField", model, choices.getChoiceList(), 
				choices.getChoiceRenderer());
		add(formComponent);
		
	}
}
