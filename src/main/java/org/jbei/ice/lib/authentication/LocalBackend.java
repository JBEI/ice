package org.jbei.ice.lib.authentication;

import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;

public class LocalBackend implements IAuthenticationBackend {
	public Account authenticate(String userId, String password) {
		Account account = null;

		try {
			account = AccountManager.getByEmail(userId);

			if ((account == null)
					|| (!account.getPassword().equals(
							AccountManager.encryptPassword(password)))) {
				return null;
			}
		} catch (ManagerException e) {
			e.printStackTrace();
		}

		return account;
	}
}
