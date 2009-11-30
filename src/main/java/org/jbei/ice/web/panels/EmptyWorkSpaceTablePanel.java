package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class EmptyWorkSpaceTablePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public EmptyWorkSpaceTablePanel(String id) {
		super(id);
		add(new Label("msg", "Your workspace is empty! Try adding parts to your workspace."));
	}

}
