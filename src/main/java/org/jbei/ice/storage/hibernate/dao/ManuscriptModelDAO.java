package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ManuscriptModel;

import java.util.List;

/**
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class ManuscriptModelDAO extends HibernateRepository<ManuscriptModel> {

    @Override
    public ManuscriptModel get(long id) {
        return super.get(ManuscriptModel.class, id);
    }

    public List<ManuscriptModel> list(int offset, int size) throws DAOException {
        try {
            return currentSession().createCriteria(ManuscriptModel.class)
                    .setFirstResult(offset)
                    .setMaxResults(size)
                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
