package org.jbei.ice.web.panels.adminpage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.panels.AccountsDataViewPanel;

public class EditUserAccountPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Panel panel;	
	private Label headerLinkLabel;
	private Label headerLabel;
	private AjaxLink<Object> headerLink;
	
	public EditUserAccountPanel(String id) {
		super(id);
		
		// defaults used when page is first loaded
		panel = new AccountsDataViewPanel("userEditPanel");
		panel.setOutputMarkupId(true);
		
		headerLinkLabel = new Label("edit_user_header_text", "Add New User");
		headerLinkLabel.setOutputMarkupId(true);
		
		headerLink = new AddUserLink();
		headerLink.add(headerLinkLabel);
		
		headerLabel = new Label("current_panel_header", "All User Accounts");
		headerLabel.setOutputMarkupId(true);
		
		add(panel);
		add(headerLink);
		add(headerLabel);
		add(new TextField<String>("accountFilter"));
	}
	
	private class AddUserLink extends AjaxLink<Object> {

		private static final long serialVersionUID = 1L;

		public AddUserLink() {
			super("edit_user_header_link");
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			
			Panel newPanel = new AdminAccountUpdatePanel("userEditPanel");
			newPanel.setOutputMarkupId(true);
			panel.replaceWith(newPanel);
			panel = newPanel;
			
			// change link to "show all"
			Label newLabel = new Label("edit_user_header_text", "Show All Accounts");
			newLabel.setOutputMarkupId(true);
			headerLinkLabel.replaceWith(newLabel);
			headerLinkLabel = newLabel;
			
			ShowAllAccountsLink showAllAccounts = new ShowAllAccountsLink();
			showAllAccounts.setOutputMarkupId(true);
			showAllAccounts.add(newLabel);
			headerLink.replaceWith(showAllAccounts);
			headerLink = showAllAccounts;
			
			// change header label
			Label newHeaderLabel = new Label("current_panel_header", "New User Registration");
			newHeaderLabel.setOutputMarkupId(true);
			headerLabel.replaceWith(newHeaderLabel);
			headerLabel = newHeaderLabel;
				
			target.addComponent(showAllAccounts);
			target.addComponent(newLabel);
			target.addComponent(newPanel);
			target.addComponent(newHeaderLabel);
		}
	}
	
	private class ShowAllAccountsLink extends AjaxLink<Object> {
		
		private static final long serialVersionUID = 1L;

		public ShowAllAccountsLink() {
			super("edit_user_header_link");
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			
			Panel newPanel = new AccountsDataViewPanel("userEditPanel");
			newPanel.setOutputMarkupId(true);
			panel.replaceWith(newPanel);
			panel = newPanel;
			
			// change link to "show all"
			Label newLabel = new Label("edit_user_header_text", "Add New User");
			newLabel.setOutputMarkupId(true);
			headerLinkLabel.replaceWith(newLabel);
			headerLinkLabel = newLabel;
			
			AddUserLink addUserLink = new AddUserLink();
			addUserLink.setOutputMarkupId(true);
			addUserLink.add(newLabel);
			headerLink.replaceWith(addUserLink);
			headerLink = addUserLink;
			
			// change header label
			Label newHeaderLabel = new Label("current_panel_header", "All User Accounts");
			newHeaderLabel.setOutputMarkupId(true);
			headerLabel.replaceWith(newHeaderLabel);
			headerLabel = newHeaderLabel;
				
			target.addComponent(addUserLink);
			target.addComponent(newLabel);
			target.addComponent(newPanel);
			target.addComponent(newHeaderLabel);
		}
	}
}
