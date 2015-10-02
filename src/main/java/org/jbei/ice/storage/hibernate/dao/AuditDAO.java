package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

import java.util.ArrayList;

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

    public ArrayList<Audit> getAuditsForEntry(Entry entry) {
        try {
            Query query = currentSession().createQuery("from " + Audit.class.getName() + " where entry=:entry");
            query.setParameter("entry", entry);
            return new ArrayList<Audit>(query.list());
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
}
