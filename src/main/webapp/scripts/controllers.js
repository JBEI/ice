'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'angularMoment']);

iceControllers.controller('ActionMenuController', function ($stateParams, $scope, $window, $rootScope, $location, $cookieStore, Folders, Entry, WebOfRegistries, Files, Selection, Upload, FolderSelection) {
    $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
    $scope.entrySelected = false;

    // reset all on state change
    $rootScope.$on('$stateChangeStart',
        function (event, toState, toParams, fromState, fromParams) {
            $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
            $scope.entrySelected = false;
            Selection.reset();
        });

    var sid = $cookieStore.get("sessionId");
    var folders = Folders();
    var entry = Entry(sid);
    $scope.selectedFolders = [];

    // retrieve personal list of folders user can add or move parts to
    $scope.retrieveUserFolders = function () {
        $scope.userFolders = undefined;
        $scope.selectedFolders = [];

        folders.getByType({folderType: "personal", canEdit: "true"}, function (data) {
            if (data.length)
                $scope.userFolders = data;
        });
    };

    // select a folder in the pull down
    $scope.selectFolderForMoveTo = function (folder, $event) {
        if ($event) {
            $event.preventDefault();
            $event.stopPropagation();
        }

        var i = $scope.selectedFolders.indexOf(folder);
        if (i == -1) {
            $scope.selectedFolders.push(folder);
        } else {
            $scope.selectedFolders.splice(i, 1);
        }
        folder.isSelected = !folder.isSelected;
    };

    // create entry selection object that provides context for user selection
    var getEntrySelection = function () {
        var folderSelected = FolderSelection.getSelectedFolder();

        if (!folderSelected)
            folderSelected = $stateParams.collection;
        else
            folderSelected = folderSelected.id;

        var selectionType;
        if (!isNaN(folderSelected))
            selectionType = 'FOLDER';
        else
            selectionType = 'COLLECTION';

        var entrySelection = {
            all: Selection.getSelection().type == 'ALL',
            folderId: folderSelected,
            selectionType: selectionType,
            entryType: Selection.getSelection().type,
            entries: [],
            destination: angular.copy($scope.selectedFolders)
        };

        var selectedEntriesObjectArray = Selection.getSelectedEntries();
        for (var i = 0; i < selectedEntriesObjectArray.length; i += 1) {
            entrySelection.entries.push(selectedEntriesObjectArray[i].id);
        }
        return entrySelection;
    };

    $scope.addEntriesToFolders = function () {
        var entrySelection = getEntrySelection();
        folders.addSelectionToFolders({}, entrySelection, function (updatedFolders) {
            if (updatedFolders) {
                // result contains list of destination folders
                $scope.updatePersonalCollections();
                Selection.reset();
            }
        });
    };

    $scope.removeEntriesFromFolder = function () {
        // remove selected entries from the current folder
        folders.removeEntriesFromFolder({folderId: $scope.collectionFolderSelected.id}, getEntrySelection(),
            function (result) {
                if (result) {
                    $scope.$broadcast("RefreshAfterDeletion");  // todo
                    $scope.$broadcast("UpdateCollectionCounts");
                    $scope.updatePersonalCollections();
                    Selection.reset();
                }
            }, function (error) {
                console.error(error);
            });
    };

    // remove entries from folder and add to selected folders
    $scope.moveEntriesToFolders = function () {
        $scope.removeEntriesFromFolder();
        $scope.addEntriesToFolders();
    };

    $scope.deleteSelectedEntries = function () {
        var entries = Selection.getSelectedEntries();
        Entry(sid).moveEntriesToTrash(entries,
            function (result) {
                $scope.$broadcast("RefreshAfterDeletion");
                $scope.$broadcast("UpdateCollectionCounts");
                $location.path("folders/personal")
            }, function (error) {
                console.log(error);
            })
    };

    $rootScope.$on("EntrySelected", function (event, count) {
        $scope.addToDisabled = !count;
    });

    $scope.canEdit = function () {
        return Selection.canEdit();
    };

    $scope.canDelete = function () {
        return Selection.canDelete();
    };

    $scope.canMoveFromFolder = function () {
        if (!Selection.hasSelection())
            return false;

        if (!FolderSelection.canEditSelectedFolder())
            return false;

        // must be contained in folder
        if (!FolderSelection.getSelectedFolder())
            return false;

        return true;
    };

    // function that handles "edit" click
    $scope.editEntry = function () {
        var selectedEntries = Selection.getSelectedEntries();
        var upload = Upload(sid);

        if (selectedEntries.length > 1) {
            var type;
            for (type in Selection.getSelectedTypes()) {
                break;
            }

            // first create bulk upload
            upload.create({
                name: "Bulk Edit",
                type: type,
                status: 'BULK_EDIT',
                entryList: selectedEntries
            }, function (result) {
                console.log(result);
                $location.path("upload/" + result.id);
            }, function (error) {
                console.error("error creating bulk upload", error);
            });
        } else {
            $location.path('/entry/edit/' + selectedEntries[0].id);
        }
        $scope.editDisabled = true;
    };

    $scope.retrieveRegistryPartners = function () {
        WebOfRegistries().query({}, function (result) {
            $scope.registryPartners = result;
        }, function (error) {
            console.error(error);
        });
    };

    $scope.transferEntriesToRegistry = function () {
        var entrySelection = getEntrySelection();
        angular.forEach($scope.registryPartners.partners, function (partner) {
            if (partner.selected) {
                WebOfRegistries().transferEntries({partnerId: partner.id}, entrySelection,
                    function (result) {
                        Selection.reset();
                    }, function (error) {
                        console.error(error);
                    })
            }
        });
    };

    // todo : getEntrySelection() should be moved to Selection
    $scope.csvExport = function () {
        var selection = getEntrySelection();
        var files = Files();

        // retrieve from server
        files.getCSV(selection,
            function (result) {
                if (result && result.value) {
                    $window.open("rest/file/tmp/" + result.value, "_self");
                    Selection.reset();
                }

            }, function (error) {
                console.log(error);
            });
    };

    $rootScope.$on("CollectionSelected", function (event, data) {
        $scope.collectionSelected = data;
        Selection.reset();
    });

    $rootScope.$on("CollectionFolderSelected", function (event, data) {
        $scope.collectionFolderSelected = data;
        Selection.reset();
    });
});

