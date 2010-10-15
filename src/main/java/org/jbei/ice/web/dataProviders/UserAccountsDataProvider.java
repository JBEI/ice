package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.web.common.ViewException;

public class UserAccountsDataProvider extends SortableDataProvider<Account> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public Iterator<Account> iterator(int first, int count) {
		
		try {
			List<Account> accountList = new ArrayList<Account>(AccountController.getAllByFirstName());
			
			// TODO sort (if such functionality is needed)
			//Collections.sort( accountList, comparator ) ;
			
			return accountList.subList(first, first+count).iterator();
		} catch (ControllerException e) {
			throw new ViewException(e);
		}
	}

	@Override
	public int size() {
		try {
			return AccountController.getAllByFirstName().size();
		} catch (ControllerException e) {
			throw new ViewException(e);
		}
	}

	@Override
	public IModel<Account> model(Account object) {
		return new Model<Account>(object);
	}
}
