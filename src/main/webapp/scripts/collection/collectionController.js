'use strict';

angular.module('ice.collection.controller', [])
    // controller for <ice.menu.collections> directive
    .controller('CollectionMenuController', function ($cookieStore, $scope, $modal, $rootScope, $location, $stateParams, Folders) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();

        //
        // initialize
        //

        // folders contained in the selected folder (default selected to personal)
        $scope.selectedCollectionFolders = undefined;
        $scope.selectedFolder = $stateParams.collection === undefined ? 'personal' : $stateParams.collection;
        $rootScope.$emit("CollectionSelected", $scope.selectedFolder);

        // retrieve collections contained in the selectedFolder
        folders.getByType({folderType:$scope.selectedFolder},
            function (result) {
                $scope.selectedCollectionFolders = result;
            }, function (error) {
                console.error(error);
            });

        // retrieve folder counts
        var updateCounts = function () {
            folders.query(function (result) {
                if (result === undefined || $scope.collectionList === undefined)
                    return;

                for (var i = 0; i < $scope.collectionList.length; i += 1) {
                    var item = $scope.collectionList[i];
                    item.count = result[item.name];
                }
            });
        };
        updateCounts();

        //
        // end initialize
        //

        // Menu count change handler
        $scope.$on("UpdateCollectionCounts", function (event) {
            updateCounts();
        });

        // updates the counts for personal collection to indicate items removed/added
        $scope.updatePersonalCollections = function () {
            var folder = $scope.selectedFolder ? $scope.selectedFolder : "personal";

            folders.getByType({folderType:folder},
                function (result) {
                    if (result) {
                        $scope.selectedCollectionFolders = result;
                    }
                }, function (error) {
                    console.error(error);
                });
        };

        // called from collections-menu-details.html when a collection's folder is selected
        // simply changes state to folder and allows the controller for that to handle it
        $scope.selectCollectionFolder = function (folder) {
            $rootScope.$emit("CollectionFolderSelected", folder);
            // type on server is PUBLIC, PRIVATE, SHARED, UPLOAD
            var type = folder.type.toLowerCase();
            if (type !== "upload")
                type = "folders";

            $location.path("/" + type + "/" + folder.id);
            $scope.folder = undefined;   // this forces "Loading..." to be shown
        };

        //
        // called when a collection is selected. Collections are pre-defined ['Featured', 'Deleted', etc]
        // and some allow folders and when that is selected then the selectCollectionFolder() is called
        //
        $scope.selectCollection = function (name) {
            $rootScope.$emit("CollectionSelected", name);
            $location.path("/folders/" + name);
            $scope.selectedFolder = name;
            $scope.selectedCollectionFolders = undefined;

            // retrieve sub folders for selected collection
            folders.getByType({folderType:$scope.selectedFolder, folderId:name},
                function (result) {
                    $scope.selectedCollectionFolders = result;
                },
                function (error) {
                    console.error(error);
                });
        };

        $scope.currentPath = function (param) {
            if ($stateParams.collection === undefined && param === 'personal')
                return true;
            return $stateParams.collection === param;
        };

        // BulkUploadNameChange handler
        $scope.$on("BulkUploadNameChange", function (event, data) {
            if (data === undefined || $scope.selectedFolder !== "bulkUpload" || $scope.selectedCollectionFolders === undefined) // todo : use vars
                return;

            for (var i = 0; i < $scope.selectedCollectionFolders.length; i += 1) {
                var subFolder = $scope.selectedCollectionFolders[i];
                if (subFolder.id !== data.id)
                    continue;

                $scope.selectedCollectionFolders[i].folderName = data.name;
                break;
            }
        });
    });
