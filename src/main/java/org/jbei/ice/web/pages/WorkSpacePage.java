package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.jbei.ice.lib.permissions.WorkSpace;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.WorkSpaceTablePanel;

public class WorkSpacePage extends ProtectedPage {
	public WorkSpacePage(PageParameters parameters) {
		super(parameters);
		
		WorkSpace workSpace = ((IceSession)getSession()).getAccountPreferences().getWorkSpace();
		WorkSpaceTablePanel workSpaceTablePanel = new WorkSpaceTablePanel("workSpacePanel", workSpace, 50);
		
		add(workSpaceTablePanel);
		
	}
}