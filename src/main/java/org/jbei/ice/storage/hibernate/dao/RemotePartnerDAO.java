package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.RemotePartner;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Accessor Object for managing {@link RemotePartner} Objects
 *
 * @author Hector Plahar
 */
public class RemotePartnerDAO extends HibernateRepository<RemotePartner> {

    @SuppressWarnings("unchecked")
    public List<RemotePartner> getRegistryPartners() throws DAOException {
        try {
            List list = currentSession().createCriteria(RemotePartner.class).list();
            return new ArrayList<>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves remote partners by url. the url is also a unique identifier
     * for a partner
     *
     * @param url partner url to retrieve by
     * @return partner is found, null otherwise
     */
    public RemotePartner getByUrl(String url) throws DAOException {
        try {
            Object object = currentSession().createCriteria(RemotePartner.class.getName())
                    .add(Restrictions.eq("url", url)).uniqueResult();
            if (object == null)
                return null;

            return (RemotePartner) object;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public RemotePartner get(long id) {
        return super.get(RemotePartner.class, id);
    }
}
