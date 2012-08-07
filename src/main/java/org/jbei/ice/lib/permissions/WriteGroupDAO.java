package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.model.WriteGroup;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Hector Plahar
 */
public class WriteGroupDAO extends HibernateRepository<WriteGroup> {

    public void removeWriteGroup(Entry entry, Group group) throws DAOException {

        String queryString = "delete " + WriteGroup.class.getName() + " where entry = :entry and group = :group";
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
        String queryString = "delete " + WriteGroup.class.getName() + " where entry = :entry";
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
     * Add write permission for the specified {@link Group} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link WriteGroup} object to the database..
     *
     * @param entry Entry to give write permission to.
     * @param group Group to give write permission to.
     * @throws DAOException
     */
    public void addWriteGroup(Entry entry, Group group) throws DAOException {
        Set<Group> groups = getWriteGroup(entry);
        boolean alreadyAdded = false;
        for (Group existingGroup : groups) {
            if (existingGroup.getId() == group.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            groups.add(group);
            setWriteGroup(entry, groups);
        }
    }

    /**
     * Retrieve {@link Group}s with write permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    public Set<Group> getWriteGroup(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select group from " + WriteGroup.class.getName() + " where entry = :entry";
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            String msg = "Could not get Write Group of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            closeSession(session);
        }
    }
}
