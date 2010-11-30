package org.jbei.ice.test.managers;

import org.jbei.ice.lib.managers.ConfigurationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.utils.PopulateInitialDatabase;
import org.jbei.ice.lib.utils.UtilityException;
import org.junit.Assert;
import org.junit.Test;

public class TestStorageManager {

    @Test
    public void CrudStorageScheme() throws ManagerException, UtilityException {

        Storage location1 = null;
        Storage location2 = null;

        PopulateInitialDatabase.initializeDatabase();

        Storage plasmidScheme = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.PLASMID_STORAGE_DEFAULT).getValue());
        String[] labels1 = { "1", "2", "3" };
        String[] labels2 = { "1", "2", "4" };

        location1 = StorageManager.getLocation(plasmidScheme, labels1);
        location2 = StorageManager.getLocation(plasmidScheme, labels2);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

        Storage strainScheme = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.STRAIN_STORAGE_DEFAULT).getValue());

        location1 = StorageManager.getLocation(strainScheme, labels1);
        location2 = StorageManager.getLocation(strainScheme, labels2);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

        Storage partScheme = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.PART_STORAGE_DEFAULT).getValue());

        location1 = StorageManager.getLocation(partScheme, labels1);
        location2 = StorageManager.getLocation(partScheme, labels2);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

        Storage arabidopsisScheme = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.PART_STORAGE_DEFAULT).getValue());
        String[] labels3 = { "1", "2", "3" };
        String[] labels4 = { "1", "2", "4" };

        location1 = StorageManager.getLocation(arabidopsisScheme, labels3);
        location2 = StorageManager.getLocation(arabidopsisScheme, labels4);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

    }
}
