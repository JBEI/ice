package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ApiKey;

import java.util.List;

/**
 * @author Hector Plahar
 */
public class ApiKeyDAO extends HibernateRepository<ApiKey> {

    @Override
    public ApiKey get(long id) throws DAOException {
        return super.get(ApiKey.class, id);
    }

    public List<ApiKey> getApiKeysForUser(String userId, int limit, int start, boolean asc) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ApiKey.class.getName());
            criteria.add(Restrictions.eq("ownerEmail", userId));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(start);
            return criteria.list();
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


