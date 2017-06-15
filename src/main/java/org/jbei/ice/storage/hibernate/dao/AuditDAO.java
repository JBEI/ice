package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Audit;
import org.jbei.ice.storage.model.Entry;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Accessor for {@link Audit} objects
 *
 * @author Hector Plahar
 */
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
            if (sort == null)
                sort = "id";
            CriteriaQuery<Audit> query = getBuilder().createQuery(Audit.class);
            Root<Audit> from = query.from(Audit.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            query.orderBy(asc ? getBuilder().asc(from.get(sort)) : getBuilder().desc(from.get(sort)));
            return currentSession().createQuery(query).setFirstResult(offset).setMaxResults(limit).list();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int getAuditsForEntryCount(Entry entry) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Audit> from = query.from(Audit.class);
            query.select(getBuilder().countDistinct(from.get("id"))).where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).uniqueResult().intValue();
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public int deleteAll(Entry entry) {
        try {
            CriteriaDelete<Audit> query = getBuilder().createCriteriaDelete(Audit.class);
            Root<Audit> from = query.from(Audit.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).executeUpdate();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
