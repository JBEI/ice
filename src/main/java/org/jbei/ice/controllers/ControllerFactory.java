package org.jbei.ice.controllers;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.search.SearchController;

/**
 * Factory for retrieving the available controllers for the application
 *
 * @author Hector Plahar
 */
public class ControllerFactory {

    private static PermissionsController permissionsController;
    private static AccountController accountController;
    private static AttachmentController attachmentController;
    private static SampleController sampleController;
    private static SequenceAnalysisController sequenceAnalysisController;
    private static SequenceController sequenceController;
    private static EntryController entryController;
    private static GroupController groupController;
    private static StorageController storageController;
    private static FolderController folderController;
    private static BulkUploadController bulkUploadController;
    private static ConfigurationController configurationController;
    private static PreferencesController preferencesController;
    private static SearchController searchController;

    public static PermissionsController getPermissionController() {
        if (permissionsController == null)
            permissionsController = new PermissionsController();
        return permissionsController;
    }

    public static AccountController getAccountController() {
        if (accountController == null) {
            accountController = new AccountController();
        }
        return accountController;
    }

    public static AttachmentController getAttachmentController() {
        if (attachmentController == null)
            attachmentController = new AttachmentController();
        return attachmentController;
    }

    public static SampleController getSampleController() {
        if (sampleController == null)
            sampleController = new SampleController();
        return sampleController;
    }

    public static SequenceAnalysisController getSequenceAnalysisController() {
        if (sequenceAnalysisController == null)
            sequenceAnalysisController = new SequenceAnalysisController();
        return sequenceAnalysisController;
    }

    public static SequenceController getSequenceController() {
        if (sequenceController == null)
            sequenceController = new SequenceController();
        return sequenceController;
    }

    public static EntryController getEntryController() {
        if (entryController == null)
            entryController = new EntryController();
        return entryController;
    }

    public static GroupController getGroupController() {
        if (groupController == null)
            groupController = new GroupController();
        return groupController;
    }

    public static StorageController getStorageController() {
        if (storageController == null)
            storageController = new StorageController();
        return storageController;
    }

    public static FolderController getFolderController() {
        if (folderController == null)
            folderController = new FolderController();
        return folderController;
    }

    public static BulkUploadController getBulkUploadController() {
        if (bulkUploadController == null)
            bulkUploadController = new BulkUploadController();
        return bulkUploadController;
    }

    public static ConfigurationController getConfigurationController() {
        if (configurationController == null)
            configurationController = new ConfigurationController();
        return configurationController;
    }

    public static PreferencesController getPreferencesController() {
        if (preferencesController == null)
            preferencesController = new PreferencesController();
        return preferencesController;
    }

    public static SearchController getSearchController() {
        if (searchController == null)
            searchController = new SearchController();
        return searchController;
    }

}
