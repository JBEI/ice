package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserRestrictionEnzymes;

public class UserRestrictionEnzymesService {
    public UserRestrictionEnzymes fetchUserRestrictionEnzymes(String authToken) {
        UserRestrictionEnzymes userRestrictionEnzymes = null;
        try {
            Account account = AccountManager.getAccountByAuthToken(authToken);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getRestrictionEnzymes() != null
                    && !accountPreferences.getRestrictionEnzymes().isEmpty()) {
                try {
                    userRestrictionEnzymes = (UserRestrictionEnzymes) SerializationUtils
                            .deserializeFromString(accountPreferences.getRestrictionEnzymes());
                } catch (SerializationUtils.SerializationUtilsException e) {
                    e.printStackTrace();
                }
            } else {
                userRestrictionEnzymes = new UserRestrictionEnzymes();
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }

        return userRestrictionEnzymes;
    }

    public void saveUserRestrictionEnzymes(String authToken,
            UserRestrictionEnzymes userRestrictionEnzymes) {
        try {
            Account account = AccountManager.getAccountByAuthToken(authToken);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountManager.getAccountPreferences(account);

            String serializedUserRestrictionEnzymes = "";
            try {
                serializedUserRestrictionEnzymes = SerializationUtils
                        .serializeToString(userRestrictionEnzymes);
            } catch (SerializationUtils.SerializationUtilsException e) {
                e.printStackTrace();
            }

            if (accountPreferences != null) {
                accountPreferences.setRestrictionEnzymes(serializedUserRestrictionEnzymes);

                AccountManager.save(accountPreferences);
            } else {
                AccountManager.save(new AccountPreferences(account, "",
                        serializedUserRestrictionEnzymes));
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }
    }
}
