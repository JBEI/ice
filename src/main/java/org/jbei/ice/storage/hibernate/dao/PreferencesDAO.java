package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Data accessor for working with preference objects
 *
 * @author Hector Plahar
 */
public class PreferencesDAO extends HibernateRepository<Preference> {

    @SuppressWarnings("unchecked")
    public ArrayList<Preference> getAccountPreferences(Account account, ArrayList<PreferenceKey> keys)
            throws DAOException {
        Session session = currentSession();
        ArrayList<String> keyString = new ArrayList<>();
        for (PreferenceKey key : keys)
            keyString.add(key.name());

        try {
            Criteria criteria = session.createCriteria(Preference.class)
                                       .add(Restrictions.eq("account", account))
                                       .add(Restrictions.in("key", keyString));
            return new ArrayList<Preference>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Preference getPreference(Account account, String key) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Preference.class)
                                   .add(Restrictions.eq("account", account))
                                   .add(Restrictions.eq("key", key.toUpperCase()));
        try {
            return (Preference) criteria.uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public HashMap<String, String> retrievePreferenceValues(Account account, HashSet<String> keys) throws DAOException {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Preference.class)
                                   .add(Restrictions.eq("account", account));
        Iterator iterator = criteria.list().iterator();
        HashMap<String, String> results = new HashMap<>();
        while (iterator.hasNext()) {
            Preference preference = (Preference) iterator.next();
            if (keys.contains(preference.getKey().toUpperCase())) {
                results.put(preference.getKey().toUpperCase().trim(), preference.getValue().trim());
            }
        }

        return results;
    }

    public Preference createOrUpdatePreference(Account account, String key, String value) throws DAOException {
        Criteria criteria = currentSession().createCriteria(Preference.class)
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("key", key));
        Preference preference = (Preference) criteria.uniqueResult();
        if (preference == null) {
            preference = new Preference(account, key, value);
            return create(preference);
        }

        preference.setValue(value);
        return update(preference);
    }

    @Override
    public Preference get(long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
