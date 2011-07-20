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

public class StorageController extends Controller {

    public StorageController(Account account) {
        super(account, new SamplePermissionVerifier());
    }

    public Storage retrieveStorageBy(String name, String index, StorageType type, long parentId)
            throws ControllerException {
        try {
            return StorageManager.retrieveStorageBy(name, index, type, parentId);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public List<Storage> retrieveAllStorageSchemes() throws ControllerException {
        try {
            return StorageManager.getAllStorageSchemes();
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public Storage retrieveStorageTube(String barcode) throws ControllerException {
        try {
            return StorageManager.retrieveStorageTube(barcode);
        } catch (ManagerException me) {
            throw new ControllerException(me);
        }
    }

    public Storage update(Storage storage) throws ControllerException {
        try {
            return StorageManager.update(storage);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }

    public Storage save(Storage storage) throws ControllerException {
        try {
            return StorageManager.save(storage);
        } catch (ManagerException e) {
            throw new ControllerException(e);
        }
    }
}