iceControllers.controller('RegisterController', function ($scope, $resource, $location, User) {
    $scope.errMsg = undefined;
    $scope.registerSuccess = undefined;
    $scope.newUser = {
        firstName: undefined,
        lastName: undefined,
        institution: undefined,
        email: undefined,
        about: undefined
    };

    $scope.submit = function () {
        var validates = true;
        // validate
        console.log($scope.newUser);

        if (!$scope.newUser.firstName) {
            $scope.firstNameError = true;
            validates = false;
        }

        if (!$scope.newUser.lastName) {
            $scope.lastNameError = true;
            validates = false;
        }

        if (!$scope.newUser.email) {
            $scope.emailError = true;
            validates = false;
        }

        if (!$scope.newUser.institution) {
            $scope.institutionError = true;
            validates = false;
        }

        if (!$scope.newUser.about) {
            $scope.aboutError = true;
            validates = false;
        }

        if (!validates)
            return;

        User().createUser($scope.newUser, function (data) {
            if (data.length != 0)
                $scope.registerSuccess = true;
            else
                $scope.errMsg = "Could not create account";

        }, function (error) {
            $scope.errMsg = "Error creating account";
        });
    };

    $scope.cancel = function () {
        $location.path("login");
    }
});

iceControllers.controller('ForgotPasswordController', function ($scope, $resource, $location, $rootScope, $sce, User) {
    $scope.user = {};

    $scope.resetPassword = function () {
        $scope.user.processing = true;

        if ($scope.user.email === undefined) {
            $scope.user.error = true;
            $scope.user.processing = false;
            return;
        }

        User().resetPassword({}, $scope.user, function (success) {
            $scope.user.processing = false;
            $scope.user.processed = true;
        }, function (error) {
            console.error(error);
            $scope.user.error = true;
            $scope.user.processing = false;
        });
    };

    $scope.redirectToLogin = function () {
        $location.path("login");
    }
});

