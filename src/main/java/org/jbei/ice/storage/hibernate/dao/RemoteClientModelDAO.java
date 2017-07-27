package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.RemoteClientModel;
import org.jbei.ice.storage.model.RemotePartner;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class RemoteClientModelDAO extends HibernateRepository<RemoteClientModel> {

    @Override
    public RemoteClientModel get(long id) {
        return super.get(RemoteClientModel.class, id);
    }

    /**
     * Retrieves clients belonging to specified group
     *
     * @param group group whose members are to be retrieved
     * @return remote clients that have been added to the specified group
     */
    public List<RemoteClientModel> getClientsForGroup(Group group) {
        try {
            CriteriaQuery<RemoteClientModel> query = getBuilder().createQuery(RemoteClientModel.class);
            Root<RemoteClientModel> from = query.from(RemoteClientModel.class);
            Join<RemoteClientModel, Group> groups = from.join("groups");
            query.where(getBuilder().equal(groups.get("id"), group.getId()), getBuilder().isNotNull(from.get("email")));
            return currentSession().createQuery(query).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Retrieves number of clients for the specified group
     *
     * @param group group whose clients are of interest
     * @return number of clients for group
     * @throws DAOException
     */
    public int getClientCount(Group group) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<RemoteClientModel> from = query.from(RemoteClientModel.class);
            Join<RemoteClientModel, Group> groups = from.join("groups");
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(getBuilder().equal(groups.get("id"), group.getId()), getBuilder().isNotNull(from.get("email")));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public RemoteClientModel getModel(String email, RemotePartner remotePartner) {
        try {
            CriteriaQuery<RemoteClientModel> query = getBuilder().createQuery(RemoteClientModel.class);
            Root<RemoteClientModel> from = query.from(RemoteClientModel.class);
            query.where(
                    getBuilder().equal(from.get("email"), email),
                    getBuilder().equal(from.get("remotePartner"), remotePartner)
            );
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
