package org.jbei.ice.lib.managers;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;

public class AccountManager extends Manager {
	public static AccountPreferences getAccountPreferences(int id) throws ManagerException {
		AccountPreferences accountPreferences = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from AccountPreferences where id = :id");
			query.setEntity("id", id);

			accountPreferences = (AccountPreferences) query.uniqueResult();
		} catch (Exception e) {
			String msg = "Could not get AccountPreferences by id";
			Logger.error(msg);
			throw new ManagerException(msg);
		}

		return accountPreferences;
	}

	public static AccountPreferences getAccountPreferences(Account account)
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

	public static AccountPreferences save(
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
		return get(7);
	}

	public static Account get(int id) throws ManagerException {
		Account account = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from Account where id = :id");
			query.setParameter("id", id);

			account = (Account) query.uniqueResult();
		} catch (HibernateException e) {
			Logger.warn("Couldn't retrieve Account by id");
			throw new ManagerException("Couldn't retrieve Account by id: "
					+ String.valueOf(id), e);
		}

		return account;
	}
	
	public static Account getByLogin(String login) throws ManagerException {
		Account account = null;
		try {
			Query query = HibernateHelper.getSession().createQuery(
					"from Account where email = :email");
			query.setParameter("email", login);
			account = (Account) query.uniqueResult();
				
		} catch (HibernateException e) {
			Logger.warn("Couldn't retrieve Account by email");
			throw new ManagerException("Couldn't retrieve Account by email: "
					+ login);
		}
		
		return account;
	}
	
	public static Account save(Account account) throws ManagerException {
		try {
			Account result = (Account) dbSave(account);
			return result;
			
		} catch (Exception e) {
			String msg = "Could not save account " + account.getEmail();
			Logger.error(msg);
			throw new ManagerException(msg);
		}
	}
		
}