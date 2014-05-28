package org.jbei.ice.lib.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.Preference;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AccountDAO;
import org.jbei.ice.lib.dao.hibernate.PreferencesDAO;
import org.jbei.ice.lib.dto.bulkupload.PreferenceInfo;
import org.jbei.ice.lib.dto.search.SearchBoostField;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.lib.dto.user.UserPreferences;

/**
 * Controller for managing user preferences.
 *
 * @author Hector Plahar
 */
public class PreferencesController {

    private final PreferencesDAO dao;

    public PreferencesController() {
        dao = DAOFactory.getPreferencesDAO();
    }

    public HashMap<PreferenceKey, String> retrieveAccountPreferences(Account account, ArrayList<PreferenceKey> keys)
            throws ControllerException {
        HashMap<PreferenceKey, String> preferences = new HashMap<>();
        ArrayList<Preference> results;
        try {
            results = dao.getAccountPreferences(account, keys);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

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
        Account account = accountDAO.getByEmail(requester);
        Account requestedAccount = accountDAO.get(userId);

        if (account == null || requestedAccount == null)
            return null;

        if (account.getType() != AccountType.ADMIN && !requester.equalsIgnoreCase(requestedAccount.getEmail()))
            return null;

        ArrayList<PreferenceKey> keys = new ArrayList<>();
        keys.add(PreferenceKey.PRINCIPAL_INVESTIGATOR);
        keys.add(PreferenceKey.FUNDING_SOURCE);

        ArrayList<Preference> preferences = dao.getAccountPreferences(requestedAccount, keys);
        if (preferences == null)
            return null;

        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setUserId(requestedAccount.getEmail());
        for (Preference preference : preferences) {
            userPreferences.getPreferences().add(preference.toDataTransferObject());
        }
        return userPreferences;
    }

    /**
     * Retrieves preference with the exact key value pair for specified account
     *
     * @param account owning account
     * @param key     unique key identifier. Typically one of {@link PreferenceKey}
     * @param value   value associated with key
     * @return retrieved preference object
     * @throws ControllerException
     */
    public Preference retrievePreference(Account account, String key, String value) throws ControllerException {
        try {
            return dao.retrievePreference(account, key, value);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public HashMap<String, String> retrieveUserPreferenceList(Account account, List<SearchBoostField> fields)
            throws ControllerException {
        try {
            HashSet<String> values = new HashSet<>();
            for (SearchBoostField field : fields)
                values.add("BOOST_" + field.name());
            return dao.retrievePreferenceValues(account, values);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    // really an update
    public boolean saveSetting(Account account, String key, String value) throws ControllerException {
        try {
            return dao.createOrUpdatePreference(account, key, value) != null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Preference createPreference(Account account, String key, String value) throws ControllerException {
        Preference preference = new Preference(account, key.toUpperCase(), value);
        try {
            return dao.create(preference);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public PreferenceInfo updatePreference(String requesterEmail, long userId, String key, String value) {
        if (value == null)
            return null;
        Account account = DAOFactory.getAccountDAO().get(userId);   // todo : check permissions
        Preference preference = dao.createOrUpdatePreference(account, key, value);
        return preference.toDataTransferObject();
    }
}
