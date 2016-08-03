'use strict';

angular.module('ice.wor.controller', [])
    .controller('WorContentController', function ($rootScope, $scope, $location, $uibModal, $cookieStore, $stateParams,
                                                  WorService, Util) {
        $scope.selectedPartner = $stateParams.partner;
        $scope.loadingPage = true;
        $scope.queryParams = {
            limit: 15,
            offset: 0,
            hstep: [15, 30, 50, 100],
            sort: 'created',
            partnerId: $stateParams.partner,
            currentPage: 1
        };

        $scope.webResults = undefined;

        // init: retrieve first page of all public entries
        Util.get('rest/web/' + $scope.queryParams.partnerId + '/entries', function (result) {
            $scope.loadingPage = false;
            $scope.webResults = result;
            WorService.setSelectedPartner($scope.webResults.registryPartner);
        }, $scope.queryParams, function () {
            $scope.loadingPage = false;
            WorService.setSelectedPartner(undefined);
            Util.setFeedback("Error retrieving entries", "danger");
        });

        $scope.maxSize = 5;
        $scope.worContentsPopoverTemplate = "scripts/folder/template.html";

        $scope.worContentsPageChange = function () {
            $scope.loadingPage = true;
            $scope.queryParams.offset = ($scope.queryParams.currentPage - 1) * $scope.queryParams.limit;

            Util.get('rest/web/' + $scope.queryParams.partnerId + '/entries', function (result) {
                $scope.webResults = result;
                $scope.loadingPage = false;
            }, $scope.queryParams, function () {
                $scope.loadingPage = false;
                $scope.webResults = undefined;
            });
        };

        $scope.sort = function (sortType) {
            $scope.webResults = null;
            $scope.queryParams.sort = sortType;
            $scope.queryParams.offset = 0;
            $scope.queryParams.asc = !$scope.queryParams.asc;
            $scope.loadingPage = false;

            Util.get('rest/web/' + $scope.queryParams.partnerId + '/entries', function (result) {
                $scope.loadingPage = false;
                $scope.webResults = result;
                $scope.queryParams.currentPage = 1;
            }, $scope.queryParams, function () {
                $scope.webResults = null;
                $scope.loadingPage = false;
            });
        };

        $scope.tooltipDetails = function (entry) {
            Util.get('rest/web/' + $stateParams.partner + '/entries/' + entry.id + '/tooltip', function (result) {
                $scope.currentTooltip = result;
            })
        };

        $scope.hStepChanged = function () {
            Util.get("rest/web/" + $stateParams.partner + "/entries", function (result) {
                $scope.webResults = result;
                $scope.queryParams.currentPage = 1;
            }, $scope.queryParams);
        };
    })
    .controller('WorFolderContentController', function ($location, $rootScope, $scope, $stateParams,
                                                        WorService, Util) {
        var id;
        $scope.remoteRetrieveError = undefined;
        if ($stateParams.folderId === undefined)
            id = $scope.partnerId;
        else
            id = $stateParams.folderId;

        $scope.maxSize = 5;
        $scope.itemsPerPage = 15;

        $scope.params = {folderId: id, id: $stateParams.partner, offset: 0, currentPage: 1};

        var getRemoteFolderEntries = function () {
            Util.get('rest/partners/' + $stateParams.partner + '/folders/' + id, function (result) {
                $scope.selectedPartnerFolder = result;
                $scope.selectedPartner = WorService.getSelectedPartner();
                if ($scope.selectedPartner == undefined) {
                    $scope.selectedPartner = {id: $stateParams.partner};
                    WorService.setSelectedPartner($scope.selectedPartner)
                }
                $scope.loadingPage = false;
            }, $scope.params, function () {
                $scope.selectedPartnerFolder = undefined;
                $scope.remoteRetrieveError = true;
                $scope.loadingPage = false;
            });
        };

        // init
        getRemoteFolderEntries();

        $scope.worFolderContentPageChange = function () {
            $scope.loadingPage = true;
            $scope.params.offset = ($scope.params.currentPage - 1) * 15;
            getRemoteFolderEntries();
        };

        $scope.worFolderContentsPopoverTemplate = "scripts/folder/template.html";

        $scope.tooltipDetails = function (entry) {
            Util.get("rest/web/" + $stateParams.partner + "/entries/" + entry.id + "/tooltip", function (result) {
                $scope.currentTooltip = result;
            });
        };

        $scope.getRemoteEntryDetails = function (partnerId, entryId, index) {
            var position = (($scope.currentPage - 1) * $scope.itemsPerPage) + index;
            var url = "/web/" + partnerId;
            if ($scope.params && $scope.params.folderId)
                url += "/folder/" + $scope.params.folderId;

            WorService.setContextCallback(function (offset, callback) {
                $scope.params.offset = offset;
                $scope.params.limit = 1;

                Util.get('rest/partners/' + partnerId + '/folders/' + $scope.params.folderId, function (result) {
                    callback(result.entries[0].id);
                }, $scope.params)
            }, $scope.selectedPartnerFolder.count, position, url);

            $location.path("web/" + partnerId + "/entry/" + entryId, true);
        };
    })
    .controller('WorEntryController', function ($location, $scope, $window, $stateParams,
                                                $cookieStore, EntryService, WorService, Util) {
        $scope.notFound = undefined;
        $scope.remoteEntry = undefined;
        $scope.sessionId = $cookieStore.get("sessionId");

        Util.get('rest/partners/' + $stateParams.partner, function (result) {
            $scope.currentPartner = result;
        });

        // retrieve specified entry
        var retrieveEntry = function (entryId) {

            Util.get('rest/web/' + $stateParams.partner + '/entries/' + entryId, function (result) {
                $scope.remoteEntry = EntryService.convertToUIForm(result);
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());
                $scope.remoteEntry.partnerId = $stateParams.partner;

                Util.get('rest/web/' + $stateParams.partner + '/entries/' + entryId + '/statistics', function (stats) {
                    $scope.remoteEntryStatistics = stats;
                });

            }, {}, function (error) {
                if (error)
                    $scope.notFound = true;
            });
        };
        // init
        retrieveEntry($stateParams.entryId);

        //
        // context navigation
        //
        $scope.context = WorService.getContext();

        // get previous entry
        $scope.prevEntryInContext = function () {
            $scope.context.offset -= 1;
            $scope.context.callback($scope.context.offset, function (result) {
                retrieveEntry(result);
            });
        };

        $scope.nextEntryInContext = function () {
            $scope.context.offset += 1;
            $scope.context.callback($scope.context.offset, function (result) {
                retrieveEntry(result);
            });
        };

        $scope.backTo = function () {
            $location.path($scope.context.back);
        };

        var menuSubDetails = $scope.subDetails = WorService.getMenu();

        $scope.showSelection = function (index) {
            angular.forEach(menuSubDetails, function (details) {
                details.selected = false;
            });
            menuSubDetails[index].selected = true;
            $scope.selection = menuSubDetails[index].url;
        };

        // check if a selection has been made
        var menuOption = $stateParams.option;
        if (menuOption === undefined) {
            $scope.selection = menuSubDetails[0].url;
            menuSubDetails[0].selected = true;
        } else {
            menuSubDetails[0].selected = false;
            for (var i = 1; i < menuSubDetails.length; i += 1) {
                if (menuSubDetails[i].id === menuOption) {
                    $scope.selection = menuSubDetails[i].url;
                    menuSubDetails[i].selected = true;
                    break;
                }
            }

            if ($scope.selection === undefined) {
                $scope.selection = menuSubDetails[0].url;
                menuSubDetails[0].selected = true;
            }
        }

        $scope.getAttachments = function () {
            Util.list('rest/partners/' + $stateParams.partner + '/entries/' + $stateParams.entryId + '/attachments',
                function (result) {
                    $scope.remoteAttachments = result;
                });
        };

        $scope.downloadRemoteAttachment = function (attachment) {
            $window.open("rest/file/remote/" + $stateParams.partner + "/attachment/" + attachment.fileId, "_self");
        };
    })
    .controller('WebOfRegistriesDetailController', function ($scope, $cookieStore, $location, $stateParams) {
        $scope.selectRemotePartnerFolder = function (folder) {
            $scope.partnerId = $stateParams.partner;
            $location.path('web/' + $stateParams.partner + "/folder/" + folder.id);
        };
    })
    .controller('WebOfRegistriesController', function ($rootScope, $scope, $location, $uibModal, $cookieStore,
                                                       $stateParams, Util) {
        $scope.newPartner = undefined;
        $scope.partnerStatusList = [
            {status: 'BLOCKED', action: 'Block'},
            {status: 'APPROVED', action: 'Approve'}
        ];

        //
        // retrieve web of registries partners
        //
        $scope.wor = undefined;
        $scope.isWorEnabled = false;
        $scope.restrictPublic = false;

        Util.get('/rest/config/JOIN_WEB_OF_REGISTRIES', function (result) {
            var joined = result.value === 'yes';
            $scope.isWorEnabled = joined;
            if (!$rootScope.settings)
                $rootScope.settings = {};
            $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
        });

        Util.get("rest/web", function (result) {
            $scope.wor = result;
        }, {approved_only: false});

        // get admin only setting
        Util.get("rest/config/RESTRICT_PUBLIC_ENABLE", function (result) {
            $scope.restrictPublic = result.value.toLowerCase() == "yes";
        });

        $scope.restrictPublicEnable = function () {
            $scope.restrictPublic = !$scope.restrictPublic;
            var setting = {key: 'RESTRICT_PUBLIC_ENABLE', value: $scope.restrictPublic ? "yes" : "no"};
            Util.update("rest/config", setting, {}, function (result) {
                console.log(result);
            })
        };

        //
        // enable or disable web of registries functionality
        //
        $scope.enableDisableWor = function () {
            var value = $scope.isWorEnabled ? 'no' : 'yes';
            Util.update("rest/config", {key: 'JOIN_WEB_OF_REGISTRIES', value: value}, {}, function (result) {
                var joined = result.value === 'yes';
                $scope.isWorEnabled = joined;
                if (!$rootScope.settings)
                    $rootScope.settings = {};
                $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
            });
        };

        //
        // add remote partner to web of registries
        //
        $scope.addWebPartner = function () {
            Util.post("rest/partners", $scope.newPartner, function (result) {
                if (!result) {
                    Util.setFeedback('Error adding web partner', 'danger');
                    return;
                }

                $scope.showAddRegistryForm = false;
                $scope.newPartner = undefined;

                Util.get("rest/web", function (result) {
                    $scope.wor = result;
                }, {approved_only: false});
            });
        };

        // update the keys
        $scope.refreshPartner = function (partner) {
            partner.refreshing = true;
            Util.update("rest/partners/" + partner.id + "/apiKey", {}, {}, function (result) {

            })
        };

        // remove web of registries partner
        $scope.removePartner = function (partner, index) {
            Util.remove("rest/partners/" + partner.id, {}, function () {
                $scope.wor.partners.splice(index, 1);
            });
        };

        // set the status of a partner
        $scope.setPartnerStatus = function (partner, newStatus) {
            if (partner.status == newStatus)
                return;

            Util.update("rest/partners/" + partner.id, {status: newStatus}, {}, function (result) {
                partner = result;
            });
        };

        //
        $scope.selectPartner = function (partner) {
            $location.path("web/" + partner.id);
            $scope.selectedPartner = partner.id;
            Util.list("rest/partners/" + partner.id + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        };

        $scope.retryRemotePartnerContact = function () {
            // todo
        }
    })
    .controller('WebOfRegistriesMenuController', function ($rootScope, $scope, $location, $uibModal, $cookieStore,
                                                           $stateParams, Util, localStorageService) {
        // retrieve web of registries partners
        Util.get("rest/web", function (result) {
            $scope.wor = result;
        }, {approved_only: true})
        $scope.selectedPartner = $stateParams.partner;

        if ($scope.selectedPartner) {
            Util.list("rest/partners/" + $scope.selectedPartner + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        }

        $scope.worMenuSortParams = localStorageService.get('worCollectionFolderSortParams');
        if (!$scope.worMenuSortParams) {
            $scope.worMenuSortParams = {field: 'creationTime', asc: true};
        }

        $scope.sortWorCollectionFolders = function () {
            if ($scope.worMenuSortParams.field == 'creationTime') {
                if (!$scope.worMenuSortParams.asc) {
                    $scope.worMenuSortParams.field = 'folderName';
                }
                $scope.worMenuSortParams.asc = false;
            } else {
                // sort by name
                if ($scope.worMenuSortParams.asc) {
                    $scope.worMenuSortParams.field = 'creationTime';
                }
                $scope.worMenuSortParams.asc = true;
            }
            localStorageService.set('worCollectionFolderSortParams', $scope.worMenuSortParams);
        };

        // retrieve web of registries setting
        Util.get('/rest/config/JOIN_WEB_OF_REGISTRIES', function (result) {
            if (!$scope.settings)
                $scope.settings = {};

            $scope.settings['JOIN_WEB_OF_REGISTRIES'] = (result && result.value === 'yes');
        });

        // retrieves public folders for specified registry and re-directs
        $scope.selectPartner = function (partner) {
            $scope.selectedPartnerFolders = undefined;
            if ($scope.selectedPartner == partner.id) {
                $scope.selectedPartner = undefined;
                return;
            }

            $location.path("web/" + partner.id);
            $scope.selectedPartner = partner.id;

            Util.list("rest/partners/" + partner.id + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        }
    })
    .controller('WorEntrySamplesController', function ($scope, $stateParams, Util) {
        $scope.samples = undefined;

        Util.list('rest/partners/' + $stateParams.partner.id + '/parts/' + $stateParams.entryId + '/samples',
            function (result) {
                $scope.samples = result;
            });
    })
    .controller('WorEntryCommentController', function ($scope, $stateParams, Util) {
        // retrieve remote samples
        $scope.entryComments = undefined;

        Util.get('rest/partners/' + $stateParams.partner.id + '/parts/' + $stateParams.entryId + '/comments',
            function (result) {
                $scope.entryComments = result;
            });
    })
    .controller('WorEntryTracesController', function ($scope, $stateParams, Util) {
        // retrieve remote samples
        $scope.traceSequences = undefined;

        Util.get("rest/partners/" + $stateParams.partner.id + "/parts/" + $stateParams.entryId + "/traces",
            function (result) {
                $scope.traceSequences = result;
            }, {}, function () {
                $scope.traceSequences = undefined;
            });
    });
