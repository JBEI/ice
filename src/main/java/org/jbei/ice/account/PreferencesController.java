package org.jbei.ice.account;

import org.jbei.ice.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.dto.search.SearchBoostField;
import org.jbei.ice.dto.user.PreferenceKey;
import org.jbei.ice.dto.user.UserPreferences;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.PreferencesDAO;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Controller for managing user preferences.
 *
 * @author Hector Plahar
 */
public class PreferencesController {

    private final PreferencesDAO dao;
    private final AccountDAO accountDAO;

    public PreferencesController() {
        dao = DAOFactory.getPreferencesDAO();
        accountDAO = DAOFactory.getAccountDAO();
    }

    public HashMap<PreferenceKey, String> retrieveAccountPreferences(AccountModel account, ArrayList<PreferenceKey> keys) {
        HashMap<PreferenceKey, String> preferences = new HashMap<>();
        List<Preference> results = dao.getAccountPreferences(account, keys);

        for (Preference preference : results) {
            PreferenceKey key = PreferenceKey.fromString(preference.getKey());
            // bulk upload preferences are shared with user preferences. user preferences have underscores
            // bulk uploads do not.
            if (key == null) {
                continue;
            }
            preferences.put(key, preference.getValue());
        }

        return preferences;
    }

    public UserPreferences getUserPreferences(String requester, long userId) {
        AccountDAO accountDAO = DAOFactory.getAccountDAO();
        AccountModel account = accountDAO.getByEmail(requester);
        AccountModel requestedAccount = accountDAO.get(userId);

        if (account == null || requestedAccount == null)
            return null;

        if (account.getType() != AccountType.ADMIN && !requester.equalsIgnoreCase(requestedAccount.getEmail()))
            return null;

        ArrayList<PreferenceKey> keys = new ArrayList<>();
        keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
        keys.add(PreferenceKey.FUNDING_SOURCE);

        List<Preference> preferences = dao.getAccountPreferences(requestedAccount, keys);
        if (preferences == null)
            return null;

        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setUserId(requestedAccount.getEmail());
        for (Preference preference : preferences) {
            userPreferences.getPreferences().add(preference.toDataTransferObject());
        }
        return userPreferences;
    }

    public String getPreferenceValue(String userId, String preferenceKey) {
        AccountModel account = accountDAO.getByEmail(userId);
        if (account == null)
            return null;

        Preference preference = dao.getPreference(account, preferenceKey);
        if (preference == null)
            return null;

        return preference.getValue();
    }

    public HashMap<String, String> retrieveUserPreferenceList(AccountModel account, List<SearchBoostField> fields) {
        HashSet<String> values = new HashSet<>();
        for (SearchBoostField field : fields)
            values.add("BOOST_" + field.name());
        return dao.retrievePreferenceValues(account, values);
    }

    public Preference createPreference(AccountModel account, String key, String value) {
        Preference preference = new Preference(account, key.toUpperCase(), value);
        return dao.create(preference);
    }

    public PreferenceInfo updatePreference(String requesterEmail, long userId, String key, String value) {
        if (value == null)
            return null;
        AccountModel account = DAOFactory.getAccountDAO().get(userId);   // todo : check permissions
        Preference preference = dao.createOrUpdatePreference(account, key, value);
        return preference.toDataTransferObject();
    }
}
