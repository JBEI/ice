package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.StorageScheme;
import org.jbei.ice.lib.utils.Utils;

public class StorageManager {

    public static Storage get(long id) throws ManagerException {
        Storage result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from " + Storage.class.getName() + " where id = :id");
            query.setLong("id", id);
            result = (Storage) query.uniqueResult();
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

    public static Storage get(String uuid) throws ManagerException {
        Storage result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " where uuid = :uuid");
            query.setString("uuid", uuid);
            result = (Storage) query.uniqueResult();
        } catch (Exception e) {
            String msg = "Could not get Location by uuid: " + uuid + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    public static Storage update(Storage location) throws ManagerException {
        if (location == null) {
            return null;
        }

        if (location.getUuid() == null) {
            location.setUuid(Utils.generateUUID());
        }
        try {
            DAO.save(location);
        } catch (DAOException e) {
            String msg = "Could not save location: " + location.getName() + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return location;
    }

    public static Storage save(Storage location) throws ManagerException {
        return update(location);
    }

    public static void delete(Storage location) throws ManagerException {
        try {
            DAO.delete(location);
        } catch (DAOException e) {
            String msg = "Could not delete location " + location.getName() + ":" + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    // storage scheme methods

    public static StorageScheme getStorageScheme(long id) throws ManagerException {
        StorageScheme result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + StorageScheme.class.getName()
                    + " where id = :id");
            query.setLong("id", id);
            result = (StorageScheme) query.uniqueResult();
        } catch (Exception e) {
            String msg = "Could not get StorageScheme by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    public static StorageScheme getStorageScheme(String label) throws ManagerException {
        StorageScheme result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + StorageScheme.class.getName()
                    + " where label = :label");
            query.setString("label", label);
            result = (StorageScheme) query.uniqueResult();
        } catch (Exception e) {
            String msg = "Could not get StorageScheme by label: " + label + " " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<StorageScheme> getAllStorageSchemes() throws ManagerException {
        ArrayList<StorageScheme> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + StorageScheme.class.getName());

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                result = (ArrayList<StorageScheme>) list;
            }
        } catch (Exception e) {
            String msg = "Could not get all schemes " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    public static StorageScheme update(StorageScheme scheme) throws ManagerException {
        try {
            DAO.save(scheme);
        } catch (DAOException e) {
            String msg = "Could not save location: " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        }

        return scheme;
    }

    public static void delete(StorageScheme scheme) throws ManagerException {
        try {
            DAO.delete(scheme);
        } catch (DAOException e) {
            String msg = "Could not delete storage scheme " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    public static Storage getLocation(StorageScheme scheme, String[] labels, Storage head)
            throws ManagerException {
        Storage result = null;
        Storage parent = head;
        List<Storage> schemes = scheme.getSchemes();

        if (schemes.size() != labels.length) {
            throw new ManagerException("Storage Scheme and label lengths are not equal");
        }
        try {
            for (int i = 0; i < labels.length; i++) {
                result = getOrCreateChildLocation(schemes.get(i), labels[i], parent);
                parent = result;
            }
        } catch (ManagerException e) {
            String msg = "Could not retrieve child " + e.toString();
            Logger.error(msg);
            // ok to return null on fail
        }

        return result;
    }

    private static Storage getOrCreateChildLocation(Storage template, String itemLabel,
            Storage parent) throws ManagerException {
        Storage result = null;
        parent = StorageManager.get(parent.getId());

        if (parent == null) {
            return null;
        }
        Set<Storage> children = parent.getChildren();
        String constructedName = template.getName() + " " + itemLabel;

        for (Storage child : children) {
            if (constructedName.equals(child.getName())) {
                result = child;
                break;
            }
        }

        if (result == null) {
            result = new Storage();
            result.setName(constructedName);
            result.setParent(parent);
            result.setStorageType(template.getStorageType());
            result.setOwnerEmail(parent.getOwnerEmail());
            result = StorageManager.save(result);
        }
        return result;
    }

}