iceControllers.controller('MessageController', function ($scope, $location, $cookieStore, $stateParams, Message) {
    var message = Message($cookieStore.get('sessionId'));
    var profileId = $stateParams.id;
    $location.path("profile/" + profileId + "/messages", false);
    message.query(function (result) {
        $scope.messages = result;
    });
});

iceControllers.controller('LoginController', function ($scope,
                                                       $location,
                                                       $cookieStore,
                                                       $cookies,
                                                       $rootScope,
                                                       Authentication,
                                                       Settings,
                                                       AccessToken) {
    $scope.login = {};
    $scope.submit = function () {
        $scope.errMsg = undefined;
        $scope.login.processing = true;

        // validate email
        if ($scope.login.email === undefined || $scope.login.email.trim() === "") {
            $scope.login.emailError = true;
        }

        // validate password
        if ($scope.login.password === undefined || $scope.login.password.trim() === "") {
            $scope.login.passwordError = true;
        }

        if ($scope.login.emailError || $scope.login.passwordError) {
            $scope.login.processing = false;
            return;
        }

        var token = AccessToken();
        token.createToken({}, $scope.login,
            function (success) {
                if (success && success.sessionId) {
                    $rootScope.user = success;
                    $cookieStore.put('userId', success.email);
                    $cookieStore.put('sessionId', success.sessionId);
                    var loginDestination = $cookies.loginDestination || '/';
                    $cookies.loginDestination = null;
                    $scope.errMsg = undefined;
                    $location.path(loginDestination);
                } else {
                    $cookieStore.remove('userId');
                    $cookieStore.remove('sessionId');
                    $scope.errMsg = "Login failed";
                }
                $scope.login.processing = false;
            },
            function (error) {
                $scope.login.processing = false;
                $scope.errMsg = "Login failed";
            }
        );

//        Authentication.login($scope.userId, $scope.userPassword);
    };

    $scope.goToRegister = function () {
        $location.path("register");
    };

    $scope.canCreateAccount = false;
    $scope.canChangePassword = false;
    $scope.errMsg = undefined;

    Settings().getSetting({key: 'NEW_REGISTRATION_ALLOWED'}, function (result) {
        $scope.canCreateAccount = (result !== undefined && result.key === 'NEW_REGISTRATION_ALLOWED'
        && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    Settings().getSetting({key: 'PASSWORD_CHANGE_ALLOWED'}, function (result) {
        $scope.canChangePassword = (result !== undefined && result.key === 'PASSWORD_CHANGE_ALLOWED'
        && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });
});

// turning out to be pretty specific to the permissions
iceControllers.controller('GenericTabsController', function ($scope, $cookieStore, User) {
    console.log("GenericTabsController");
    var panes = $scope.panes = [];
    var sessionId = $cookieStore.get("sessionId");

    $scope.activateTab = function (pane) {
        angular.forEach(panes, function (pane) {
            pane.selected = false;
        });
        pane.selected = true;
    };

    this.addPane = function (pane) {
        // activate the first pane that is added
        if (panes.length == 0)
            $scope.activateTab(pane);
        panes.push(pane);
    };
});

iceControllers.controller('FullScreenFlashController', function ($scope, $stateParams, $sce) {
    $scope.sessionId = $stateParams.sessionId;
    $scope.entryId = $stateParams.entryId;
    $scope.vectorEditor = $sce.trustAsHtml('<object type="application/x-shockwave-flash" data="swf/ve/VectorEditor.swf?entryId=' + $scope.entryId + '&sessionId=' + $scope.sessionId + '" id="vectoreditor" width="100%" height="' + ($(window).height() - 100) + 'px"><param name="wmode" value="opaque" /></object>');

    $(window).resize(function () {
        var height = $(this).height();
        $('#vectoreditor').height(height - 100);
    });
});

iceControllers.controller('FolderPermissionsController', function ($scope, $modalInstance, $cookieStore, Folders, Permission, User, folder) {
    var sessionId = $cookieStore.get("sessionId");
    var panes = $scope.panes = [];
    $scope.folder = folder;
    $scope.userFilterInput = undefined;
    var folders = Folders();

    $scope.activateTab = function (pane) {
        angular.forEach(panes, function (pane) {
            pane.selected = false;
        });
        pane.selected = true;
        if (pane.title === 'Read')
            $scope.activePermissions = $scope.readPermissions;
        else
            $scope.activePermissions = $scope.writePermissions;

        angular.forEach($scope.users, function (item) {
            for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                item.selected = (item.id !== undefined && item.id === $scope.activePermissions[i].articleId);
            }
        });
    };

    // retrieve permissions for folder
    folders.permissions({folderId: folder.id}, function (result) {
        $scope.readPermissions = [];
        $scope.writePermissions = [];

        angular.forEach(result, function (item) {
            if (item.type === 'WRITE_FOLDER')
                $scope.writePermissions.push(item);
            else
                $scope.readPermissions.push(item);
        });

        $scope.panes.push({title: 'Read', count: $scope.readPermissions.length, selected: true});
        $scope.panes.push({title: 'Write', count: $scope.writePermissions.length});

        $scope.activePermissions = $scope.readPermissions;
    });

    this.addPane = function (pane) {
        // activate the first pane that is added
        if (panes.length == 0)
            $scope.activateTab(pane);
        panes.push(pane);
    };

    $scope.closeModal = function () {
        $modalInstance.close('cancel'); // todo : pass object to inform if folder is shared or cleared
    };

    $scope.showAddPermissionOptionsClick = function () {
        $scope.showPermissionInput = true;
    };

    $scope.closePermissionOptions = function () {
        $scope.users = undefined;
        $scope.showPermissionInput = false;
    };

    var removePermission = function (permissionId) {
        folders.removePermission({folderId: folder.id, permissionId: permissionId},
            function (result) {
                if (!result)
                    return;

                // check which pane is selected
                var pane;
                if ($scope.panes[0].selected)
                    pane = $scope.panes[0];
                else
                    pane = $scope.panes[1];

                var i = -1;

                for (var idx = 0; idx < $scope.activePermissions.length; idx += 1) {
                    if (permissionId != $scope.activePermissions[idx].id) {
                        i = idx;
                        break;
                    }
                }

                if (i == -1)
                    return;

                $scope.activePermissions.splice(i, 1);
                pane.count = $scope.activePermissions.length;
            });
    };

    $scope.setPropagatePermission = function (folder) {
        folder.propagatePermission = !folder.propagatePermission;
        folders.update({folderId: folder.id}, folder, function (result) {

        }, function (error) {

        })
    };

    $scope.addRemovePermission = function (permission) {
        permission.selected = !permission.selected;
        if (!permission.selected) {
            removePermission(permission.id);
            return;
        }

        // add permission
        var pane;
        for (var i = 0; i < panes.length; i += 1) {
            if (panes[i].selected) {
                permission.type = panes[i].title.toUpperCase() + "_FOLDER";
                pane = panes[i];
                break;
            }
        }
        permission.typeId = folder.id;

        folders.addPermission({folderId: folder.id}, permission, function (result) {
            // result is the permission object
            if (result.type == 'READ_FOLDER') {
                $scope.readPermissions.push(result);
                $scope.activePermissions = $scope.readPermissions;
            }
            else {
                $scope.writePermissions.push(result);
                $scope.activePermissions = $scope.writePermissions;
            }

            permission.id = result.id;
            pane.count = $scope.activePermissions.length;
        });
    };

    $scope.enablePublicRead = function (folder) {
        Folders().enablePublicReadAccess({id: folder.id}, function (result) {
            folder.publicReadAccess = true;
        }, function (error) {

        });
    };

    $scope.disablePublicRead = function (folder) {
        Folders().disablePublicReadAccess({folderId: folder.id}, function (result) {
            folder.publicReadAccess = false;
        }, function (error) {

        })
    };

    $scope.deletePermission = function (index, permission) {
        removePermission(permission.id);
    };

    $scope.filter = function (val) {
        if (!val) {
            $scope.accessPermissions = undefined;
            return;
        }

        $scope.filtering = true;
        Permission().filterUsersAndGroups({limit: 10, val: val},
            function (result) {
                $scope.accessPermissions = result;
                $scope.filtering = false;
            }, function (error) {
                $scope.filtering = false;
                $scope.accessPermissions = undefined;
            });
    };
});
