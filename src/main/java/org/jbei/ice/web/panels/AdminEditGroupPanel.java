package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Group;

public class AdminEditGroupPanel extends Panel {
 
	private static final long serialVersionUID = 1L;
	private final Group group;

	public AdminEditGroupPanel(String id, Group group) {
		super(id);
		this.group = group;
	}

}
