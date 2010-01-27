package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserPreferences;

public class UserPreferencesService {
    public UserPreferences fetchUserPreferences(String authToken) {
        UserPreferences userPreferences = null;
        try {
            Account account = AccountManager.getAccountByAuthToken(authToken);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getPreferences() != null
                    && !accountPreferences.getPreferences().isEmpty()) {
                try {
                    System.out.println(accountPreferences.getPreferences());

                    userPreferences = (UserPreferences) SerializationUtils
                            .deserializeFromString(accountPreferences.getPreferences());
                } catch (SerializationUtils.SerializationUtilsException e) {
                    e.printStackTrace();
                }
            } else {
                userPreferences = new UserPreferences();
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        return userPreferences;
    }

    public void saveUserPreferences(String authToken, UserPreferences preferences) {
        try {
            Account account = AccountManager.getAccountByAuthToken(authToken);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            String serializedPreferences = "";
            try {
                serializedPreferences = SerializationUtils.serializeToString(preferences);
            } catch (SerializationUtils.SerializationUtilsException e) {
                e.printStackTrace();
            }

            if (accountPreferences != null) {
                accountPreferences.setPreferences(serializedPreferences);

                AccountManager.save(accountPreferences);
            } else {
                AccountManager.save(new AccountPreferences(account, serializedPreferences, ""));
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }
    }
}
