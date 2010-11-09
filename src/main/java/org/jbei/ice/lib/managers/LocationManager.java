package org.jbei.ice.lib.managers;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.LocationNew;

public class LocationManager {

    public static LocationNew get(long id) throws ManagerException {
        LocationNew result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + LocationNew.class.getName()
                    + " where id = :id");
            query.setLong("id", id);
            result = (LocationNew) query.uniqueResult();
        } catch (Exception e) {
            String msg = "Could not get Location by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static LocationNew update(LocationNew location) throws ManagerException {
        try {
            DAO.save(location);
        } catch (DAOException e) {
            String msg = "Could not save location: " + location.getName() + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return location;
    }

    public static LocationNew save(LocationNew location) throws ManagerException {
        return update(location);
    }

    public static void delete(LocationNew location) throws ManagerException {
        try {
            DAO.delete(location);
        } catch (DAOException e) {
            String msg = "Could not delete location " + location.getName() + ":" + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }
}
