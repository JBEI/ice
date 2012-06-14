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
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.EntryType;

/**
 * Manager to manipulate {@link Storage} objects in the database.
 * 
 * @author Timothy Ham, Hector Plahar
 * 
 */
public class StorageManager {

    /**
     * Retrieve {@link Storage} object from the database by its id. Optionally, retrieve children at
     * this time.
     * 
     * @param id
     * @param fetchChildren
     *            True if children are to be fetched.
     * @return Storage object.
     * @throws ManagerException
     */
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

    /**
     * Retrieve {@link Storage} object by its uuid.
     * 
     * @param uuid
     * @return Storage object.
     * @throws ManagerException
     */
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
     * Retrieves {@link Storage} object representing a tube. The 2Dbarcode for a tube is unique
     * across plates so this method is expected to return a single results. Compare to wells in 96
     * well plate that have same type and index across multiple plates
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

        if (results.size() > 1)
            throw new ManagerException("Expecting single result, received \"" + results.size()
                    + "\" for index " + barcode);

        return results.get(0);
    }

    /**
     * Retrieve a {@link Storage} object by its index and {@link StorageType} fields.
     * 
     * @param index
     * @param type
     * @return List of Storage objects.
     * @throws ManagerException
     */
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

    /**
     * Save the given {@link Storage} object in the database.
     * 
     * @param location
     * @return Saved Storage object.
     * @throws ManagerException
     */
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

    /**
     * Save the given {@link Storage} object in the database.
     * 
     * @param location
     * @return Saved Storage object.
     * @throws ManagerException
     */
    public static Storage save(Storage location) throws ManagerException {
        return update(location);
    }

    /**
     * Delete the given {@link Storage} object in the database.
     * 
     * @param location
     * @throws ManagerException
     */
    public static void delete(Storage location) throws ManagerException {
        try {
            DAO.delete(location);
        } catch (DAOException e) {
            String msg = "Could not delete location " + location.getName() + ":" + e.toString();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    /**
     * Retrieve all {@link Storage} objects with non-empty schemes.
     * 
     * @return List of Storage objects with schemes.
     * @throws ManagerException
     */
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

    /**
     * Retrieve all {@link Storage} objects with schemes for a given entryType.n
     * 
     * @param entryType
     * @return List of Storage objects with schemes.
     */
    @SuppressWarnings("unchecked")
    public static List<Storage> getAllStorageRoot() throws ManagerException {
        ArrayList<Storage> result = null;
        Session session = DAO.newSession();
        try {
            Query query = session.createQuery("FROM " + Storage.class.getName()
                    + " storage WHERE storage.storageType = :storageType");
            query.setParameter("storageType", StorageType.GENERIC);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null)
                result = (ArrayList<Storage>) list;

        } catch (Exception e) {
            String msg = "Could not get all generic types: " + e.toString();
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
            EntryType type = EntryType.nameToType(entryType);
            if (type == null)
                return null;

            switch (type) {
            case STRAIN:
                uuid = ConfigurationManager.get(ConfigurationKey.STRAIN_STORAGE_ROOT).getValue();
                break;

            case PLASMID:
                uuid = ConfigurationManager.get(ConfigurationKey.PLASMID_STORAGE_ROOT).getValue();
                break;

            case PART:
                uuid = ConfigurationManager.get(ConfigurationKey.PART_STORAGE_ROOT).getValue();
                break;

            case ARABIDOPSIS:
                uuid = ConfigurationManager.get(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT)
                        .getValue();
                break;
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
            Logger.error(e);
        }
        return result;
    }

    /**
     * Retrieve or create {@link Storage} object with parent hierarchy as specified in the template
     * scheme as specified inside the given {@link Storage} object, with given labels, ordered from
     * parent to child.
     * 
     * @param scheme
     *            {@link Storage} object with the template scheme.
     * @param labels
     *            Text labels, ordered from parent to child.
     * @return Storage object in the database.
     * @throws ManagerException
     */
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
            String msg = "Could not retrieve child ";
            Logger.error(msg, e);
            // ok to return null on fail
        }

        return result;
    }

    /**
     * Retrieve or create a {@link Storage} object as a child of the given {@link Storage} parent
     * object, with the name from the template object, and index itemLabel.
     * 
     * @param template
     * @param itemLabel
     * @param parent
     * @return Storage object.
     * @throws ManagerException
     */
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

    /**
     * Retrieve {@link Storage} object by its name, index, {@link StorageType} and parent id.
     * 
     * @param name
     * @param index
     * @param type
     * @param parentId
     * @return Storage object.
     * @throws ManagerException
     */
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

    /**
     * Retrieve the root level {@link Storage} object with the scheme that is used by the given
     * {@link Storage} object.
     * 
     * @param storage
     * @return Root level Storage object.
     */
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

    /**
     * Retrieve the parent {@link Storage} objects of a given {@link Storage} object, up to, but
     * excluding the Storage object containing the scheme.
     * <p>
     * Useful for getting all the parents of a Storage object, except the scheme containing root
     * object.
     * 
     * @param storage
     * @return parent storage object.
     */
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

    /**
     * Check if the {@link Storage} object given agrees with the scheme as specified by its parents.
     * 
     * @param storage
     * @return True if the scheme is in agreement with the hierarchy.
     */
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
