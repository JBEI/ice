package org.jbei.ice.controllers;

import java.util.List;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.controllers.permissionVerifiers.SamplePermissionVerifier;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;

/**
 * ABI to manipulate {@link Storage}.
 * 
 * @author Hector Plahar
 * 
 */
public class StorageController extends Controller {

    public StorageController(Account account) {
        super(account, new SamplePermissionVerifier());
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
            return StorageManager.retrieveStorageBy(name, index, type, parentId);
        } catch (ManagerException e) {
            throw new ControllerException(e);
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
            return StorageManager.getAllStorageSchemes();
        } catch (ManagerException e) {
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
            return StorageManager.retrieveStorageTube(barcode);
        } catch (ManagerException me) {
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
            return StorageManager.update(storage);
        } catch (ManagerException e) {
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
            return StorageManager.save(storage);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }
}
