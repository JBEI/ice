package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ClientModel;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.RemotePartner;

/**
 * @author Hector Plahar
 */
public class ClientModelDAO extends HibernateRepository<ClientModel> {

    @Override
    public ClientModel get(long id) {
        return super.get(ClientModel.class, id);
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
            Number number = (Number) currentSession().createCriteria(ClientModel.class)
                    .add(Restrictions.eq("group", group))
                    .add(Restrictions.isNotNull("email"))
                    .setProjection(Projections.countDistinct("id"))
                    .uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public ClientModel getModel(String email, RemotePartner remotePartner) throws DAOException {
        try {
            return (ClientModel) currentSession().createCriteria(ClientModel.class)
                    .add(Restrictions.eq("email", email))
                    .add(Restrictions.eq("remotePartner", remotePartner)).uniqueResult();
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }
}
