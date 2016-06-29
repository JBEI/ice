'use strict';

angular.module('ice.collection.service', [])
    .factory('FolderSelection', function ($rootScope) {
        var selectedCollection;
        var selectedFolder;

        return {
            selectFolder: function (folder) {
                selectedFolder = folder;
                $rootScope.$emit("CollectionFolderSelected", folder);
            },

            selectCollection: function (collection) {
                // if user is on /{domain}/folder/{id} and refreshes, selectCollection is triggered
                if (!isNaN(collection)) {
                    return;
                }

                selectedCollection = collection;
                selectedFolder = undefined;
                $rootScope.$emit("CollectionSelected", collection);
            },
            
            getSelectedCollection: function() {
                return selectedCollection;
            },

            canEditSelectedFolder: function () {
                if (selectedCollection === "personal")
                    return true;

                return selectedFolder && selectedFolder.canEdit;
            },

            getSelectedFolder: function () {
                return selectedFolder;
            },

            reset: function () {
                selectedCollection = undefined;
                selectedFolder = undefined;
            }
        }
    })
    .factory('CollectionMenuOptions', function () {
        var collectionList = [
            {
                name: 'available',
                description: '',
                display: 'Featured',
                icon: 'fa-certificate',
                iconOpen: 'fa-certificate dark-orange',
                alwaysVisible: true
            },
            {
                name: 'personal',
                description: 'Personal entries',
                display: 'Personal',
                icon: 'fa-folder',
                iconOpen: 'fa-folder-open dark_blue',
                alwaysVisible: true
            },
            {
                name: 'shared',
                description: 'Folders & Entries shared with you',
                display: 'Shared',
                icon: 'fa-share-alt',
                iconOpen: 'fa-share-alt green',
                alwaysVisible: true
            },
            {
                name: 'drafts',
                description: 'Entries from bulk upload still in progress',
                display: 'Drafts',
                icon: 'fa-pencil',
                iconOpen: 'fa-pencil brown',
                alwaysVisible: true
            },
            {
                name: 'pending',
                description: 'Entries from bulk upload waiting approval',
                display: 'Pending Approval',
                icon: 'fa-moon-o',
                iconOpen: 'fa-moon-o purple',
                alwaysVisible: false
            },
            {
                name: 'deleted',
                description: 'Deleted Entries',
                display: 'Deleted',
                icon: 'fa-trash-o',
                iconOpen: 'fa-trash red',
                alwaysVisible: false
            },
            {
                name: 'transferred',
                description: 'Transferred entries',
                display: 'Transferred',
                icon: 'fa-exchange',
                iconOpen: 'fa-exchange',
                alwaysVisible: false
            }
        ];

        return {
            getCollectionOptions: function () {
                return collectionList;
            }
        }
    })

;