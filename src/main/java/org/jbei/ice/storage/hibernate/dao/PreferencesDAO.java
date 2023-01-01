package org.jbei.ice.storage.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.HibernateException;
import org.jbei.ice.dto.user.PreferenceKey;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Preference;

import java.util.*;

/**
 * Data accessor for working with preference objects
 *
 * @author Hector Plahar
 */
public class PreferencesDAO extends HibernateRepository<Preference> {

    public List<Preference> getAccountPreferences(AccountModel account, List<PreferenceKey> keys) {
        ArrayList<String> keyString = new ArrayList<>();
        for (PreferenceKey key : keys)
            keyString.add(key.name());

        try {
            CriteriaQuery<Preference> query = getBuilder().createQuery(Preference.class);
            Root<Preference> from = query.from(Preference.class);
            query.where(getBuilder().equal(from.get("account"), account), from.get("key").in(keyString));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Preference getPreference(AccountModel account, String key) {
        try {
            CriteriaQuery<Preference> query = getBuilder().createQuery(Preference.class);
            Root<Preference> from = query.from(Preference.class);
            query.where(
                    getBuilder().equal(from.get("account"), account),
                    getBuilder().equal(from.get("key"), key.toUpperCase())
            );
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public HashMap<String, String> retrievePreferenceValues(AccountModel account, HashSet<String> keys) {
        try {
            CriteriaQuery<Preference> query = getBuilder().createQuery(Preference.class);
            Root<Preference> from = query.from(Preference.class);
            query.where(getBuilder().equal(from.get("account"), account), from.get("key").in(keys));
            List<Preference> result = currentSession().createQuery(query).list();

            Iterator iterator = result.iterator();
            HashMap<String, String> results = new HashMap<>();
            while (iterator.hasNext()) {
                Preference preference = (Preference) iterator.next();
                if (keys.contains(preference.getKey().toUpperCase())) {
                    results.put(preference.getKey().toUpperCase().trim(), preference.getValue().trim());
                }
            }
            return results;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Preference createOrUpdatePreference(AccountModel account, String key, String value) {
        try {
            CriteriaQuery<Preference> query = getBuilder().createQuery(Preference.class);
            Root<Preference> from = query.from(Preference.class);
            query.where(
                    getBuilder().equal(from.get("account"), account),
                    getBuilder().equal(from.get("key"), key.toUpperCase())
            );
            Optional<Preference> optional = currentSession().createQuery(query).uniqueResultOptional();
            if (optional.isPresent()) {
                Preference preference = optional.get();
                if (preference.getValue().equalsIgnoreCase(value))
                    return preference;
                currentSession().update(preference);
                return preference;
            } else {
                return this.create(new Preference(account, key, value));
            }
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public Preference get(long id) {
        return super.get(Preference.class, id);
    }
}
