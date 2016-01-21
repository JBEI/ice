'use strict';

angular.module('ice.wor.controller', [])
    .controller('WorContentController', function ($rootScope, $scope, $location, $uibModal, $cookieStore, $stateParams,
                                                  WebOfRegistries, WorService, Util) {
        $scope.selectedPartner = $stateParams.partner;
        $scope.loadingPage = true;
        var wor = WebOfRegistries();
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
        wor.getPublicEntries($scope.queryParams, function (result) {
            $scope.loadingPage = false;
            $scope.webResults = result;
            WorService.setSelectedPartner($scope.webResults.registryPartner);
        }, function (error) {
            console.error(error);
            $scope.loadingPage = false;
            WorService.setSelectedPartner(undefined);
            Util.setFeedback("Error retrieving entries", "danger");
        });

        $scope.maxSize = 5;
        $scope.worContentsPopoverTemplate = "scripts/folder/template.html";

        $scope.worContentsPageChange = function () {
            $scope.loadingPage = true;
            $scope.queryParams.offset = ($scope.queryParams.currentPage - 1) * $scope.queryParams.limit;

            wor.getPublicEntries($scope.queryParams, function (result) {
                $scope.webResults = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
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

            wor.getPublicEntries($scope.queryParams, function (result) {
                $scope.loadingPage = false;
                $scope.webResults = result;
                $scope.queryParams.currentPage = 1;
            }, function (error) {
                console.log(error);
                $scope.webResults = null;
                $scope.loadingPage = false;
            });
        };

        $scope.tooltipDetails = function (entry) {
            wor.getToolTip({partnerId: $stateParams.partner, entryId: entry.id}, function (result) {
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
    .controller('WorFolderContentController', function ($location, $rootScope, $scope, $stateParams, Remote,
                                                        WorService, WebOfRegistries) {
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
            Remote().getFolderEntries($scope.params, function (result) {
                $scope.selectedPartnerFolder = result;
                $scope.selectedPartner = WorService.getSelectedPartner();
                if ($scope.selectedPartner == undefined) {
                    $scope.selectedPartner = {id: $stateParams.partner};
                    WorService.setSelectedPartner($scope.selectedPartner)
                }
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
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
            WebOfRegistries().getToolTip({partnerId: $stateParams.partner, entryId: entry.id}, function (result) {
                $scope.currentTooltip = result;
            })
        };

        $scope.getRemoteEntryDetails = function (partnerId, entryId, index) {
            var position = (($scope.currentPage - 1) * $scope.itemsPerPage) + index;
            var url = "/web/" + partnerId;
            if ($scope.params && $scope.params.folderId)
                url += "/folder/" + $scope.params.folderId;

            WorService.setContextCallback(function (offset, callback) {
                $scope.params.offset = offset;
                $scope.params.limit = 1;

                Remote().getFolderEntries($scope.params, function (result) {
                    callback(result.entries[0].id);
                });
            }, $scope.selectedPartnerFolder.count, position, url);

            $location.path("web/" + partnerId + "/entry/" + entryId, true);
        };
    })
    .controller('WorEntryController', function ($location, $scope, $window, WebOfRegistries, $stateParams,
                                                EntryService, WorService) {
        var web = WebOfRegistries();
        $scope.notFound = undefined;
        $scope.remoteEntry = undefined;

        web.getPartner({partnerId: $stateParams.partner}, function (result) {
            $scope.currentPartner = result;
        }, function (error) {
            console.error(error);
        });

        // retrieve specified entry
        var retrieveEntry = function (entryId) {
            web.getPublicEntry({partnerId: $stateParams.partner, entryId: entryId}, function (result) {
                $scope.remoteEntry = EntryService.convertToUIForm(result);
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());
                $scope.remoteEntry.partnerId = $stateParams.partner;

                web.getPublicEntryStatistics({partnerId: $stateParams.partner, entryId: entryId}, function (stats) {
                    $scope.remoteEntryStatistics = stats;
                });

            }, function (error) {
                console.error(error);
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
            web.getPublicEntryAttachments({
                partnerId: $stateParams.partner,
                entryId: $stateParams.entryId
            }, function (result) {
                $scope.remoteAttachments = result;
            }, function (error) {
                console.error(error);
            });
        };

        $scope.downloadRemoteAttachment = function (attachment) {
            $window.open("rest/file/remote/" + $stateParams.partner + "/attachment/" + attachment.fileId, "_self");
        };
    })
    .controller('WebOfRegistriesDetailController', function ($scope, $cookieStore, $location, $stateParams) {
        var sessionId = $cookieStore.get("sessionId");

        $scope.selectRemotePartnerFolder = function (folder) {
            $scope.partnerId = $stateParams.partner;
            $location.path('web/' + $stateParams.partner + "/folder/" + folder.id);
        };
    })
    .controller('WebOfRegistriesController', function ($rootScope, $scope, $location, $uibModal, $cookieStore,
                                                       $stateParams, WebOfRegistries, Remote, Util, Settings) {
        var setting = Settings($cookieStore.get("sessionId"));
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

        Util.get('/rest/config/JOIN_WEB_OF_REGISTRIES', function (result) {
            var joined = result.value === 'yes';
            $scope.isWorEnabled = joined;
            if (!$rootScope.settings)
                $rootScope.settings = {};
            $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
        });

        var wor = WebOfRegistries();
        wor.query({approved_only: false}, function (result) {
            $scope.wor = result;
        });

        //
        // enable or disable web of registries functionality
        //
        $scope.enableDisableWor = function () {
            var value = $scope.isWorEnabled ? 'no' : 'yes';
            setting.update({}, {key: 'JOIN_WEB_OF_REGISTRIES', value: value},
                function (result) {
                    var joined = result.value === 'yes';
                    $scope.isWorEnabled = joined;
                    if (!$rootScope.settings)
                        $rootScope.settings = {};
                    $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = joined;
                }, function (error) {
                    console.error(error);
                });
        };

        //
        // add remote partner to web of registries
        //
        $scope.addPartner = function () {
            Util.post("rest/web/partner", $scope.newPartner, function (result) {
                if (!result) {
                    Util.setFeedback('Error adding web partner', 'danger');
                    console.error("Error adding");
                    return;
                }

                $scope.showAddRegistryForm = false;
                $scope.newPartner = undefined;

                Util.get("rest/web", function (result) {
                    $scope.wor = result;
                }, {approved_only: false});
            });
        };

        //
        // remove web of registries partner
        //
        $scope.removePartner = function (partner, index) {
            wor.removePartner({url: partner.url}, function (result) {
                $scope.wor.partners.splice(index, 1);
            });
        };

        //
        // set the status of a partner
        //
        $scope.setPartnerStatus = function (partner, newStatus) {
            partner.status = newStatus;
            wor.updatePartner({url: partner.url}, partner, function (result) {
            });
        };

        //
        //
        //
        $scope.selectPartner = function (partner) {
            $location.path("web/" + partner.id);
            $scope.selectedPartner = partner.id;
            Util.list("rest/remote/" + partner.id + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        }
    })
    .controller('WebOfRegistriesMenuController', function ($rootScope, $scope, $location, $uibModal, $cookieStore,
                                                           $stateParams, WebOfRegistries, Remote, Settings, Util) {
        // retrieve web of registries partners
        $scope.wor = WebOfRegistries().query({approved_only: true});
        $scope.selectedPartner = $stateParams.partner;

        if ($scope.selectedPartner) {
            Util.list("rest/remote/" + $scope.selectedPartner + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        }

        // retrieve web of registries setting
        var sessionId = $cookieStore.get("sessionId");
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
            Util.list("rest/remote/" + partner.id + "/available", function (result) {
                $scope.selectedPartnerFolders = result;
            });
        }
    })
    .controller('WorEntrySamplesController', function ($scope, $stateParams, Remote) {

        // retrieve remote samples
        var remote = Remote();
        $scope.samples = undefined;

        remote.samples({id: $stateParams.partner, partId: $stateParams.entryId}, function (result) {
            $scope.samples = result;
        }, function (error) {
            console.error(error);
        });
    })
    .controller('WorEntryCommentController', function ($scope, $stateParams, Remote) {
        // retrieve remote samples
        var remote = Remote();
        $scope.entryComments = undefined;

        remote.comments({id: $stateParams.partner, partId: $stateParams.entryId}, function (result) {
            $scope.entryComments = result;
        }, function (error) {
            console.error(error);
        });
    })
    .controller('WorEntryTracesController', function ($scope, $stateParams, Remote) {
        // retrieve remote samples
        var remote = Remote();
        $scope.traceSequences = undefined;

        remote.traces({id: $stateParams.partner, partId: $stateParams.entryId}, function (result) {
            $scope.traceSequences = result;
        }, function (error) {
            console.error(error);
            $scope.traceSequences = undefined;
        });
    });
