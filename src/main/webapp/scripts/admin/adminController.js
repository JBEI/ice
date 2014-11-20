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

            // retrieve site wide settings
            var settings = Settings(sessionId);
            settings.get(function (result) {

                angular.forEach(result, function (setting) {
                    if (generalSettingKeys.indexOf(setting.key) != -1) {
                        $scope.generalSettings.push({'key':(setting.key.replace(/_/g, ' ')).toLowerCase(), 'value':setting.value, 'editMode':false});
                    }

                    if (emailSettingKeys.indexOf(setting.key) != -1) {
                        $scope.emailSettings.push({'key':(setting.key.replace(/_/g, ' ')).toLowerCase(), 'value':setting.value, 'editMode':false});
                    }
                });
            });
        };

        var menuOption = $stateParams.option;

        var menuOptions = $scope.profileMenuOptions = [
            {url:'/scripts/admin/settings.html', display:'Settings', selected:true, icon:'fa-cogs'},
            {id:'web', url:'/scripts/admin/wor.html', display:'Web of Registries', selected:false, icon:'fa-globe'},
            {id:'users', url:'/scripts/admin/users.html', display:'Users', selected:false, icon:'fa-user'},
            {id:'groups', url:'/scripts/admin/groups.html', display:'Groups', selected:false, icon:'fa-group'},
            {id:'transferred', url:'/scripts/admin/transferred.html', display:'Transferred Entries', selected:false, icon:'fa-list'},
            {id:'samples', url:'/scripts/admin/sample-requests.html', display:'Sample Requests', selected:false, icon:'fa-shopping-cart'}
        ];

        $scope.showSelection = function (index) {
            angular.forEach(menuOptions, function (details) {
                details.selected = false;
            });

            menuOptions[index].selected = true;
            $scope.adminOptionSelection = menuOptions[index].url;
            $scope.selectedDisplay = menuOptions[index].display;
            if (menuOptions[index].id) {
                $location.path("/admin/" + menuOptions[index].id);
            } else {
                $location.path("/admin");
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
    })
    .controller('AdminTransferredEntriesController', function ($rootScope, $location, $scope, Folders) {
        // get all entries that are transferred
        $scope.transferredEntries = undefined;
        Folders().folder({folderId:'transferred'}, function (result) {
            console.log(result);
            $scope.transferredEntries = result;
        }, function (error) {
            console.error(error);
        });

        $scope.acceptEntries = function () {
        };

        $scope.rejectEntries = function () {
        };

        $scope.showEntryDetails = function (entry, index) {
            if (!$scope.params.offset) {
                $scope.params.offset = index;
            }
            $rootScope.collectionContext = $scope.params;
            $location.path("/entry/" + entry.id);
        };
    })
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookieStore, Samples) {
        $rootScope.error = undefined;
        $scope.selectOptions = ['PENDING', 'FULFILLED', 'REJECTED'];

        var samples = Samples($cookieStore.get("sessionId"));
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.params = {sort:'requested', asc:false};

        var requestSamples = function () {
            $scope.loadingPage = true;
            samples.requests($scope.params, function (result) {
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
        requestSamples();

        $scope.setSamplePage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.currentPage = pageNo;
            $scope.params.offset = (pageNo - 1) * 15;
            if ($scope.filter) {
                $scope.params.filter = $scope.filter;
            }

            requestSamples();
        };

        $scope.updateStatus = function (request, newStatus) {
            samples.update({requestId:request.id, status:newStatus}, function (result) {
                if (result === undefined || result.id != request.id)
                    return;

                var i = $scope.sampleRequests.requests.indexOf(request);
                if (i != -1) {
                    $scope.sampleRequests.requests[i] = result;
                }
            }, function (error) {

            });
        };

        $scope.filterSampleRecords = function () {
            $scope.loadingPage = true;
            // initial sample request (uses default paging values)
            $scope.params.filter = $scope.filter;
            requestSamples();
        };

        $scope.sort = function (field) {
            console.log("sort", field);
        };
    })
    .controller('AdminUserController', function ($rootScope, $scope, $stateParams, $cookieStore, User) {
        $scope.maxSize = 5;
        $scope.currentPage = 1;
        $scope.newProfile = undefined;

        var user = User($cookieStore.get("sessionId"));

        user.list(function (result) {
            $scope.userList = result;
        });

        $scope.setUserListPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            var offset = (pageNo - 1) * 15;
            user.list({offset:offset}, function (result) {
                $scope.userList = result;
                $scope.loadingPage = false;
            });
        };

        $scope.createProfile = function () {
            console.log($scope.newProfile);  // todo
        }
    });