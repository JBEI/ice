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
    ];

    var emailSettingKeys = [
        'SMTP_HOST',
        'ADMIN_EMAIL',
        'BULK_UPLOAD_APPROVER_EMAIL',
        'SEND_EMAIL_ON_ERRORS',
        'ERROR_EMAIL_EXCEPTION_PREFIX'
    ];

    return {
        generalSettingKeys: function () {
            return generalSettingKeys;
        },

        getEmailKeys: function () {
            return emailSettingKeys;
        }
    }
});
