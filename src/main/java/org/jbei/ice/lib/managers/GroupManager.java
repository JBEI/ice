package org.jbei.ice.lib.managers;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;

/**
 * Manager to manipulate {@link Group} objects.
 * 
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 * 
 */
public class GroupManager {

    /**
     * Retrieve {@link Group} object from the database by its uuid.
     * 
     * @param uuid
     * @return Group object.
     * @throws ManagerException
     */
    public static Group get(String uuid) throws ManagerException {
        Group result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from Group where uuid = :uuid");
            query.setString("uuid", uuid);
            result = (Group) query.uniqueResult();
        } catch (Exception e) {
            String str = "Could not get Group by uuid: " + uuid + " " + e.toString();
            Logger.error(str, e);
            throw new ManagerException(str);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieve {@link Group} object from the database by its id.
     * 
     * @param id
     * @return Group object.
     * @throws ManagerException
     */
    public static Group get(long id) throws ManagerException {
        Group result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from Group where id = :id");
            query.setLong("id", id);
            result = (Group) query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Group by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieve the Everybody {@link Group} object.
     * 
     * @return Everybody Group object.
     * @throws ManagerException
     */
    public static Group getEverybodyGroup() throws ManagerException {
        Group result = null;
        try {
            result = get(PopulateInitialDatabase.everyoneGroup);
            if (result == null) {
                result = PopulateInitialDatabase.createFirstGroup();
            }
        } catch (Exception e) {
            Logger.warn("populating everyoneGroup failed");
        }
        return result;
    }

    /**
     * Retrieve all the {@link Group} objects in the database.
     * 
     * @return SEt of Groups.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public static Set<Group> getAll() throws ManagerException {
        LinkedHashSet<Group> groups = new LinkedHashSet<Group>();
        Session session = DAO.newSession();
        try {
            String queryString = "from Group";
            Query query = session.createQuery(queryString);
            groups.addAll(query.list());
        } catch (HibernateException e) {
            String msg = "Could not retrieve all groups: " + e.toString();
            Logger.warn(msg);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return groups;
    }

    public static Set<Group> getMatchingGroups(String token, int limit) throws ManagerException {
        Session session = DAO.newSession();
        try {
            token = token.toUpperCase();
            String queryString = "from " + Group.class.getName() + " where (UPPER(label) like '%"
                    + token + "%')";
            Query query = session.createQuery(queryString);
            if (limit > 0)
                query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            Logger.error(e);
            throw new ManagerException("Error retrieving matching groups", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Create new {@link Group} object in the database, using parameters.
     * 
     * @param uuid
     * @param label
     * @param description
     * @param parent
     * @return Saved Group object.
     * @throws ManagerException
     */
    public static Group create(String uuid, String label, String description, Group parent)
            throws ManagerException {
        Group g = new Group();
        g.setUuid(uuid);
        g.setLabel(label);
        g.setDescription(description);
        g.setParent(parent);

        Group saved = null;
        try {
            saved = (Group) DAO.save(g);
        } catch (DAOException e) {
            e.printStackTrace();
            String msg = "Could not save group " + label + " to database: " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }
        return saved;

    }

    /**
     * Create a new {@link Group} object in the database, using parameters.
     * 
     * @param label
     * @param description
     * @param parent
     * @return Saved Group object.
     * @throws ManagerException
     */
    public static Group create(String label, String description, Group parent)
            throws ManagerException {
        String uuid = java.util.UUID.randomUUID().toString();
        return create(uuid, label, description, parent);

    }

    /**
     * Update the given {@link Group} object in the database.
     * 
     * @param group
     * @return Saved Group object.
     * @throws ManagerException
     */
    public static Group update(Group group) throws ManagerException {
        try {
            DAO.save(group);
        } catch (DAOException e) {
            e.printStackTrace();
            String msg = "Could not save group " + group.getLabel() + " to database: "
                    + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);

        }
        return group;
    }

    /**
     * Delete the given {@link Group} object in the database.
     * 
     * @param group
     * @throws ManagerException
     */
    public static void delete(Group group) throws ManagerException {
        try {
            DAO.delete(group);
        } catch (DAOException e) {
            e.printStackTrace();
            String msg = "Could not delete group " + group.getUuid() + ": " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }
    }

    /**
     * Save the given {@link Group} object in the database.
     * 
     * @param group
     * @return Saved Group object.
     * @throws ManagerException
     */
    public static Group save(Group group) throws ManagerException {
        Group result = null;
        try {
            result = (Group) DAO.save(group);
        } catch (DAOException e) {
            e.printStackTrace();
            String msg = "Could not save group " + group.getUuid();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
        return result;
    }
}
