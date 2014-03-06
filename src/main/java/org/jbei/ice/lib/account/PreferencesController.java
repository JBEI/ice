package org.jbei.ice.lib.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.Preference;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.search.SearchBoostField;
import org.jbei.ice.lib.shared.dto.user.PreferenceKey;

import org.apache.commons.lang.math.NumberUtils;

/**
 * Controller for managing user preferences.
 *
 * @author Hector Plahar
 */
public class PreferencesController {

    private final PreferencesDAO dao;

    public PreferencesController() {
        dao = new PreferencesDAO();
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
            // bulk upload's do not.
            if (key == null) {
                continue;
            }
            preferences.put(key, preference.getValue());
        }

        return preferences;
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
            for (SearchBoostField field : fields) {
                values.add("BOOST_" + field.name());
            }
            return dao.retrievePreferenceValues(account, values);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    // really an update
    public boolean saveSetting(Account account, String key, String value) throws ControllerException {
        // check if a search boost setting. hack: expecting that search boosts will only use numeric values TODO
        if (NumberUtils.isNumber(value)) {
            try {
                SearchBoostField boostField = SearchBoostField.valueOf(key.toUpperCase());
                if (boostField != null) {
                    key = "BOOST_" + boostField.name();
                }
            } catch (Exception e) {
                Logger.debug(e.getMessage());
            }
        }

        try {
            return dao.saveOrUpdatePreference(account, key, value) != null;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Preference createPreference(Account account, String key, String value) throws ControllerException {
        Preference preference = new Preference(account, key.toUpperCase(), value);
        try {
            return dao.save(preference);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
