package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.ManuscriptModel;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class ManuscriptModelDAO extends HibernateRepository<ManuscriptModel> {

    @Override
    public ManuscriptModel get(long id) {
        return super.get(ManuscriptModel.class, id);
    }

    public int getTotalCount(String filter) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<ManuscriptModel> from = query.from(ManuscriptModel.class);
            if (filter != null && !filter.trim().isEmpty()) {
                query.where(getBuilder().like(getBuilder().lower(from.get("title")), "%" + filter.toLowerCase() + "%"));
            }
            query.select(getBuilder().countDistinct(from.get("id")));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<ManuscriptModel> list(String sort, boolean asc, int offset, int size, String filter) {
        try {
            CriteriaQuery<ManuscriptModel> query = getBuilder().createQuery(ManuscriptModel.class);
            Root<ManuscriptModel> from = query.from(ManuscriptModel.class);
            if (filter != null && !filter.trim().isEmpty()) {
                query.where(getBuilder().like(getBuilder().lower(from.get("title")), "%" + filter.toLowerCase() + "%"));
            }
            query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(size).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
