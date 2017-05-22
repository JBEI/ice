'use strict';

var adminService = angular.module('ice.admin.service', []);

adminService.factory('AdminSettings', function () {
    var generalSettingKeys = [
        'TEMPORARY_DIRECTORY',
        'DATA_DIRECTORY',
        'PROJECT_NAME',
        'PART_NUMBER_DIGITAL_SUFFIX',
        'PART_NUMBER_DELIMITER',
        'NEW_REGISTRATION_ALLOWED',
        'PROFILE_EDIT_ALLOWED',
        'PASSWORD_CHANGE_ALLOWED',
        'PART_NUMBER_PREFIX',
        'URI_PREFIX',
        'BLAST_INSTALL_DIR'
        //'ADD_TO_CART_DEFAULT_SET_TO_LOCAL'
    ];

    var emailSettingKeys = [
        'ADMIN_EMAIL',
        'BULK_UPLOAD_APPROVER_EMAIL',
        'SEND_EMAIL_ON_ERRORS',
        'ERROR_EMAIL_EXCEPTION_PREFIX'
    ];

    var emailTypeKeys = [
        'EMAILER',
        'GMAIL_APPLICATION_PASSWORD',
        'SMTP_HOST'
    ];

    // indicates which of the keys are boolean
    var booleanKeys = [
        'NEW_REGISTRATION_ALLOWED',
        'PASSWORD_CHANGE_ALLOWED',
        'PROFILE_EDIT_ALLOWED',
        'SEND_EMAIL_ON_ERRORS'
        //'ADD_TO_CART_DEFAULT_SET_TO_LOCAL'
    ];

    var menuOptions = [
        {url: 'scripts/admin/settings.html', display: 'Settings', selected: true, icon: 'fa-cogs'},
        {
            id: 'web',
            url: 'scripts/admin/wor.html',
            display: 'Web of Registries',
            selected: false,
            icon: 'fa-globe',
            description: 'Share/access entries with/on other ICE instances'
        },
        {id: 'users', url: 'scripts/admin/users.html', display: 'Users', selected: false, icon: 'fa-user'},
        {
            id: 'groups',
            url: 'scripts/admin/groups.html',
            display: 'Public Groups',
            selected: false,
            icon: 'fa-group'
        },
        {
            id: 'samples', url: 'scripts/admin/sample-requests.html', display: 'Sample Requests', selected: false,
            icon: 'fa-shopping-cart'
        },
        {
            id: 'annotations-curation',
            url: 'scripts/admin/curation.html',
            display: 'Annotations Curation',
            description: 'Curate existing annotations for automatic sequence annotation',
            selected: false,
            icon: 'fa-language'
        },
        {
            id: 'manuscripts',
            url: 'scripts/admin/manuscripts.html',
            display: 'Editor Tools',
            selected: false,
            icon: 'fa-newspaper-o'
        }
    ];

    return {
        generalSettingKeys: function () {
            return generalSettingKeys;
        },

        getEmailKeys: function () {
            return emailSettingKeys;
        },

        getEmailTypeKeys: function () {
            return emailTypeKeys;
        },

        getBooleanKeys: function () {
            return booleanKeys;
        },

        canAutoInstall: function (key) {
            return key == 'BLAST_INSTALL_DIR'
        },

        getMenuOptions: function () {
            return menuOptions;
        }
    }
});
