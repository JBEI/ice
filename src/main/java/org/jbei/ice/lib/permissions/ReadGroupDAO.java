package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * Hibernate Data accessor object for {@link org.jbei.ice.lib.permissions.model.ReadGroup}
 *
 * @author Hector Plahar
 */
public class ReadGroupDAO extends HibernateRepository<ReadGroup> {

    /**
     * Retrieve {@link Group}s with read permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public Set<Group> getReadGroups(Entry entry) throws DAOException {
        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(ReadGroup.class)
                                       .add(Restrictions.eq("entry", entry))
                                       .setProjection(Projections.property("group"));
            List list = criteria.list();
            return new HashSet<Group>(list);

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean entryHasGroups(Set<Group> groups, Entry entry) throws DAOException {
        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(ReadGroup.class)
                                       .add(Restrictions.eq("entry", entry))
                                       .add(Restrictions.in("group", groups))
                                       .setProjection(Projections.rowCount());
            return ((Integer) criteria.uniqueResult()) > 0;

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public void removeReadGroup(Entry entry, Group group) throws DAOException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry and readGroup.group = :group";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.setEntity("group", group);
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not remove read group \"" + group.getLabel() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Set read permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.ReadGroup} objects using the given {@link
     * Group}s.
     *
     * @param entry  Entry to give permission to.
     * @param groups Groups to give read permission to.
     * @throws DAOException
     */
    public void setReadGroup(Entry entry, Set<Group> groups) throws DAOException {
        String queryString = "delete ReadGroup readGroup where readGroup.entry = :entry";
        Session session = newSession();
        session.getTransaction().begin();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        session.getTransaction().commit();
        try {
            for (Group group : groups) {
                ReadGroup readGroup = new ReadGroup(entry, group);
                super.saveOrUpdate(readGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set Read Group of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
}
