'use strict';

angular.module('ice.admin.controller', [])
    .controller('AdminController', function ($rootScope, $location, $scope, $stateParams, $cookieStore, Settings,
                                             AdminSettings) {

        // retrieve general setting
        $scope.getSetting = function () {
            var sessionId = $cookieStore.get("sessionId");

            $scope.generalSettings = [];
            $scope.emailSettings = [];
            $scope.booleanSettings = ['NEW_REGISTRATION_ALLOWED', 'PASSWORD_CHANGE_ALLOWED',
                'PROFILE_EDIT_ALLOWED', 'SEND_EMAIL_ON_ERRORS'];

            // retrieve site wide settings
            var settings = Settings(sessionId);
            settings.get(function (result) {

                angular.forEach(result, function (setting) {
                    if (AdminSettings.generalSettingKeys().indexOf(setting.key) != -1) {
                        $scope.generalSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': $scope.booleanSettings.indexOf(setting.key) != -1
                        });
                    }

                    if (AdminSettings.getEmailKeys().indexOf(setting.key) != -1) {
                        $scope.emailSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': $scope.booleanSettings.indexOf(setting.key) != -1
                        });
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
                icon: 'fa-globe'
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
                id: 'api-keys',
                url: 'scripts/admin/all-api-keys.html',
                display: 'API Keys',
                selected: false,
                icon: 'fa-key'
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
            $scope.selectedDisplay = menuOptions[index].display;
            if (menuOptions[index].id) {
                $location.path("admin/" + menuOptions[index].id);
            } else {
                $location.path("admin");
            }
        };

        if (menuOption === undefined) {
            $scope.adminOptionSelection = menuOptions[0].url;
            menuOptions[0].selected = true;
            $scope.selectedDisplay = menuOptions[0].display;
        } else {
            menuOptions[0].selected = false;
            for (var i = 1; i < menuOptions.length; i += 1) {
                if (menuOptions[i].id === menuOption) {
                    $scope.adminOptionSelection = menuOptions[i].url;
                    menuOptions[i].selected = true;
                    $scope.selectedDisplay = menuOptions[i].display;
                    break;
                }
            }

            if ($scope.adminOptionSelection === undefined) {
                $scope.adminOptionSelection = menuOptions[0].url;
                menuOptions[0].selected = true;
                $scope.selectedDisplay = menuOptions[0].display;
            }
        }

        var setting = Settings($cookieStore.get("sessionId"));

        $scope.rebuildBlastIndex = function () {
            setting.rebuildBlast({}, function () {
            });
        };

        $scope.rebuildLuceneIndex = function () {
            setting.rebuildLucene({}, function () {
            });
        };

        $scope.submitSetting = function (newSetting) {
            var visualKey = newSetting.key;
            newSetting.key = (newSetting.key.replace(/ /g, '_')).toUpperCase();

            setting.update({}, newSetting, function (result) {
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
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookieStore, Samples,
                                                          $uibModal) {
        $rootScope.error = undefined;

        $scope.selectOptions = ['ALL', 'PENDING', 'FULFILLED', 'REJECTED'];

        var samples = Samples($cookieStore.get("sessionId"));
        $scope.maxSize = 5;
        $scope.params = {sort: 'requested', asc: false, currentPage: 1, status: undefined};

        $scope.requestSamples = function () {
            $scope.loadingPage = true;
            var params = angular.copy($scope.params);
            if (params.status == 'ALL')
                params.status = undefined;

            samples.requests(params, function (result) {
                $scope.sampleRequests = result;
                $scope.loadingPage = false;
                $scope.indexStart = ($scope.currentPage - 1) * 15;
            }, function (data) {
                if (data.status === 401) {
                    $location.path('/login');
                    return;
                }
                $scope.loadingPage = false;
            });
        };

        // initial sample request (uses default paging values)
        $scope.requestSamples();

        $scope.sampleRequestPageChanged = function () {
            $scope.params.offset = ($scope.params.currentPage - 1) * 15;
            if ($scope.filter) {
                $scope.params.filter = $scope.filter;
            }

            $scope.requestSamples();
        };

        $scope.updateStatus = function (request, newStatus) {
            samples.update({requestId: request.id, status: newStatus}, function (result) {
                if (result === undefined || result.id != request.id)
                    return;

                var i = $scope.sampleRequests.requests.indexOf(request);
                if (i != -1) {
                    $scope.sampleRequests.requests[i].status = result.status;
                }
            }, function (error) {

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
    .controller('AdminUserController', function ($rootScope, $scope, $stateParams, $cookieStore, User) {
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.newProfile = {show: false};
        $scope.userListParams = {sort: 'lastName', asc: true, currentPage: 1, status: undefined};

        var user = User($cookieStore.get("sessionId"));
        var getUsers = function () {
            $scope.loadingPage = true;
            user.list($scope.userListParams, function (result) {
                $scope.userList = result;
                $scope.loadingPage = false;
            }, function (error) {
                $scope.loadingPage = false;
            });
        };

        getUsers();
        $scope.userListPageChanged = function () {
            $scope.loadingPage = true;
            $scope.userListParams.offset = ($scope.userListParams.currentPage - 1) * 15;
            getUsers();
        };

        $scope.createProfile = function () {
            $scope.newProfile.sendEmail = false;
            user.createUser($scope.newProfile, function (result) {
                $scope.newProfile.password = result.password;
                getUsers();
            })
        };

        $scope.setUserAccountType = function (userItem, accountType) {
            if (!accountType)
                accountType = 'NORMAL';

            var userCopy = angular.copy(userItem);
            userCopy.accountType = accountType;

            user.update({userId: userItem.id}, userCopy, function (result) {
                userItem.accountType = result.accountType;
                userItem.isAdmin = result.isAdmin;
            }, function (error) {
                console.log(error);
            });
        };

        $scope.filterChanged = function () {
            getUsers();
        }
    })
    .controller('AdminApiKeysController', function ($scope, Util) {
        $scope.apiKeys = undefined;

        // retrieve existing api keys for current user
        $scope.retrieveKeys = function () {
            Util.get("rest/api-keys", function (result) {
                $scope.apiKeys = result.data;
            }, {getAll: true});
        };

        // init
        $scope.retrieveKeys();
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
                $scope.adminGroupsPagingParams.available = result.resultCount;
            }, $scope.adminGroupsPagingParams);
        };

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
    .controller('AdminManuscriptsController', function ($scope, $uibModal, Util) {
        $scope.manuscripts = [];
        $scope.manuscriptsParams = {sort: 'creationTime', asc: true, currentPage: 1, maxCount: 5, offset: 0, limit: 15};

        $scope.getManuscripts = function () {
            Util.get("rest/manuscripts", function (result) {
                $scope.manuscripts = result;
            }, $scope.manuscriptsParams);
        };
        $scope.getManuscripts();

        $scope.openManuscriptAddRequest = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/manuscript-create.html',
                controller: 'CreateManuscriptController'
            });

            modalInstance.result.then(function (selectedItem) {
                $scope.getManuscripts();
            });
        };

        $scope.updatePaperStatus = function (manuscript, status) {
            if (manuscript.status == status)
                return;
            Util.update("rest/manuscripts/" + manuscript.id, {status: status}, function (result) {
                manuscript.status = status;
            })
        };

        $scope.deleteManuscript = function (manuscript) {
            Util.remove("rest/manuscripts/" + manuscript.id, {}, function (result) {
                var i = $scope.manuscripts.indexOf(manuscript);
                $scope.manuscripts.splice(i, 1);
            });
        }
    })
    .controller('CreateManuscriptController', function ($scope, $uibModalInstance, $cookieStore, $http, Util) {
        $scope.newManuscript = {status: "UNDER_REVIEW"};

        $scope.cancel = function () {
            $uibModalInstance.close();
            $uibModalInstance.dismiss('cancel');
        };
        // get folders I can edit or see (or shared with me?)

        $scope.createNewPaper = function () {
            console.log($scope.newManuscript);
            Util.post("rest/manuscripts", $scope.newManuscript, function (result) {
                $scope.cancel();
            }, {}, function (error) {

            });
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
            $scope.newManuscript.folder = $item;
        }
    })
;