package org.jbei.ice.test.managers;

import org.jbei.ice.lib.managers.ConfigurationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.StorageScheme;
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

        Storage plasmidHead = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.DEFAULT_PLASMID_STORAGE_HEAD).getValue());
        StorageScheme plasmidScheme = StorageManager
                .getStorageScheme(PopulateInitialDatabase.DEFAULT_PLASMID_STORAGE_SCHEME_NAME);
        String[] labels1 = { "1", "2", "3", "4" };
        String[] labels2 = { "1", "2", "3", "5" };

        location1 = StorageManager.getLocation(plasmidScheme, labels1, plasmidHead);
        location2 = StorageManager.getLocation(plasmidScheme, labels2, plasmidHead);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

        Storage strainHead = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.DEFAULT_STRAIN_STORAGE_HEAD).getValue());
        StorageScheme strainScheme = StorageManager
                .getStorageScheme(PopulateInitialDatabase.DEFAULT_STRAIN_STORAGE_SCHEME_NAME);

        location1 = StorageManager.getLocation(strainScheme, labels1, strainHead);
        location2 = StorageManager.getLocation(strainScheme, labels2, strainHead);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

        Storage partHead = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.DEFAULT_PART_STORAGE_HEAD).getValue());
        StorageScheme partScheme = StorageManager
                .getStorageScheme(PopulateInitialDatabase.DEFAULT_PART_STORAGE_SCHEME_NAME);
        if (partHead != null) {
            location1 = StorageManager.getLocation(partScheme, labels1, partHead);
            location2 = StorageManager.getLocation(partScheme, labels2, partHead);
            Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());
        }

        Storage arabidopsisHead = StorageManager.get(ConfigurationManager.get(
            ConfigurationKey.DEFAULT_ARABIDOPSIS_STORAGE_HEAD).getValue());
        StorageScheme arabidopsisScheme = StorageManager
                .getStorageScheme(PopulateInitialDatabase.DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME);
        String[] labels3 = { "1", "2", "3" };
        String[] labels4 = { "1", "2", "4" };

        location1 = StorageManager.getLocation(arabidopsisScheme, labels3, arabidopsisHead);
        location2 = StorageManager.getLocation(arabidopsisScheme, labels4, arabidopsisHead);
        Assert.assertTrue(location1.getParent().getId() == location2.getParent().getId());

    }
}
