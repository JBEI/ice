package org.jbei.ice.web.panels;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class RowItemPanel extends Panel {
	protected String name;
	protected String description;
	protected boolean checkBox;
	protected Date date;
	
	public RowItemPanel(String id) {
		super(id);
	}
	
	public RowItemPanel(String id, String name, String description, 
			boolean checkBox, Date date) {
		
		super(id);
		add(new CheckBox("checkBox", new Model(checkBox)));
		add(new Label("name", name));
		add(new Label("description", description));
		
		add(new Label("date", date.toString()));
	}
	
	
	

}
