package org.jbei.ice.lib.group;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

/**
 * Manager to manipulate {@link Group} objects.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
class GroupDAO extends HibernateRepository {

    /**
     * Retrieve {@link Group} object from the database by its uuid.
     * 
     * @param uuid
     * @return Group object.
     * @throws ManagerException
     */
    public Group get(String uuid) throws DAOException {
        return (Group) super.getByUUID(Group.class, uuid);
    }

    /**
     * Retrieve {@link Group} object from the database by its id.
     * 
     * @param id
     * @return Group object.
     * @throws ManagerException
     */
    public Group get(long id) throws DAOException {
        return (Group) super.get(Group.class, id);
    }

    /**
     * Retrieve all the {@link Group} objects in the database.
     * 
     * @return SEt of Groups.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public Set<Group> getAll() throws DAOException {
        LinkedHashSet<Group> groups = new LinkedHashSet<Group>();
        Session session = DAO.newSession();
        try {
            String queryString = "from Group";
            Query query = session.createQuery(queryString);
            groups.addAll(query.list());
        } catch (HibernateException e) {
            String msg = "Could not retrieve all groups: " + e.toString();
            Logger.warn(msg);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return groups;
    }

    public Set<Group> getMatchingGroups(String token, int limit) throws DAOException {
        Session session = DAO.newSession();
        session.beginTransaction();
        try {
            token = token.toUpperCase();
            String queryString = "from " + Group.class.getName() + " where (UPPER(label) like '%"
                    + token + "%')";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            session.getTransaction().commit();
            return result;

        } catch (Exception e) {
            Logger.error(e);
            session.getTransaction().rollback();
            throw new DAOException("Error retrieving matching groups", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Update the given {@link Group} object in the database.
     * 
     * @param group
     * @return Saved Group object.
     * @throws ManagerException
     */
    public Group update(Group group) throws DAOException {
        return (Group) super.saveOrUpdate(group);
    }

    /**
     * Delete the given {@link Group} object in the database.
     * 
     * @param group
     * @throws ManagerException
     */
    public void delete(Group group) throws DAOException {
        super.delete(group);
    }

    /**
     * Save the given {@link Group} object in the database.
     * 
     * @param group
     * @return Saved Group object.
     * @throws ManagerException
     */
    public Group save(Group group) throws DAOException {
        return (Group) super.saveOrUpdate(group);
    }
}
