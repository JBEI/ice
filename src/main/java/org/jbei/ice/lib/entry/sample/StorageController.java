package org.jbei.ice.lib.entry.sample;

import java.util.List;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.Utils;

/**
 * ABI to manipulate {@link Storage}.
 *
 * @author Hector Plahar
 */
public class StorageController {

    private final StorageDAO dao;

    public StorageController() {
        dao = new StorageDAO();
    }

    /**
     * Retrieve {@link Storage} object by its name, index, type and the parent id from the database.
     *
     * @param name
     * @param index
     * @param type
     * @param parentId
     * @return Storage object.
     * @throws ControllerException
     */
    public Storage retrieveStorageBy(String name, String index, StorageType type, long parentId)
            throws ControllerException {
        try {
            return dao.retrieveStorageBy(name, index, type, parentId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Storage retrieveByUUID(String uuid) throws ControllerException {
        try {
            return dao.get(uuid);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    /**
     * Retrieve {@link Storage} that are schemas from the database.
     *
     * @return List of {@link Storage} objects that are schemas.
     * @throws ControllerException
     */
    public List<Storage> retrieveAllStorageSchemes() throws ControllerException {
        try {
            return dao.getAllStorageSchemes();
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Retrieve a {@link Storage} object from the database by the bar code from the database.
     *
     * @param barcode
     * @return Storage.
     * @throws ControllerException
     */
    public Storage retrieveStorageTube(String barcode) throws ControllerException {
        try {
            return dao.retrieveStorageTube(barcode);
        } catch (DAOException me) {
            throw new ControllerException(me);
        }
    }

    /**
     * Update the {@link Storage} object in the database.
     *
     * @param storage
     * @return Saved storage.
     * @throws ControllerException
     */
    public Storage update(Storage storage) throws ControllerException {
        try {
            return dao.update(storage);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Save the {@link Storage} object in the database.
     *
     * @param storage
     * @return Saved storage.
     * @throws ControllerException
     */
    public Storage save(Storage storage) throws ControllerException {
        try {
            if (storage.getUuid() == null || storage.getUuid().isEmpty()) {
                String uuid = Utils.generateUUID();
                storage.setUuid(uuid);
            }

            return dao.save(storage);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Storage getLocation(Storage strainScheme, String[] labels) throws ControllerException {
        try {
            return dao.getLocation(strainScheme, labels);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Storage get(long id, boolean fetchChildren) throws ControllerException {
        try {
            return dao.get(id, fetchChildren);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public List<Storage> getStorageSchemesForEntryType(String entryType) throws ControllerException {
        try {
            return dao.getStorageSchemesForEntryType(entryType);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    void delete(Storage storage) throws ControllerException {
        try {
            dao.delete(storage);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}
