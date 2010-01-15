package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.permissions.WorkSpace;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.WorkSpaceTablePanel;

public class WorkSpacePage extends ProtectedPage {
	public WorkSpacePage(PageParameters parameters) {
		super(parameters);

		Component workSpaceTablePanel;

		WorkSpace workSpace = ((IceSession) getSession()).getAccountPreferences().getWorkSpace();
		if (workSpace == null) {
			workSpaceTablePanel = new EmptyMessagePanel("workSpacePanel", "Your workspace is empty! Try adding entries into your work space");
		} else {
			workSpaceTablePanel = new WorkSpaceTablePanel("workSpacePanel", workSpace, 50);
		}

		workSpaceTablePanel.setOutputMarkupId(true);

		add(workSpaceTablePanel);
	}
}
