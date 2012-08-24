package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.WriteGroup;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
public class WriteGroupDAO extends HibernateRepository<WriteGroup> {

    public void removeWriteGroup(Entry entry, Group group) throws DAOException {

        String queryString = "delete " + WriteGroup.class.getName() + " where entry_id = :entry and group_id = :group";
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
            String msg = "Could not remove write group \"" + group.getLabel() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Set write permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.WriteGroup} objects using the given {@link
     * Group}s.
     *
     * @param entry  Entry to give permission to.
     * @param groups Groups to give write permission to.
     * @throws DAOException
     */
    public void setWriteGroup(Entry entry, Set<Group> groups) throws DAOException {
        String queryString = "delete " + WriteGroup.class.getName() + " where entry_id = :entry";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.executeUpdate();
            session.getTransaction().commit();

            for (Group group : groups) {
                WriteGroup writeGroup = new WriteGroup(entry, group);
                super.save(writeGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set WriteGroup of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve {@link Group}s with write permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public Set<Group> getWriteGroups(Entry entry) throws DAOException {

        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(WriteGroup.class)
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
            Criteria criteria = session.createCriteria(WriteGroup.class)
                                       .add(Restrictions.eq("entry", entry))
                                       .add(Restrictions.in("group", groups))
                                       .setProjection(Projections.rowCount());
            return ((Integer) criteria.uniqueResult()) > 0;

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}
