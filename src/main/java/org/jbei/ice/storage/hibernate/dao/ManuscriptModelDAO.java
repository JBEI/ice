package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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

    public int getTotalCount(String filter) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ManuscriptModel.class)
                    .setProjection(Projections.rowCount());
            if (filter != null && !filter.trim().isEmpty()) {
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("title", filter, MatchMode.ANYWHERE)));
            }

            Number number = (Number) criteria.uniqueResult();
            return number.intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<ManuscriptModel> list(String sort, boolean asc, int offset, int size, String filter)
            throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(ManuscriptModel.class)
                    .addOrder(asc ? Order.asc(sort) : Order.desc(sort));
            if (filter != null && !filter.trim().isEmpty()) {
                criteria.add(Restrictions.disjunction()
                        .add(Restrictions.ilike("title", filter, MatchMode.ANYWHERE)));
            }
            return criteria.setFirstResult(offset)
                    .setMaxResults(size)
                    .list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
