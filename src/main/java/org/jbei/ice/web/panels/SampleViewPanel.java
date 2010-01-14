package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;

public class SampleViewPanel extends Panel {

	public SampleViewPanel(String id, Entry entry) {
		super(id);
		
		add(new Label("test", "here be samples"));
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

}
