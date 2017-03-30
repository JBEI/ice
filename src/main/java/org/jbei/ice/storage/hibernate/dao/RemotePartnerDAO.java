package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.RemotePartner;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Data Accessor Object for managing {@link RemotePartner} Objects
 *
 * @author Hector Plahar
 */
public class RemotePartnerDAO extends HibernateRepository<RemotePartner> {

    public List<RemotePartner> getRegistryPartners() {
        try {
            CriteriaQuery<RemotePartner> query = getBuilder().createQuery(RemotePartner.class);
            query.from(RemotePartner.class);
            return currentSession().createQuery(query).list();
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
    public RemotePartner getByUrl(String url) {
        try {
            CriteriaQuery<RemotePartner> query = getBuilder().createQuery(RemotePartner.class);
            Root<RemotePartner> from = query.from(RemotePartner.class);
            query.where(getBuilder().equal(from.get("url"), url));
            Optional<RemotePartner> result = currentSession().createQuery(query).uniqueResultOptional();
            if (result.isPresent())
                return result.get();
            return null;
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
