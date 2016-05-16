'use strict';

angular.module('ice.profile.service', [])
    .factory('ProfileService', function () {
        return {
            profileMenuOptions: function () {
                return [
                    {
                        url: 'scripts/profile/profile-information.html',
                        display: 'Profile',
                        selected: true,
                        icon: 'fa-user',
                        open: true
                    },
                    {
                        id: 'prefs',
                        url: 'scripts/profile/preferences.html',
                        display: 'Settings',
                        selected: false,
                        icon: 'fa-cog'
                    },
                    {
                        id: 'groups',
                        url: 'scripts/profile/groups.html',
                        display: 'Private Groups',
                        selected: false,
                        icon: 'fa-group'
                    },
                    {
                        id: 'messages',
                        url: 'scripts/profile/messages.html',
                        display: 'Messages',
                        selected: false,
                        icon: 'fa-envelope-o'
                    },
                    {
                        id: 'samples',
                        url: 'scripts/profile/samples.html',
                        display: 'Samples',
                        selected: false,
                        icon: 'fa-shopping-cart'
                    },
                    {
                        id: 'entries',
                        url: 'scripts/profile/entries.html',
                        display: 'Entries',
                        selected: false,
                        icon: 'fa-th-list',
                        open: true
                    },
                    {
                        id: 'api-keys',
                        url: 'scripts/profile/api-keys.html',
                        display: 'API Keys',
                        selected: false,
                        icon: 'fa-key'
                    }
                ];
            },

            preferenceEntryDefaults: function () {
                return [
                    {display: "Principal Investigator", id: "PRINCIPAL_INVESTIGATOR", help: "Enter Email or Name"},
                    {display: "Funding Source", id: "FUNDING_SOURCE"}
                ]
            }
        }
    })
;
