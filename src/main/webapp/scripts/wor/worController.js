'use strict';

angular.module('ice.wor.controller', [])
    .controller('WorContentController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, WorService) {
        $scope.selectedPartner = $stateParams.partner;
        $scope.loadingPage = true;
        var wor = WebOfRegistries();
        $scope.queryParams = {limit:15, offset:0, sort:'created', partnerId:$stateParams.partner};
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
        });

        $scope.currentPage = 1;
        $scope.maxSize = 5;

        $scope.setPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            $scope.queryParams.offset = (pageNo - 1) * 15;

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
                $scope.currentPage = 1;
            }, function (error) {
                console.log(error);
                $scope.webResults = null;
                $scope.loadingPage = false;
            });
        };

        $scope.tooltipDetails = function (entry) {
            wor.getToolTip({partnerId:$stateParams.partner, entryId:entry.id}, function (result) {
                $scope.currentTooltip = result;
            })
        };
    })
    .controller('WorFolderContentController', function ($location, $rootScope, $scope, $stateParams, Remote, WorService, WebOfRegistries) {
        var id;
        $scope.remoteRetrieveError = undefined;
        if ($stateParams.folderId === undefined)
            id = $scope.partnerId;
        else
            id = $stateParams.folderId;

        $scope.currentPage = 1;
        $scope.maxSize = 5;
        $scope.itemsPerPage = 15;

        $scope.folderPageParams = {folderId:id, id:$stateParams.partner, offset:0};

        var getRemoteFolderEntries = function () {
            Remote().getFolderEntries($scope.folderPageParams, function (result) {
                $scope.selectedPartnerFolder = result;
                $scope.selectedPartner = WorService.getSelectedPartner();
                if ($scope.selectedPartner == undefined) {
                    $scope.selectedPartner = {id:$stateParams.partner};
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

        $scope.setPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            $scope.currentPage = pageNo;
            $scope.folderPageParams.offset = (pageNo - 1) * 15;
            getRemoteFolderEntries();
        };

        $scope.tooltipDetails = function (entry) {
            WebOfRegistries().getToolTip({partnerId:$stateParams.partner, entryId:entry.id}, function (result) {
                $scope.currentTooltip = result;
            })
        };

        $scope.getRemoteEntryDetails = function (partnerId, entryId, index) {
            var position = (($scope.currentPage - 1) * $scope.itemsPerPage) + index;
            var contextObject = {partner:partnerId, count:$scope.selectedPartnerFolder.count, position:position, params:$scope.folderPageParams, limit:1};
            WorService.setContextObject(contextObject);
            $location.path("/web/" + partnerId + "/entry/" + entryId, true);
        };
    })
    .controller('WorEntryController', function ($scope, $window, WebOfRegistries, $stateParams, EntryService, WorService, Remote) {
        var web = WebOfRegistries();
        $scope.notFound = undefined;
        $scope.remoteEntry = undefined;

        web.getPartner({partnerId:$stateParams.partner}, function (result) {
            $scope.currentPartner = result;
        }, function (error) {
            console.error(error);
        });

        // retrieve specified entry
        var retrieveEntry = function (entryId) {
            web.getPublicEntry({partnerId:$stateParams.partner, entryId:entryId}, function (result) {
                $scope.remoteEntry = EntryService.convertToUIForm(result);
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());

                web.getPublicEntryStatistics({partId:$stateParams.id}, function (stats) {
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

        var getNextInContext = function () {
            $scope.context.params.limit = 1;

            Remote().getFolderEntries($scope.context.params, function (result) {
                if (!result.entries)
                    return;

                var entryId = result.entries[0].id;
                console.log(entryId);
                retrieveEntry(entryId);

                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.remoteRetrieveError = true;
                $scope.loadingPage = false;
            });
        };

        // get previous entry
        $scope.prevEntryInContext = function () {
            $scope.context.position -= 1;
            $scope.context.params.offset = $scope.context.position;
            getNextInContext();
        };

        $scope.nextEntryInContext = function () {
            $scope.context.position += 1;
            $scope.context.params.offset = $scope.context.position;
            getNextInContext();
        };

        var menuSubDetails = $scope.subDetails = [
            {url:'/views/wor/entry/general-information.html', display:'General Information', isPrivileged:false, icon:'fa-exclamation-circle'},
            {id:'sequences', url:'/views/wor/entry/sequence-analysis.html', display:'Sequence Analysis', isPrivileged:false, countName:'traceSequenceCount', icon:'fa-search-plus'},
            {id:'comments', url:'/views/wor/entry/comments.html', display:'Comments', isPrivileged:false, countName:'commentCount', icon:'fa-comments-o'},
            {id:'samples', url:'/views/wor/entry/samples.html', display:'Samples', isPrivileged:false, countName:'sampleCount', icon:'fa-flask'}
//            {id:'experiments', url:'/views/entry/experiments.html', display:'Experimental Data', isPrivileged:false, countName:'experimentalDataCount', icon:'fa-magic'}
        ];

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
            web.getPublicEntryAttachments({partnerId:$stateParams.partner, entryId:$stateParams.entryId}, function (result) {
                $scope.remoteAttachments = result;
            }, function (error) {
                console.error(error);
            });
        };

        $scope.downloadRemoteAttachment = function (attachment) {
            $window.open("/rest/file/remote/" + $stateParams.partner + "/attachment/" + attachment.fileId, "_self");
        };
    })
    .controller('WebOfRegistriesDetailController',function ($scope, $cookieStore, $location, $stateParams) {
        var sessionId = $cookieStore.get("sessionId");

        $scope.selectRemotePartnerFolder = function (folder) {
            $scope.partnerId = $stateParams.partner;
            $location.path('/web/' + $stateParams.partner + "/folder/" + folder.id);
        };
    }).controller('WebOfRegistriesController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote, Settings) {
        var setting = Settings($cookieStore.get("sessionId"));

        // retrieve web of registries partners
        $scope.wor = undefined;
        $scope.isWorEnabled = false;
        setting.getSetting({}, {key:'JOIN_WEB_OF_REGISTRIES'}, function (result) {
            console.log(result);
            $scope.isWorEnabled = result.value === "yes";
        });

        var wor = WebOfRegistries();
//    $scope.getPartners = function(approveOnly) {
        wor.query({approved_only:false}, function (result) {
            $scope.wor = result;
        });
//    };

        $scope.enableDisableWor = function () {
            var value = $scope.isWorEnabled ? 'no' : 'yes';
            setting.update({}, {key:'JOIN_WEB_OF_REGISTRIES', value:value},
                function (result) {
                    $scope.isWorEnabled = result.value === 'yes';
                    $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = $scope.isWorEnabled ? "yes" : "no";
                }, function (error) {
                    console.error(error);
                });

//            setting.update({}, newSetting, function (result) {
//                newSetting.key = visualKey;
//                newSetting.value = result.value;
//                newSetting.editMode = false;
//            });
        };

        $scope.newPartner = undefined;
        $scope.addPartner = function () {
            wor.addPartner({}, $scope.newPartner, function (result) {
                $scope.wor = result;
                $scope.showAddRegistryForm = false;
                $scope.newPartner = undefined;
            });
        };

        $scope.removePartner = function (partner, index) {
            wor.removePartner({url:partner.url}, function (result) {
                $scope.wor.partners.splice(index, 1);
            });
        };

        $scope.approvePartner = function (partner, index) {
            partner.status = 'APPROVED';
            wor.updatePartner({url:partner.url}, partner, function (result) {

            });
        };

        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                console.log(result);
                $scope.selectedPartnerFolders = result;
            });
        }
    })
    .controller('WebOfRegistriesMenuController', function ($scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote) {
        // retrieve web of registries partners
        $scope.wor = undefined;
        var wor = WebOfRegistries();
        wor.query({approved_only:true}, function (result) {
            $scope.wor = result;
        });

        // retrieves public folders for specified registry and re-directs
        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                $scope.selectedPartnerFolders = result;
            }, function (error) {
                console.error(error);
            });
        }
    })
    .controller('WorEntrySamplesController', function ($scope, $stateParams, Remote) {
        // retrieve remote samples
        var remote = Remote();
        $scope.samples = undefined;

        remote.samples({id:$stateParams.partner, partId:$stateParams.entryId}, function (result) {
            $scope.samples = result;
        }, function (error) {
            console.error(error);
        });
    })
    .controller('WorEntryCommentController', function ($scope, $stateParams, Remote) {
        // retrieve remote samples
        var remote = Remote();
        $scope.entryComments = undefined;

        remote.comments({id:$stateParams.partner, partId:$stateParams.entryId}, function (result) {
            $scope.entryComments = result;
        }, function (error) {
            console.error(error);
        });
    })
;
