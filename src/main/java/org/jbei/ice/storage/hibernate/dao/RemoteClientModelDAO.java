package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.RemoteClientModel;
import org.jbei.ice.storage.model.RemotePartner;

/**
 * @author Hector Plahar
 */
public class RemoteClientModelDAO extends HibernateRepository<RemoteClientModel> {

    @Override
    public RemoteClientModel get(long id) {
        return super.get(RemoteClientModel.class, id);
    }

    /**
     * Retrieves number of clients for the specified group
     *
     * @param group
     * @return number of clients for group
     * @throws DAOException
     */
    public int getClientCount(Group group) throws DAOException {
        try {
            Number number = (Number) currentSession().createCriteria(RemoteClientModel.class)
                    .add(Restrictions.eq("group", group))
                    .add(Restrictions.isNotNull("email"))
                    .setProjection(Projections.countDistinct("id"))
                    .uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public RemoteClientModel getModel(String email, RemotePartner remotePartner) throws DAOException {
        try {
            return (RemoteClientModel) currentSession().createCriteria(RemoteClientModel.class)
                    .add(Restrictions.eq("email", email))
                    .add(Restrictions.eq("remotePartner", remotePartner)).uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }
}
