package org.jbei.ice.lib.managers;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;

public class AccountManager extends Manager {
	public static AccountPreferences get(int id) throws ManagerException {
		AccountPreferences accountPreferences = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from AccountPreferences where id = :id");
			query.setEntity("id", id);

			accountPreferences = (AccountPreferences) query.uniqueResult();
		} catch (Exception e) {
			throw new ManagerException("Could not get AccountPreferences by id");
		}

		return accountPreferences;
	}

	public static AccountPreferences getByAccount(Account account)
			throws ManagerException {
		AccountPreferences accountPreferences = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from AccountPreferences where account = :account");
			query.setParameter("account", account);

			accountPreferences = (AccountPreferences) query.uniqueResult();
		} catch (Exception e) {
			throw new ManagerException(
					"Could not get AccountPreferences by account");
		}

		return accountPreferences;
	}

	public static AccountPreferences create(
			AccountPreferences accountPreferences) throws ManagerException {
		AccountPreferences result;
		try {
			result = (AccountPreferences) dbSave(accountPreferences);
		} catch (Exception e) {
			throw new ManagerException(
					"Could not create AccountPreferences in db");
		}
		return result;
	}

	public static Account getAccountByAuthToken(String authToken)
			throws ManagerException {
		return getById(7);
	}

	public static Account getById(int id) throws ManagerException {
		Account account = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from Account where id = :id");
			query.setParameter("id", id);

			account = (Account) query.uniqueResult();
		} catch (HibernateException e) {
			throw new ManagerException("Couldn't retrieve Account by id: "
					+ String.valueOf(id), e);
		}

		return account;
	}
}