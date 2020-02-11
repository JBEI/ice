'use strict';

angular.module('ice.admin.controller', [])
    .controller('AdminController', function ($rootScope, $location, $scope, $stateParams, AdminSettings, Util, $interval, $uibModal) {
        $scope.luceneRebuild = undefined;
        $scope.blastRebuild = undefined;
        let lucenePromise;
        let blastPromise;

        //
        // init : reset menu options
        //
        let menuOption = $stateParams.option;
        let menuOptions = $scope.adminMenuOptions = AdminSettings.getMenuOptions();

        angular.forEach(menuOptions, function (details) {
            details.selected = (details.id === menuOption && menuOption !== undefined);
        });

        const getLuceneIndexStatus = function () {
            Util.get("rest/search/indexes/LUCENE/status", function (result) {
                if (result.total === 0)
                    $interval.cancel(lucenePromise);
                $scope.luceneRebuild = {done: result.done, total: result.total};
            }, {}, function (error) {
                $interval.cancel(lucenePromise);
                $scope.luceneRebuild = undefined;
            })
        };
        lucenePromise = $interval(getLuceneIndexStatus, 2000);

        const getBlastStatus = function () {
            Util.get("rest/search/indexes/BLAST/status", function (result) {
                if (!result.total)
                    $interval.cancel(blastPromise);
                $scope.blastRebuild = {done: result.done, total: result.total};
            }, {}, function (error) {
                $interval.cancel(blastPromise);
                $scope.blastRebuild = undefined;
            })
        };
        blastPromise = $interval(getBlastStatus, 2000);

        // save email type settings
        $scope.selectEmailType = function (type) {
            if (!$scope.emailConfig.edit) {
                $scope.emailConfig.edit = true;
            }

            $scope.emailConfig.type = type;
        };

        $scope.saveEmailConfig = function () {
            $scope.submitSetting({key: "EMAILER", value: $scope.emailConfig.type});

            if ($scope.emailConfig.type === "GMAIL") {
                $scope.submitSetting({key: "GMAIL_APPLICATION_PASSWORD", value: $scope.emailConfig.pass});
                $scope.submitSetting({key: "SMTP_HOST", value: ""});
            } else {
                $scope.submitSetting({key: "GMAIL_APPLICATION_PASSWORD", value: ""});
                $scope.submitSetting({key: "SMTP_HOST", value: $scope.emailConfig.smtp});
            }
            $scope.emailConfig.edit = false;
        };

        $scope.showChangeAuthClassModal = function () {
            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/change-authentication-class.html',
                controller: "AdminChangeAuthentication",
                backdrop: "static",
                resolve: {
                    selection: function () {
                        return $scope.authenticationClass.value;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                if (!result || !result.value)
                    return;

                $scope.authenticationClass.value = result.value;
            })
        };

        // retrieve general setting
        $scope.getSetting = function () {
            $scope.generalSettings = [];
            $scope.emailSettings = [];
            $scope.sampleRequestSettings = [];
            $scope.emailConfig = {type: "", smtp: "", pass: "", edit: false, showEdit: false, showPass: false};

            // retrieve site wide settings
            Util.list('rest/config', function (result) {
                angular.forEach(result, function (setting) {
                    if (AdminSettings.generalSettingKeys().indexOf(setting.key) !== -1) {
                        $scope.generalSettings.push({
                            'originalKey': setting.key,
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': AdminSettings.getBooleanKeys().indexOf(setting.key) !== -1,
                            'canAutoInstall': AdminSettings.canAutoInstall(setting.key)
                        });
                    }

                    if (AdminSettings.getEmailKeys().indexOf(setting.key) !== -1) {
                        $scope.emailSettings.push({
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'isBoolean': AdminSettings.getBooleanKeys().indexOf(setting.key) !== -1
                        });
                    }

                    if (AdminSettings.getEmailTypeKeys().indexOf(setting.key) !== -1) {
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

                    if (setting.key === 'AUTHENTICATION_CLASS') {
                        // expect there to be only one
                        $scope.authenticationClass = {
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                        };
                    }
                });
            });
        };

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
            for (let i = 1; i < menuOptions.length; i += 1) {
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
            Util.update("rest/search/indexes/blast", {}, {}, function () {
                blastPromise = $interval(getBlastStatus, 2000);
            });
        };

        $scope.rebuildLuceneIndex = function () {
            Util.update("rest/search/indexes/lucene", {}, {}, function () {
                lucenePromise = $interval(getLuceneIndexStatus, 2000);
            });
        };

        $scope.submitSetting = function (newSetting) {
            let visualKey = newSetting.key;
            newSetting.key = (newSetting.key.replace(/ /g, '_')).toUpperCase();

            Util.update("rest/config", newSetting, {}, function (result) {
                newSetting.key = visualKey;
                newSetting.value = result.value;
                newSetting.editMode = false;
            });
        };

        $scope.submitBooleanSetting = function (booleanSetting) {
            if (booleanSetting.value === undefined || booleanSetting.value.toLowerCase() === "no")
                booleanSetting.value = "yes";
            else
                booleanSetting.value = "no";

            $scope.submitSetting(booleanSetting);
        };

        // sends a message to the server to auto install a (general) setting's value
        $scope.autoInstallSetting = function (setting) {
            $scope.autoInstalling = setting.originalKey;
            // put to /rest/config/value
            Util.update("/rest/config/value", {key: setting.originalKey}, {}, function (result) {
                if (result.key === setting.originalKey)
                    setting.value = result.value;
                $scope.autoInstalling = undefined;
            }, function (error) {
                $scope.autoInstalling = undefined;
            });
        }
    })
    .controller('AdminChangeAuthentication', function ($scope, $uibModalInstance, selection, Util) {
        $scope.authClass = {
            selection: selection,
            options: [
                {display: "Default", key: "org.jbei.ice.lib.account.authentication.LocalAuthentication"},
                {display: "LDAP", key: "org.jbei.ice.lib.account.authentication.ldap.LdapAuthentication"},
                {display: "None", key: "org.jbei.ice.lib.account.authentication.UserIdAuthentication"}
            ]
        };

        $scope.applyAuthentication = function () {
            Util.update("/rest/config", {
                key: 'AUTHENTICATION_CLASS',
                value: $scope.authClass.selection
            }, {}, function (result) {
                console.log(result);
                $uibModalInstance.close(result);
            }, function (failure) {

            })
        }
    })
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookies, $uibModal, Util, AdminSettings) {
        $rootScope.error = undefined;

        $scope.selectOptions = ['ALL', 'PENDING', 'FULFILLED', 'REJECTED'];
        $scope.selectedRequests = [];
        $scope.maxSize = 5;
        $scope.params = {
            sort: 'requested',
            asc: false,
            currentPage: 1,
            status: 'ALL',
            limit: 15,
            hstep: [15, 30, 50, 100]
        };

        $scope.exportSelectedSamples = function () {
            let selectedIds = [];
            for (let i = 0; i < $scope.selectedRequests.length; i += 1) {
                selectedIds.push($scope.selectedRequests[i].id);
            }

            let clickEvent = new MouseEvent("click", {
                "view": window,
                "bubbles": true,
                "cancelable": false
            });

            Util.download("rest/samples/requests/file", selectedIds).$promise.then(function (result) {
                let url = URL.createObjectURL(new Blob([result.data]));
                let a = document.createElement('a');
                a.href = url;
                a.download = result.filename();
                a.target = '_blank';
                a.dispatchEvent(clickEvent);
                $scope.selectedRequests = [];
            });
        };

        $scope.selectRequest = function (request) {
            let i = $scope.selectedRequests.indexOf(request);
            if (i === -1) {
                $scope.selectedRequests.push(request);
            } else {
                $scope.selectedRequests.splice(i, 1);
            }
        };

        $scope.getBSLLabel = function (bslValue) {
            switch (bslValue) {
                default:
                    return "";

                case 1:
                case 2:
                    return "BSL " + bslValue;

                case -1:
                    return "RESTRICTED";
            }
        };

        $scope.requestSamples = function () {
            $scope.loadingPage = true;
            const params = angular.copy($scope.params);

            Util.get("rest/samples/requests", function (result) {
                console.log(result);

                $scope.sampleRequests = result;
                $scope.loadingPage = false;
                $scope.indexStart = ($scope.currentPage - 1) * $scope.params.limit;
            }, params, function (error) {
                $scope.loadingPage = false;
            });
        };

        $scope.samplesFilterTemplate = "scripts/admin/popover/sample-filter-template.html";
        $scope.params.status = ['FULFILLED', 'IN_CART', 'PENDING', 'REJECTED'];
        $scope.samplesRequesterNameTemplate = "scripts/admin/popover/sample-requester-name-filter-template.html";

        $scope.sampleFilterChecked = function (filter) {
            let idx = $scope.params.status.indexOf(filter);
            if (idx === -1)
                $scope.params.status.push(filter);
            else
                $scope.params.status.splice(idx, 1);
            $scope.requestSamples();
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

        $scope.exportSampleFolder = function (folderId) {
            const clickEvent = new MouseEvent("click", {
                "view": window,
                "bubbles": true,
                "cancelable": false
            });

            Util.download("/rest/folders/" + folderId + "/file").$promise.then(function (result) {
                let url = URL.createObjectURL(new Blob([result.data]));
                let a = document.createElement('a');
                a.href = url;
                a.download = result.filename();
                a.target = '_blank';
                a.dispatchEvent(clickEvent);
            });
        };

        $scope.sortFolderRequests = function (sort) {
            $scope.folderRequests.params.asc = ($scope.folderRequests.params.sort === sort ? !$scope.folderRequests.params.asc : false);
            $scope.folderRequests.params.currentPage = 1;
            $scope.folderRequests.params.sort = sort;
            $scope.folderRequestPageChanged();
        };

        $scope.folderRequestPageChanged = function () {
            $scope.folderRequests.params.offset = ($scope.folderRequests.params.currentPage - 1) * $scope.folderRequests.params.limit;

            Util.get("rest/samples/requests", function (result) {
                $scope.folderRequests.available = result.count;
                $scope.folderRequests.results = result.requests;
            }, $scope.folderRequests.params);
        };

        $scope.initFolderRequests = function () {
            $scope.folderRequests = {
                available: 0,
                results: [],
                params: {
                    asc: false,
                    sort: 'requested',
                    limit: 15,
                    currentPage: 1,
                    hstep: [15, 30, 50, 100],
                    isFolder: true
                },
            };
            $scope.folderRequestPageChanged();
        };

        $scope.retrieveSampleSettings = function () {
            Util.list("rest/samples/requests/settings", function (result) {
                angular.forEach(result, function (setting) {
                    if (AdminSettings.getSampleRequestKeys().indexOf(setting.key) !== -1) {
                        $scope.sampleRequestSettings.push({
                            'originalKey': setting.key,
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'type': AdminSettings.getKeyType(setting.key)
                        });
                    }
                });
            }, {}, function (error) {
                console.log(error);
            })
        };

        $scope.updateStatus = function (request, newStatus, isFolder) {
            Util.update("rest/samples/requests/" + request.id + "?status=" + newStatus, {}, {isFolder: isFolder ? isFolder : false}, function (result) {
                if (result === undefined || result.id !== request.id)
                    return;

                if (!isFolder) {
                    let i = $scope.sampleRequests.requests.indexOf(request);
                    if (i !== -1) {
                        $scope.sampleRequests.requests[i].status = result.status;
                    }
                } else {
                    const i = $scope.folderRequests.results.indexOf(request);
                    if (i !== -1) {
                        $scope.folderRequests.results[i].status = result.status;
                    }
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
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/sample-location-search.html',
                controller: "AdminSampleLocationSearch",
                backdrop: "static"
            });
        };

        $scope.importSamples = function () {
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/import-samples.html',
                controller: 'AdminSampleImport',
                backdrop: "static"
            })
        };

        $scope.sanitizeCsv = function () {
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/create-samples.html',
                controller: 'AdminSampleCreate',
                backdrop: "static",
                size: 'lg'
            })
        };

        $scope.getPrimarySample = function (locations) {
            if (locations.length != 3)
                return locations[0];

            for (let i = 0; i < locations.length; i += 1) {
                if (locations[i].label.indexOf("backup") == -1)
                    return locations[i];
            }
            return locations[0];
        }
    })
    .controller('AdminSampleCreate', function ($scope, FileUploader, Authentication, $window) {
        $scope.sampleUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/samples/file",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        $scope.sampleUploader.onProgressItem = function (item, progress) {
            $scope.progress = progress;
        };

        $scope.sampleUploader.onSuccessItem = function (item, response, status, header) {
            $scope.processingFile = false;
            $scope.result = {success: true, data: response};
            $window.open("rest/file/tmp/" + response.fileId + "?filename=data.csv", "_self");
        };

        $scope.sampleUploader.onAfterAddingFile = function () {
            $scope.processingFile = false;
            $scope.result = {success: false};
        };

        $scope.sampleUploader.onBeforeUploadItem = function () {
            $scope.processingFile = true;
        };

        $scope.sampleUploader.onCompleteAll = function () {
            $scope.processingFile = false;
        };
    })
    .controller('AdminSampleImport', function ($scope, $uibModalInstance, Util, FileUploader, Authentication) {
        $scope.progress = 0;

        let uploader = $scope.sampleImportUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/samples",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        uploader.onProgressItem = function (item, progress) {
            $scope.progress = progress;
        };

        uploader.onSuccessItem = function (item, response, status, header) {
            $scope.processingFile = false;
            $scope.result = {success: true, data: response};
        };

        uploader.onAfterAddingFile = function () {
            $scope.processingFile = false;
            $scope.result = {success: false};
        };

        uploader.onBeforeUploadItem = function () {
            $scope.processingFile = true;
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = false;
        };
    })
    .controller('AdminSampleLocationSearch', function ($scope, $uibModalInstance, Util) {
        $scope.sample = {};

        $scope.searchSampleLocation = function () {
            $scope.sample.searching = true;
            $scope.sample.notFound = false;
            $scope.sample.partId = undefined;

            Util.list("rest/samples/" + $scope.sample.location, function (result) {
                $scope.sample.searching = false;

                if (result && result.length) {
                    $scope.sample.partId = result[0].partId;
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

        let getUsers = function () {
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

            let userCopy = angular.copy(userItem);
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
                $scope.adminGroupsPagingParams.available = result.resultCount;
            }, $scope.adminGroupsPagingParams);
        };
        $scope.groupListPageChanged();

        $scope.openCreatePublicGroupModal = function (group) {
            let modalInstance = $uibModal.open({
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

                let msg = "Group successfully ";
                if (group && group.id)
                    msg += "updated";
                else
                    msg += "created";
                Util.setFeedback(msg, "success");
                $scope.groupListPageChanged();
            })
        };

        $scope.deletePublicGroup = function (group) {
            Util.remove("rest/groups/" + group.id, null, function () {
                const i = $scope.groups.indexOf(group);
                if (i !== -1)
                    $scope.groups.splice(i, 1);
            })
        }
    })
    .controller('AdminGroupsModalController', function ($http, $scope, $cookies, $uibModalInstance,
                                                        currentGroup, Util) {
        $scope.enteredUser = undefined;

        if (currentGroup && currentGroup.id) {
            Util.get("rest/groups/" + currentGroup.id + "/members", function (result) {
                $scope.newPublicGroup = angular.copy(currentGroup);
                $scope.newPublicGroup.members = result.members;
            });
        } else {
            $scope.newPublicGroup = {type: 'PUBLIC', members: []};
        }

        $scope.savePublicGroup = function () {
            if ($scope.newPublicGroup.id) {
                Util.update("rest/groups/" + $scope.newPublicGroup.id, $scope.newPublicGroup, {}, function (result) {
                    $uibModalInstance.close(result);
                });
            } else {
                Util.post("rest/groups", $scope.newPublicGroup, function (result) {
                    $uibModalInstance.close(result);
                });
            }
        };

        $scope.filter = function (val) {
            return $http.get('rest/users/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': $cookies.get("sessionId")},
                params: {
                    val: val
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.userSelectionForGroupAdd = function ($item, $model, $label) {
            $scope.newPublicGroup.members.push($item);

            // reset
            $scope.newUserName = undefined;
            $scope.newPublicGroup.type = 'ACCOUNT';
        };

        $scope.removeUserFromGroup = function (user) {
            const index = $scope.newPublicGroup.members.indexOf(user);
            if (index)
                $scope.newPublicGroup.members.splice(index, 1);
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
            let modalInstance = $uibModal.open({
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
            let modalInstance = $uibModal.open({
                templateUrl: 'scripts/admin/modal/manuscript-delete.html',
                controller: 'DeleteManuscriptController',
                resolve: {
                    manuscript: function () {
                        return manuscript;
                    }
                }
            });

            modalInstance.result.then(function (deletedManuscript) {
                const i = $scope.manuscripts.data.indexOf(deletedManuscript);
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
    .controller('CreateManuscriptController', function ($scope, $uibModalInstance, $cookies, $http, manuscript,
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
                headers: {'X-ICE-Authentication-SessionId': $cookies.get("sessionId")},
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
            let pasted = event.originalEvent.clipboardData.getData('text/plain');
            let replace = event.currentTarget.baseURI + "folders/";
            let idx = pasted.indexOf(replace);

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

        const getFeatures = function () {
            $scope.loadingCurationTableData = true;
            Util.get("rest/annotations", function (result) {
                $scope.features = result.data;

                angular.forEach($scope.features, function (feature) {
                    for (let i = 0; i < feature.features.length; i += 1) {
                        const f = feature.features[i];
                        if (!f.curation || !f.curation.exclude) {
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
            if ($scope.selectedFeature === feature)
                $scope.selectedFeature = undefined;
            else
                $scope.selectedFeature = feature;

            $scope.curationFeaturesParams = {offset: 0, limit: 8, currentPage: 1};
        };

        $scope.selectAllFeatures = function (feature) {
            const features = [];
            for (let i = 0; i < feature.features.length; i += 1) {
                let f = feature.features[i];
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
    })
    .controller('AdminCustomFieldsController', function (Util, $scope, $uibModal) {
        $scope.selection = 'plasmid';

        $scope.options = [
            {name: "Built in field", value: 'EXISTING'},
            {name: 'Text', value: 'TEXT_INPUT'},
            {name: 'Options', value: 'MULTI_CHOICE'},
            {name: 'Options with Text', value: 'MULTI_CHOICE_PLUS'}];

        $scope.optionsText = function (value) {
            for (let i = 0; i < $scope.options.length; i += 1) {
                if (value === $scope.options[i].value) {
                    return $scope.options[i].name;
                }
            }
            return value;
        };

        const retrievePartFields = function () {
            $scope.partCustomFields = undefined;
            $scope.loading = true;
            Util.get("rest/fields/" + $scope.selection, function (result) {
                $scope.partCustomFields = result.data;
            }, {}, function (error) {
                $scope.loading = false;
            })
        };

        // init
        retrievePartFields();

        $scope.selectedTab = function (selection) {
            if (selection === $scope.selection)
                return;

            $scope.selection = selection;
            retrievePartFields();
        };

        $scope.deleteCustomField = function (customField) {
            Util.remove("rest/fields/" + customField.entryType + "/" + customField.id, {}, function (result) {
                const index = $scope.partCustomFields.indexOf(customField);
                if (index !== -1)
                    $scope.partCustomFields.splice(index, 1);
            })
        };

        $scope.addNewCustomEntryField = function () {
            const modal = $uibModal.open({
                templateUrl: 'scripts/admin/modal/new-custom-field.html',
                controller: "AdminNewCustomField",
                resolve: {
                    entryType: function () {
                        return $scope.selection;
                    }
                },
                backdrop: "static"
            });

            modal.result.then(function (result) {
                if (!result)
                    return;

                if (!$scope.partCustomFields)
                    $scope.partCustomFields = [];
                $scope.partCustomFields.push(result);
            });
        };
    })
    .controller('AdminNewCustomField', function (Util, $scope, $uibModalInstance, entryType, EntryService) {
        $scope.field = {required: false, options: [], entryType: entryType.toUpperCase()};
        $scope.options = [
            {name: "Built in field", value: 'EXISTING'},
            {name: 'Text', value: 'TEXT_INPUT'},
            {name: 'Options', value: 'MULTI_CHOICE'},
            {name: 'Options with Text', value: 'MULTI_CHOICE_PLUS'}];

        $scope.existingOptions = EntryService.getFieldsForType(entryType);

        // adds option
        $scope.addOption = function (afterIndex) {
            $scope.field.options.push({});
        };

        $scope.removeOption = function (index) {
            $scope.field.options.splice(index, 1);
        };

        $scope.change = function () {
            switch ($scope.field.fieldType.value) {
                case "MULTI_CHOICE":
                    $scope.field.options = [{}];
                    break;

                case "MULTI_CHOICE_PLUS":
                    $scope.field.options = [{}];
                    break;
            }
        };

        $scope.existingFieldSelected = function () {
            $scope.field.required = $scope.field.existingFieldObject.required;
            $scope.field.label = $scope.field.existingFieldObject.label;
            $scope.field.options = [{name: "schema", value: $scope.field.existingFieldObject.schema}]
            $scope.field.existingField = $scope.field.existingFieldObject.label.toUpperCase();
        };

        $scope.createCustomLink = function () {
            $scope.field.fieldType = $scope.field.fieldType.value;
            Util.post("rest/fields/" + entryType, $scope.field, function (result) {
                $uibModalInstance.close(result);
            })
        };

        // determines whether the "create" button in the form should be enabled or disabled based on information
        // entered by user
        $scope.disableCreateButton = function () {
            if (!$scope.field.label)
                return true;

            if (!$scope.field.fieldType)
                return true;

            switch ($scope.field.fieldType.value) {
                case "MULTI_CHOICE":
                case "MULTI_CHOICE_PLUS":
                    for (let i = 0; i < $scope.field.options.length; i += 1) {
                        if (!$scope.field.options[i].value)
                            return true;
                    }
                    break;
            }

            return false;
        }
    });