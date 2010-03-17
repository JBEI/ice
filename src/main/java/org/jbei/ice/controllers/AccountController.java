package org.jbei.ice.controllers;

import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.AccountPreferencesManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;

public class AccountController {
    public static Account get(int id) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.get(id);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static Set<Account> getAllByFirstName() throws ControllerException {
        Set<Account> accounts = null;

        try {
            accounts = AccountManager.getAllByFirstName();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return accounts;
    }

    public static Account getByEmail(String email) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getByEmail(email);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static Account save(Account account) throws ControllerException {
        Account result = null;

        try {
            result = AccountManager.save(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public static Boolean isModerator(Account account) throws ControllerException {
        Boolean result = false;

        try {
            result = AccountManager.isModerator(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return result;
    }

    public static Boolean isValidPassword(Account account, String password)
            throws ControllerException {
        if (account == null) {
            throw new ControllerException("Failed ot verify password for null Account!");
        }

        Boolean result = false;

        if (account.getPassword().equals(encryptPassword(password))) {
            result = true;
        }

        return result;
    }

    public static Account getAccountByAuthToken(String authToken) throws ControllerException {
        Account account = null;

        try {
            account = AccountManager.getAccountByAuthToken(authToken);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return account;
    }

    public static String encryptPassword(String password) {
        return Utils.encryptMD5(JbeirSettings.getSetting("SECRET_KEY") + password);
    }

    public static AccountPreferences getAccountPreferences(Account account)
            throws ControllerException {
        AccountPreferences accountPreferences;

        try {
            accountPreferences = AccountPreferencesManager.getAccountPreferences(account);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }

        return accountPreferences;
    }

    public static void saveAccountPreferences(AccountPreferences accountPreferences)
            throws ControllerException {
        try {
            AccountPreferencesManager.save(accountPreferences);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }
}
