'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'angularMoment']);

iceControllers.controller('ActionMenuController', function ($stateParams, $uibModal, $scope, $window, $rootScope,
                                                            $location, $cookieStore, Folders, Entry, WebOfRegistries,
                                                            Files, Selection, Upload, FolderSelection, Util) {
    $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
    $scope.entrySelected = false;

    // reset all on state change
    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
        $scope.entrySelected = false;
        $rootScope.hasError = true;
        Selection.reset();
    });

    var sid = $cookieStore.get("sessionId");
    var folders = Folders();
    var entry = Entry(sid);
    $scope.selectedFolders = [];

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

    $scope.removeEntriesFromFolder = function () {
        var entrySelection = getEntrySelection();
        Util.post("rest/folders/" + $scope.collectionFolderSelected.id + "/entries",
            entrySelection, function (result) {
                if (result) {
                    $rootScope.$broadcast("RefreshAfterDeletion");  // todo
                    $scope.$broadcast("UpdateCollectionCounts");
                    $scope.updateSelectedCollectionFolders();
                    Selection.reset();
                    var word = entrySelection.entries.length == 1 ? 'Entry' : "Entries";
                    Util.setFeedback(word + ' successfully removed', 'success');
                }
            }, {move: false});
    };

    // deletes the selected entries (or current entry)
    // "select all" cannot be used to delete entries. they have to be explicitly selected
    $scope.deleteSelectedEntries = function () {
        var entries = Selection.getSelectedEntries();
        Util.post("rest/parts/trash", entries, function () {
            $rootScope.$broadcast("RefreshAfterDeletion");
            $scope.$broadcast("UpdateCollectionCounts");
            $location.path("folders/personal");
        });
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
        // something has been selected
        if (!Selection.hasSelection())
            return false;

        // has selected entries so check if user can edit the current selected folder
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
            $location.path('entry/edit/' + selectedEntries[0].id);
        }
        $scope.editDisabled = true;
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

    $scope.updateSelectedCollectionFolders = function () {
        var folder = $scope.selectedFolder ? $scope.selectedFolder : "personal";
        if (folder == "available")
            folder = "featured";

        Util.list("rest/collections/" + folder.toUpperCase() + "/folders", function (result) {
            $scope.selectedCollectionFolders = result;
        });
    };

    $scope.openAddToFolderModal = function (isMove) {
        var modalInstance = $uibModal.open({
            templateUrl: 'views/modal/add-to-folder-modal.html',
            controller: "AddToFolderController",
            backdrop: "static",
            resolve: {
                move: function () {
                    return isMove;
                },
                selectedFolder: function () {
                    return $scope.collectionFolderSelected;
                }
            }
        });

        modalInstance.result.then(function (result) {
            if (result) {
                $scope.$broadcast("UpdateCollectionCounts");
                $scope.updateSelectedCollectionFolders();
                Selection.reset();
            }
        });
    };

    $scope.openTransferEntriesModal = function () {
        var modalInstance = $uibModal.open({
            templateUrl: 'views/modal/transfer-entries-modal.html',
            controller: "TransferEntriesToPartnersModal",
            backdrop: "static",
            resolve: {
                selectedFolder: function () {
                    return $scope.collectionFolderSelected;
                }
            }
        });
    }
});

