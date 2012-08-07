package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Hibernate Data accessor object for {@link org.jbei.ice.lib.permissions.model.ReadGroup}
 *
 * @author Hector Plahar
 */
public class ReadGroupDAO extends HibernateRepository<ReadGroup> {

    /**
     * Add read permission for the specified {@link Group} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link ReadGroup} object to the database..
     *
     * @param entry Entry to give read permission to.
     * @param group Group to give read permission to.
     * @throws DAOException
     */
    public void addReadGroup(Entry entry, Group group) throws DAOException {
        Set<Group> groups = getReadGroup(entry);
        boolean alreadyAdded = false;
        for (Group existingGroup : groups) {
            if (existingGroup.getId() == group.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            groups.add(group);
            setReadGroup(entry, groups);
        }
    }

    /**
     * Retrieve {@link Group}s with read permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    public Set<Group> getReadGroup(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select readGroup.group from ReadGroup readGroup where readGroup.entry = :entry";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Read Group of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
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
