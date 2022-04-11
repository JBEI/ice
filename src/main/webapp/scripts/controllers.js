'use strict';

const iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'angularMoment']);

iceControllers.controller('ActionMenuController', function ($stateParams, $uibModal, $scope, $window, $rootScope,
                                                            $location, Selection, FolderSelection, Util, RemoteSelection) {
    $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
    $scope.entrySelected = false;

    // reset all on state change
    $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
        $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
        $scope.entrySelected = false;
        $rootScope.hasError = true;
        Selection.reset();
    });

    $scope.selectedFolders = [];
    $scope.selectedRemote = RemoteSelection.getSelection();

    $scope.closeFeedbackAlert = function () {
        Util.clearFeedback();
    };

    // select a folder in the pull down
    $scope.selectFolderForMoveTo = function (folder, $event) {
        if ($event) {
            $event.preventDefault();
            $event.stopPropagation();
        }

        var i = $scope.selectedFolders.indexOf(folder);
        if (i === -1) {
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

        var searchQuery = Selection.getSearch();

        var selectionType;
        if (!isNaN(folderSelected))
            selectionType = 'FOLDER';
        else {
            if (searchQuery)
                selectionType = 'SEARCH';
            else
                selectionType = 'COLLECTION';
        }

        var entrySelection = {
            all: Selection.getSelection().type === 'ALL',
            folderId: folderSelected,
            selectionType: selectionType,
            entryType: Selection.getSelection().type,
            entries: [],
            searchQuery: searchQuery,
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
                    $rootScope.$broadcast("RefreshAfterDeletion");
                    $scope.$broadcast("UpdateCollectionCounts");
                    $scope.updateSelectedCollectionFolders();
                    Selection.reset();
                    var word = entrySelection.entries.length == 1 ? 'Entry' : entrySelection.entries.length + " entries";
                    Util.setFeedback(word + ' successfully removed from folder', 'success');
                }
            }, {move: false});
    };

    // deletes the selected entries (or current entry)
    // "select all" cannot be used to delete entries. they have to be explicitly selected
    $scope.deleteSelectedEntries = function () {
        var entries = Selection.getSelectedEntries();
        if (entries[0].visible == 'DELETED') { //if deleted THEN PROMPT
            var modalInstance = $uibModal.open({
                templateUrl: 'views/modal/user-deletion-prompt-modal.html',
                controller: "PermanentEntryDeletionConfirmationModalController",
                backdrop: "static",
                resolve: {
                    allEntries: function () {
                        return entries;
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
        } else {
            var currLocation = "folders/";
            if (FolderSelection.getSelectedFolder() == null) {
                currLocation = currLocation.concat(FolderSelection.getSelectedCollection());
            } else {
                currLocation = currLocation.concat(FolderSelection.getSelectedFolder().id);
            }

            Util.post("rest/parts/trash", entries, function () {
                $rootScope.$broadcast("RefreshAfterDeletion");
                $scope.$broadcast("UpdateCollectionCounts");
                $location.path(currLocation);
                Selection.reset();
                var word = entries.length == 1 ? 'Entry' : entries.length + " entries";
                Util.setFeedback(word + ' successfully deleted', 'success');
            });
        }
    };

    $scope.restoreSelectedEntries = function () {
        var entrySelection = Selection.getSelectedEntries();
        var entryIds = [];

        for (var i = 0; i < entrySelection.length; i++) {
            entryIds.push(parseInt(entrySelection[i].id));
        }

        Util.update("rest/parts", entryIds, {visibility: "OK"}, function () {
            $rootScope.$broadcast("RefreshAfterDeletion");
            $scope.$broadcast("UpdateCollectionCounts");
            $location.path("folders/deleted");

            Selection.reset();
            var word = entryIds.length == 1 ? 'Entry' : entryIds.length + " entries";
            Util.setFeedback(word + ' successfully restored', 'success');
        });
    };

    $scope.submitSelectedImportEntry = function () {
        var entrySelection = Selection.getSelectedEntries();

        Util.update("rest/parts", [parseInt(entrySelection[0].id)], {visibility: "OK"}, function () {
            $rootScope.$broadcast("RefreshAfterDeletion");
            $scope.$broadcast("UpdateCollectionCounts");
            $rootScope.$emit("CollectionSelection", "pending");
            $location.path("folders/pending");
            Selection.reset();
            Util.setFeedback('Entry successfully approved', 'success');
        });
    };

    $rootScope.$on("EntrySelected", function (event, count) {
        $scope.addToDisabled = !count;
    });

    $scope.canAddToFolder = function () {
        return (!$scope.addToDisabled || $scope.selectedRemote.length) && !this.isDealingWithDeleted();
    };

    $scope.canExport = function () {
        return FolderSelection.getSelectedFolder() != undefined || Selection.hasSelection();
    };

    $scope.canEdit = function () {
        return Selection.canEdit();
    };

    $scope.canDelete = function () {
        return Selection.canEdit() && Selection.canDelete();
    };

// Working in "deleted" collection or with deleted entry
    $scope.isDealingWithDeleted = function () {
        if (Selection.getSelectedEntries().length == 0) {
            return $stateParams.collection == 'deleted';
        } else {
            return Selection.getSelectedEntries()[0].visible == "DELETED";
        }
    };

    $scope.canRestore = function () {
        return Selection.canRestore();
    };

    $scope.canApprovePending = function () {
        return Selection.isAdmin();
    };

    $scope.canAcceptTransfer = function () {
        if (Selection.getSelectedEntries().length != 0) {
            return Selection.getSelectedEntries()[0].visible == "TRANSFERRED" && Selection.isAdmin();
        } else {
            return (FolderSelection.getSelectedFolder() && FolderSelection.getSelectedFolder().type == "TRANSFERRED");
        }
    };

// used to enable/disable the transfer action menu button
    $scope.transferAvailable = function () {
        return FolderSelection.getSelectedFolder() != undefined;
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

        if (selectedEntries.length > 1) {
            var type;
            for (type in Selection.getSelectedTypes()) {
                break;   // todo : ??
            }

            // first create bulk upload
            Util.update("rest/uploads", {
                name: "Bulk Edit",
                type: type,
                status: 'BULK_EDIT',
                entryList: selectedEntries
            }, {}, function (result) {
                $location.path("upload/" + result.id);
            });
        } else {
            $location.path('entry/edit/' + selectedEntries[0].id);
        }
        $scope.editDisabled = true;
    };

    $scope.bulkUpdateSequences = function () {
        $uibModal.open({
            controller: function ($scope, FileUploader, Authentication) {
                $scope.bulkSampleEditUploader = new FileUploader({
                    scope: $scope, // to automatically update the html. Default: $rootScope
                    url: "rest/sequences",
                    method: 'PUT',
                    removeAfterUpload: true,
                    headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
                    autoUpload: true,
                    queueLimit: 1 // can only upload 1 file
                });

                $scope.bulkSampleEditUploader.onProgressItem = function (item, progress) {
                    $scope.progress = progress;
                };

                $scope.bulkSampleEditUploader.onAfterAddingFile = function () {
                    $scope.processingFile = false;
                    $scope.result = {success: false};
                };

                $scope.bulkSampleEditUploader.onBeforeUploadItem = function () {
                    $scope.processingFile = true;
                };

                $scope.bulkSampleEditUploader.onCompleteAll = function () {
                    $scope.processingFile = false;
                };

                $scope.bulkSampleEditUploader.onSuccessItem = function (item, response, status, header) {
                    console.log("success", status, response);
                    $scope.processingFile = false;
                    $scope.result = {success: true, data: response};
                };

                $scope.bulkSampleEditUploader.onErrorItem = function (item, response, status, headers) {
                    $scope.result = {success: false};
                    $scope.processingFile = undefined;
                    console.log(item, response, status, headers);
                };
            },
            templateUrl: "views/modal/bulk-sequences-update.html",
            backdrop: 'static',
            size: 'lg'
        });
    };

    $scope.bulkUpdateTraces = function () {
        console.log("foo");
        $uibModal.open({
            controller: function ($scope, FileUploader, Authentication) {
                $scope.bulkTracesEditUploader = new FileUploader({
                    scope: $scope, // to automatically update the html. Default: $rootScope
                    url: "rest/traces",
                    method: 'PUT',
                    removeAfterUpload: true,
                    headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
                    autoUpload: true,
                    queueLimit: 1 // can only upload 1 file
                });

                $scope.bulkTracesEditUploader.onProgressItem = function (item, progress) {
                    $scope.progress = progress;
                };

                $scope.bulkTracesEditUploader.onAfterAddingFile = function () {
                    $scope.processingFile = false;
                    $scope.result = {success: false};
                };

                $scope.bulkTracesEditUploader.onBeforeUploadItem = function () {
                    $scope.processingFile = true;
                };

                $scope.bulkTracesEditUploader.onCompleteAll = function () {
                    $scope.processingFile = false;
                };

                $scope.bulkTracesEditUploader.onSuccessItem = function (item, response, status, header) {
                    $scope.processingFile = false;
                    $scope.result = {success: true, data: response};
                };

                $scope.bulkTracesEditUploader.onErrorItem = function (item, response, status, headers) {
                    $scope.result = {success: false};
                    $scope.processingFile = undefined;
                    console.log(item, response, status, headers);
                };
            },
            templateUrl: "views/modal/bulk-traces-update.html",
            backdrop: 'static',
            size: 'lg'
        });
    };

    $scope.csvExport = function (includeSequences) {
        var selection = getEntrySelection();
        var formats = {sequenceFormats: []};//, entryFields: ["name"]};
        if (includeSequences)
            formats.sequenceFormats.push("genbank");

        // retrieve from server
        Util.post("rest/file/csv", selection, function (result) {
            if (result && result.value) {
                $window.open("rest/file/tmp/" + result.value, "_self");
            }
        }, formats);
    };

    $scope.customizeExport = function () {
        $uibModal.open({
            controller: "CustomExportController",
            templateUrl: "scripts/entry/export/custom-export-modal.html",
            backdrop: 'static',
            size: 'lg',
            resolve: {
                selectedTypes: function () {
                    return Selection.getSelectedTypes()
                },

                selection: function () {
                    return getEntrySelection();
                }
            }
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
        if (folder === "available")
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
        $uibModal.open({
            templateUrl: 'views/modal/transfer-entries-modal.html',
            controller: "TransferEntriesToPartnersModal",
            backdrop: "static",
            resolve: {
                selectedFolder: function () {
                    return $scope.collectionFolderSelected;
                }
            }
        });
    };

    $scope.acceptTransferredEntries = function () {
        var selection = getEntrySelection();
        if (selection.folderId && !Selection.hasSelection()) {
            // approve folder
            Util.update("/rest/folders/" + selection.folderId, {
                id: selection.folderId,
                type: 'PRIVATE'
            }, function (result) {
                console.log(result);
            })
        } else {
            var entrySelection = Selection.getSelectedEntries();

            for (var i = 0; i < entrySelection.length; i++) {
                Util.update("rest/parts", [parseInt(entrySelection[i].id)], {visibility: "OK"}, function () {
                    $rootScope.$broadcast("RefreshAfterDeletion");
                    $scope.$broadcast("UpdateCollectionCounts");
                    $location.path("folders/transferred");
                });
            }

            Selection.reset();
            var word = entrySelection.length == 1 ? ' entry' : ' entries';
            Util.setFeedback('Transfer of ' + entrySelection.length + word + ' successfully accepted', 'success');
        }
    }
});

iceControllers.controller('TransferEntriesToPartnersModal', function ($scope, $uibModalInstance, Util, FolderSelection,
                                                                      $stateParams, Selection, selectedFolder) {
    $scope.selectedFolder = selectedFolder;
    //console.log(FolderSelection.getSelectedFolder(), selectedFolder);

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
                Util.post('rest/partners/' + partner.id + '/entries', entrySelection, function (result) {
                    Selection.reset();
                    $scope.closeModal();
                });
            }
        });
    };

    $scope.selectPartnerForTransfer = function (partner) {
        var indexOf = $scope.selectedPartners.indexOf(partner);
        if (indexOf !== -1) {
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
                                                             Selection, RemoteSelection, move, selectedFolder, $stateParams) {

    // get folders that I can edit
    $scope.getPersonalFolders = function () {
        Util.list("rest/folders", function (result) {
            $scope.userFolders = result;
        }, {canEdit: 'true'});
    };

    // init
    $scope.selectedFolders = [];
    $scope.newFolder = {creating: false};

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
            all: Selection.getSelection().type === 'ALL',
            folderId: folderSelected,
            selectionType: selectionType,
            entryType: Selection.getSelection().type,
            entries: [],
            remoteEntries: [],
            destination: angular.copy($scope.selectedFolders)
        };

        var selectedEntriesObjectArray = Selection.getSelectedEntries();
        for (var i = 0; i < selectedEntriesObjectArray.length; i += 1) {
            entrySelection.entries.push(selectedEntriesObjectArray[i].id);
        }

        for (var j = 0; j < RemoteSelection.getSelection().length; j += 1)
            entrySelection.remoteEntries.push(RemoteSelection.getSelection()[j]);

        return entrySelection;
    };

    $scope.submitNewFolderForCreation = function () {
        if (!$scope.newFolder.folderName) {
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
                    $uibModalInstance.close(res);
                }
            });
        }
    };
});

