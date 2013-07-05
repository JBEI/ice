package org.jbei.ice.lib.net;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.logging.Logger;

import org.hibernate.HibernateException;

/**
 * Data Accessor Object for managing {@link RemotePartner} Objects
 *
 * @author Hector Plahar
 */
class RemotePartnerDAO extends HibernateRepository<RemotePartner> {

    @SuppressWarnings("unchecked")
    public ArrayList<RemotePartner> retrieveRegistryPartners() throws DAOException {
        try {
            List list = currentSession().createCriteria(RemotePartner.class).list();
            return new ArrayList<RemotePartner>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
