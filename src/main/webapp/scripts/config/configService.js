'use strict';

angular.module('ice.config.service', [])
    .factory("Configuration", function () {
        const options = [
            {
                id: 'dataDir',
                url: 'scripts/config/config-data-directory.html',
                active: false
            },
            {
                id: 'database',
                url: 'scripts/admin/wor.html',
            },
            {
                id: 'users',
                url: 'scripts/admin/users.html',
                active: false,
                icon: 'fa-user'
            }];

        return {
            getOptions: function () {
                return options;
            }
        }
    });
