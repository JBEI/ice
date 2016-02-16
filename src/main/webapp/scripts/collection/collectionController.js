'use strict';

angular.module('ice.collection.controller', [])
    // controller for <ice.menu.collections> directive
    .controller('CollectionMenuController', function ($cookieStore, $scope, $uibModal, $rootScope, $location, $stateParams,
                                                      Folders, FolderSelection, EntryContextUtil, Util) {
        var folders = Folders();

        // retrieve (to refresh the information such as part counts) all the sub folders under
        // $scope.selectedFolder (defaults to "personal" if not set)
        $scope.updateSelectedCollectionFolders = function () {
            var folder = $scope.selectedFolder ? $scope.selectedFolder : "personal";
            if (folder == "available")
                folder = "featured";

            Util.list("rest/collections/" + folder.toUpperCase() + "/folders", function (result) {
                $scope.selectedCollectionFolders = result;
            });
        };

        //
        // initialize
        //

        // folders contained in the selected folder (default selected to personal)
        $scope.selectedCollectionFolders = undefined;

        // can either be a collection ("shared") or a folder id (34)
        $scope.selectedFolder = $stateParams.collection === undefined ? 'personal' : $stateParams.collection;

        // retrieve collections contained in the selectedFolder (only if a collection)
        if (isNaN($scope.selectedFolder)) {
            if ($scope.selectedFolder.toLowerCase() == "available")
                $scope.selectedFolder = "featured";

            FolderSelection.selectCollection($scope.selectedFolder);
            $scope.updateSelectedCollectionFolders();
        } else {
            // selected folder is a number. folder selected, need collection it is contained in
            Util.get("rest/folders/" + $scope.selectedFolder, function (result) {
                if (result.type == 'PUBLIC')
                    $scope.selectedFolder = "available";
                else
                    $scope.selectedFolder = 'personal';

                FolderSelection.selectCollection($scope.selectedFolder);
                $scope.updateSelectedCollectionFolders();
            });
        }
        //
        // end initialize
        //

        $scope.addCollectionIconClick = function () {
            $scope.$broadcast("ShowCollectionFolderAdd");
        };

        // updates the numbers for the collections
        $scope.updateCollectionCounts = function () {
            Util.get("rest/collections/counts", function (result) {
                if (result === undefined || $scope.collectionList === undefined)
                    return;

                for (var i = 0; i < $scope.collectionList.length; i += 1) {
                    var item = $scope.collectionList[i];
                    item.count = result[item.name];
                }
            });
        };

        // Menu count change handler
        $scope.$on("UpdateCollectionCounts", function (event) {
            $scope.updateCollectionCounts();
        });

        //
        // called from collections-menu-details.html when a collection's folder is selected
        // simply changes state to folder and allows the controller for that to handle it
        //
        $scope.selectCollectionFolder = function (folder) {
            // type on server is PUBLIC, PRIVATE, SHARED, UPLOAD
            EntryContextUtil.resetContext();
            var type = folder.type.toLowerCase();
            if (type !== "upload") {
                FolderSelection.selectFolder(folder);
                type = "folders";
            }

            $location.path(type + "/" + folder.id);
        };

        // todo :
        $scope.acceptTransferredFolder = function (folder) {
            console.log(folder);
            // update folder status
        };

        //
        // called when a collection is selected. Collections are pre-defined ['Featured', 'Deleted', etc]
        // and some allow folders and when that is selected then the selectCollectionFolder() is called
        //
        $scope.selectCollection = function (name) {
            EntryContextUtil.resetContext();
            FolderSelection.selectCollection(name);
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

            for (var i = 0; i < $scope.selectedCollectionFolders.length; i += 1) {
                var subFolder = $scope.selectedCollectionFolders[i];
                if (subFolder.id !== data.id)
                    continue;

                $scope.selectedCollectionFolders[i].folderName = data.name;
                break;
            }
        });
    })
    .controller('FolderPermissionsController', function ($scope, $http, $uibModalInstance, $cookieStore, Folders, Permission,
                                                         Util, User, folder) {
        var sessionId = $cookieStore.get("sessionId");
        $scope.folder = folder;
        $scope.userFilterInput = undefined;
        var folders = Folders();
        $scope.newPermission = {canWrite: false, canRead: true, article: 'ACCOUNT', typeId: folder.id};
        $scope.permissions = [];
        $scope.placeHolder = "Enter user name or email";
        $scope.resultSubField = "email";
        $scope.webPartners = [];

        // retrieve permissions for folder
        Util.list("rest/folders/" + folder.id + "/permissions", function (result) {
            angular.forEach(result, function (perm) {
                var permission = angular.copy(perm);
                permission.canWrite = perm.type == 'WRITE_FOLDER';
                permission.canRead = perm.type == 'READ_FOLDER';
                $scope.permissions.push(permission);
            });
        });

        var getWebPartners = function () {
            Util.list("rest/partners", function (result) {
                $scope.webPartners = result;
            });
        };

        $scope.setPermission = function (isWrite) {
            if (isWrite) {
                $scope.newPermission.canWrite = !$scope.newPermission.canWrite;
                if ($scope.newPermission.canWrite) {
                    $scope.newPermission.canRead = false;
                }
            } else {
                $scope.newPermission.canRead = !$scope.newPermission.canRead;
                if ($scope.newPermission.canRead) {
                    $scope.newPermission.canWrite = false;
                }
            }
        };

        $scope.closeModal = function () {
            $uibModalInstance.close('cancel'); // todo : pass object to inform if folder is shared or cleared
        };

        $scope.setPermissionArticle = function (type) {
            $scope.newPermission.article = type.toUpperCase();
            $scope.newPermission.articleId = undefined;
            $scope.newPermission.partner = undefined;

            switch (type.toLowerCase()) {
                case "account":
                    $scope.placeHolder = "Enter user name or email";
                    $scope.resultSubField = "email";
                    break;

                case "group":
                    $scope.placeHolder = "Enter group name";
                    $scope.resultSubField = "label";
                    break;

                case "remote":
                    getWebPartners();
                    $scope.placeHolder = "Enter remote user email";
                    break;
            }
        };

        $scope.setPropagatePermission = function (folder) {
            folder.propagatePermission = !folder.propagatePermission;
            folders.update({folderId: folder.id}, folder, function (result) {

            })
        };

        $scope.userSelectionForPermissionAdd = function (item, model, label) {
            $scope.newPermission.articleId = item.id;
            console.log("selected", item, model, label);
        };

        $scope.removePermission = function (permission) {
            var pid = permission.id;
            if (permission.article == "REMOTE")
                pid = permission.articleId;

            Util.remove("rest/folders/" + folder.id + "/permissions/" + pid, {}, function (result) {
                var idx = $scope.permissions.indexOf(permission);
                if (idx < 0)
                    return;
                $scope.permissions.splice(idx, 1);
            });
        };

        $scope.addNewPermission = function () {
            console.log($scope.newPermission);

            if ($scope.newPermission.canWrite) {
                $scope.newPermission.type = 'WRITE_FOLDER';
            } else {
                $scope.newPermission.type = 'READ_FOLDER';
            }
            $scope.newPermission.typeId = folder.id;

            if ($scope.newPermission.article.toLowerCase() == "remote") {
                $scope.newPermission.userId = $scope.userFilterInput;
            }

            Util.post("rest/folders/" + folder.id + "/permissions", $scope.newPermission, function (result) {
                $scope.permissions.push(result);
                console.log($scope.permissions);
            });
        };

        $scope.enableDisablePublicRead = function () {
            if (!$scope.folder.publicReadAccess) {
                //enable
                Util.update("rest/folders/" + folder.id + "/permissions/public", {}, {}, function (result) {
                    folder.publicReadAccess = true;
                })
            } else {
                // disable
                Util.remove("rest/folders/" + folder.id + "/permissions/public", {}, function (result) {
                    folder.publicReadAccess = false;
                });
            }
        };

        $scope.filter = function (val) {
            switch ($scope.newPermission.article.toLowerCase()) {
                case "account":
                default :
                    return $http.get('rest/users/autocomplete', {
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")},
                        params: {
                            val: val
                        }
                    }).then(function (res) {
                        return res.data;
                    });

                case "group":
                    return $http.get('rest/groups/autocomplete', {
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")},
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

// deals with sub collections e.g. /folders/:id
// retrieves the contents of folders
    .
    controller('CollectionFolderController', function ($rootScope, $scope, $location, $uibModal, $cookieStore,
                                                       $stateParams, Folders, Entry, EntryContextUtil,
                                                       Selection, Util, localStorageService) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();
        var entry = Entry(sessionId);
        var resource = "collections";

        console.log($stateParams);

        $scope.folderPageChange = function () {
            $scope.loadingPage = true;
            $scope.params.offset = ($scope.params.currentPage - 1) * $scope.params.limit;
            Util.get("rest/" + resource + "/" + $scope.params.folderId + "/entries", function (result) {
                if (resource == "collections") {
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
            }, $scope.params);
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
            created: {field: "creationTime", display: "Created", selected: true}
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
        var storedFields = localStorageService.get('entryHeaderFields');
        if (!storedFields) {
            // set default headers
            var entryHeaderFields = [];
            for (var key in $scope.entryHeaders) {
                if (!$scope.entryHeaders.hasOwnProperty(key))
                    continue;

                var header = $scope.entryHeaders[key];
                if (header.selected) {
                    entryHeaderFields.push(header.field);
                }
            }

            // and store
            localStorageService.set('entryHeaderFields', entryHeaderFields);
        } else {
            // set user selected
            for (var key in $scope.entryHeaders) {
                if (!$scope.entryHeaders.hasOwnProperty(key))
                    continue;

                var header = $scope.entryHeaders[key];
                header.selected = (storedFields.indexOf(header.field) != -1);
            }
        }

        $scope.maxSize = 5;  // number of clickable pages to show in pagination
        var subCollection = $stateParams.collection;   // folder id or one of the defined collections (Shared etc)   ]
        // retrieve folder contents. all folders are redirected to /folder/{id} which triggers this
        if (subCollection !== undefined) {
            $scope.folder = undefined;
            $scope.params.folderId = subCollection;
            if (isNaN($scope.params.folderId))
                resource = "collections";
            else
                resource = "folders";

            var context = EntryContextUtil.getContext();
            if (context) {
                var pageNum = (Math.floor(context.offset / $scope.params.limit)) + 1;
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
            var storedFields = localStorageService.get('entryHeaderFields');

            if (header.selected) {
                // selected by user, add to stored list
                storedFields.push(header.field);
                localStorageService.set('entryHeaderFields', storedFields);
            } else {
                // not selected by user, remove from stored list
                var i = storedFields.indexOf(header.field);
                if (i != -1) {
                    storedFields.splice(i, 1);
                    localStorageService.set('entryHeaderFields', storedFields);
                }
            }
        };

        $rootScope.$on("RefreshAfterDeletion", function (event, data) {
            $scope.params.currentPage = 1;
            $scope.folderPageChange();
        });

        $scope.sort = function (sortType) {
            $scope.folder = null;
            $scope.params.offset = 0;
            if ($scope.params.sort == sortType)
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

        $scope.showEntryDetails = function (entry, index) {
            if (!$scope.params.offset) {
                $scope.params.offset = index;
            }

            var offset = (($scope.params.currentPage - 1) * $scope.params.limit) + index;
            EntryContextUtil.setContextCallback(function (offset, callback) {
                $scope.params.offset = offset;
                $scope.params.limit = 1;

                Util.get("rest/" + resource + "/" + $scope.params.folderId + "/entries", function (result) {
                    if (resource == "collections") {
                        callback(result.data[0].id);
                    } else {
                        callback(result.entries[0].id);
                    }
                    $scope.loadingPage = false;
                }, $scope.params);
            }, $scope.params.count, offset, "folders/" + $scope.params.folderId, $scope.params.sort);

            $location.path("entry/" + entry.id);
        };

        $scope.tooltipDetails = function (e) {
            $scope.currentTooltip = undefined;
            Util.get("rest/parts/" + e.id + "/tooltip", function (result) {
                $scope.currentTooltip = result;
            });
        };

        $scope.folderPopupTemplateUrl = "scripts/folder/template.html";

        // opens a modal that presents user with options to share selected folder
        $scope.openFolderShareSettings = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/folder/modal/folder-permissions.html',
                controller: "FolderPermissionsController",
                backdrop: "static",
                resolve: {
                    folder: function () {
                        return $scope.folder;
                    }
                }
            });
        };

        $scope.getDisplay = function (permission) {
            if (permission.article === 'ACCOUNT')
                return permission.display.replace(/[^A-Z]/g, '');

            // group
            return permission.display;
        };

        $scope.shareText = function (permission) {
            var display = "";
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
            var tmp = {id: $scope.folder.id, type: newType};
            Util.update('rest/folders/' + $scope.folder.id, tmp, function (result) {
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

            var modalInstance = $uibModal.open({
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

                var tmp = {id: $scope.folder.id, folderName: newName};
                folders.update({id: tmp.id}, tmp, function (result) {
                    $scope.folder.folderName = result.folderName;
                })
            })
        }
    })
    // also the main controller
    .controller('CollectionController', function ($scope, $state, $filter, $location, $cookieStore, $rootScope,
                                                  Folders, Settings, Search, Authentication, Samples,
                                                  CollectionMenuOptions, Util) {
        // todo : set on all
        var searchUrl = "search";
        if ($location.path().slice(0, searchUrl.length) != searchUrl) {
            $location.search('q', null);
        }

        var sessionId = $cookieStore.get("sessionId");
        $scope.searchFilters = {};
        $rootScope.settings = {};


        // retrieve site wide settings
        $scope.pageCounts = function (currentPage, resultCount, maxPageCount) {
            if (maxPageCount == undefined)
                maxPageCount = 30;
            var pageNum = ((currentPage - 1) * maxPageCount) + 1;

            // number on this page
            var pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
            return pageNum + " - " + $filter('number')(pageCount) + " of " + $filter('number')(resultCount);
        };

        // retrieve user settings

        // default list of collections (move to service)
        $scope.collectionList = CollectionMenuOptions.getCollectionOptions();

        // entry items that can be created
        $scope.items = [
            {name: "Plasmid", type: "plasmid"},
            {name: "Strain", type: "strain"},
            {name: "Part", type: "part"},
            {name: "Arabidopsis Seed", type: "arabidopsis"}
        ];

        if ($location.path() === "/") {
            // change state to trigger collection-selection.html (see ice.app.js)
            $location.path("folders/personal");
        }

        var samples = Samples(sessionId);

        // selected entries
        $scope.selection = [];
        $scope.shoppingCartContents = [];

        if (!$rootScope.user) {
            Util.get("rest/accesstokens", function (result) {
                $rootScope.user = result;
                Util.get("rest/samples/requests/" + $rootScope.user.id, function (result) {
                    $scope.shoppingCartContents = result.requests;
                })
            });
        } else {
            Util.get("rest/samples/requests/" + $rootScope.user.id, function (result) {
                $scope.shoppingCartContents = result.requests;
            })
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

        $scope.submitShoppingCart = function () {
            var contentIds = [];
            for (var idx = 0; idx < $scope.shoppingCartContents.length; idx += 1)
                contentIds.push($scope.shoppingCartContents[idx].id);

            Util.update("rest/samples/requests", contentIds, {status: 'PENDING'}, function (result) {
                $scope.shoppingCartContents = [];
                $scope.openShoppingCart = false;
            });
        };

        $scope.shoppingCartTemplate = "scripts/collection/popover/shopping-cart-template.html";

        // remove sample request
        $scope.removeFromCart = function (content, entry) {
            if (entry) {
                var partId = entry.id;
                for (var idx = 0; idx < $scope.shoppingCartContents.length; idx += 1) {
                    if ($scope.shoppingCartContents[idx].partData.id == partId) {
                        content = $scope.shoppingCartContents[idx];
                        break;
                    }
                }
            }

            if (content) {
                var contentId = content.id;
                samples.removeRequestFromCart({requestId: contentId}, function (result) {
                    var idx = $scope.shoppingCartContents.indexOf(content);
                    if (idx >= 0) {
                        $scope.shoppingCartContents.splice(idx, 1);
                    } else {
                        // todo : manual scan and remove
                    }
                }, function (error) {
                    console.error(error);
                });
            }
        };

        // search
        $scope.runUserSearch = function (filters) {
            $scope.loadingSearchResults = true;

            Search().runAdvancedSearch(filters,
                function (result) {
                    $scope.searchResults = result;
                    $scope.loadingSearchResults = false;
//                $scope.$broadcast("SearchResultsAvailable", result);
                },
                function (error) {
                    $scope.loadingSearchResults = false;
//                $scope.$broadcast("SearchResultsAvailable", undefined);
                    $scope.searchResults = undefined;
                    console.log(error);
                }
            );
        };

        $rootScope.$on('SamplesInCart', function (event, data) {
            $scope.shoppingCartContents = data;
        });

        // table
        $scope.alignmentGraph = function (searchResult) {
            var ptsPerPixel = searchResult.queryLength / 100;
            var start = Number.MAX_VALUE;
            var end = Number.MIN_VALUE;
            var stripes = {};

            var started = false;
            var matchDetails = searchResult.matchDetails;
            $scope.headers = {};
            $scope.sequences = {}; //setup scope for query alignment

            for (var i = 0; i < matchDetails.length; i++) {
                var line = matchDetails[i].replace(/"/g, ' ').trim();

                //parse Query for score, gaps, & strand data
                if (line.lastIndexOf("Query", 0) === 0) {
                    $scope.headers.score = e;
                    var s = matchDetails[1].split(",");
                    var c = s[0];
                    var o = c.split("=");
                    var e = o[1].trim(); //score data
                    $scope.headers.gaps = d;
                    var g = matchDetails[2].split(",");
                    var a = g[1];
                    var p = a.split("=");
                    var d = p[1].trim();//gaps data
                    $scope.headers.strand = r;
                    var t = matchDetails[3].split("=");
                    var r = (t[1]); //strand data
                }

                if (line.lastIndexOf("Strand", 0) === 0) {
                    started = true;
                    var l = []; //empty array filled with the following contents
                    l = line.split(" "); //separate Query alignment by spaces;
                    var tmp = (l[2]); //last nucleotide location for Query alignment
                    if (tmp < start) //tmp will always be less than MAX_NUMBER
                        start = tmp;

                    tmp = (l[l.length - 1]); //nucleotide bases
                    if (tmp > end)
                        end = tmp;
                } else if (line.lastIndexOf("Score", 0) === 0) {
                    if (started) {
                        stripes[start] = end;
                        start = Number.MAX_VALUE;
                        end = Number.MIN_VALUE;
                    }
                }
            }

            var prevStart = 0;
            var defColor = "#444";
            var stripColor = "";

            // stripe color is based on alignment score
            if (searchResult.score >= 200)
                stripColor = "orange";
            else if (searchResult.score < 200 && searchResult.score >= 80)
                stripColor = "green";
            else if (searchResult.score < 80 && searchResult.score >= 50)
                stripColor = "blue";
            else
                stripColor = "red";

            var fillEnd = 100;
            var html = "<table cellpadding=0 cellspacing=0><tr>";
            var results = [];

            for (var key in stripes) {
                if (!stripes.hasOwnProperty(key))
                    continue;

                var stripeStart = key;
                var stripeEnd = stripes[key];

                var stripeBlockLength = (Math.round((stripeEnd - stripeStart) / ptsPerPixel));
                var fillStart = (Math.round(stripeStart / ptsPerPixel));

                var width;
                if (prevStart >= fillStart && prevStart != 0)
                    width = 1;
                else
                    width = fillStart - prevStart;

                results.push(width);

                html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                    + width + "px; height: 10px\"></hr></td>";

                // mark stripe
                prevStart = (fillStart - prevStart) + stripeBlockLength;
                html += "<td><hr style=\"background-color: " + stripColor + "; border: 0px; width: "
                    + stripeBlockLength + "px; height: 10px\"></hr></td>";
                fillEnd = fillStart + stripeBlockLength;
            }

            if (fillEnd < 100) {
                html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                    + (100 - fillEnd) + "px; height: 10px\"></hr></td>";
            }

            html += "</tr></table>";

//        $scope.stripes = stripes;

            return results;
        };
    })
    .controller('CollectionDetailController', function ($scope, $cookieStore, Folders, $stateParams, $location, Util) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();
        $scope.hideAddCollection = true;

        $scope.$on("ShowCollectionFolderAdd", function (e) {
            $scope.hideAddCollection = false;
        });

        // creates a personal folder
        $scope.createPersonalFolder = function () {
            var details = {folderName: $scope.newCollectionName};
            Util.post("/rest/folders", details, function (result) {
                $scope.selectedCollectionFolders.splice(0, 0, result);
                $scope.newCollectionName = "";
                $scope.hideAddCollection = true;
            });
        };

        $scope.deleteCollection = function (folder) {
            // expected folders that can be deleted have type "PRIVATE" and "UPLOAD"
            folders.delete({folderId: folder.id, type: folder.type}, function (result) {
                var l = $scope.selectedCollectionFolders.length;
                for (var j = 0; j < l; j += 1) {
                    if ($scope.selectedCollectionFolders[j].id === result.id) {
                        $scope.selectedCollectionFolders.splice(j, 1);
                        break;
                    }
                }

                // if the deleted folder is one user is currently on, re-direct to personal collection
                if (folder.id == $stateParams.collection) {
                    $location.path("folders/personal");
                }
            }, function (error) {
                console.error(error);
            });
        }
    })
;



