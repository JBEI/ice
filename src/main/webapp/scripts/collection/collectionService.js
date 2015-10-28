'use strict';

angular.module('ice.collection.service', [])
    .factory('FolderSelection', function ($rootScope, Util) {
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
                    // TODO : folder selected, not collection
                    return;
                }

                selectedCollection = collection;
                selectedFolder = undefined;
                $rootScope.$emit("CollectionSelected", collection);
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
;