iceControllers.controller('RegisterController', function ($scope, $resource, $location, Util) {
    $scope.errMsg = undefined;
    $scope.registerSuccess = undefined;
    $scope.processing = undefined;

    $scope.newUser = {
        firstName: undefined,
        lastName: undefined,
        institution: undefined,
        email: undefined,
        about: undefined
    };

    $scope.submit = function () {
        var validates = true;
        $scope.processing = true;
        // validate

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

        if (!validates) {
            $scope.processing = false;
            return;
        }

        Util.post("rest/users", $scope.newUser, function (data) {
            $scope.processing = false;
            if (data.length !== 0)
                $scope.registerSuccess = true;
            else
                $scope.errMsg = "Could not create account";
        }, {}, function (error) {
            $scope.errMsg = "Error creating account";
            $scope.processing = false;
        });
    };

    $scope.cancel = function () {
        $location.path("login");
    }
});

iceControllers.controller('ForgotPasswordController', function ($scope, $resource, $location, Util) {
    $scope.user = {};

    $scope.resetPassword = function () {
        $scope.user.processing = true;

        if ($scope.user.email === undefined) {
            $scope.user.error = true;
            $scope.user.processing = false;
            return;
        }

        Util.post("rest/users/password", $scope.user, function (data) {
            $scope.user.processing = false;
            $scope.user.processed = true;
        }, {}, function (error) {
            $scope.user.error = true;
            $scope.user.processing = false;
        });
    };

    $scope.redirectToLogin = function () {
        $location.path("login");
    }
});