iceControllers.controller('TransferEntriesToPartnersModal', function ($scope, $uibModalInstance, Util, FolderSelection, $stateParams, Selection) {
    $scope.closeModal = function () {
        $uibModalInstance.close();
    };

    $scope.retrieveRegistryPartners = function () {
        Util.get("rest/web", function (result) {
            $scope.registryPartners = result;
        });
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

    $scope.transferEntriesToRegistry = function () {
        var entrySelection = getEntrySelection();
        angular.forEach($scope.registryPartners.partners, function (partner) {
            if (partner.selected) {
                Util.post('rest/web/' + partner.id + '/transfer', entrySelection, function (result) {
                    Selection.reset();
                    $scope.closeModal();
                });
            }
        });
    };

    $scope.selectPartnerForTransfer = function (partner) {
        var indexOf = $scope.selectedPartners.indexOf(partner);
        if (indexOf != -1) {
            $scope.selectedPartners.splice(indexOf, 1);
        } else {
            $scope.selectedPartners.push(partner);
        }
        partner.selected = !partner.selected
    };


    //
    // init
    //
    $scope.selectedPartners = [];
    $scope.retrieveRegistryPartners();
});

iceControllers.controller('AddToFolderController', function ($rootScope, $scope, $uibModalInstance, Util, FolderSelection,
                                                             Selection, move, selectedFolder, $stateParams) {
    $scope.getPersonalFolders = function () {
        Util.list("rest/collections/PERSONAL/folders", function (result) {
            $scope.userFolders = [];
            for (var i = 0; i < result.length; i += 1) {
                $scope.userFolders.push({id: result[i].id, name: result[i].folderName, type: result[i].type});
            }
        }, {canEdit: 'true'});
    };

    // init
    $scope.selectedFolders = [];
    $scope.newFolder = {creating: false};

    $scope.closeModal = function (res) {
        $uibModalInstance.close(res);
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

    $scope.submitNewFolderForCreation = function () {
        if ($scope.newFolder.folderName == undefined || $scope.newFolder.folderName === '') {
            $scope.newFolder.error = true;
            return;
        }

        Util.post("rest/folders", $scope.newFolder, function (result) {
            $scope.newFolder = {creating: false};
            $scope.getPersonalFolders();
        });
    };

    // updates the counts for personal collection to indicate items removed/added
    $scope.updateSelectedFolderCounts = function () {
        var selectedFolder = $scope.selectedFolder ? $scope.selectedFolder : "personal";
        Util.list("rest/collections/" + selectedFolder.toUpperCase() + "/folders", function (result) {
            if (result) {
                $scope.selectedCollectionFolders = result;
            }
        });
    };

    // folder selected by user in the pop up
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

    // adds entries to selected folders
    // based on user selected, this action may remove the entries from the source folder first
    $scope.performAction = function () {
        var entrySelection = getEntrySelection();
        if (move === true && selectedFolder) {
            Util.post("rest/folders/" + selectedFolder.id + "/entries", entrySelection, function (result) {
                $rootScope.$emit("RefreshAfterDeletion");
                $scope.$broadcast("UpdateCollectionCounts");
                $scope.updateSelectedFolderCounts();
                Selection.reset();
                $scope.closeModal(result);
            }, {move: true});
        } else {
            // add to folder (see entrySelection) for details
            Util.update("rest/folders/entries", entrySelection, {}, function (res) {
                if (res) {
                    $rootScope.$emit("RefreshAfterDeletion");
                    $scope.updateSelectedFolderCounts();
                    Selection.reset();
                    $scope.closeModal(res);
                }
            });
        }
    };
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

iceControllers.controller('LoginController', function ($scope, $location, $cookieStore, $cookies, $rootScope,
                                                       Authentication, Settings, Util) {

    // init
    $scope.login = {};
    $scope.canCreateAccount = false;
    $scope.canChangePassword = false;
    $scope.errMsg = undefined;

    Util.get('rest/config/NEW_REGISTRATION_ALLOWED', function (result) {
        $scope.canCreateAccount = (result !== undefined && result.key === 'NEW_REGISTRATION_ALLOWED'
        && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    Util.get('rest/config/PASSWORD_CHANGE_ALLOWED', function (result) {
        $scope.canChangePassword = (result !== undefined && result.key === 'PASSWORD_CHANGE_ALLOWED'
        && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    // login function
    $scope.getAccessToken = function () {
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

        Util.post("rest/accesstokens", $scope.login,
            function (success) {
                if (success && success.sessionId) {
                    $rootScope.user = success;
                    $cookieStore.put('userId', success.email);
                    $cookieStore.put('sessionId', success.sessionId);
                    var loginDestination = $cookies.loginDestination || '/';
                    $cookies.loginDestination = null;
                    $location.path(loginDestination);
                    Util.clearFeedback();
                } else {
                    $cookieStore.remove('userId');
                    $cookieStore.remove('sessionId');
                }
                $scope.login.processing = false;
            }, null, function (error) {
                $scope.errMsg = error.statusText;
            });
    };

    $scope.goToRegister = function () {
        $location.path("register");
    };
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

