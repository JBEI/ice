package org.jbei.ice.lib.managers;

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

public class GroupManager {

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
            session.close();
        }

        return result;
    }

    public static Group get(int id) throws ManagerException {
        Group result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from Group where id = :id");
            query.setInteger("id", id);
            result = (Group) query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Group by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            session.close();
        }

        return result;
    }

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
            session.close();
        }
        return groups;
    }

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

    public static Group create(String label, String description, Group parent)
            throws ManagerException {
        String uuid = java.util.UUID.randomUUID().toString();
        return create(uuid, label, description, parent);

    }

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

    public static void main(String[] args) {
        try {

            /*
            Group g = create("Test", "test group", null);
            String uuid = g.getUuid();
            int id = g.getId();
            Group gotById = getGroup(g.getId());
            Group gotByUuid = getGroup(g.getUuid());
            if (gotById != gotByUuid) {
            	System.out.println("DOH!!!!");
            }
            gotByUuid.setLabel("Test-changed label");
            Group updatedGroup = update(gotByUuid);
            System.out.println("" + g.getId() + "=?" + updatedGroup.getId());
            delete(g);
            Group deletedGroup = getGroup(id);
            if (deletedGroup == null) {
            	System.out.println("OK!");
            }
             */

            /*
            Group jbeiGroup = create("JBEI", "JBEI root group", null);
            Group fuelSynthesis = create("Fuel Synthesis", "Fuel Synthesis group", jbeiGroup);
            */
            get(1);

        } catch (ManagerException e) {
            e.printStackTrace();
        }

    }
}
