package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.dataProviders.GroupsDataProvider;

public class GroupsDataViewPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public GroupsDataViewPanel(String id) {
		super(id);
		
		GroupsDataView groupsDataView = new GroupsDataView("groups_data_view", new GroupsDataProvider());
		add(groupsDataView);
		add(new JbeiPagingNavigator("navigator", groupsDataView));
	}
}
