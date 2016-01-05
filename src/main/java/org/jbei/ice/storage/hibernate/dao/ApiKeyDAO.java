package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ApiKey;

import java.util.List;

/**
 * Data Accessor object for retrieving {@link ApiKey} objects
 *
 * @author Hector Plahar
 */
public class ApiKeyDAO extends HibernateRepository<ApiKey> {

    @Override
    public ApiKey get(long id) throws DAOException {
        return super.get(ApiKey.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<ApiKey> getApiKeysForUser(String userId, String sort, int limit, int start, boolean asc)
            throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ApiKey.class.getName());
            criteria.add(Restrictions.eq("ownerEmail", userId));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            return criteria.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ApiKey> getAllApiKeys(String sort, int limit, int start, boolean asc) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ApiKey.class)
                    .setFirstResult(start)
                    .setMaxResults(limit);
            criteria.addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            return criteria.list();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public long getApiKeysCount(String userId) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ApiKey.class);
            if (userId != null)
                criteria.add(Restrictions.eq("ownerEmail", userId));
            Number number = (Number) criteria.setProjection(Projections.rowCount()).uniqueResult();
            if (number != null)
                return number.longValue();
            return 0l;
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public ApiKey getByClientId(String clientId) throws DAOException {
        try {
            return (ApiKey) currentSession().createCriteria(ApiKey.class.getName())
                    .add(Restrictions.eq("clientId", clientId))
                    .uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }
}


