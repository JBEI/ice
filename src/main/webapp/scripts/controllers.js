'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'vr.directives.slider', 'angularMoment']);

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

        folders.getByType({folderType: "personal"}, function (data) {
            if (data.length)
                $scope.userFolders = data;
        });
    };

    // select a folder in the pull down
    $scope.select = function (folder, $event) {
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
                $location.path("/folders/personal")
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
                $location.path("/upload/" + result.id);
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

    // todo : getSelection should return the selection object that matches EntrySelection
    $scope.csvExport = function () {
        var entries = Selection.getSelectedEntries();
        // if selected.selected
        var selectedIds = [];
        for (var i = 0; i < entries.length; i += 1) {
            selectedIds.push(parseInt(entries[i].id));
        }

        var selection = Selection.getSelection();
        selection.entries = selectedIds;
        var files = Files();

        // retrieve from server
        files.getCSV(selection,
            function (result) {
                if (result && result.value) {
                    $window.open("/rest/file/tmp/" + result.value, "_self");
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
        $location.path("/login");
    }
});

iceControllers.controller('ForgotPasswordController', function ($scope, $resource, $location, User) {
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
        $location.path("/login");
    }
});

iceControllers.controller('MessageController', function ($scope, $location, $cookieStore, $stateParams, Message) {
    var message = Message($cookieStore.get('sessionId'));
    var profileId = $stateParams.id;
    $location.path("/profile/" + profileId + "/messages", false);
    message.query(function (result) {
        $scope.messages = result;
    });
});

iceControllers.controller('ProfileGroupsController', function ($rootScope, $scope, $location, $cookieStore, $stateParams, User, Group) {
    var profileId = $stateParams.id;
    $location.path("/profile/" + profileId + "/groups", false);
    $scope.selectedUsers = [];
    $scope.selectedRemoteUsers = [];
    $scope.myGroups = [];
    $scope.groupsIBelong = [];
    $scope.enteredUser = undefined;
    $scope.showCreateGroup = false;

    var user = User($cookieStore.get('sessionId'));
    var group = Group();

    // init: retrieve groups user belongs to and created
    user.getGroups({userId: profileId}, function (result) {
        angular.forEach(result, function (item) {
            if (item.ownerEmail && item.ownerEmail === $rootScope.user.email)
                $scope.myGroups.push(item);
            else
                $scope.groupsIBelong.push(item);
        });

        $scope.userGroups = result;
    });

    $scope.switchToEditMode = function (selectedGroup) {
        selectedGroup.edit = true;
        group.members({groupId: selectedGroup.id}, function (result) {
            selectedGroup.members = result;
        }, function (error) {
            console.error(error);
            selectedGroup.members = undefined;
        });
    };

    $scope.selectGroupUser = function (selectedGroup, user) {
        var index = selectedGroup.members.indexOf(user);
        if (index == -1)
            selectedGroup.members.push(user);
        else
            selectedGroup.members.splice(index, 1);
    };

    $scope.filterUsers = function (val) {
        if (!val) {
            $scope.userMatches = undefined;
            return;
        }

        $scope.filtering = true;
        user.filter({limit: 10, val: val},
            function (result) {
                $scope.userMatches = result;
                $scope.filtering = false;
            }, function (error) {
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

    $scope.cancelGroupCreate = function () {
        $scope.selectedUsers = undefined;
        $scope.showCreateGroup = false;
        $scope.userMatches = undefined;
    };

    $scope.resetSelectedUsers = function () {
        $scope.selectedUsers = [];
    };

    $scope.createGroup = function (groupName, groupDescription) {
        $scope.newGroup = {label: groupName, description: groupDescription, members: $scope.selectedUsers};
        user.createGroup({userId: profileId}, $scope.newGroup, function (result) {
            $scope.myGroups.splice(0, 0, result);
            $scope.showCreateGroup = false;
        }, function (error) {
            console.error(error);
        })
    };

    $scope.updateGroup = function (selectedGroup) {
        group.update({groupId: selectedGroup.id}, selectedGroup, function (result) {
            selectedGroup.memberCount = selectedGroup.members.length;
            selectedGroup.edit = false;
        }, function (error) {
            console.error(error);
        });
    }
});

iceControllers.controller('ProfileEntryController', function ($scope, $location, $cookieStore, $stateParams, User) {
    $scope.maxSize = 5;
    $scope.currentPage = 1;

    var user = User($cookieStore.get("sessionId"));
    var profileId = $stateParams.id;
    $location.path("/profile/" + profileId + "/entries", false);
    var params = {userId: profileId};

    user.getEntries(params, function (result) {
        $scope.folder = result;
    });

    $scope.sort = function (sortType) {
        $scope.folder = null;
        params.sort = sortType;
        params.offset = 0;
        params.asc = !params.asc;
        user.getEntries(params, function (result) {
            $scope.folder = result;
            $scope.currentPage = 1;
        }, function (error) {
            console.error(error);
        });
    };

    $scope.setUserEntriesPage = function (pageNo) {
        if (pageNo == undefined || isNaN(pageNo))
            pageNo = 1;

        console.log(pageNo);
        $scope.loadingPage = true;
        params.offset = (pageNo - 1) * 15;
        user.getEntries(params, function (result) {
            console.log("result", result);
            $scope.folder = result;
            $scope.loadingPage = false;
        }, function (error) {
            console.error(error);
        });
    };
});

iceControllers.controller('ProfileSamplesController', function ($scope, $cookieStore, $location, $stateParams, User) {
    $scope.maxSize = 15;
    $scope.currentPage = 1;

    $scope.pendingSampleRequests = undefined;

    var user = User($cookieStore.get("sessionId"));
    var profileId = $stateParams.id;
    user.samples({userId: profileId},
        function (result) {
            $scope.userSamples = result;
        }, function (error) {
            console.error(error);
        });

    $scope.setUserSamplePage = function (pageNo) {
        if (pageNo == undefined || isNaN(pageNo))
            pageNo = 1;

        $scope.loadingPage = true;
        $scope.offset = (pageNo - 1) * 15;
        user.samples({offset: $scope.offset}, {userId: profileId},
            function (result) {
                $scope.userSamples = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.loadingPage = false;
            });
    }
});

iceControllers.controller('ProfileController', function ($scope, $location, $cookieStore, $rootScope, $stateParams, User, Settings) {
    $scope.showChangePassword = false;
    $scope.showEditProfile = false;
    $scope.showSendMessage = false;
    $scope.changePass = {};
    $scope.passwordChangeAllowed = false;

    // get settings
    Settings().getSetting({key: 'PASSWORD_CHANGE_ALLOWED'}, function (result) {
        $scope.passwordChangeAllowed = (result.value == 'yes');
    });

    $scope.preferenceEntryDefaults = [
        {display: "Principal Investigator", id: "PRINCIPAL_INVESTIGATOR", help: "Enter Email or Name"},
        {display: "Funding Source", id: "FUNDING_SOURCE"}
    ];
    $scope.searchPreferenceDefaults = [
        {display: "Alias", id: "alias"},
        {display: "Backbone", id: "backbone"},
        {display: "Keywords", id: "keywords"},
        {display: "Name", id: "name"},
        {display: "Part ID", id: "partId"},
        {display: "Summary", id: "summary"}
    ];

    $scope.handleSliderChange = function (model) {
        console.log("handleSliderChange", model);
    };

    $scope.translate = function (value) {
        switch (value) {
            case "1":
            default:
                return "default";
            case "2":
                return "low";
            case "3":
                return "medium";
            case "4":
                return "high";
            case "5":
                return "very high";
        }
    };

    $scope.preferences = {};
    $scope.searchPreferences = {};

    var user = User($cookieStore.get('sessionId'));
    var profileOption = $stateParams.option;
    var profileId = $stateParams.id;

    $scope.savePreference = function (pref) {
        if (!$scope.preferences[pref.id]) {
            pref.invalid = true;
            return;
        }

        user.updatePreference({userId: profileId, value: $scope.preferences[pref.id]}, {preferenceKey: pref.id},
            function (result) {
                pref.edit = false;
            });
    };

    var menuOptions = $scope.profileMenuOptions = [
        {
            url: '/views/profile/profile-information.html',
            display: 'Profile',
            selected: true,
            icon: 'fa-user',
            open: true
        },
        {id: 'prefs', url: '/views/profile/preferences.html', display: 'Preferences', selected: false, icon: 'fa-cog'},
        {id: 'groups', url: '/views/profile/groups.html', display: 'Groups', selected: false, icon: 'fa-group'},
        {
            id: 'messages',
            url: '/views/profile/messages.html',
            display: 'Messages',
            selected: false,
            icon: 'fa-envelope-o'
        },
        {
            id: 'samples',
            url: '/views/profile/samples.html',
            display: 'Samples',
            selected: false,
            icon: 'fa-shopping-cart'
        },
        {
            id: 'entries',
            url: '/views/profile/entries.html',
            display: 'Entries',
            selected: false,
            icon: 'fa-th-list',
            open: true
        }
    ];

    $scope.showSelection = function (index) {
        var selectedOption = menuOptions[index];
        if (!selectedOption)
            return;

        var canViewSelected = selectedOption.open || user.isAdmin || ($scope.profile.email === $rootScope.user.email);
        if (!canViewSelected)
            return;

        angular.forEach(menuOptions, function (details) {
            details.selected = false;
        });
        selectedOption.selected = true;
        $scope.profileOptionSelection = menuOptions[index].url;
        if (selectedOption.id) {
            $location.path("/profile/" + profileId + "/" + selectedOption.id);
        } else {
            $location.path("/profile/" + profileId);
        }
    };

    // initialize view
    if (profileOption === undefined) {
        $scope.profileOptionSelection = menuOptions[0].url;
        menuOptions[0].selected = true;
    } else {
        menuOptions[0].selected = false;
        for (var i = 1; i < menuOptions.length; i += 1) {
            if (menuOptions[i].id === profileOption) {
                $scope.profileOptionSelection = menuOptions[i].url;
                menuOptions[i].selected = true;
                break;
            }
        }

        if ($scope.profileOptionSelection === undefined) {
            $scope.profileOptionSelection = menuOptions[0].url;
            menuOptions[0].selected = true;
        }
    }

    // retrieve profile information from server
    user.query({userId: profileId}, function (result) {
        $scope.profile = result;
        user.getPreferences({userId: profileId}, function (prefs) {
            $scope.profile.preferences = prefs;
            if (prefs.preferences == undefined)
                return;

            for (var i = 0; i < prefs.preferences.length; i += 1) {
                $scope.preferences[prefs.preferences[i].key] = prefs.preferences[i].value;
            }
        });
    });

    $scope.editClick = function (message, profile, password) {
        $scope.showChangePassword = password;
        $scope.showEditProfile = profile;
        $scope.showSendMessage = message;
    };

    $scope.updatePassword = function () {
        var pass = $scope.changePass;
        console.log(pass);

        if (!$scope.changePass || $scope.changePass.current === undefined || !$scope.changePass.current.length) {
            $scope.changePasswordError = "Please enter your current password";
            $scope.currentError = true;
            return;
        }

        // check new password value
        if (pass.new === undefined || pass.new.length === 0) {
            $scope.changePasswordError = "Please enter a new password for your account";
            $scope.newPassError = true;
            return;
        }

        // check for new password confirm value
        if (pass.new2 === undefined || pass.new2.length === 0) {
            $scope.changePasswordError = "Please confirm the new password for your account";
            $scope.newPass2Error = true;
            return;
        }

        // check for matching password values
        if (pass.new2 !== pass.new) {
            $scope.changePasswordError = "The password for your account does not match";
            $scope.newPassError = true;
            $scope.newPass2Error = true;
            return;
        }

        var user = User($cookieStore.get("sessionId"));

        // validate existing password
        var userId = $cookieStore.get('userId');
        $scope.passwordChangeSuccess = undefined;
        $scope.changePasswordError = undefined;

//        var userObj = {sessionId:$cookieStore.get("sessionId"), password:$scope.changePass.current, email:userId};

        // authenticate new password
//        user.resetPassword({}, userObj, function (result) {
//            if (result == null) {
//                $scope.changePasswordError = "Current password is invalid";
//                $scope.currentError = true;
//                return;
//            }

        user.changePassword({},
            {email: userId, password: pass.new},
            function (success) {
                console.log("password change", success);
                if (!success) {
                    $scope.changePasswordError = "There was an error changing your password";
                } else {
                    $scope.passwordChangeSuccess = true;
                }
            }, function (error) {
                $scope.changePasswordError = "There was an error changing your password";
            });
        //  change password
//        }, function (error) {
//            $scope.changePasswordError = "There was an error changing your password";
//        });
    };

    $scope.updateProfile = function () {
        user.update({userId: profileId}, $scope.editProfile, function (result) {
            $scope.profile = result;
            $scope.editClick(false, false, false);
        });
    };

    $scope.switchtoEditMode = function () {
        $scope.editProfile = angular.copy($scope.profile);
    }
});

iceControllers.controller('LoginController', function ($scope, $location, $cookieStore, $cookies, $rootScope, Authentication, Settings, AccessToken) {
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
        $location.path("/register");
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
    $scope.vectorEditor = $sce.trustAsHtml('<object type="application/x-shockwave-flash" data="/swf/ve/VectorEditor.swf?entryId=' + $scope.entryId + '&sessionId=' + $scope.sessionId + '" id="vectoreditor" width="100%" height="' + ($(window).height() - 100) + 'px"><param name="wmode" value="opaque" /></object>');

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
