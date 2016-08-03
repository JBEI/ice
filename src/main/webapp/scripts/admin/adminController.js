'use strict';

angular.module('ice.admin.controller', [])
    .controller('AdminController', function ($rootScope, $location, $scope, $stateParams, $cookieStore,
                                             AdminSettings, Util) {

        // save email type settings
        $scope.selectEmailType = function (type) {
            if (!$scope.emailConfig.edit) {
                $scope.emailConfig.edit = true;
            }

            $scope.emailConfig.type = type;
        };

        $scope.saveEmailConfig = function () {
            $scope.submitSetting({key: "EMAILER", value: $scope.emailConfig.type});

            if ($scope.emailConfig.type == "GMAIL") {
                $scope.submitSetting({key: "GMAIL_APPLICATION_PASSWORD", value: $scope.emailConfig.pass});
                $scope.submitSetting({key: "SMTP_HOST", value: ""});
            } else {
                $scope.submitSetting({key: "GMAIL_APPLICATION_PASSWORD", value: ""});
                $scope.submitSetting({key: "SMTP_HOST", value: $scope.emailConfig.smtp});
            }
            $scope.emailConfig.edit = false;
        };

        // retrieve general setting
        $scope.getSetting = function () {
            var sessionId = $cookieStore.get("sessionId");

            $scope.generalSettings = [];
            $scope.emailSettings = [];
            $scope.emailConfig = {type: "", smtp: "", pass: "", edit: false, showEdit: false, showPass: false};

            // retrieve site wide settings
            Util.list('rest/config', function (result) {
                angular.forEach(result, function (setting) {
                    if (AdminSettings.generalSettingKeys().indexOf(setting.key) != -1) {
                        $scope.generalSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': AdminSettings.getBooleanKeys().indexOf(setting.key) != -1
                        });
                    }

                    if (AdminSettings.getEmailKeys().indexOf(setting.key) != -1) {
                        $scope.emailSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': AdminSettings.getBooleanKeys().indexOf(setting.key) != -1
                        });
                    }

                    if (AdminSettings.getEmailTypeKeys().indexOf(setting.key) != -1) {
                        switch (setting.key) {
                            case 'EMAILER':
                                $scope.emailConfig.type = setting.value;
                                break;

                            case 'GMAIL_APPLICATION_PASSWORD':
                                $scope.emailConfig.pass = setting.value;
                                break;

                            case 'SMTP_HOST':
                                $scope.emailConfig.smtp = setting.value;
                                break;
                        }
                    }
                });
            });
        };

        var menuOption = $stateParams.option;

        var menuOptions = $scope.adminMenuOptions = [
            {url: 'scripts/admin/settings.html', display: 'Settings', selected: true, icon: 'fa-cogs'},
            {
                id: 'web',
                url: 'scripts/admin/wor.html',
                display: 'Web of Registries',
                selected: false,
                icon: 'fa-globe',
                description: 'Share/access entries with/on other ICE instances'
            },
            {id: 'users', url: 'scripts/admin/users.html', display: 'Users', selected: false, icon: 'fa-user'},
            {
                id: 'groups',
                url: 'scripts/admin/groups.html',
                display: 'Public Groups',
                selected: false,
                icon: 'fa-group'
            },
            {
                id: 'samples', url: 'scripts/admin/sample-requests.html', display: 'Sample Requests', selected: false,
                icon: 'fa-shopping-cart'
            },
            {
                id: 'annotations-curation',
                url: 'scripts/admin/curation.html',
                display: 'Annotations Curation',
                description: 'Curate annotations for auto annotations',
                selected: false,
                icon: 'fa-language'
            },
            {
                id: 'manuscripts',
                url: 'scripts/admin/manuscripts.html',
                display: 'Editor Tools',
                selected: false,
                icon: 'fa-newspaper-o'
            }
        ];

        $scope.showSelection = function (index) {
            angular.forEach(menuOptions, function (details) {
                details.selected = false;
            });

            menuOptions[index].selected = true;
            $scope.adminOptionSelection = menuOptions[index].url;
            $scope.selectedOption = menuOptions[index];
            if (menuOptions[index].id) {
                $location.path("admin/" + menuOptions[index].id);
            } else {
                $location.path("admin");
            }
        };

        if (menuOption === undefined) {
            $scope.adminOptionSelection = menuOptions[0].url;
            menuOptions[0].selected = true;
            $scope.selectedOption = menuOptions[0];
        } else {
            menuOptions[0].selected = false;
            for (var i = 1; i < menuOptions.length; i += 1) {
                if (menuOptions[i].id === menuOption) {
                    $scope.adminOptionSelection = menuOptions[i].url;
                    menuOptions[i].selected = true;
                    $scope.selectedOption = menuOptions[i];
                    break;
                }
            }

            if ($scope.adminOptionSelection === undefined) {
                $scope.adminOptionSelection = menuOptions[0].url;
                menuOptions[0].selected = true;
                $scope.selectedOption = menuOptions[0];
            }
        }

        $scope.rebuildBlastIndex = function () {
            Util.update("rest/search/indexes/blast");
        };

        $scope.rebuildLuceneIndex = function () {
            Util.update("rest/search/indexes/lucene");
        };

        $scope.submitSetting = function (newSetting) {
            var visualKey = newSetting.key;
            newSetting.key = (newSetting.key.replace(/ /g, '_')).toUpperCase();

            Util.update("rest/config", newSetting, {}, function (result) {
                newSetting.key = visualKey;
                newSetting.value = result.value;
                newSetting.editMode = false;
            });
        };

        $scope.submitBooleanSetting = function (booleanSetting) {
            if (booleanSetting.value == undefined || booleanSetting.value.toLowerCase() === "no")
                booleanSetting.value = "yes";
            else
                booleanSetting.value = "no";

            $scope.submitSetting(booleanSetting);
        }
    })
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookieStore, $uibModal, Util) {
        $rootScope.error = undefined;

        $scope.selectOptions = ['ALL', 'PENDING', 'FULFILLED', 'REJECTED'];
        $scope.maxSize = 5;
        $scope.params = {
            sort: 'requested',
            asc: false,
            currentPage: 1,
            status: 'ALL',
            limit: 15,
            hstep: [15, 30, 50, 100]
        };

        $scope.requestSamples = function () {
            $scope.loadingPage = true;
            var params = angular.copy($scope.params);
            if (params.status == 'ALL')
                params.status = undefined;

            Util.get("rest/samples/requests", function (result) {
                $scope.sampleRequests = result;
                $scope.loadingPage = false;
                $scope.indexStart = ($scope.currentPage - 1) * $scope.params.limit;
            }, params, function (error) {
                $scope.loadingPage = false;
            });
        };

        // initial sample request (uses default paging values)
        $scope.requestSamples();

        $scope.sampleRequestPageChanged = function () {
            $scope.params.offset = ($scope.params.currentPage - 1) * $scope.params.limit;
            if ($scope.filter) {
                $scope.params.filter = $scope.filter;
            }

            $scope.requestSamples();
        };

        $scope.updateStatus = function (request, newStatus) {
            Util.update("rest/samples/requests/" + request.id + "?status=" + newStatus, {}, {}, function (result) {
                if (result === undefined || result.id != request.id)
                    return;

                var i = $scope.sampleRequests.requests.indexOf(request);
                if (i != -1) {
                    $scope.sampleRequests.requests[i].status = result.status;
                }
            });
        };

        $scope.sort = function (field) {
            if ($scope.loadingPage)
                return;

            $scope.loadingPage = true;
            if ($scope.params.sort === field)
                $scope.params.asc = !$scope.params.asc;
            else
                $scope.params.asc = false;

            $scope.params.sort = field;
            $scope.requestSamples();
        };

        $scope.searchSampleLocations = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/sample-location-search.html',
                controller: "AdminSampleLocationSearch",
                backdrop: "static"
            });
        }
    })
    .controller('AdminSampleLocationSearch', function ($scope, $uibModalInstance, Util) {
        $scope.closeModal = function () {
            $uibModalInstance.close();
        };

        $scope.sample = {};

        $scope.searchSampleLocation = function () {
            $scope.sample.searching = true;
            $scope.sample.notFound = false;
            $scope.sample.partId = undefined;

            Util.list("rest/samples/" + $scope.sample.location, function (result) {
                $scope.sample.searching = false;

                if (result && result.length) {
                    $scope.sample.partId = result[0].partId;
                    //Util.get("rest/parts/" + $scope.sample.partId, function (entry) {
                    //    $scope.sample.entry = entry;
                    //});entry
                } else {
                    $scope.sample.notFound = true;
                }
            }, {}, function (error) {
                $scope.sample.searching = false;
                $scope.sample.notFound = true;
            })
        }
    })
    .controller('AdminUserController', function ($rootScope, $scope, Util) {
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.newProfile = {show: false};
        $scope.userListParams = {sort: 'lastName', asc: true, currentPage: 1, limit: 15, status: undefined};

        var getUsers = function () {
            $scope.loadingPage = true;

            Util.get("rest/users", function (result) {
                $scope.userList = result;
                $scope.loadingPage = false;
            }, $scope.userListParams);
        };

        getUsers();
        $scope.userListPageChanged = function () {
            $scope.userListParams.offset = ($scope.userListParams.currentPage - 1) * $scope.userListParams.limit;
            getUsers();
        };

        $scope.createProfile = function () {
            $scope.newProfile.sendEmail = false;
            Util.post("rest/users", $scope.newProfile, function (result) {
                $scope.newProfile.password = result.password;
                getUsers();
            })
        };

        $scope.setUserAccountType = function (userItem, accountType) {
            if (!accountType)
                accountType = 'NORMAL';

            var userCopy = angular.copy(userItem);
            userCopy.accountType = accountType;

            Util.update("rest/users/" + userItem.id, userCopy, {}, function (result) {
                userItem.accountType = result.accountType;
                userItem.isAdmin = result.isAdmin;
            })
        };

        $scope.filterChanged = function () {
            getUsers();
        }
    })
    .controller('AdminGroupsController', function ($scope, $uibModal, Util) {
        $scope.groups = undefined;

        $scope.adminGroupsPagingParams = {
            offset: 0,
            limit: 10,
            available: 0,
            currentPage: 1,
            maxSize: 5,
            type: 'PUBLIC'
        };

        $scope.groupListPageChanged = function () {
            Util.get("rest/groups", function (result) {
                $scope.groups = result.data;
                console.log(result);
                $scope.adminGroupsPagingParams.available = result.resultCount;
            }, $scope.adminGroupsPagingParams);
        };
        $scope.groupListPageChanged();

        $scope.openCreatePublicGroupModal = function (group) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/create-public-group.html',
                controller: 'AdminGroupsModalController',
                backdrop: "static",
                //size: "lg",
                resolve: {
                    currentGroup: function () {
                        return group;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                if (!result)
                    return;

                Util.setFeedback("Public group successfully created", "success");
                $scope.groupListPageChanged();
            })
        }
    })
    .controller('AdminGroupsModalController', function ($scope, $uibModalInstance, currentGroup, Util) {
        $scope.selectedUsers = [];

        if (currentGroup)
            $scope.newPublicGroup = currentGroup;
        else
            $scope.newPublicGroup = {type: 'PUBLIC'};

        $scope.closeCreatePublicGroupModal = function () {
            $uibModalInstance.close();
        };

        $scope.createNewPublicGroup = function () {
            $scope.newPublicGroup.members = $scope.selectedUsers;
            Util.post("rest/groups", $scope.newPublicGroup, function (result) {
                $uibModalInstance.close(result);
            });
        };

        $scope.filterUsers = function (val) {
            if (!val) {
                $scope.userMatches = undefined;
                return;
            }

            $scope.filtering = true;

            Util.list("rest/users/autocomplete", function (result) {
                $scope.userMatches = result;
                $scope.filtering = false;
            }, {limit: 10, val: val}, function (error) {
                $scope.filtering = false;
                $scope.userMatches = undefined;
            });
        };

        $scope.selectUser = function (user) {
            var index = $scope.selectedUsers.indexOf(user);
            if (index == -1)
                $scope.selectedUsers.push(user);
            else
                $scope.selectedUsers.splice(index, 1);
        };
    })
    .controller('AdminManuscriptsController', function ($scope, $uibModal, $window, $location, Util) {
        $scope.manuscriptsParams = {
            sort: 'creationTime',
            asc: false,
            currentPage: 1,
            maxCount: 5,
            offset: 0,
            limit: 15
        };

        // todo : not
        $scope.baseUrl = $location.absUrl().replace($location.path(), '');

        $scope.getManuscripts = function () {
            Util.get("rest/manuscripts", function (result) {
                $scope.manuscripts = result;
            }, $scope.manuscriptsParams);
        };
        $scope.getManuscripts();

        $scope.sort = function (field) {
            $scope.manuscriptsParams.offset = 0;
            if ($scope.manuscriptsParams.sort == field)
                $scope.manuscriptsParams.asc = !$scope.manuscriptsParams.asc;
            else
                $scope.manuscriptsParams.asc = false;
            $scope.manuscriptsParams.sort = field;
            $scope.getManuscripts();
        };

        $scope.openManuscriptAddRequest = function (selectedManuscript) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/manuscript-create.html',
                controller: 'CreateManuscriptController',
                resolve: {
                    manuscript: function () {
                        return selectedManuscript;
                    },

                    baseUrl: function () {
                        return $scope.baseUrl;
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                $scope.getManuscripts();
            });
        };

        $scope.confirmManuscriptDelete = function (manuscript) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/manuscript-delete.html',
                controller: 'DeleteManuscriptController',
                resolve: {
                    manuscript: function () {
                        return manuscript;
                    }
                }
            });

            modalInstance.result.then(function (deletedManuscript) {
                var i = $scope.manuscripts.data.indexOf(deletedManuscript);
                $scope.manuscripts.data.splice(i, 1);
            });
        };

        $scope.downloadManuscriptFiles = function (manuscript) {
            manuscript.downloading = true;
            Util.get("rest/manuscripts/" + manuscript.id + "/files/zip", function (result) {
                manuscript.downloading = false;
                if (result && result.zipFileName) {
                    $window.open("rest/file/tmp/" + result.zipFileName + "?filename=" + manuscript.authorFirstName + "_"
                        + manuscript.authorLastName + "_collection.zip", "_self");
                }
            })
        };

        $scope.updatePaperStatus = function (manuscript, status) {
            if (manuscript.status == status)
                return;
            Util.update("rest/manuscripts/" + manuscript.id, {status: status}, {}, function (result) {
                manuscript.status = status;
            })
        };
    })
    .controller('CreateManuscriptController', function ($scope, $uibModalInstance, $cookieStore, $http, manuscript,
                                                        baseUrl, Util) {
        $scope.submitButtonText = "Create";
        $scope.modalHeaderTitle = "Add New Paper";
        $scope.invalidFolder = false;

        if (manuscript) {
            $scope.newManuscript = manuscript;
            $scope.submitButtonText = "Update";
            $scope.modalHeaderTitle = "Update Paper";
            $scope.newManuscript.selectedFolderName = baseUrl + '/folders/' + $scope.newManuscript.folder.id;
        } else
            $scope.newManuscript = {status: "UNDER_REVIEW"};

        $scope.cancel = function () {
            $uibModalInstance.close();
            $uibModalInstance.dismiss('cancel');
        };
        // get folders I can edit or see (or shared with me?)

        $scope.createNewPaper = function () {
            if ($scope.newManuscript.id) {
                Util.update("rest/manuscripts/" + $scope.newManuscript.id, $scope.newManuscript, {}, function (result) {
                    $scope.cancel();
                });
            } else {
                Util.post("rest/manuscripts", $scope.newManuscript, function (result) {
                    $scope.cancel();
                }, {}, function (error) {

                });
            }
        };

        $scope.filterFolders = function (token) {
            return $http.get('rest/folders/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")},
                params: {
                    val: token
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.folderSelection = function ($item, $model, $label) {
            $scope.invalidFolder = false;
            $scope.newManuscript.folder = $item;
        };

        $scope.pasteFolder = function (event) {
            var pasted = event.originalEvent.clipboardData.getData('text/plain');
            var replace = event.currentTarget.baseURI + "folders/";
            var idx = pasted.indexOf(replace);

            if (idx != 0) {
                console.error("Could not parse pasted");
                $scope.invalidFolder = true;
                return;
            }

            pasted = pasted.slice(idx + replace.length);
            if (isNaN(pasted)) {
                $scope.invalidFolder = true;
                console.error(pasted + " is not a number");
                return;
            }

            Util.get("rest/folders/" + pasted, function (result) {
                $scope.newManuscript.folder = result;
            });
        };
    })
    .controller('DeleteManuscriptController', function ($scope, $uibModalInstance, manuscript, Util) {
        $scope.manuscript = manuscript;
        $scope.errorDeleting = undefined;

        $scope.cancelDeleteRequest = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.deleteManuscript = function () {
            $scope.errorDeleting = undefined;
            Util.remove("rest/manuscripts/" + manuscript.id, {}, function (result) {
                $uibModalInstance.close(manuscript);
            }, function (error) {
                $scope.errorDeleting = true;
            });
        }
    })
    .controller('AdminCurationController', function ($scope, Util) {
        $scope.curationTableParams = {offset: 0, limit: 15, currentPage: 1, maxSize: 5};
        $scope.curationFeaturesParams = {offset: 0, limit: 8, currentPage: 1};
        $scope.selectedFeature = undefined;
        $scope.dynamicPopover = {templateUrl: 'entryPopoverTemplate.html'}

        var getFeatures = function () {
            $scope.loadingCurationTableData = true;
            Util.get("rest/annotations", function (result) {
                $scope.features = result.data;

                angular.forEach($scope.features, function (feature) {
                    for (var i = 0; i < feature.features.length; i += 1) {
                        var f = feature.features[i];
                        if (f.curation == undefined || !f.curation.exclude) {
                            feature.allSelected = false;
                            return;
                        }
                    }
                    feature.allSelected = true;
                });

                $scope.curationTableParams.available = result.resultCount;
                $scope.loadingCurationTableData = false;
            }, $scope.curationTableParams)
        };
        getFeatures();

        $scope.featureListPageChanged = function () {
            $scope.curationTableParams.offset = ($scope.curationTableParams.currentPage - 1) * $scope.curationTableParams.limit;
            getFeatures();
        };

        $scope.selectFeature = function (feature) {
            if ($scope.selectedFeature == feature)
                $scope.selectedFeature = undefined;
            else
                $scope.selectedFeature = feature;

            $scope.curationFeaturesParams = {offset: 0, limit: 8, currentPage: 1};
        };

        $scope.selectAllFeatures = function (feature) {
            var features = [];
            for (var i = 0; i < feature.features.length; i += 1) {
                var f = feature.features[i];
                features.push({id: f.id, curation: {exclude: !feature.allSelected}});
            }

            Util.update("rest/annotations", features, {}, function (result) {
                feature.allSelected = !feature.allSelected;
                $scope.selectedFeature = feature;
            })
        };

        $scope.checkFeatureItem = function (feature, featureItem) {
            featureItem.selected = !featureItem.selected;
            if (featureItem.selected)
                feature.selectCount += 1;
            else
                feature.selectCount -= 1;
        };

        $scope.rebuildFeatures = function () {
            Util.update("rest/annotations/indexes");
        };
    }
);