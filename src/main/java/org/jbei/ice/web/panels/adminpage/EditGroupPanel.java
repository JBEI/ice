package org.jbei.ice.web.panels.adminpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.GroupsDataViewPanel;

public class EditGroupPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Panel panel;
	private Label addGroupText;
	private Label groupHeaderLabel;
	private AjaxFallbackLink<Object> headerLink;

	public EditGroupPanel(String id) {
		super(id);
		
		panel = new GroupsDataViewPanel("admin_group_selection");
		panel.setOutputMarkupId(true);
		
		headerLink = new AddNewGroupLink();			
		addGroupText = new Label("add_new_group_text", "Add New Group");
		addGroupText.setOutputMarkupId(true);
		headerLink.add(addGroupText);
		
		groupHeaderLabel = new Label("group_edit_header", "All Groups");
		groupHeaderLabel.setOutputMarkupId(true);
		
		add(headerLink);
		add(groupHeaderLabel);
		add(panel);
	}
	
	private class AddNewGroupLink extends AjaxFallbackLink<Object> {
			
		private static final long serialVersionUID = 1L;

		protected AddNewGroupLink() {
			super("add_new_group_link");
		}
		
		@Override
		public void onClick(AjaxRequestTarget target) {
			Panel newPanel = new AdminUpdateGroupPanel("admin_group_selection");
			newPanel.setOutputMarkupId(true);
			panel.replaceWith(newPanel);
			panel = newPanel;
			
			Label newLabel = new Label("add_new_group_text", "Show Groups");
			newLabel.setOutputMarkupId(true);
			addGroupText.replaceWith(newLabel);
			addGroupText = newLabel;
						
			ShowAllGroupsLink showAllGroups = new ShowAllGroupsLink();
			showAllGroups.setOutputMarkupId(true);
			showAllGroups.add(newLabel);
			headerLink.replaceWith(showAllGroups);
			headerLink = showAllGroups;
			
			Label newHeaderLabel = new Label("group_edit_header", "Add New Group");
			newHeaderLabel.setOutputMarkupId(true);			
			groupHeaderLabel.replaceWith(newHeaderLabel);
			groupHeaderLabel = newHeaderLabel;
			
			target.addComponent(newHeaderLabel);
			target.addComponent(showAllGroups);
			target.addComponent(newLabel);
			target.addComponent(newPanel);
		}
	}
	
	private class ShowAllGroupsLink extends AjaxFallbackLink<Object> {

		private static final long serialVersionUID = 1L;

		public ShowAllGroupsLink() {
			super("add_new_group_link");
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			Panel showGroupsPanel = new GroupsDataViewPanel("admin_group_selection");
			showGroupsPanel.setOutputMarkupId(true);
			panel.replaceWith(showGroupsPanel);
			panel = showGroupsPanel;
			
			Label newLabel = new Label("add_new_group_text", "Add New Group"); 
			newLabel.setOutputMarkupId(true);
			addGroupText.replaceWith(newLabel);
			addGroupText = newLabel;
			
			AddNewGroupLink newLink = new AddNewGroupLink();
			newLink.setOutputMarkupId(true);
			newLink.add(newLabel);
			headerLink.replaceWith(newLink);
			headerLink = newLink;
			
			Label newGroupHeader = new Label("group_edit_header", "All Groups");
			newGroupHeader.setOutputMarkupId(true);
			groupHeaderLabel.replaceWith(newGroupHeader);
			groupHeaderLabel = newGroupHeader;
						
			target.addComponent(newGroupHeader);
			target.addComponent(showGroupsPanel);
			target.addComponent(newLabel);
			target.addComponent(newLink);
		}
	}
}
