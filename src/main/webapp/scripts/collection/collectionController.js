'use strict';

angular.module('ice.collection.controller', [])
    // controller for <ice.menu.collections> directive
    .controller('CollectionMenuController', function ($cookieStore, $scope, $modal, $rootScope, $location, $stateParams, Folders, FolderSelection) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();

        $rootScope.$on('$stateChangeStart',
            function (event, toState, toParams, fromState, fromParams) {
                console.log(toState, toParams, fromState, fromParams);
            });

        //
        // initialize
        //

        // folders contained in the selected folder (default selected to personal)
        $scope.selectedCollectionFolders = undefined;
        $scope.selectedFolder = $stateParams.collection === undefined ? 'personal' : $stateParams.collection;
        FolderSelection.selectCollection($scope.selectedFolder);

        // retrieve collections contained in the selectedFolder (only if a collection)
        if (isNaN($scope.selectedFolder)) {
            folders.getByType({folderType: $scope.selectedFolder},
                function (result) {
                    $scope.selectedCollectionFolders = result;
                }, function (error) {
                    console.error(error);
                });
        }

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

            folders.getByType({folderType: folder},
                function (result) {
                    if (result) {
                        $scope.selectedCollectionFolders = result;
                    }
                }, function (error) {
                    console.error(error);
                });
        };

        //
        // called from collections-menu-details.html when a collection's folder is selected
        // simply changes state to folder and allows the controller for that to handle it
        //
        $scope.selectCollectionFolder = function (folder) {
            // type on server is PUBLIC, PRIVATE, SHARED, UPLOAD
            var type = folder.type.toLowerCase();
            if (type !== "upload") {
                FolderSelection.selectFolder(folder);
                type = "folders";
            }

            $location.path("/" + type + "/" + folder.id);
        };

        //
        // called when a collection is selected. Collections are pre-defined ['Featured', 'Deleted', etc]
        // and some allow folders and when that is selected then the selectCollectionFolder() is called
        //
        $scope.selectCollection = function (name) {
            FolderSelection.selectCollection(name);
            $location.path("/folders/" + name);
            $scope.selectedFolder = name;

            // name and display differ for "Featured". using this till they are reconciled
            for (var i = 0; i < $scope.collectionList.length; i += 1) {
                if ($scope.collectionList[i].name === name) {
                    $scope.selectedCollection = $scope.collectionList[i].display;
                    break;
                }
            }

            $scope.selectedCollectionFolders = undefined;

            // retrieve sub folders for selected collection
            folders.getByType({folderType: name},
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
    })
    // deals with sub collections e.g. /folders/:id
    // retrieves the contents of folders
    .controller('CollectionFolderController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, Folders, Entry, EntryContextUtil, Selection) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();
        var entry = Entry(sessionId);

        // param defaults
        $scope.params = {'asc': false, 'sort': 'created'};
        var subCollection = $stateParams.collection;   // folder id or one of the defined collections (Shared etc)

        // retrieve folder contents. all folders are redirected to /folder/{id} which triggers this
        if (subCollection !== undefined) {
            $scope.folder = undefined;
            $scope.params.folderId = subCollection;

            // retrieve contents of collection (e,g, "personal")
            folders.folder($scope.params, function (result) {
                $scope.loadingPage = false;
                $scope.folder = result;
                $scope.params.count = $scope.folder.count;
            });
        }

        // paging
        $scope.currentPage = 1;
        $scope.maxSize = 5;  // number of clickable pages to show in pagination

        $scope.setPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            if ($scope.params.folderId === undefined)
                $scope.params.folderId = 'personal';
            $scope.params.offset = (pageNo - 1) * 15;

            folders.folder($scope.params, function (result) {
                $scope.folder = result;
                $scope.loadingPage = false;
            });
        };

        $scope.$on("RefreshAfterDeletion", function (event, data) {
            $scope.setPage(1);
        });

        $scope.sort = function (sortType) {
            $scope.folder = null;
            $scope.params.offset = 0;
            if ($scope.params.sort == sortType)
                $scope.params.asc = !$scope.params.asc;
            else
                $scope.params.asc = false;

            $scope.params.sort = sortType;

            folders.folder($scope.params, function (result) {
                $scope.folder = result;
                $scope.currentPage = 1;
            });
        };

        $scope.selectAllClass = function () {
            if (Selection.allSelected())
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

            var offset = (($scope.currentPage - 1) * 15) + index;
            EntryContextUtil.setContextCallback(function (offset, callback) {
                $scope.params.offset = offset;
                $scope.params.limit = 1;

                Folders().folder($scope.params,
                    function (result) {
                        callback(result.entries[0].id);
                    });
            }, $scope.params.count, offset, "/folders/" + $scope.params.folderId);

            $location.path("/entry/" + entry.id);
        };

        $scope.tooltipDetails = function (e) {
            $scope.currentTooltip = undefined;
            entry.tooltip({partId: e.id},
                function (result) {
                    $scope.currentTooltip = result;
                }, function (error) {
                    console.error(error);
                });
        };

        // opens a modal that presents user with options to share selected folder
        $scope.openFolderShareSettings = function () {
            var modalInstance = $modal.open({
                templateUrl: '/views/modal/folder-permissions.html',
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
            folders.update({id: tmp.id}, tmp, function (result) {
                $scope.folder.type = result.type;
                if (newType === 'PUBLIC')
                    $location.path('/folders/available');
                else
                    $location.path('/folders/personal');
                // todo : send message to be received by the collection menu
            }, function (error) {
                console.error(error);
            });
        }
    })
    // also the main controller
    .controller('CollectionController', function ($scope, $state, $filter, $location, $cookieStore, $rootScope, Folders, Settings, Search, Samples) {
        // todo : set on all
        var searchUrl = "/search";
        if ($location.path().slice(0, searchUrl.length) != searchUrl) {
            $location.search('q', null);
        }

        var sessionId = $cookieStore.get("sessionId");
        $scope.searchFilters = {};
        $rootScope.settings = {};

        // retrieve site wide settings
        $scope.pageCounts = function (currentPage, resultCount) {
            var maxPageCount = 15;
            var pageNum = ((currentPage - 1) * maxPageCount) + 1;

            // number on this page
            var pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
            return pageNum + " - " + $filter('number')(pageCount) + " of " + $filter('number')(resultCount);
        };

        // retrieve user settings

        // default list of collections
        $scope.collectionList = [
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
                alwaysVisible: false
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
            }
        ];

        // entry items that can be created
        $scope.items = [
            {name: "Plasmid", type: "plasmid"},
            {name: "Strain", type: "strain"},
            {name: "Part", type: "part"},
            {name: "Arabidopsis Seed", type: "arabidopsis"}
        ];

        if ($location.path() === "/") {
            // change state
            $location.path("/folders/personal");
//        // a bit of a hack. the folders are a child state so when
//        // url/folder/personal is accessed, this code is still executed (stateParams do not help here)
//        // so that causes personal folder to be retrieved twice
//        $scope.folder = undefined; // should already be undefined
//
//        var folders = Folders;
//        folders.folder({folderId:'personal'}, function (result) {
//            $scope.folder = result;
//        });
        }

        var samples = Samples(sessionId);

        // selected entries
        $scope.selection = [];
        $scope.shoppingCartContents = [];
        samples.userRequests({status: 'IN_CART'}, {userId: $rootScope.user.id}, function (result) {
            $scope.shoppingCartContents = result.requests;
        });

        $scope.hidePopovers = function (hide) {
            $scope.openShoppingCart = !hide;
        };

        $scope.submitShoppingCart = function () {
            var contentIds = [];
            for (var idx = 0; idx < $scope.shoppingCartContents.length; idx += 1)
                contentIds.push($scope.shoppingCartContents[idx].id);

            samples.submitRequests({status: 'PENDING'}, contentIds, function (result) {
                $scope.shoppingCartContents = [];
                $scope.openShoppingCart = false;
            }, function (error) {
                console.error(error);
            })
        };

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
    .controller('CollectionDetailController', function ($scope, $cookieStore, Folders, $stateParams, $location) {
        var sessionId = $cookieStore.get("sessionId");
        var folders = Folders();

        $scope.createCollection = function () {
            var details = {folderName: $scope.newCollectionName};
            folders.create(details, function (result) {
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
                    $location.path("/folders/personal");
                }
            }, function (error) {
                console.error(error);
            });
        }
    });



