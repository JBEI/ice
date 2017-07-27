package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ApiKey;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

/**
 * Data Accessor object for retrieving {@link ApiKey} objects
 *
 * @author Hector Plahar
 */
public class ApiKeyDAO extends HibernateRepository<ApiKey> {

    @Override
    public ApiKey get(long id) {
        return super.get(ApiKey.class, id);
    }

    /**
     * Retrieves list of available api keys for specified user, using the paging parameters
     *
     * @param userId unique identifier for user whose keys are being retrieved
     * @param sort   sort field
     * @param limit  paging limit
     * @param start  paging start
     * @param asc    sort field ascending
     * @return list of available matching keys
     */
    public List<ApiKey> getApiKeysForUser(String userId, String sort, int limit, int start, boolean asc) {
        try {
            CriteriaQuery<ApiKey> query = getBuilder().createQuery(ApiKey.class);
            Root<ApiKey> from = query.from(ApiKey.class);
            query.where(getBuilder().equal(from.get("ownerEmail"), userId))
                    .orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)))
                    .distinct(true);
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves all available api keys restricted by the paging parameters
     *
     * @param sort  sort field
     * @param limit paging limit
     * @param start paging start
     * @param asc   sort field ascending
     * @return list of available matching keys
     */
    public List<ApiKey> getAllApiKeys(String sort, int limit, int start, boolean asc) {
        try {
            CriteriaQuery<ApiKey> query = getBuilder().createQuery(ApiKey.class);
            Root<ApiKey> from = query.from(ApiKey.class);
            query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setFirstResult(start).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves the maximum number of api keys that can be retrieved for a specified user
     *
     * @param userId unique user identifier
     * @return number of available api keys that match the restrictions
     */
    public long getApiKeysCount(String userId) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<ApiKey> from = query.from(ApiKey.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            if (userId != null)
                query.where(getBuilder().equal(from.get("ownerEmail"), userId)).distinct(true);
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves an api key by client id.
     * If one exists it is expected that the client id + the creator id uniquely retrieves it. In other words,
     * the same user cannot have two api keys for the same client
     *
     * @param clientId client identifier for api key
     * @return container that may or may not contain the found key
     * @throws DAOException if more that one api key is found or there is a problem accessing the database
     */
    public Optional<ApiKey> getByClientId(String clientId) {
        try {
            CriteriaQuery<ApiKey> query = getBuilder().createQuery(ApiKey.class);
            Root<ApiKey> from = query.from(ApiKey.class);
            query.where(getBuilder().equal(from.get("clientId"), clientId));
            return currentSession().createQuery(query).uniqueResultOptional();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}


