package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

import java.util.List;

/**
 * Accessor for {@link Audit} objects
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class AuditDAO extends HibernateRepository<Audit> {

    /**
     * Retrieves audit referenced by unique identifier
     *
     * @param id unique identifier for audit class
     * @return Audit object if one is found with identifier
     */
    @Override
    public Audit get(long id) {
        return super.get(Audit.class, id);
    }

    public List<Audit> getAuditsForEntry(Entry entry, int limit, int offset, boolean asc, String sort) {
        try {
            Criteria criteria = currentSession().createCriteria(Audit.class)
                    .add(Restrictions.eq("entry", entry));
            criteria.setMaxResults(limit);
            criteria.setFirstResult(offset);
            return criteria.list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getHistoryCount(Entry entry) {
        Number itemCount = (Number) currentSession().createCriteria(Audit.class)
                .setProjection(Projections.countDistinct("id"))
                .add(Restrictions.eq("entry", entry)).uniqueResult();
        if (itemCount != null)
            return itemCount.intValue();
        return 0;
    }

    public int deleteAll(Entry entry) {
        try {
            Session session = currentSession();
            Query query = session.createQuery("delete from " + Audit.class.getName() + " where entry=:entry");
            query.setParameter("entry", entry);
            return query.executeUpdate();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
