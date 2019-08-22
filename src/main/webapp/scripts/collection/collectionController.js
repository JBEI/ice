'use strict';

angular.module('ice.collection.controller', [])
// controller for <ice.menu.collections> directive
    .controller('CollectionMenuController', function ($cookies, $scope, $uibModal, $rootScope, $location,
                                                      $stateParams, FolderSelection, EntryContextUtil, Util,
                                                      localStorageService) {

        $scope.folderDisplayLimit = 8;

        $scope.adjustCollectionFoldersVisible = function () {
            $scope.folderDisplayLimit = $scope.folderDisplayLimit ? undefined : 8;
        };

        // retrieve (to refresh the information such as part counts) all the sub folders under
        // $scope.selectedFolder (defaults to "personal" if not set)
        $scope.updateSelectedCollectionFolders = function () {
            let folder = $scope.selectedFolder ? $scope.selectedFolder : "personal";
            if (folder === "available")
                folder = "featured";

            Util.list("rest/collections/" + folder.toUpperCase() + "/folders", function (result) {
                $scope.selectedCollectionFolders = result;
            });
        };

        $scope.sortParams = localStorageService.get('collectionFolderSortParams');
        if (!$scope.sortParams) {
            $scope.sortParams = {field: 'creationTime', asc: true};
        }

        $scope.sortCollectionFolders = function () {
            if ($scope.sortParams.field === 'creationTime') {
                if (!$scope.sortParams.asc) {
                    $scope.sortParams.field = 'folderName';
                }
                $scope.sortParams.asc = false;
            } else {
                // sort by name
                if ($scope.sortParams.asc) {
                    $scope.sortParams.field = 'creationTime';
                }
                $scope.sortParams.asc = true;
            }
            localStorageService.set('collectionFolderSortParams', $scope.sortParams);
        };

        //
        // initialize
        //

        // folders contained in the selected folder (default selected to personal)
        $scope.selectedCollectionFolders = undefined;

        // can either be a collection ("shared") or a folder id (34)
        $scope.selectedFolder = $stateParams.collection;

        // retrieve collections contained in the selectedFolder (only if a collection)
        if (isNaN($scope.selectedFolder)) {
            if ($scope.selectedFolder && $scope.selectedFolder.toLowerCase() === "available")
                $scope.selectedFolder = "featured";

            FolderSelection.selectCollection($scope.selectedFolder);
            $scope.updateSelectedCollectionFolders();
        } else {
            // selected folder is a number. folder selected, need collection it is contained in
            Util.get("rest/folders/" + $scope.selectedFolder, function (result) {
                switch (result.type) {
                    case 'PUBLIC':
                        $scope.selectedFolder = "available";
                        break;

                    case 'PRIVATE':
                    default:
                        $scope.selectedFolder = 'personal';
                        break;

                    case 'TRANSFERRED':
                        $scope.selectedFolder = 'transferred';
                        break;

                    case 'SHARED':
                    case 'REMOTE':
                        $scope.selectedFolder = 'shared';
                        break;
                }

                FolderSelection.selectCollection($scope.selectedFolder);
                FolderSelection.selectFolder(result);
                $scope.updateSelectedCollectionFolders();
            });
        }
        //
        // end initialize
        //

        $scope.addCollectionIconClick = function () {
            $scope.$broadcast("ShowCollectionFolderAdd");
        };

        $scope.setQueryCollectionFolderValue = function () {
            $scope.$broadcast("SetShowQueryCollectionFolder");
        };

        // updates the numbers for the collections
        $scope.updateCollectionCounts = function () {
            Util.get("rest/collections/counts", function (result) {
                if (result === undefined || $scope.collectionList === undefined)
                    return;

                for (let i = 0; i < $scope.collectionList.length; i += 1) {
                    let item = $scope.collectionList[i];
                    item.count = result[item.name];
                }
            });
        };

        // Menu count change handler
        $scope.$on("UpdateCollectionCounts", function () {
            $scope.updateCollectionCounts();
        });

        $rootScope.$on("CollectionSelection", function (event, data) {
            $scope.selectCollection(data);
        });

        //
        // called from collections-menu-details.html when a collection's folder is selected
        // simply changes state to folder and allows the controller for that to handle it
        //
        $scope.selectCollectionFolder = function (folder) {

            // type on server is PUBLIC, PRIVATE, SHARED, UPLOAD
            EntryContextUtil.resetContext();
            let type = folder.type.toLowerCase();
            if (type !== "upload") {
                FolderSelection.selectFolder(folder);
                type = "folders";
            }

            $location.path(type + "/" + folder.id);
        };

        //
        // called when a collection is selected. Collections are pre-defined ['Featured', 'Deleted', etc]
        // and some allow folders and when that is selected then the selectCollectionFolder() is called
        //
        $scope.selectCollection = function (name) {
            if (!isNaN(name))
                return;

            EntryContextUtil.resetContext();
            FolderSelection.selectCollection(name);
            $location.search({});
            $location.path("folders/" + name);
            $scope.selectedFolder = name;

            // name and display differ for "Featured". using this till they are reconciled
            if (name === 'available')
                name = 'featured';

            $scope.selectedCollection = name;
            $scope.selectedCollectionFolders = undefined;

            // retrieve sub folders for selected collection
            Util.list("rest/collections/" + $scope.selectedCollection.toUpperCase() + "/folders",
                function (result) {
                    $scope.selectedCollectionFolders = result;
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

            for (let i = 0; i < $scope.selectedCollectionFolders.length; i += 1) {
                let subFolder = $scope.selectedCollectionFolders[i];
                if (subFolder.id !== data.id)
                    continue;

                $scope.selectedCollectionFolders[i].folderName = data.name;
                break;
            }
        });
    })
    .controller('FolderPermissionsController', function ($rootScope, $scope, $http, $uibModalInstance, $cookies,
                                                         Util, folder) {
        $scope.folder = folder;
        $scope.selectedPermission = undefined;
        $scope.newPermission = {canWrite: false, canRead: true, article: 'ACCOUNT', typeId: folder.id};
        $scope.permissions = [];
        $scope.placeHolder = "Enter user name or email";
        $scope.webPartners = [];

        $scope.folder.canSetPublicPermission = undefined;
        Util.get("rest/config/RESTRICT_PUBLIC_ENABLE", function (result) {
            if (!result)
                return;

            $scope.folder.canSetPublicPermission = (result.value.toLowerCase() === "no") || $rootScope.user.isAdmin;
        });

        // retrieve permissions for folder
        Util.list("rest/folders/" + folder.id + "/permissions", function (result) {
            angular.forEach(result, function (perm) {
                let permission = angular.copy(perm);
                permission.canWrite = perm.type === 'WRITE_FOLDER';
                permission.canRead = perm.type === 'READ_FOLDER';
                $scope.permissions.push(permission);
            });
        });

        let getWebPartners = function () {
            Util.list("rest/partners", function (result) {
                $scope.webPartners = result;
            });
        };

        $scope.setPermission = function (isWrite) {
            if (isWrite) {
                // write is clicked
                if (!$scope.newPermission.canRead)
                    return;

                $scope.newPermission.canWrite = !$scope.newPermission.canWrite;
                if ($scope.newPermission.canWrite) {
                    $scope.newPermission.canRead = false;
                }
            } else {
                if (!$scope.newPermission.canWrite)
                    return;

                $scope.newPermission.canRead = !$scope.newPermission.canRead;
                if ($scope.newPermission.canRead) {
                    $scope.newPermission.canWrite = false;
                }
            }
        };

        $scope.closeModal = function () {
            $uibModalInstance.close($scope.permissions);
        };

        $scope.setPermissionArticle = function (type) {
            $scope.newPermission.article = type.toUpperCase();
            $scope.newPermission.articleId = undefined;
            $scope.newPermission.partner = undefined;

            switch (type.toLowerCase()) {
                case "account":
                    $scope.placeHolder = "Enter user name or email";
                    break;

                case "group":
                    $scope.placeHolder = "Enter group name";
                    break;

                case "remote":
                    getWebPartners();
                    $scope.placeHolder = "Enter remote user email";
                    break;
            }
        };

        $scope.setPropagatePermission = function (folder) {
            folder.propagatePermission = !folder.propagatePermission;
            Util.update("rest/folders/" + folder.id, folder, {});
        };

        $scope.disablePermissionAdd = function () {
            if (!$scope.newPermission.article)
                return true;

            switch ($scope.newPermission.article.toLowerCase()) {
                case "account":
                case "group":
                    return !$scope.newPermission.articleId;

                case "remote":
                    return !$scope.selectedPermission || !$scope.newPermission.partner;
            }
            return true;
        };

        $scope.userSelectionForPermissionAdd = function (item) {
            $scope.newPermission.articleId = item.id;
        };

        $scope.removePermission = function (permission) {
            let pid = permission.id;
            if (permission.article === "REMOTE")
                pid = permission.articleId;

            Util.remove("rest/folders/" + folder.id + "/permissions/" + pid, {}, function () {
                let idx = $scope.permissions.indexOf(permission);
                if (idx < 0)
                    return;
                $scope.permissions.splice(idx, 1);
            });
        };

        // converts the permission object to a form the backend expects and
        // adds a new access privilege for the current folder
        $scope.addNewPermission = function () {
            if ($scope.newPermission.canWrite) {
                $scope.newPermission.type = 'WRITE_FOLDER';
            } else {
                $scope.newPermission.type = 'READ_FOLDER';
            }
            $scope.newPermission.typeId = folder.id;

            if ($scope.newPermission.article.toLowerCase() === "remote") {
                $scope.newPermission.userId = $scope.selectedPermission;
            }

            console.log($scope.newPermission);

            Util.post("rest/folders/" + folder.id + "/permissions", $scope.newPermission, function (result) {
                result.canWrite = result.type === 'WRITE_FOLDER';
                result.canRead = result.type === 'READ_FOLDER';
                $scope.permissions.push(result);

                $scope.newPermission.articleId = undefined;
                $scope.newPermission.partner = undefined;
                $scope.selectedPermission = undefined;
            });
        };

        $scope.enableDisablePublicRead = function () {
            if (!$scope.folder.publicReadAccess) {
                //enable
                Util.update("rest/folders/" + folder.id + "/permissions/public", {}, {}, function () {
                    folder.publicReadAccess = true;
                })
            } else {
                // disable
                Util.remove("rest/folders/" + folder.id + "/permissions/public", {}, function () {
                    folder.publicReadAccess = false;
                });
            }
        };

        $scope.filter = function (val) {
            switch ($scope.newPermission.article.toLowerCase()) {
                case "account":
                    return $http.get('rest/users/autocomplete', {
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get("sessionId")},
                        params: {
                            val: val
                        }
                    }).then(function (res) {
                        if (!res || !res.data)
                            return [];

                        let arr = [];
                        let length = res.data.length;
                        for (let i = 0; i < length; i++) {
                            let val = res.data[i];
                            arr.push({id: val.id, label: val.firstName + " " + val.lastName, description: val.email});
                        }
                        return arr;
                    });

                case "group":
                    return $http.get('rest/groups/autocomplete', {
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get("sessionId")},
                        params: {
                            token: val
                        }
                    }).then(function (res) {
                        return res.data;
                    });

                case "remote":
                    return;
            }
        };
    })
    .controller('FolderPartsUploadController', function ($scope, Authentication, FileUploader, Util, folder,
                                                         $uibModalInstance) {
        $scope.serverResult = undefined;
        $scope.processingFile = undefined;
        $scope.progress = 0;
        $scope.checkNames = false;

        // file upload
        let uploader = $scope.partsUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/entries",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        uploader.onAfterAddingFile = function (item) {
            item.url = item.url + "?checkName=" + $scope.checkNames;
        };

        uploader.onProgressItem = function (item, progress) {
            $scope.serverError = undefined;
            $scope.progress = progress;

            if (progress !== "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        };

        uploader.onSuccessItem = function (item, response) {
            $scope.serverResult = {data: response, total: response.length, valid: []};
            for (let i = 0; i < response.length; i += 1) {
                let datum = response[i];
                if (datum.partData) {
                    $scope.serverResult.valid.push(datum.partData.id);
                }
            }
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
        };

        uploader.onErrorItem = function () {
            $scope.serverError = true;
            $scope.processingFile = undefined;
        };

        $scope.setCheckName = function (value) {
            $scope.checkNames = value;
        };

        $scope.addValidEntriesToFolder = function () {
            if (!$scope.serverResult.valid || !$scope.serverResult.valid.length)
                return;

            $scope.serverResult.addingToFolder = true;
            let entrySelection = {
                destination: [folder],
                entries: $scope.serverResult.valid
            };

            Util.update("rest/folders/entries", entrySelection, {}, function (res) {
                if (!res)
                    return;
                $scope.$broadcast("UpdateCollectionCounts");

                console.log(res);
                $uibModalInstance.close(res);
            }, function (failure) {
                console.error(failure);
            });
        }
    })
    .controller('FolderCreateSamplesController', function (Util, $scope, folder, $uibModalInstance, SampleService) {
        $scope.Plate96Rows = SampleService.getPlate96Rows();
        $scope.Plate96Cols = SampleService.getPlate96Cols();

        $scope.submitFolderForSampleCreation = function () {
            $scope.isConflict = false;
            Util.update("rest/folders/" + folder.id + "/SAMPLE", {}, {}, function (result) {
                console.log(result);
                $uibModalInstance.close();
            }, function (error) {
                console.log("error", error);
                $scope.isConflict = (error.status === 409);
            })
        }
    })

    // deals with sub collections e.g. /folders/:id
    // retrieves the contents of folders
    .controller('CollectionFolderController', function ($rootScope, $scope, $location, $uibModal, $stateParams,
                                                        EntryContextUtil, Selection, Util, localStorageService) {
        $rootScope.$emit("CollectionSelection", $stateParams.collection);
        let resource = "collections";

        $scope.folderPageChange = function () {
            $scope.notFound = undefined;
            $scope.loadingPage = true;
            $scope.params.offset = ($scope.params.currentPage - 1) * $scope.params.limit;

            $scope.params.fields = [];
            for (let key in $scope.entryHeaders) {
                if (!$scope.entryHeaders.hasOwnProperty(key))
                    continue;

                let header = $scope.entryHeaders[key];
                if (header.selected) {
                    $scope.params.fields.push(header.field);
                }
            }

            Util.get("rest/" + resource + "/" + $scope.params.folderId + "/entries", function (result) {
                if (resource === "collections") {
                    $scope.folder = {entries: result.data, count: result.resultCount};
                    $scope.params.count = result.resultCount; // used in context display
                } else {
                    // retrieved folders
                    $scope.folder = result;
                    $scope.params.count = result.count;
                    if (result.canEdit)
                        $scope.folderNameTooltip = "Click to rename";
                }
                $scope.loadingPage = false;
            }, $scope.params, function (err) {
                $scope.notFound = true;
                $scope.loadingPage = false;
            });
        };

        //
        // init
        //

        // default entry headers
        $scope.entryHeaders = {
            status: {field: "status", display: "Status", selected: true},
            hasSample: {field: "hasSample", display: "Has Sample", selected: true},
            hasSequence: {field: "hasSequence", display: "Has Sequence", selected: true},
            alias: {field: "alias", display: "Alias"},
            created: {field: "creationTime", display: "Created", selected: true},
            links: {field: "links", display: "Links"}
        };

        // default init params
        $scope.params = {
            'asc': false,
            'sort': 'created',
            currentPage: 1,
            hstep: [15, 30, 50, 100],
            limit: 30
        };

        // get client stored headers
        let storedFields = localStorageService.get('entryHeaderFields');
        if (!storedFields) {
            // set default headers
            let entryHeaderFields = [];
            for (let key in $scope.entryHeaders) {
                if (!$scope.entryHeaders.hasOwnProperty(key))
                    continue;

                let header = $scope.entryHeaders[key];
                if (header.selected) {
                    entryHeaderFields.push(header.field);
                }
            }

            // and store
            localStorageService.set('entryHeaderFields', entryHeaderFields);
        } else {
            // set user selected
            for (let key in $scope.entryHeaders) {
                if (!$scope.entryHeaders.hasOwnProperty(key))
                    continue;

                let header = $scope.entryHeaders[key];
                header.selected = (storedFields.indexOf(header.field) !== -1);
            }
        }

        $scope.maxSize = 5;  // number of clickable pages to show in pagination
        let subCollection = $stateParams.collection;   // folder id or one of the defined collections (Shared etc)   ]
        // retrieve folder contents. all folders are redirected to /folder/{id} which triggers this
        if (subCollection !== undefined) {
            $scope.folder = undefined;
            $scope.params.folderId = subCollection;
            if (isNaN($scope.params.folderId))
                resource = "collections";
            else
                resource = "folders";

            let context = EntryContextUtil.getContext();
            if (context) {
                let pageNum = (Math.floor(context.offset / $scope.params.limit)) + 1;
                $scope.params.sort = context.sort;
                $scope.params.currentPage = pageNum;
            }

            $scope.folderPageChange();
        }

        // custom header selection or de-selection by user
        $scope.selectedHeaderField = function (header, $event) {
            if ($event) {
                $event.preventDefault();
                $event.stopPropagation();
            }
            header.selected = !header.selected;
            let storedFields = localStorageService.get('entryHeaderFields');

            if (header.selected) {
                // selected by user, add to stored list
                storedFields.push(header.field);
                localStorageService.set('entryHeaderFields', storedFields);
            } else {
                // not selected by user, remove from stored list
                let i = storedFields.indexOf(header.field);
                if (i !== -1) {
                    storedFields.splice(i, 1);
                    localStorageService.set('entryHeaderFields', storedFields);
                }
            }

            // refresh data
            $scope.folderPageChange();
        };

        $rootScope.$on("RefreshAfterDeletion", function () {
            $scope.params.currentPage = 1;
            $scope.folderPageChange();
        });

        $scope.sort = function (sortType) {
            $scope.folder = null;
            $scope.params.offset = 0;
            if ($scope.params.sort === sortType)
                $scope.params.asc = !$scope.params.asc;
            else
                $scope.params.asc = false;
            $scope.params.sort = sortType;
            $scope.folderPageChange();
        };

        $scope.hStepChanged = function () {
            $scope.folderPageChange();
        };

        $scope.selectAllClass = function () {
            if (Selection.allSelected() || $scope.folder.entries.length === Selection.getSelectedEntries().length)
                return 'fa-check-square-o';

            if (Selection.hasSelection())
                return 'fa-minus-square';
            return 'fa-square-o';
        };

        $scope.setType = function (type) {
            Selection.setTypeSelection(type);
        };

        $scope.selectAll = function () {
            if (Selection.allSelected())
                Selection.setTypeSelection('none');
            else
                Selection.setTypeSelection('all');
        };

        $scope.isSelected = function (entry) {
            if (Selection.isSelected(entry))
                return true;

            return Selection.searchEntrySelected(entry);
        };

        $scope.select = function (entry) {
            Selection.selectEntry(entry);
        };

        $scope.showEntryDetails = function (entry, index, sub) {
            if (!$scope.params.offset) {
                $scope.params.offset = index;
            }

            let offset = (($scope.params.currentPage - 1) * $scope.params.limit) + index;
            EntryContextUtil.setContextCallback(function (offset, callback) {
                $scope.params.offset = offset;
                $scope.params.limit = 1;

                Util.get("rest/" + resource + "/" + $scope.params.folderId + "/entries", function (result) {
                    if (resource === "collections") {
                        callback(result.data[0].id);
                    } else {
                        callback(result.entries[0].id);
                    }
                    $scope.loadingPage = false;
                }, $scope.params);
            }, $scope.params.count, offset, "folders/" + $scope.params.folderId, $scope.params.sort);

            //$location.search("fid", $scope.folder.id);
            let url = "entry/" + entry.id;
            if (sub)
                url += '/' + sub;

            $location.path(url);
            $location.search({});

            if ($scope.folder && $scope.folder.type === 'REMOTE') {
                $location.search("folderId", $scope.folder.id);
                $location.search("remote", true);
            }
        };

        $scope.tooltipDetails = function (e) {
            $scope.currentTooltip = undefined;
            let params = {};
            if ($scope.folder && $scope.folder.type === 'REMOTE') {
                params.remote = true;
                params.folderId = $scope.folder.id;
            }

            Util.get("rest/parts/" + e.id + "/tooltip", function (result) {
                $scope.currentTooltip = result;
            }, params);
        };

        $scope.folderPopupTemplateUrl = "scripts/folder/template.html";

        // opens a modal that presents user with options to share selected folder
        $scope.openFolderShareSettings = function () {
            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/folder/modal/folder-permissions.html',
                controller: "FolderPermissionsController",
                backdrop: "static",
                resolve: {
                    folder: function () {
                        return $scope.folder;
                    }
                }
            });

            modalInstance.result.then(function (updatedPermissions) {
                if (updatedPermissions) {
                    $scope.folder.accessPermissions = updatedPermissions;
                }
            });
        };

        $scope.openPartsUploadDialog = function () {
            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/folder/modal/folder-upload-parts.html',
                controller: "FolderPartsUploadController",
                backdrop: "static",
                resolve: {
                    folder: function () {
                        return $scope.folder;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                if (!result)
                    return;
                $scope.params.currentPage = 1;
                $scope.folderPageChange();
            });
        };

        $scope.openSampleCreationDialog = function () {
            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/folder/modal/folder-create-samples.html',
                controller: "FolderCreateSamplesController",
                backdrop: "static",
                resolve: {
                    folder: function () {
                        return $scope.folder;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                // if (!result)
                //     return;
                // $scope.params.currentPage = 1;
                // $scope.folderPageChange();
            });
        };

        const clickEvent = new MouseEvent("click", {
            "view": window,
            "bubbles": true,
            "cancelable": false
        });

        $scope.exportSampleFolder = function () {
            Util.download("/rest/folders/" + $scope.folder.id + "/file").$promise.then(function (result) {
                let url = URL.createObjectURL(new Blob([result.data]));
                let a = document.createElement('a');
                a.href = url;
                a.download = result.filename();
                a.target = '_blank';
                a.dispatchEvent(clickEvent);
            });
        };

        $scope.markSampleFolder = function (approved) {

        };

        $scope.getDisplay = function (permission) {
            if (permission.article === 'ACCOUNT')
                return permission.display.replace(/[^A-Z]/g, '');

            // group
            if (permission.article === 'GROUP')
                return permission.display;

            return "Remote";
        };

        $scope.shareText = function (permission) {
            let display = "";
            if (permission.article === 'GROUP')
                display = "Members of ";

            display += permission.display;

            if (permission.type.lastIndexOf("WRITE", 0) === 0)
                display += " can edit";
            else
                display += " can read";
            return display;
        };

        $scope.changeFolderType = function (newType) {
            let tmp = {id: $scope.folder.id, type: newType};
            Util.update('rest/folders/' + $scope.folder.id, tmp, {}, function (result) {
                $scope.folder.type = result.type;
                if (newType === 'PUBLIC')
                    $location.path('folders/available');
                else
                    $location.path('folders/personal');
            });
        };

        $scope.showFolderRenameModal = function () {
            if (!$scope.folder.canEdit)
                return;

            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/folder/modal/rename-folder.html',
                controller: function ($scope, $uibModalInstance, folderName) {
                    $scope.newFolderName = folderName;
                },
                backdrop: 'static',
                resolve: {
                    folderName: function () {
                        return $scope.folder.folderName;
                    }
                },
                size: 'sm'
            });

            modalInstance.result.then(function (newName) {
                if (newName === $scope.folder.folderName)
                    return;

                let tmp = {id: $scope.folder.id, folderName: newName};
                Util.update("rest/folders/" + tmp.id, tmp, {}, function (result) {
                    $scope.folder.folderName = result.folderName;
                })
            })
        }
    })
    // also the main controller
    .controller('CollectionController', function ($scope, $state, $filter, $location, $rootScope, EntryService,
                                                  Authentication, CollectionMenuOptions, Util) {
        // todo : set on all
        let searchUrl = "search";
        if ($location.path().slice(0, searchUrl.length) !== searchUrl) {
            $location.search('q', null);
        }

        $scope.searchFilters = {};
        $rootScope.settings = {};

        // retrieve site wide settings
        $scope.pageCounts = function (currentPage, resultCount, maxPageCount) {
            if (maxPageCount === undefined)
                maxPageCount = 30;
            let pageNum = ((currentPage - 1) * maxPageCount) + 1;

            // number on this page
            let pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
            return $filter('number')(pageNum) + " - " + $filter('number')(pageCount) + " of " + $filter('number')(resultCount);
        };

        // retrieve user settings

        // default list of collections (move to service)
        $scope.collectionList = CollectionMenuOptions.getCollectionOptions();

        // entry items that can be created
        $scope.items = EntryService.getEntryItems();

        if ($location.path() === "/") {
            // change state to trigger collection-selection.html (see ice.app.js)
            $location.path("folders/personal");
        }

        // selected entries
        $scope.selection = [];
        $scope.shoppingCartContents = [];

        // get any sample requests
        if ($rootScope.user) {
            Util.get("rest/samples/requests/" + $rootScope.user.id, function (result) {
                $scope.shoppingCartContents = result.requests;
            });
        }

        $scope.createEntry = {
            isOpen: false
        };

        $scope.toggleUploadDropdown = function ($event, createType) {
            $event.preventDefault();
            $event.stopPropagation();
            if ($scope.createType === createType) {
                $scope.createEntry.isOpen = !$scope.createEntry.isOpen;
            } else {
                $scope.createType = createType;
                $scope.createEntry.isOpen = true;
            }
        };

        $scope.createBulkUpload = function (type) {
            if ("upload" === $scope.createType) {
                Util.update("rest/uploads", {type: type}, {}, function (result) {
                    if (result && result.id) {
                        $location.path("upload/" + result.id);
                    } else {
                        Util.setFeedback("Error creating upload", "danger");
                    }
                });
            } else {
                $location.path($scope.createType + "/" + type);
            }
        };

        $scope.submitShoppingCart = function () {
            let contentIds = [];
            for (let idx = 0; idx < $scope.shoppingCartContents.length; idx += 1)
                contentIds.push($scope.shoppingCartContents[idx].id);

            Util.update("rest/samples/requests", contentIds, {status: 'PENDING'}, function () {
                $scope.shoppingCartContents = [];
                $scope.openShoppingCart = false;
            });
        };

        $scope.shoppingCartTemplate = "scripts/collection/popover/shopping-cart-template.html";

        $scope.entrySampleInCart = function (entry) {
            if (!entry)
                return false;

            for (let idx = 0; idx < $scope.shoppingCartContents.length; idx += 1) {
                if ($scope.shoppingCartContents[idx].partData.id === entry.id) {
                    return true;
                }
            }

            return false;
        };

        // remove sample request from cart
        $scope.removeFromCart = function (content, entry) {
            if (entry) {
                let partId = entry.id;
                for (let idx = 0; idx < $scope.shoppingCartContents.length; idx += 1) {
                    if ($scope.shoppingCartContents[idx].partData.id === partId) {
                        content = $scope.shoppingCartContents[idx];
                        break;
                    }
                }
            }

            if (content) {
                Util.remove("rest/samples/requests/" + content.id, {}, function () {
                    let idx = $scope.shoppingCartContents.indexOf(content);
                    $scope.shoppingCartContents.splice(idx, 1);
                });
            }
        };

        // search
        $scope.runUserSearch = function (filters) {
            $scope.loadingSearchResults = true;

            Util.post("rest/search", filters,
                function (result) {
                    $scope.searchResults = result;
                    $scope.loadingSearchResults = false;
                }, {},
                function () {
                    $scope.loadingSearchResults = false;
                    $scope.searchResults = undefined;
                }
            );
        };

        $rootScope.$on('SamplesInCart', function () {
            Util.get("rest/samples/requests/" + $rootScope.user.id, function (result) {
                $scope.shoppingCartContents = result.requests;
            }, {limit: 1000, status: 'IN_CART'});
        });
    })
    .controller('CollectionDetailController', function ($scope, $cookies, $stateParams, $location, Util) {
        $scope.hideAddCollection = true;
        $scope.showQueryCollectionFolder = false;

        $scope.$on("ShowCollectionFolderAdd", function () {
            $scope.hideAddCollection = false;
            $scope.showQueryCollectionFolder = false;
        });

        $scope.$on("SetShowQueryCollectionFolder", function () {
            $scope.showQueryCollectionFolder = !$scope.showQueryCollectionFolder;
            $scope.hideAddCollection = true;
            if ($scope.showQueryCollectionFolder === false)
                $scope.filterCollectionFoldersText = "";
        });

        // creates a personal folder
        $scope.createPersonalFolder = function () {
            let details = {folderName: $scope.newCollectionName};
            Util.post("/rest/folders", details, function (result) {
                $scope.selectedCollectionFolders.splice(0, 0, result);
                $scope.newCollectionName = "";
                $scope.hideAddCollection = true;
            });
        };

        $scope.deleteCollection = function (folder) {
            // expected folders that can be deleted have type "PRIVATE" and "UPLOAD"
            Util.remove("rest/folders/" + folder.id, {type: folder.type}, function (result) {
                let l = $scope.selectedCollectionFolders.length;
                for (let j = 0; j < l; j += 1) {
                    if ($scope.selectedCollectionFolders[j].id === result.id) {
                        $scope.selectedCollectionFolders.splice(j, 1);
                        break;
                    }
                }

                // if the deleted folder is one user is currently on, re-direct to personal collection
                if (folder.id === $stateParams.collection) {
                    $location.path("folders/personal");
                }
            });
        }
    })
;