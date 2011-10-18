package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.Utils;

public class StorageManager {

    public static Storage get(long id, boolean fetchChildren) throws ManagerException {
        Storage result = null;
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from " + Storage.class.getName() + " where id = :id");
            query.setLong("id", id);
            result = (Storage) query.uniqueResult();
            if (fetchChildren && result != null) {
                result.getChildren().size();
            }
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

    /**
     * Retrieves Storage representing a tube. The 2Dbarcode for a tube is unique across plates so
     * this method is expected to return
     * a single results. Compare to wells in 96 well plate that have same type and index across
     * multiple plates
     * 
     * @param barcode
     *            unique identifier for storage tube
     * @return retrieved Storage
     * @throws ManagerException
     *             on exception
     */
    public static Storage retrieveStorageTube(String barcode) throws ManagerException {
        List<Storage> results = StorageManager.retrieveStorageByIndex(barcode, StorageType.TUBE);

        if (results == null || results.isEmpty()) {
            return null;
        }

        if (results.size() > 1) {
            throw new ManagerException("Expecting single result, received " + results.size());
        }

        return results.get(0);
    }

    @SuppressWarnings("unchecked")
    public static List<Storage> retrieveStorageByIndex(String index, StorageType type)
            throws ManagerException {
        List<Storage> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " where index = :index and storage_type = :type");
            query.setString("index", index);
            query.setString("type", type.name());

            List<Storage> list = query.list();
            if (list != null) {
                result = list;
            }
        } catch (Exception e) {
            String msg = "Could not get Storage by index: " + index + " " + e.toString();
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

    @SuppressWarnings("unchecked")
    public static List<Storage> getAllStorageSchemes() throws ManagerException {
        ArrayList<Storage> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " storage where storage.storageType = :storageType");
            query.setParameter("storageType", StorageType.SCHEME);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                result = (ArrayList<Storage>) list;
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

    @SuppressWarnings("unchecked")
    public static List<Storage> getStorageSchemesForEntryType(String entryType) {
        ArrayList<Storage> result = new ArrayList<Storage>();
        Session session = DAO.newSession();
        try {
            String uuid = null;
            if (Entry.STRAIN_ENTRY_TYPE.equals(entryType)) {
                uuid = ConfigurationManager.get(ConfigurationKey.STRAIN_STORAGE_ROOT).getValue();

            } else if (Entry.PLASMID_ENTRY_TYPE.equals(entryType)) {
                uuid = ConfigurationManager.get(ConfigurationKey.PLASMID_STORAGE_ROOT).getValue();

            } else if (Entry.PART_ENTRY_TYPE.equals(entryType)) {
                uuid = ConfigurationManager.get(ConfigurationKey.PART_STORAGE_ROOT).getValue();

            } else if (Entry.ARABIDOPSIS_SEED_ENTRY_TYPE.equals(entryType)) {
                uuid = ConfigurationManager.get(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT)
                        .getValue();
            }

            if (uuid != null) {
                Storage parent = get(uuid);
                Query query = session
                        .createQuery("from "
                                + Storage.class.getName()
                                + " storage where storage.parent = :parent AND storage.storageType = :storageType");
                query.setParameter("parent", parent);
                query.setParameter("storageType", StorageType.SCHEME);
                result.addAll(query.list());

            }
        } catch (ManagerException e) {
            // log error and pass
            Logger.error(e.toString());
        }
        return result;
    }

    public static Storage getLocation(Storage scheme, String[] labels) throws ManagerException {
        Storage result = null;
        Storage parent = scheme;
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

        Storage result = retrieveStorageBy(template.getName(), itemLabel,
            template.getStorageType(), parent.getId());

        if (result == null) {
            result = new Storage();
            result.setName(template.getName());
            result.setIndex(itemLabel);
            result.setParent(parent);
            result.setStorageType(template.getStorageType());
            result.setOwnerEmail(parent.getOwnerEmail());
            result = StorageManager.save(result);
        }
        return result;
    }

    public static Storage retrieveStorageBy(String name, String index, StorageType type,
            long parentId) throws ManagerException {
        Session session = DAO.newSession();
        try {
            Query query = session
                    .createQuery("from "
                            + Storage.class.getName()
                            + " storage where storage.name = :name and storage.index = :index and storage.storageType = :storageType and parent_id = :parentId");
            query.setString("index", index);
            query.setString("name", name);
            query.setParameter("storageType", type);
            query.setLong("parentId", parentId);

            Storage result = (Storage) query.uniqueResult();

            return result;
        } catch (Exception e) {
            String msg = "Could not retrieve storage " + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static Storage getSchemeContainingParentStorage(Storage storage) {
        if (storage == null) {
            return null;
        }
        Storage result = null;
        Storage current = storage;
        while (true) {
            if (current.getStorageType() == StorageType.SCHEME) {
                result = current;
                break;
            } else {
                current = current.getParent();
                if (current == null) {
                    break;
                }
            }
        }

        return result;
    }

    public static List<Storage> getStoragesUptoScheme(Storage storage) {
        if (storage == null) {
            return null;
        }
        ArrayList<Storage> result = new ArrayList<Storage>();
        Storage current = storage;
        while (current != null && current.getStorageType() != StorageType.SCHEME) {
            result.add(current);
            current = current.getParent();
        }
        return result;
    }

    public static boolean isStorageSchemeInAgreement(Storage storage) {
        boolean result = true;
        if (storage.getStorageType() == StorageType.SCHEME) {
            // should not compare scheme to itself
            return false;
        }
        Storage supposedScheme = StorageManager.getSchemeContainingParentStorage(storage);

        ArrayList<String> schemeNames = new ArrayList<String>();
        for (Storage item : supposedScheme.getSchemes()) {
            schemeNames.add(item.getName());
        }

        ArrayList<String> actualNames = new ArrayList<String>();
        Storage currentStorage = storage;
        while (currentStorage.getId() != supposedScheme.getId()) {
            actualNames.add(currentStorage.getName());
            currentStorage = currentStorage.getParent();
        }
        Collections.reverse(actualNames);

        String schemeName = null;
        String actualName = null;
        if (schemeNames.size() == actualNames.size()) {
            int index = 0;
            while (index < actualNames.size()) {
                schemeName = schemeNames.get(index);
                actualName = actualNames.get(index);
                if (actualName.equals(schemeName)) {
                    index++;
                } else {
                    // name doesn't match
                    result = false;
                    break;
                }
            }
        } else { // sizes don't match
            result = false;
        }
        return result;
    }

}
