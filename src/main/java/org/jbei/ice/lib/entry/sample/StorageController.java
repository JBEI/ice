package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.entry.EntryType;

import java.util.ArrayList;
import java.util.List;

/**
 * ABI to manipulate {@link Storage}.
 *
 * @author Hector Plahar
 */
public class StorageController {

    public static final String DEFAULT_PLASMID_STORAGE_SCHEME_NAME = "Plasmid Storage (Default)";
    public static final String DEFAULT_STRAIN_STORAGE_SCHEME_NAME = "Strain Storage (Default)";
    public static final String DEFAULT_PART_STORAGE_SCHEME_NAME = "Part Storage (Default)";
    public static final String DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME = "Arabidopsis Storage (Default)";

    private final StorageDAO dao;
    private final ConfigurationController configurationController;

    public StorageController() {
        dao = new StorageDAO();
        configurationController = ControllerFactory.getConfigurationController();
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

    public void createStrainStorageRoot() throws ControllerException {
        Configuration strainRootConfig = configurationController.getConfiguration(ConfigurationKey.STRAIN_STORAGE_ROOT);
        if (strainRootConfig != null)
            return;

        Storage strainRoot = new Storage("Strain Storage Root", "Default Strain Storage Root",
                StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
        strainRoot = save(strainRoot);
        configurationController.setPropertyValue(ConfigurationKey.STRAIN_STORAGE_ROOT, strainRoot.getUuid());

        Storage defaultStrain = new Storage(DEFAULT_STRAIN_STORAGE_SCHEME_NAME,
                DEFAULT_STRAIN_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                AccountController.SYSTEM_ACCOUNT_EMAIL, strainRoot);
        ArrayList<Storage> schemes = new ArrayList<>();
        schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
        schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
        schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
        defaultStrain.setSchemes(schemes);
        defaultStrain = save(defaultStrain);
        configurationController.setPropertyValue(ConfigurationKey.STRAIN_STORAGE_DEFAULT, defaultStrain.getUuid());
    }

    public void createPlasmidStorageRoot() throws ControllerException {
        Configuration config = configurationController.getConfiguration(ConfigurationKey.PLASMID_STORAGE_ROOT);
        if (config != null)
            return;

        Storage root = new Storage("Plasmid Storage Root", "Default Plasmid Storage Root",
                StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
        root = save(root);
        configurationController.setPropertyValue(ConfigurationKey.PLASMID_STORAGE_ROOT, root.getUuid());

        Storage storage = new Storage(DEFAULT_PLASMID_STORAGE_SCHEME_NAME,
                DEFAULT_PLASMID_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                AccountController.SYSTEM_ACCOUNT_EMAIL, root);
        ArrayList<Storage> schemes = new ArrayList<>();
        schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
        schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
        schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
        storage.setSchemes(schemes);
        storage = save(storage);
        configurationController.setPropertyValue(ConfigurationKey.PLASMID_STORAGE_DEFAULT, storage.getUuid());
    }

    public void createPartStorageRoot() throws ControllerException {
        Configuration config = configurationController.getConfiguration(ConfigurationKey.PART_STORAGE_ROOT);
        if (config != null)
            return;

        Storage root = new Storage("Part Storage Root", "Default Part Storage Root",
                StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
        root = save(root);
        configurationController.setPropertyValue(ConfigurationKey.PART_STORAGE_ROOT, root.getUuid());

        Storage storage = new Storage(DEFAULT_PART_STORAGE_SCHEME_NAME,
                DEFAULT_PART_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                AccountController.SYSTEM_ACCOUNT_EMAIL, root);
        ArrayList<Storage> schemes = new ArrayList<>();
        schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
        schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
        schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
        storage.setSchemes(schemes);
        storage = save(storage);
        configurationController.setPropertyValue(ConfigurationKey.PART_STORAGE_DEFAULT, storage.getUuid());
    }

    public void createSeedStorageRoot() throws ControllerException {
        Configuration config = configurationController.getConfiguration(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT);
        if (config != null)
            return;

        Storage root = new Storage("Part Storage Root", "Default Part Storage Root",
                StorageType.GENERIC, AccountController.SYSTEM_ACCOUNT_EMAIL, null);
        root = save(root);
        configurationController.setPropertyValue(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT, root.getUuid());

        Storage storage = new Storage(DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME,
                DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                AccountController.SYSTEM_ACCOUNT_EMAIL, root);
        ArrayList<Storage> schemes = new ArrayList<>();
        schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
        schemes.add(new Storage("Box", "", StorageType.BOX_INDEXED, "", null));
        schemes.add(new Storage("Tube Number", "", StorageType.WELL, "", null));
        schemes.add(new Storage("Tube Barcode", "", StorageType.TUBE, "", null));
        storage.setSchemes(schemes);
        storage = save(storage);
        configurationController.setPropertyValue(ConfigurationKey.ARABIDOPSIS_STORAGE_DEFAULT, storage.getUuid());
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
        EntryType type = EntryType.nameToType(entryType);
        String uuid = null;
        if (type == null)
            return null;

        switch (type) {
            case STRAIN:
            default:
                uuid = configurationController.getPropertyValue(ConfigurationKey.STRAIN_STORAGE_ROOT);
                break;

            case PLASMID:
                uuid = configurationController.getPropertyValue(ConfigurationKey.PLASMID_STORAGE_ROOT);
                break;

            case PART:
                uuid = configurationController.getPropertyValue(ConfigurationKey.PART_STORAGE_ROOT);
                break;

            case ARABIDOPSIS:
                uuid = configurationController.getPropertyValue(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT);
                break;
        }

        try {
            return dao.getStorageSchemesForEntryType(uuid);
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