iceControllers.controller('LoginController', function ($scope, $location, $cookies, $rootScope, Util) {

    // init
    $scope.login = {canCreateAccount: false, canChangePassword: false};

    Util.get('rest/config/NEW_REGISTRATION_ALLOWED', function (result) {
        $scope.login.canCreateAccount = (result !== undefined && result.key === 'NEW_REGISTRATION_ALLOWED'
            && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    Util.get('rest/config/PASSWORD_CHANGE_ALLOWED', function (result) {
        $scope.login.canChangePassword = (result !== undefined && result.key === 'PASSWORD_CHANGE_ALLOWED'
            && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    // login function
    $scope.getAccessToken = function () {
        $scope.login.errMsg = undefined;
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
                    $cookies.put('userId', success.email);
                    $cookies.put('sessionId', success.sessionId);
                    var loginDestination = $cookies.loginDestination || '/';
                    $cookies.loginDestination = null;
                    $location.path(loginDestination);
                    Util.clearFeedback();
                } else {
                    $cookies.remove('userId');
                    $cookies.remove('sessionId');
                }
                $scope.login.processing = false;
            }, null, function (error) {
                if (error.status === 401)
                    $scope.login.errMsg = "Login failed";
            });
    };
});

// turning out to be pretty specific to the permissions
iceControllers.controller('GenericTabsController', function ($scope) {
    const panes = $scope.panes = [];

    $scope.activateTab = function (pane) {
        angular.forEach(panes, function (pane) {
            pane.selected = false;
        });
        pane.selected = true;
    };

    this.addPane = function (pane) {
        // activate the first pane that is added
        if (panes.length === 0)
            $scope.activateTab(pane);
        panes.push(pane);
    };
});

iceControllers.controller('PermanentEntryDeletionConfirmationModalController', function (EntryContextUtil, $rootScope, $scope, $location, Util, $uibModalInstance, FolderSelection, Selection, allEntries) {
    $scope.allEntries = allEntries;

    $scope.closeModal = function () {
        $uibModalInstance.close();
    };

    $scope.performAction = function () {
        Util.post("rest/parts/trash", allEntries, function () {
            // retrieve sub folders for selected collection
            $rootScope.$broadcast("RefreshAfterDeletion");
            $scope.closeModal();
            const word = allEntries.length === 1 ? 'Entry' : allEntries.length + " entries";
            Util.setFeedback(word + ' successfully deleted', 'success');
            $rootScope.$emit("CollectionSelection", "deleted");
        });
    }
});
