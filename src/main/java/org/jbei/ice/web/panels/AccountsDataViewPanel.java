package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.dataProviders.UserAccountsDataProvider;

public class AccountsDataViewPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private UserAccountsDataView userAccountsDataView;

	public AccountsDataViewPanel(String id) {
		super(id);
		
		UserAccountsDataProvider dataProvider = new UserAccountsDataProvider();
		userAccountsDataView = new UserAccountsDataView("userAccountsDataView", dataProvider, null);
		add(userAccountsDataView);
		add(new JbeiPagingNavigator("navigator", userAccountsDataView));
	}
}
