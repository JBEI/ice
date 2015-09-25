'use strict';

angular.module('ice.admin.controller', [])
    .controller('AdminController', function ($rootScope, $location, $scope, $stateParams, $cookieStore, Settings) {
        var generalSettingKeys = [
            'TEMPORARY_DIRECTORY',
            'DATA_DIRECTORY',
            'PROJECT_NAME',
            'PART_NUMBER_DIGITAL_SUFFIX',
            'PART_NUMBER_DELIMITER',
            'NEW_REGISTRATION_ALLOWED',
            'PROFILE_EDIT_ALLOWED',
            'PASSWORD_CHANGE_ALLOWED',
            'PART_NUMBER_PREFIX',
            'URI_PREFIX',
            'BLAST_INSTALL_DIR'
        ];

        var emailSettingKeys = [
            'SMTP_HOST',
            'ADMIN_EMAIL',
            'BULK_UPLOAD_APPROVER_EMAIL',
            'SEND_EMAIL_ON_ERRORS',
            'ERROR_EMAIL_EXCEPTION_PREFIX'
        ];

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
                    if (generalSettingKeys.indexOf(setting.key) != -1) {
                        $scope.generalSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': $scope.booleanSettings.indexOf(setting.key) != -1
                        });
                    }

                    if (emailSettingKeys.indexOf(setting.key) != -1) {
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

        var menuOptions = $scope.profileMenuOptions = [
            {url: 'scripts/admin/settings.html', display: 'Settings', selected: true, icon: 'fa-cogs'},
            {
                id: 'web',
                url: 'scripts/admin/wor.html',
                display: 'Web of Registries',
                selected: false,
                icon: 'fa-globe'
            },
            {id: 'users', url: 'scripts/admin/users.html', display: 'Users', selected: false, icon: 'fa-user'},
            {id: 'groups', url: 'scripts/admin/groups.html', display: 'Groups', selected: false, icon: 'fa-group'},
            {
                id: 'transferred', url: 'scripts/admin/transferred.html', display: 'Transferred Entries',
                selected: false, icon: 'fa-list'
            },
            {
                id: 'samples', url: 'scripts/admin/sample-requests.html', display: 'Sample Requests', selected: false,
                icon: 'fa-shopping-cart'
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
    .controller('AdminTransferredEntriesController', function ($rootScope, $cookieStore, $filter, $location, $scope, Folders, Entry, Util) {
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.selectedTransferredEntries = [];

        var params = {folderId: 'transferred'};

        // get all entries that are transferred
        $scope.transferredEntries = undefined;

        var getTransferredEntries = function () {
            Folders().folder(params, function (result) {
                $scope.transferredEntries = result;
                $scope.selectedTransferredEntries = [];
            }, function (error) {
                console.error(error);
            });
        };
        getTransferredEntries();

        $scope.setPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            params.offset = (pageNo - 1) * 15;
            Folders().folder(params, function (result) {
                $scope.transferredEntries = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.transferredEntries = undefined;
                $scope.loadingPage = false;
            })
        };

        $scope.acceptEntries = function () {
            var successHandler = function (result) {
                getTransferredEntries();
            };

            Util.update("rest/parts", $scope.selectedTransferredEntries, {visibility: "OK"}, successHandler);
        };

        $scope.rejectEntries = function () {
            var successHandler = function (result) {
                getTransferredEntries();
            };

            Util.update("rest/parts", $scope.selectedTransferredEntries, {visibility: "DELETED"}, successHandler);
        };

        $scope.selectTransferredEntry = function (entry) {
            var index = $scope.selectedTransferredEntries.indexOf(entry.id);
            if (index != -1) {
                $scope.selectedTransferredEntries.splice(index, 1);
                return;
            }

            // add to selected
            $scope.selectedTransferredEntries.push(entry.id);
        };

        $scope.showEntryDetails = function (entry, index) {
            if (!params.offset) {
                params.offset = index;
            }
            $rootScope.collectionContext = params;
            $location.path("entry/" + entry.id);
        };

        $scope.tranferredPopupTooltip = "scripts/admin/transferred-tooltip.html";

        $scope.transferredTooltip = function (entry) {
            $scope.tooltip = undefined;
            Entry($cookieStore.get("sessionId")).tooltip({partId: entry.id},
                function (result) {
                    $scope.tooltip = result;
                }, function (error) {
                    console.error(error);
                });
        };

        $scope.pageCounts = function (currentPage, resultCount) {
            var maxPageCount = 15;
            var pageNum = ((currentPage - 1) * maxPageCount) + 1;

            // number on this page
            var pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
            return pageNum + " - " + $filter('number')(pageCount) + " of " + $filter('number')(resultCount);
        };
    })
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookieStore, Samples) {
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
            requestSamples();
        };
    })
    .controller('AdminUserController', function ($rootScope, $scope, $stateParams, $cookieStore, User) {
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.newProfile = undefined;
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
            user.createUser($scope.newProfile, function (result) {
                $scope.showCreateProfile = false;
                getUsers();
            }, function (error) {

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
    });