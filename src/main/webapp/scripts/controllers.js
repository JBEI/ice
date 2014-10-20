'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'vr.directives.slider', 'angularMoment']);

iceControllers.controller('ActionMenuController', function ($scope, $window, $rootScope, $location, $cookieStore, Folders, Entry, WebOfRegistries, Files) {
    $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
    $scope.entrySelected = false;

    // reset all on state change
    $rootScope.$on('$stateChangeStart',
        function (event, toState, toParams, fromState, fromParams) {
            $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
            $scope.entrySelected = false;
        });

    var sid = $cookieStore.get("sessionId");
    var folders = Folders();
    var entry = Entry(sid);
    var selectedEntries = [];
    var selected;
    $scope.selectedFolders = [];

    // retrieve personal list of folders user can add or move parts to
    $scope.retrieveUserFolders = function () {
        $scope.userFolders = undefined;
        $scope.selectedFolders = [];

        folders.getByType({folderType:"personal"}, function (data) {
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

    $scope.addEntriesToFolders = function () {
        var updateFolders = [];
        angular.forEach($scope.selectedFolders, function (folder) {
            folder.entries = angular.copy(selectedEntries);
            updateFolders.push(folder);
        });

        folders.addEntriesToFolders(updateFolders,
            function (result) {
                $scope.updatePersonalCollections();
            });
    };

    $scope.removeEntriesFromFolder = function () {
        // remove selected entries from the current folder
        var entryIds = [];
        angular.forEach(selectedEntries, function (entry) {
            entryIds.push(entry.id);
        });

        folders.removeEntriesFromFolder({folderId:$scope.collectionFolderSelected.id}, entryIds,
            function (result) {
                if (result) {
                    $scope.$broadcast("RefreshAfterDeletion");
                    $scope.$broadcast("UpdateCollectionCounts");
                }
            }, function (error) {

            });
    };

    // remove entries from folder and add to selected folders
    $scope.moveEntriesToFolders = function () {

    };

    $scope.deleteSelectedEntries = function () {
        Entry(sid).moveEntriesToTrash(selectedEntries,
            function (result) {
                $scope.$broadcast("RefreshAfterDeletion");
                $scope.$broadcast("UpdateCollectionCounts");
            }, function (error) {
                console.log(error);
            })
    };

    $scope.$on("EntrySelection", function (event, data) {
        selectedEntries = [];
        selected = data;

        // all selected or some selected
        $scope.entrySelected = data.all || (data.selected && data.selected.length > 0);

        // is reading it so can add to any
        if (!$scope.entrySelected) {
            $scope.addToDisabled = true;
            $scope.editDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
        } else {
            // need read permission but assuming it already exists if can read and select it
            $scope.addToDisabled = false;

            // can remove if user can edit folder (todo : public folders?)
            var canRemove = $scope.collectionFolderSelected != undefined && $scope.collectionFolderSelected.canEdit;
            $scope.removeDisabled = !canRemove;

            // if can canRemove then should be able to also move
            $scope.moveToDisabled = !canRemove;
        }

        // can delete if all have canEdit=true
        var entryType = 'None';
        if (data.selected) {
            for (var i = 0; i < data.selected.length; i += 1) {
                var entry = data.selected[i];
                $scope.deleteDisabled = !entry.canEdit;

                // to be able to edit, they all must be the same type
                if (entryType != entry.type) {
                    // initial value?
                    if (entryType === 'None')
                        entryType = entry.type;
                    else
                        entryType = undefined;
                }
                selectedEntries.push(entry);
            }
        }

        $scope.editDisabled = $scope.deleteDisabled || entryType === 'None' || entryType === undefined;
    });

    $rootScope.$on("EntryRetrieved", function (event, data) {
        $scope.entry = data;
        $scope.editDisabled = !data.canEdit;
        $scope.entrySelected = true;
        $scope.addToDisabled = false;
        var isAdmin = $scope.user.accountType === undefined ? false : $scope.user.accountType.toLowerCase() === "admin";
        $scope.deleteDisabled = ($scope.user.email != $scope.entry.ownerEmail && !isAdmin);
        selected = {selected:[data]};
        // only owners or admins can delete

        // can remove if user can edit folder (todo : public folders?)
        var canRemove = $scope.collectionFolderSelected != undefined && $scope.collectionFolderSelected.canEdit;
        $scope.removeDisabled = !canRemove;

        // if can canRemove then should be able to also move
        $scope.moveToDisabled = !canRemove;

        selectedEntries = [data];
    });

    // function that handles "edit" click
    $scope.editEntry = function () {
        $location.path('/entry/edit/' + $scope.entry.id);
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
        var selectedIds = [];
        for (var i = 0; i < selected.selected.length; i += 1) {
            selectedIds.push(selected.selected[i].id);
        }

        angular.forEach($scope.registryPartners.partners, function (partner) {
            if (partner.selected) {
                WebOfRegistries().transferEntries({partnerId:partner.id}, selectedIds,
                    function (result) {

                    }, function (error) {
                        console.error(error);
                    })
            }
        });
    };

    $scope.csvExport = function () {
        // if selected.selected
        var selectedIds = [];
        for (var i = 0; i < selected.selected.length; i += 1) {
            selectedIds.push(selected.selected[i].id);
        }

        var files = Files();

        // retrieve from server
        files.getCSV(selectedIds,
            function (result) {
                if (result && result.value) {
                    $window.open("/rest/file/tmp/" + result.value, "_self");
                }

//                console.log(result);
//                $scope.$broadcast("RefreshAfterDeletion");
//                $scope.$broadcast("UpdateCollectionCounts");
            }, function (error) {
                console.log(error);
            });

//        if (!selectedEntries.length) {
//            $window.open("/rest/part/" + $scope.entry.id + "/csv?sid=" + $cookieStore.get("sessionId"), "_self");
//        } else {
//           console.log("selected", selected);
//        }
    };

    $rootScope.$on("CollectionSelected", function (event, data) {
        $scope.collectionSelected = data;
    });

    $rootScope.$on("CollectionFolderSelected", function (event, data) {
        $scope.collectionFolderSelected = data;
    });
});

iceControllers.controller('RegisterController', function ($scope, $resource, $location, User) {
    $scope.errMsg = undefined;
    $scope.registerSuccess = undefined;
    $scope.newUser = {firstName:undefined, lastName:undefined, institution:undefined, email:undefined, about:undefined};

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
    $scope.errMsg = undefined;
    $scope.user = {};

    $scope.resetPassword = function () {
        if ($scope.user.email === undefined) {
            $scope.user.error = true;
            return;
        }
        User().resetPassword({}, $scope.user, function (success) {
            $location.path("/login");
        }, function (error) {

        });
    };

    $scope.cancel = function () {
        $location.path("/login");
    }
});

iceControllers.controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $cookieStore, Samples) {
    $rootScope.error = undefined;
    $scope.selectOptions = ['PENDING', 'FULFILLED', 'REJECTED'];

    var samples = Samples($cookieStore.get("sessionId"));
    $scope.maxSize = 5;
    $scope.currentPage = 1;

    // initial sample request (uses default paging values)
    samples.requests(function (result) {
        $scope.sampleRequests = result;
    }, function (data) {
        if (data.status === 401) {
            $location.path('/login');
            return;
        }

        $rootScope.error = data;
    });

    $scope.setSamplePage = function (pageNo) {
        if (pageNo == undefined || isNaN(pageNo))
            pageNo = 1;

        $scope.loadingPage = true;
        $scope.offset = (pageNo - 1) * 15;
        samples.requests({offset:$scope.offset},
            function (result) {
                $scope.sampleRequests = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.loadingPage = false;
            });
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
});

iceControllers.controller('AdminUserController', function ($rootScope, $scope, $stateParams, $cookieStore, User) {
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

iceControllers.controller('AdminTransferredEntriesController', function ($rootScope, $location, $scope, Folders) {
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
});

iceControllers.controller('AdminController', function ($rootScope, $location, $scope, $stateParams, $cookieStore, Settings) {
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
            $rootScope.settings = result;

            angular.forEach($rootScope.settings, function (setting) {
                if (generalSettingKeys.indexOf(setting.key) != -1) {
                    $scope.generalSettings.push({'key':(setting.key.replace(/_/g, ' ')).toLowerCase(), 'value':setting.value, 'editMode':false});
                }

                if (emailSettingKeys.indexOf(setting.key) != -1) {
                    $scope.emailSettings.push({'key':(setting.key.replace(/_/g, ' ')).toLowerCase(), 'value':setting.value, 'editMode':false});
                }
            });
        });
    };

    $scope.adminEditSettingMode = function (type, val) {
        console.log(type, val);
//        console.log(type, $scope[type], $scope.partIdEditMode);
//        $scope[type] = val;
//        console.log(type, $scope[type], $scope.partIdEditMode);
    };

    var menuOption = $stateParams.option;

    var menuOptions = $scope.profileMenuOptions = [
        {url:'/views/admin/settings.html', display:'Settings', selected:true, icon:'fa-cogs'},
        {id:'web', url:'/views/admin/wor.html', display:'Web of Registries', selected:false, icon:'fa-globe'},
        {id:'users', url:'/views/admin/users.html', display:'Users', selected:false, icon:'fa-user'},
        {id:'groups', url:'/views/admin/groups.html', display:'Groups', selected:false, icon:'fa-group'},
        {id:'transferred', url:'/views/admin/transferred.html', display:'Transferred Entries', selected:false, icon:'fa-list'},
        {id:'samples', url:'/views/admin/sample-requests.html', display:'Sample Requests', selected:false, icon:'fa-shopping-cart'}
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
    user.getGroups({userId:profileId}, function (result) {
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
        group.members({groupId:selectedGroup.id}, function (result) {
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
        user.filter({limit:10, val:val},
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
        $scope.newGroup = {label:groupName, description:groupDescription, members:$scope.selectedUsers};
        user.createGroup({userId:profileId}, $scope.newGroup, function (result) {
            $scope.myGroups.splice(0, 0, result);
            $scope.showCreateGroup = false;
        }, function (error) {
            console.error(error);
        })
    };

    $scope.updateGroup = function (selectedGroup) {
        group.update({groupId:selectedGroup.id}, selectedGroup, function (result) {
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
    var params = {userId:profileId};

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
    user.samples({userId:profileId},
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
        user.samples({offset:$scope.offset}, {userId:profileId},
            function (result) {
                $scope.userSamples = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.loadingPage = false;
            });
    }
});

iceControllers.controller('ProfileController', function ($scope, $location, $cookieStore, $rootScope, $stateParams, User) {
    $scope.showChangePassword = false;
    $scope.showEditProfile = false;
    $scope.showSendMessage = false;
    $scope.changePass = {};

    $scope.preferenceEntryDefaults = [
        {display:"Principal Investigator", id:"PRINCIPAL_INVESTIGATOR", help:"Enter Email or Name"},
        {display:"Funding Source", id:"FUNDING_SOURCE"}
    ];
    $scope.searchPreferenceDefaults = [
        {display:"Alias", id:"alias"},
        {display:"Backbone", id:"backbone"},
        {display:"Keywords", id:"keywords"},
        {display:"Name", id:"name"},
        {display:"Part ID", id:"partId"},
        {display:"Summary", id:"summary"}
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

        user.updatePreference({userId:profileId, value:$scope.preferences[pref.id]}, {preferenceKey:pref.id},
            function (result) {
                pref.edit = false;
            });
    };

    var menuOptions = $scope.profileMenuOptions = [
        {url:'/views/profile/profile-information.html', display:'Profile', selected:true, icon:'fa-user', open:true},
        {id:'prefs', url:'/views/profile/preferences.html', display:'Preferences', selected:false, icon:'fa-cog'},
        {id:'groups', url:'/views/profile/groups.html', display:'Groups', selected:false, icon:'fa-group'},
        {id:'messages', url:'/views/profile/messages.html', display:'Messages', selected:false, icon:'fa-envelope-o'},
        {id:'samples', url:'/views/profile/samples.html', display:'Requested Samples', selected:false, icon:'fa-shopping-cart'},
        {id:'entries', url:'/views/profile/entries.html', display:'Entries', selected:false, icon:'fa-th-list', open:true}
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
    user.query({userId:profileId}, function (result) {
        $scope.profile = result;
        user.getPreferences({userId:profileId}, function (prefs) {
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
            {email:userId, password:pass.new},
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
        user.update({userId:profileId}, $scope.editProfile, function (result) {
            $scope.profile = result;
            $scope.editClick(false, false, false);
        });
    };

    $scope.switchtoEditMode = function () {
        $scope.editProfile = angular.copy($scope.profile);
    }
});

// main controller.
iceControllers.controller('CollectionController', function ($scope, $state, $filter, $location, $cookieStore, $rootScope, Folders, Settings, sessionValid, Search, Samples) {
    // todo : set on all
    var searchUrl = "/search";
    if ($location.path().slice(0, searchUrl.length) != searchUrl) {
        $location.search('q', null);
    }

    if (sessionValid === undefined || sessionValid.data.sessionId === undefined) {
        return;
    }

    var sessionId = $cookieStore.get("sessionId");
    $scope.searchFilters = {};
    $rootScope.settings = {};

    // retrieve site wide settings
    var settings = Settings(sessionId);
    settings.get(function (result) {

        for (var i = 0; i < result.length; i += 1) {
            $rootScope.settings[result[i].key] = result[i].value;
        }
    });

    $scope.appVersion = undefined;
    settings.version({}, function (result) {
//        console.log(result);
        $rootScope.appVersion = result.value;
    }, function (error) {
        console.log(error);
    });

    $scope.pageCounts = function (currentPage, resultCount) {
        var maxPageCount = 15;
        var pageNum = ((currentPage - 1) * maxPageCount) + 1;

        // number on this page
        var pageCount = (currentPage * maxPageCount) > resultCount ? resultCount : (currentPage * maxPageCount);
        return pageNum + " - " + $filter('number')(pageCount) + " of " + $filter('number')(resultCount);
    };

    // retrieve user settings

    // default list of collections
    $scope.collectionList = [
        { name:'available', display:'Available', icon:'fa-folder', iconOpen:'fa-folder-open', alwaysVisible:true},
        { name:'personal', display:'Personal', icon:'fa-folder', iconOpen:'fa-folder-open', alwaysVisible:true},
        { name:'shared', display:'Shared', icon:'fa-share-alt', iconOpen:'fa-share-alt', alwaysVisible:false},
        { name:'drafts', display:'Drafts', icon:'fa-edit', iconOpen:'fa-edit', alwaysVisible:false},
        { name:'pending', display:'Pending Approval', icon:'fa-folder', iconOpen:'fa-folder-open', alwaysVisible:false},
        { name:'deleted', display:'Deleted', icon:'fa-trash-o', iconOpen:'fa-trash', alwaysVisible:false}
    ];

    // entry items that can be created
    $scope.items = [
        {name:"Plasmid", type:"plasmid"},
        {name:"Strain", type:"strain"},
        {name:"Part", type:"part"},
        {name:"Arabidopsis Seed", type:"arabidopsis"}
    ];

    if ($location.path() === "/") {
        // change state
        $location.path("/folders/personal");
//        // a bit of a hack. the folders are a child state so when
//        // url/folder/personal is accessed, this code is still executed (stateParams do not help here)
//        // so that causes personal folder to be retrieved twice
//        $scope.folder = undefined; // should already be undefined
//
//        var folders = Folders;
//        folders.folder({folderId:'personal'}, function (result) {
//            $scope.folder = result;
//        });
    }

    var samples = Samples(sessionId);

    // selected entries
    $scope.selection = [];
    $scope.shoppingCartContents = [];
    samples.userRequests({status:'IN_CART'}, {userId:$rootScope.user.id}, function (result) {
        $scope.shoppingCartContents = result.requests;
    });

    $scope.hidePopovers = function (hide) {
        $scope.openShoppingCart = !hide;
    };

    $scope.submitShoppingCart = function () {
        var contentIds = [];
        for (var idx = 0; idx < $scope.shoppingCartContents.length; idx += 1)
            contentIds.push($scope.shoppingCartContents[idx].id);

        samples.submitRequests({status:'PENDING'}, contentIds, function (result) {
            $scope.shoppingCartContents = [];
            $scope.openShoppingCart = false;
        }, function (error) {
            console.error(error);
        })
    };

    // remove sample request
    $scope.removeFromCart = function (content, entry) {
        if (entry) {
            var partId = entry.id;
            for (var idx = 0; idx < $scope.shoppingCartContents.length; idx += 1) {
                if ($scope.shoppingCartContents[idx].partData.id == partId) {
                    content = $scope.shoppingCartContents[idx];
                    break;
                }
            }
        }

        if (content) {
            var contentId = content.id;
            samples.removeRequestFromCart({requestId:contentId}, function (result) {
                var idx = $scope.shoppingCartContents.indexOf(content);
                if (idx >= 0) {
                    $scope.shoppingCartContents.splice(idx, 1);
                } else {
                    // todo : manual scan and remove
                }
            }, function (error) {
                console.error(error);
            });
        }
    };

//    // search
    $scope.runUserSearch = function (filters) {
        $scope.loadingSearchResults = true;

        Search().runAdvancedSearch(filters,
            function (result) {
                $scope.searchResults = result;
                $scope.loadingSearchResults = false;
//                $scope.$broadcast("SearchResultsAvailable", result);
            },
            function (error) {
                $scope.loadingSearchResults = false;
//                $scope.$broadcast("SearchResultsAvailable", undefined);
                $scope.searchResults = undefined;
                console.log(error);
            }
        );
    };

    $scope.$on('SamplesInCart', function (event, data) {
        $scope.shoppingCartContents = data;
    });

    // table
    $scope.alignmentGraph = function (searchResult) {
        var ptsPerPixel = searchResult.queryLength / 100;
        var start = Number.MAX_VALUE;
        var end = Number.MIN_VALUE;
        var stripes = {};

        var started = false;
        var matchDetails = searchResult.matchDetails;
        $scope.headers = {};
        $scope.sequences = {}; //setup scope for query alignment

        for (var i = 0; i < matchDetails.length; i++) {
            var line = matchDetails[i].replace(/"/g, ' ').trim();

            //parse Query for score, gaps, & strand data
            if (line.lastIndexOf("Query", 0) === 0) {
                $scope.headers.score = e;
                var s = matchDetails[1].split(",");
                var c = s[0];
                var o = c.split("=");
                var e = o[1].trim(); //score data
                $scope.headers.gaps = d;
                var g = matchDetails[2].split(",");
                var a = g[1];
                var p = a.split("=");
                var d = p[1].trim();//gaps data
                $scope.headers.strand = r;
                var t = matchDetails[3].split("=");
                var r = (t[1]); //strand data
            }

            if (line.lastIndexOf("Strand", 0) === 0) {
                started = true;
                var l = []; //empty array filled with the following contents
                l = line.split(" "); //separate Query alignment by spaces;
                var tmp = (l[2]); //last nucleotide location for Query alignment
                if (tmp < start) //tmp will always be less than MAX_NUMBER
                    start = tmp;

                tmp = (l[l.length - 1]); //nucleotide bases
                if (tmp > end)
                    end = tmp;
            } else if (line.lastIndexOf("Score", 0) === 0) {
                if (started) {
                    stripes[start] = end;
                    start = Number.MAX_VALUE;
                    end = Number.MIN_VALUE;
                }
            }
        }

        var prevStart = 0;
        var defColor = "#444";
        var stripColor = "";

        // stripe color is based on alignment score
        if (searchResult.score >= 200)
            stripColor = "orange";
        else if (searchResult.score < 200 && searchResult.score >= 80)
            stripColor = "green";
        else if (searchResult.score < 80 && searchResult.score >= 50)
            stripColor = "blue";
        else
            stripColor = "red";

        var fillEnd = 100;
        var html = "<table cellpadding=0 cellspacing=0><tr>";
        var results = [];

        for (var key in stripes) {
            if (!stripes.hasOwnProperty(key))
                continue;

            var stripeStart = key;
            var stripeEnd = stripes[key];

            var stripeBlockLength = (Math.round((stripeEnd - stripeStart) / ptsPerPixel));
            var fillStart = (Math.round(stripeStart / ptsPerPixel));

            var width;
            if (prevStart >= fillStart && prevStart != 0)
                width = 1;
            else
                width = fillStart - prevStart;

            results.push(width);

            html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                + width + "px; height: 10px\"></hr></td>";

            // mark stripe
            prevStart = (fillStart - prevStart) + stripeBlockLength;
            html += "<td><hr style=\"background-color: " + stripColor + "; border: 0px; width: "
                + stripeBlockLength + "px; height: 10px\"></hr></td>";
            fillEnd = fillStart + stripeBlockLength;
        }

        if (fillEnd < 100) {
            html += "<td><hr style=\"background-color: " + defColor + "; border: 0px; width: "
                + (100 - fillEnd) + "px; height: 10px\"></hr></td>";
        }

        html += "</tr></table>";

//        $scope.stripes = stripes;

        return results;
    };
});

iceControllers.controller('CollectionDetailController', function ($scope, $cookieStore, Folders) {
    var sessionId = $cookieStore.get("sessionId");
    var folders = Folders();

    $scope.createCollection = function () {
        var details = {folderName:$scope.newCollectionName};
        folders.create(details, function (result) {
            $scope.selectedCollectionFolders.splice(0, 0, result);
            $scope.newCollectionName = "";
            $scope.hideAddCollection = true;
        });
    };

    $scope.deleteCollection = function (folder) {
        console.log($scope.collectionList, folder);

        folders.delete({folderId:folder.id, type:folder.type}, function (result) {
            var l = $scope.selectedCollectionFolders.length;
            for (var j = 0; j < l; j += 1) {
                if ($scope.selectedCollectionFolders[j].id === result.id) {
                    $scope.selectedCollectionFolders.splice(j, 1);
                    for (var idx = 0; idx < $scope.collectionList.length; idx += 1) {
                        var collection = $scope.collectionList[idx];
                        if (folder.type === 'upload' && collection.name === 'bulkUpload') {
                            collection.count -= folder.count;
                            break;
                        } else if (folder.type === 'folders') {
                        }
                    }
                    break;
                }
            }
        });
    }
});

// deals with sub collections e.g. /folders/:id
// retrieves the contents of folders
iceControllers.controller('CollectionFolderController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, $http, Folders, Entry, EntryContextUtil) {
    var sessionId = $cookieStore.get("sessionId");
    var folders = Folders();
    var entry = Entry(sessionId);

//    var currentSelection = $scope.selectedFolder;
    $scope.breadCrumb = "";

    // param defaults
    $scope.params = {'asc':false, 'sort':'created'};
    var subCollection = $stateParams.collection;   // folder id

    // retrieve sub folder contents
    if (subCollection !== undefined) {
        $scope.folder = undefined;   // this forces "Loading..." to be shown
        $scope.params.folderId = subCollection;

        // retrieve contents of main folder (e,g, "personal")
        folders.folder($scope.params, function (result) {
            $scope.loadingPage = false;
            $scope.folder = result;
//            $rootScope.$emit("CollectionFolderSelected",  $scope.folder);

            if (result.folderName) {
                if ($scope.breadCrumb)
                    $scope.breadCrumb += " / " + result.folderName;
                else
                    $scope.breadCrumb = result.folderName;
            }
            else
                $scope.breadCrumb = $scope.params.folderId;
            $scope.params.count = $scope.folder.count;
        });
    }

    // paging
    $scope.currentPage = 1;
    $scope.maxSize = 5;  // number of clickable pages to show in pagination

    $scope.setPage = function (pageNo) {
        if (pageNo == undefined || isNaN(pageNo))
            pageNo = 1;

        $scope.loadingPage = true;
        if ($scope.params.folderId === undefined)
            $scope.params.folderId = 'personal';
        $scope.params.offset = (pageNo - 1) * 15;
        folders.folder($scope.params, function (result) {
            $scope.folder = result;
            $scope.loadingPage = false;
        });
    };

    $scope.$on("RefreshAfterDeletion", function (event, data) {
        $scope.setPage(1);
    });

    $scope.sort = function (sortType) {
        $scope.folder = null;
        $scope.params.sort = sortType;
        $scope.params.offset = 0;
        $scope.params.asc = !$scope.params.asc;
        console.log("queryParams", $scope.params);
        folders.folder($scope.params, function (result) {
            $scope.folder = result;
            $scope.currentPage = 1;
        });
    };

    $scope.allSelected = false;
    $scope.selectAll = function () {
        $scope.allSelected = !$scope.allSelected;
        $scope.showAllSelected = !$scope.showAllSelected;

        // clear all excluded
        while ($scope.excludedEntries.length > 0) {
            $scope.excludedEntries.pop();
        }

        // and selected
        while ($scope.selectedEntries.length > 0) {
            $scope.selectedEntries.pop();
        }

        $scope.$emit("EntrySelection", {all:$scope.allSelected, folder:$scope.folder});
    };

    $scope.selectedEntries = [];
    $scope.selectedIndex = [];  // maintains only the ids
    $scope.excludedEntries = [];

    $scope.isSelected = function (entry) {
        if ($scope.allSelected) {
            return ($scope.excludedEntries.length == 0 || $scope.excludedEntries.indexOf(entry) === -1);
        }

        return ($scope.selectedEntries.indexOf(entry) !== -1);
    };

    $scope.select = function (entry) {
        if ($scope.allSelected) {
            // check if excluded
            var excludedIndex = $scope.excludedEntries.indexOf(entry);

            // if in excluded, then select (by removing from excluded)
            if (excludedIndex > -1)
                $scope.excludedEntries.splice(excludedIndex, 1);
            else // else de-selected (by adding to excluded list)
                $scope.excludedEntries.push(entry);

            if ($scope.excludedEntries.length === $scope.folder.count) {
                $scope.allSelected = false;

                // clear all excluded
                while ($scope.excludedEntries.length > 0) {
                    $scope.excludedEntries.pop();
                }
            }
        } else {
            // maintain explicit list of selected
            // check if already selected
            var i = $scope.selectedEntries.indexOf(entry);
            if (i === -1)   // not found, add to selectedEntries
                $scope.selectedEntries.push(entry);
            else
                $scope.selectedEntries.splice(i, 1);

            // check if all selected
            if ($scope.selectedEntries.length === $scope.folder.count) {
                $scope.allSelected = true;
                while ($scope.selectedEntries.length > 0) {
                    $scope.selectedEntries.pop();
                }
            }
        }

        $scope.showAllSelected = $scope.allSelected && $scope.excludedEntries.length === 0;
        var selected = $scope.allSelected && $scope.excludedEntries.length !== $scope.folder.count;
        $scope.$emit("EntrySelection", {all:selected, selected:$scope.selectedEntries, folder:$scope.folder});
    };

    $scope.showEntryDetails = function (entry, index) {
        if (!$scope.params.offset) {
            $scope.params.offset = index;
        }

        var offset = (($scope.currentPage - 1) * 15) + index;
        EntryContextUtil.setContextCallback(function (offset, callback) {
            $scope.params.offset = offset;
            $scope.params.limit = 1;

            Folders().folder($scope.params,
                function (result) {
                    callback(result.entries[0].id);
                });
        }, $scope.params.count, offset, "/folders/" + $scope.params.folderId);

        $location.path("/entry/" + entry.id);
    };

    $scope.tooltipDetails = function (e) {
        $scope.currentTooltip = undefined;
        entry.tooltip({partId:e.id},
            function (result) {
                $scope.currentTooltip = result;
            }, function (error) {
                console.error(error);
            });
    };

    // opens a modal that presents user with options to share selected folder
    $scope.openFolderShareSettings = function () {
        var modalInstance = $modal.open({
            templateUrl:'/views/modal/folder-permissions.html',
            controller:"FolderPermissionsController",
            backdrop:"static",
            resolve:{
                folder:function () {
                    return $scope.folder;
                }
            }
        });
    };

    // returns human readable text for permissions. meant to be appended to the String "Shared with "
    // (e.g. "2 users and 3 groups")
    $scope.getShareText = function (permissions) {
        if (permissions === undefined || !permissions.length) {
            return "no one";
        }

        var groupCount = 0;
        var userCount = 0;

        for (var idx = 0; idx < permissions.length; idx += 1) {
            var permission = permissions[idx];
            if (permission.article === 'ACCOUNT')
                userCount += 1;
            else
                groupCount += 1;
        }

        if (userCount == 0)
            return groupCount + (groupCount == 1 ? " group" : " groups");

        if (groupCount == 0)
            return userCount + (userCount == 1 ? " user" : " users");
    };

    $scope.changeFolderType = function (newType) {
        var tmp = {id:$scope.folder.id, type:newType};
        folders.update({id:tmp.id}, tmp, function (result) {
            $scope.folder.type = result.type;
            if (newType === 'PUBLIC')
                $location.path('/folders/available');
            else
                $location.path('/folders/personal');
            // todo : send message to be received by the collection menu
        }, function (error) {
            console.error(error);
        });
    }
});

iceControllers.controller('LoginController', function ($scope, $location, $cookieStore, $cookies, $rootScope, Authentication, Settings, AccessToken) {
    $scope.login = {};

    $scope.submit = function () {
        $scope.errMsg = undefined;

        // validate email
        if ($scope.login.email === undefined || $scope.login.email.trim() === "") {
            $scope.login.emailError = true;
        }

        // validate password
        if ($scope.login.password === undefined || $scope.login.password.trim() === "") {
            $scope.login.passwordError = true;
        }

        if ($scope.login.emailError || $scope.login.passwordError) {
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
            },
            function (error) {
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

    Settings().getSetting({key:'NEW_REGISTRATION_ALLOWED'}, function (result) {
        $scope.canCreateAccount = (result !== undefined && result.key === 'NEW_REGISTRATION_ALLOWED'
            && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });

    Settings().getSetting({key:'PASSWORD_CHANGE_ALLOWED'}, function (result) {
        $scope.canChangePassword = (result !== undefined && result.key === 'PASSWORD_CHANGE_ALLOWED'
            && (result.value.toLowerCase() === 'yes' || result.value.toLowerCase() === 'true'));
    });
});

iceControllers.controller('EditEntryController',
    function ($scope, $http, $location, $cookieStore, $rootScope, $stateParams, Entry, EntryService) {

        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);
        $scope.entry = undefined;
        entry.query({partId:$stateParams.id}, function (result) {
            $scope.entry = EntryService.convertToUIForm(result);
            $scope.entry.linkedParts = [];

            // todo : this is used in other places and should be in a service
            // convert selection markers from array of strings to array of objects for the ui
            var arrayLength = result.selectionMarkers.length;
            if (arrayLength) {
                var tmp = [];
                for (var i = 0; i < arrayLength; i++) {
                    tmp.push({value:result.selectionMarkers[i]});
                }
                angular.copy(tmp, $scope.entry.selectionMarkers);
            } else {
                $scope.entry.selectionMarkers = [
                    {}
                ];
            }

            // convert links from array of strings to array of objects for the ui
            var linkLength = result.links.length;

            if (linkLength) {
                var tmpLinkObjectArray = [];
                for (var j = 0; j < linkLength; j++) {
                    tmpLinkObjectArray.push({value:result.links[j]});
                }
                angular.copy(tmpLinkObjectArray, $scope.entry.links);
            } else {
                $scope.entry.links = [
                    {}
                ];
            }

            if (result.bioSafetyLevel)
                $scope.entry.bioSafetyLevel = "Level " + result.bioSafetyLevel;
            $scope.linkOptions = EntryService.linkOptions(result.type);
            $scope.selectedFields = EntryService.getFieldsForType(result.type);
            $scope.activePart = $scope.entry;
            console.log($scope.activePart);
        });

        $scope.cancelEdit = function () {
            $location.path("/entry/" + $stateParams.id);
        };

        $scope.getLocation = function (inputField, val) {   // todo : move to service
            return $http.get('/rest/parts/autocomplete', {
                headers:{'X-ICE-Authentication-SessionId':sid},
                params:{
                    val:val,
                    field:inputField
                }
            }).then(function (res) {
                    return res.data;
                });
        };

        // difference between this and getLocation() is getLocation() returns a list of strings
        // and this returns a list of objects
        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('/rest/parts/autocomplete/partid', {
                headers:{'X-ICE-Authentication-SessionId':sid},
                params:{
                    token:val
                }
            }).then(function (res) {
                    return res.data;
                });
        };

        $scope.addExistingPartLink = function ($item, $model, $label) {
            entry.query({partId:$model.id}, function (result) {
                $scope.activePart = result;

                // convert selection markers from array of strings to array of objects for the ui
                var arrayLength = result.selectionMarkers.length;
                if (arrayLength) {
                    var tmp = [];
                    for (var i = 0; i < arrayLength; i++) {
                        tmp.push({value:result.selectionMarkers[i]});
                    }
                    angular.copy(tmp, $scope.activePart.selectionMarkers);
                } else {
                    $scope.activePart.selectionMarkers = [
                        {}
                    ];
                }

                // convert links from array of strings to array of objects for the ui
                var linkLength = result.links.length;

                if (linkLength) {
                    var tmpLinkObjectArray = [];
                    for (var j = 0; j < linkLength; j++) {
                        tmpLinkObjectArray.push({value:result.links[j]});
                    }
                    angular.copy(tmpLinkObjectArray, $scope.activePart.links);
                } else {
                    $scope.activePart.links = [
                        {}
                    ];
                }

                $scope.activePart.isExistingPart = true;
                $scope.addExisting = false;
                $scope.entry.linkedParts.push($scope.activePart);

                $scope.colLength = 11 - $scope.entry.linkedParts.length;
                $scope.active = $scope.entry.linkedParts.length - 1;
            });
        };

        // todo : this is pretty much a copy of submitPart in CreateEntryController
        $scope.editEntry = function () {
            var canSubmit = EntryService.validateFields($scope.entry, $scope.selectedFields);
            $scope.entry.type = $scope.entry.type.toUpperCase();


            // validate contained parts, if any
            if ($scope.entry.linkedParts && $scope.entry.linkedParts.length) {
                for (var idx = 0; idx < $scope.entry.linkedParts.length; idx += 1) {
                    var canSubmitLinked = EntryService.validateFields($scope.entry.linkedParts[idx], $scope.selectedFields);
                    if (!canSubmitLinked) {
                        // show icon in tab
                        console.log("linked entry at idx " + idx + " is not valid", $scope.entry);
                        canSubmit = canSubmitLinked;
                    }
                }
            }

            if (!canSubmit) {
                $("body").animate({scrollTop:130}, "slow");
                return;
            }

            if ($scope.entry.bioSafetyLevel === 'Level 1')
                $scope.entry.bioSafetyLevel = 1;
            else
                $scope.entry.bioSafetyLevel = 2;

            // convert arrays of objects to array strings
            $scope.entry.links = EntryService.toStringArray($scope.entry.links);
            $scope.entry.selectionMarkers = EntryService.toStringArray($scope.entry.selectionMarkers);

            for (var i = 0; i < $scope.entry.linkedParts.length; i += 1) {
                $scope.entry.linkedParts[i].links = EntryService.toStringArray($scope.entry.linkedParts[i].links);
                $scope.entry.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.entry.linkedParts[i].selectionMarkers);
            }

            // convert the part to a form the server can work with
            $scope.entry = EntryService.getTypeData($scope.entry);

            entry.update({partId:$scope.entry.id}, $scope.entry, function (result) {
                $location.path("/entry/" + result.id);
            });
        };

        $scope.setActive = function (index) {
            if (!isNaN(index) && $scope.entry.linkedParts.length <= index)
                return;

            $scope.active = index;
            if (isNaN(index))
                $scope.activePart = $scope.entry;
            else
                $scope.activePart = $scope.entry.linkedParts[index];
            $scope.selectedFields = EntryService.getFieldsForType($scope.activePart.type);
        };
    });

iceControllers.controller('CreateEntryController',
    function ($http, $scope, $modal, $rootScope, $fileUploader, $location, $stateParams, $cookieStore, Entry, EntryService) {
        $scope.createType = $stateParams.type;
        $scope.showMain = true;

        // generate the various link options for selected option
        $scope.linkOptions = EntryService.linkOptions($scope.createType.toLowerCase());
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);

        // retrieves the defaults for the specified type. Note that $scope.part is the main part
        var getPartDefaults = function (type, isMain) {
            entry.query({partId:type}, function (result) {
                if (isMain) { // or if !$scope.part
                    $scope.part = result;
                    $scope.part.bioSafetyLevel = '1';
                    $scope.part.linkedParts = [];
                    $scope.part.links = [
                        {value:''}
                    ];
                    $scope.part.selectionMarkers = [
                        {value:''}
                    ];
                    $scope.part.status = 'Complete';
                    $scope.activePart = $scope.part;
                    $scope.selectedFields = EntryService.getFieldsForType($scope.createType);
                } else {
                    var newPart = result;
                    newPart.links = [];
                    newPart.selectionMarkers = [];
                    newPart.bioSafetyLevel = '1';
                    newPart.status = 'Complete';

                    $scope.selectedFields = EntryService.getFieldsForType(type);
                    $scope.part.linkedParts.push(newPart);

                    $scope.colLength = 11 - $scope.part.linkedParts.length;
                    $scope.active = $scope.part.linkedParts.length - 1;
                    $scope.activePart = $scope.part.linkedParts[$scope.active];
                }
            }, function (error) {
                console.log("Error: " + error);
            });
        };

        getPartDefaults($scope.createType, true);

        $scope.addLink = function (schema) {
            $scope.part[schema].push({value:''});
        };

        $scope.removeLink = function (schema, index) {
            $scope.part[schema].splice(index, 1);
//            $scope.colLength = 11 - $scope.part.linkedParts.length;
        };

        $scope.addNewPartLink = function (type) {
            getPartDefaults(type, false);
        };

        $scope.addExistingPartLink = function ($item, $model, $label) {
            entry.query({partId:$model.id}, function (result) {
                $scope.activePart = result;
                $scope.activePart.isExistingPart = true;
                $scope.addExisting = false;
                $scope.part.linkedParts.push($scope.activePart);

                $scope.colLength = 11 - $scope.part.linkedParts.length;
                $scope.active = $scope.part.linkedParts.length - 1;
            });
        };

        $scope.deleteNewPartLink = function (linkedPart) {
            var indexOf = $scope.part.linkedParts.indexOf(linkedPart);
            if (indexOf < 0 || indexOf >= $scope.part.linkedParts.length)
                return;

            console.log("delete", linkedPart, "at", indexOf);
            $scope.part.linkedParts.splice(indexOf, 1);

            // todo: will need to actually delete it if has an id (linkedPart.id)

            // remove from array of linked parts
            if ($scope.active === indexOf) {
                var newActive;
                // set new active
                console.log("set new active", $scope.part.linkedParts.length);
                if (indexOf + 1 < $scope.part.linkedParts.length)
                    newActive = indexOf + 1;
                else {
                    if ($scope.part.linkedParts.length === 0)
                    // not really needed since when main will not be shown if no other tabs present
                        newActive = 'main';
                    else
                        newActive = indexOf - 1;
                }
                $scope.setActive(newActive);
            }
        };

        $scope.active = 'main';
        $scope.setActive = function (index) {
            if (!isNaN(index) && $scope.part.linkedParts.length <= index)
                return;

            $scope.active = index;
            if (isNaN(index))
                $scope.activePart = $scope.part;
            else
                $scope.activePart = $scope.part.linkedParts[index];
            $scope.selectedFields = EntryService.getFieldsForType($scope.activePart.type);
        };

        $scope.getLocation = function (inputField, val) {
            return $http.get('/rest/parts/autocomplete', {
                headers:{'X-ICE-Authentication-SessionId':sid},
                params:{
                    val:val,
                    field:inputField
                }
            }).then(function (res) {
                    return res.data;
                });
        };

        $scope.nameMissing = false;
        $scope.creatorMissing = false;
        $scope.creatorEmailMissing = false;
        $scope.principalInvestigatorMissing = false;
        $scope.summaryMissing = false;

        $scope.submitPart = function () {
            // validate main part
            var canSubmit = EntryService.validateFields($scope.part, $scope.selectedFields);
            $scope.part.type = $scope.part.type.toUpperCase();

            // validate contained parts, if any
            if ($scope.part.linkedParts && $scope.part.linkedParts.length) {
                for (var idx = 0; idx < $scope.part.linkedParts.length; idx += 1) {
                    var canSubmitLinked = EntryService.validateFields($scope.part.linkedParts[idx], $scope.selectedFields);
                    if (!canSubmitLinked) {
                        // show icon in tab
                        // todo
                        console.log("linked entry at idx " + idx + " is not valid");
                        canSubmit = canSubmitLinked;
                    }
                }
            }

            if (!canSubmit) {
                $("body").animate({scrollTop:130}, "slow");
                return;
            }

            // convert arrays of objects to array strings
            $scope.part.links = EntryService.toStringArray($scope.part.links);
            $scope.part.selectionMarkers = EntryService.toStringArray($scope.part.selectionMarkers);

            for (var i = 0; i < $scope.part.linkedParts.length; i += 1) {
                $scope.part.linkedParts[i].links = EntryService.toStringArray($scope.part.linkedParts[i].links);
                $scope.part.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.part.linkedParts[i].selectionMarkers);
            }

            // convert the part to a form the server can work with
            $scope.part = EntryService.getTypeData($scope.part);

            // create or update the part depending on whether there is a current part id
            if ($scope.part.id) {
                entry.update({partId:$scope.part.id}, $scope.part, function (result) {
                    $location.path('/entry/' + result.id);
                });
            } else {
                entry.create($scope.part, function (result) {
                    $scope.$emit("UpdateCollectionCounts");
                    $location.path('/entry/' + result.id);
                    $scope.showSBOL = false;
                }, function (error) {
                    console.error(error);
                });
            }
        };

        $scope.format = 'MMM d, yyyy h:mm:ss a';

        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('/rest/parts/autocomplete/partid', {
                headers:{'X-ICE-Authentication-SessionId':sid},
                params:{
                    token:val
                }
            }).then(function (res) {
                    return res.data;
                });
        };

        // for the date picker TODO : make it a directive ???
        $scope.today = function () {
            $scope.dt = new Date();
        };
        $scope.today();

//    $scope.showWeeks = true;
//    $scope.toggleWeeks = function () {
//        $scope.showWeeks = ! $scope.showWeeks;
//    };

        $scope.clear = function () {
            $scope.dt = null;
        };

        $scope.dateOptions = {
            'year-format':"'yy'",
            'starting-day':1
        };

        $scope.cancelEntryCreate = function () {
            $location.path("/folders/personal");
        };

        // file upload
        var uploader = $scope.sequenceFileUpload = $fileUploader.create({
            scope:$scope, // to automatically update the html. Default: $rootScope
            url:"/rest/file/sequence",
            method:'POST',
//        formData:[{entryType:type}],
            removeAfterUpload:true,
            headers:{"X-ICE-Authentication-SessionId":sid},
            autoUpload:true,
            queueLimit:1 // can only upload 1 file
        });

        $scope.uploadFile = function () {
            if (!$scope.isPaste) {
                uploader.queue[0].upload();
            } else {
                console.log($scope.pastedSequence);
            }
        };

        // REGISTER HANDLERS
        uploader.bind('afteraddingfile', function (event, item) {
            console.info('After adding a file', item);
            $scope.serverError = false;
        });

        uploader.bind('beforeupload', function (event, item) {
            var entryTypeForm;
            if ($scope.active === 'main')
                entryTypeForm = {entryType:$scope.part.type.toUpperCase()};
            else
                entryTypeForm = {entryType:$scope.part.linkedParts[$scope.active].type};
            item.formData.push(entryTypeForm);
        });

        uploader.bind('progress', function (event, item, progress) {
            if (progress != "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        });

        uploader.bind('success', function (event, xhr, item, response) {
            if ($scope.active === undefined || isNaN($scope.active)) {
                // set main entry id
                $scope.part.id = response.entryId;
                $scope.part.hasSequence = true;
            } else {
                // set linked parts id
                $scope.part.linkedParts[$scope.active].id = response.entryId;
                $scope.part.linkedParts[$scope.active].hasSequence = true;
            }

            $scope.activePart.hasSequence = true;
        });

        uploader.bind('error', function (event, xhr, item, response) {
            item.remove();
            $scope.serverError = true;
        });

        uploader.bind('completeall', function (event, items) {
            $scope.processingFile = undefined;
        });
    });

iceControllers.controller('SequenceFileUploadController', function ($scope, $cookieStore, $modal, $modalInstance, $fileUploader, type, paste) {
    console.log("SequenceFileUploadController");
    var sid = $cookieStore.get("sessionId");
    $scope.isPaste = paste;
    $scope.headerText = paste ? "Paste Sequence" : "Upload Sequence file";

    var uploader = $scope.sequenceFileUpload = $fileUploader.create({
        scope:$scope, // to automatically update the html. Default: $rootScope
        url:"/rest/file/sequence",
        method:'POST',
        formData:[
            {
                entryType:type
            }
        ],
//        removeAfterUpload: true,
        headers:{"X-ICE-Authentication-SessionId":sid},
//        autoUpload: true
        queueLimit:1 // can only upload 1 file
    });

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.uploadFile = function () {
        if (!$scope.isPaste) {
            uploader.queue[0].upload();
        } else {
            console.log($scope.pastedSequence);
        }
    };

    // REGISTER HANDLERS
    uploader.bind('afteraddingfile', function (event, item) {
        console.info('After adding a file', item);
        $scope.$emit("FileAdd", item);
    });

    uploader.bind('whenaddingfilefailed', function (event, item) {
        console.info('When adding a file failed', item);
    });

    uploader.bind('afteraddingall', function (event, items) {
        console.info('After adding all files', items);
    });

    uploader.bind('beforeupload', function (event, item) {
        console.info('Before upload', item);
    });

    uploader.bind('progress', function (event, item, progress) {
        console.info('Progress: ' + progress, item);
    });

    uploader.bind('success', function (event, xhr, item, response) {
        console.info('Success', xhr, item, response);
    });

    uploader.bind('cancel', function (event, xhr, item) {
        console.info('Cancel', xhr, item);
    });

    uploader.bind('error', function (event, xhr, item, response) {
        console.info('Error', xhr, item, response);
        item.remove();
        $scope.serverError = true;
    });

    uploader.bind('complete', function (event, xhr, item, response) {
        console.info('Complete', xhr, item, response);
    });

    uploader.bind('progressall', function (event, progress) {
        console.info('Total progress: ' + progress);
    });

    uploader.bind('completeall', function (event, items) {
        console.info('Complete all', items);
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

iceControllers.controller('EntryPermissionController', function ($rootScope, $scope, $cookieStore, User, Entry, Group, filterFilter, Permission) {
        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        var panes = $scope.panes = [];
        $scope.userFilterInput = undefined;

        $scope.activateTab = function (pane) {
            angular.forEach(panes, function (pane) {
                pane.selected = false;
            });
            pane.selected = true;
            if (pane.title === 'Read')
                $scope.activePermissions = angular.copy($scope.readPermissions);
            else
                $scope.activePermissions = angular.copy($scope.writePermissions);

            angular.forEach($scope.users, function (item) {
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    item.selected = (item.id !== undefined && item.id === $scope.activePermissions[i].articleId);
                }
            });
        };

        this.addPane = function (pane) {
            // activate the first pane that is added
            if (panes.length == 0)
                $scope.activateTab(pane);
            panes.push(pane);
        };

        entry.permissions({partId:$scope.entry.id}, function (result) {
            $scope.readPermissions = [];
            $scope.writePermissions = [];

            angular.forEach(result, function (item) {
                if (item.type === 'WRITE_ENTRY')
                    $scope.writePermissions.push(item);
                else
                    $scope.readPermissions.push(item);
            });

            $scope.panes.push({title:'Read', count:$scope.readPermissions.length, selected:true});
            $scope.panes.push({title:'Write', count:$scope.writePermissions.length});

            $scope.activePermissions = angular.copy($scope.readPermissions);
        });

        $scope.filter = function () {
            var val = $scope.userFilterInput;
            if (!val) {
                $scope.accessPermissions = undefined;
                return;
            }

            $scope.filtering = true;
            Permission().filterUsersAndGroups({limit:10, val:val},
                function (result) {
                    $scope.accessPermissions = result;
                    $scope.filtering = false;
                }, function (error) {
                    $scope.filtering = false;
                    $scope.accessPermissions = undefined;
                });
        };

        $scope.showAddPermissionOptionsClick = function () {
            $scope.showPermissionInput = true;
        };

        $scope.watchInput = function () {
            $scope.filteredUsers = filterFilter($scope.users, $scope.userFilterInput);
        };

        $scope.closePermissionOptions = function () {
            $scope.users = undefined;
            $scope.showPermissionInput = false;
        };

        var removePermission = function (permissionId) {
            entry.removePermission({partId:$scope.entry.id, permissionId:permissionId},
                function (result) {
                    // check which pane is selected
                    var p1 = $scope.panes[0];
                    var p2 = $scope.panes[1];

                    for (var i = 0; i < $scope.panes.length; i += 1) {
                        var pane = $scope.panes[i];
                        if (!pane.selected)
                            continue;

                        var permissionsToIterate;
                        if (pane.title === 'Read') {
                            // read pane
                            permissionsToIterate = $scope.readPermissions;
                        } else {
                            // write pane
                            permissionsToIterate = $scope.writePermissions;
                        }

                        for (var idx = 0; idx < permissionsToIterate.length; idx += 1) {
                            if (permissionId != permissionsToIterate[idx].id)
                                continue;

                            permissionsToIterate.splice(i, 1);
                            break;
                        }

                        $scope.activePermissions = permissionsToIterate;
                        break;
                    }
                });
        };

        // when user clicks on the check box
        $scope.addRemovePermission = function (permission) {
            permission.selected = !permission.selected;
            if (!permission.selected) {
                removePermission(permission.id);
                return;
            }

            // add permission
            var type;
            for (var i = 0; i < panes.length; i += 1) {
                if (panes[i].selected) {
                    permission.type = panes[i].title.toUpperCase() + "_ENTRY";
                    break;
                }
            }

            permission.typeId = $scope.entry.id;

            entry.addPermission({partId:$scope.entry.id}, permission, function (result) {
                // result is the permission object
                $scope.entry.id = result.typeId;
                $scope.activePermissions.push(result);
                permission.permissionId = result.id;
            });
        };

        $scope.enablePublicRead = function (e) {
            entry.enablePublicRead(e, function (result) {
                $scope.entry.publicRead = true;
            })
        };

        $scope.disablePublicRead = function (e) {
            entry.disablePublicRead({partId:e.id}, function (result) {
                $scope.entry.publicRead = false;
            })
        };

        $scope.deletePermission = function (index, permission) {
            entry.removePermission({partId:$scope.entry.id, permissionId:permission.id},
                function (result) {
                    if (!result)
                        return;

                    var foundIndex = -1;
                    for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                        if (permission.id == $scope.activePermissions[i].id) {
                            foundIndex = i;
                            break;
                        }
                    }
                    if (foundIndex > -1)
                        $scope.activePermissions.splice(foundIndex, 1);

//                    angular.forEach($scope.activePermissions, function(permission){
//                        if()
//                    })
//                    console.log(index, $scope.activePermissions);
//                    $scope.activePermissions.splice(index, 1);
//                    console.log(index, $scope.activePermissions);
                });
        };
    }
)
;

iceControllers.controller('EntryDetailsController', function ($scope) {
    console.log("EntryDetailsController");
    var entryPanes = $scope.entryPanes = [];

    $scope.showPane = function (pane) {
        console.log("activate details", entryPanes.length);
        angular.forEach(entryPanes, function (pane) {
            pane.selected = false;
        });
        pane.selected = true;
    };

    this.addEntryPane = function (pane) {
        // activate the first pane that is added
        if (entryPanes.length == 0)
            $scope.activateTab(pane);
        entryPanes.push(pane);
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

iceControllers.controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $modal, $rootScope, $fileUploader, Entry, Folders, EntryService, EntryContextUtil) {
    $scope.partIdEditMode = false;
    $scope.showSBOL = true;
    $scope.context = EntryContextUtil.getContext();
    $scope.isFileUpload = false;

    var sessionId = $cookieStore.get("sessionId");
    $scope.sessionId = sessionId;

    $scope.open = function () {
        window.open('/static/swf/ve/VectorEditor?entryId=' + $scope.entry.id + '&sessionId=' + sessionId);
    };

    $scope.sequenceUpload = function (type) {
        if (type === 'file') {
            $scope.isFileUpload = true;
            $scope.isPaste = false;
        }
        else {
            $scope.isPaste = true;
            $scope.isFileUpload = false;
        }
    };

    $scope.processPastedSequence = function (event, part) {
        var sequenceString = event.originalEvent.clipboardData.getData('text/plain');
        entry.addSequenceAsString({partId:part.id}, {sequence:sequenceString}, function (result) {
            part.hasSequence = true;
        }, function (error) {
            console.log("error", error);
        });
    };

    $scope.deleteSequence = function (part) {
        var modalInstance = $modal.open({
            templateUrl:'/views/modal/delete-sequence-confirmation.html',
            controller:function ($scope, $modalInstance) {
                $scope.toDelete = part;
                $scope.processingDelete = undefined;
                $scope.delete = function () {
                    $scope.processingDelete = true;
                    entry.deleteSequence({partId:part.id}, function (result) {
                        $scope.processingDelete = false;
                        $modalInstance.close(part);
                    }, function (error) {
                        console.error(error);
                    })
                }
            },
            backdrop:"static"
        });

        modalInstance.result.then(function (part) {
            if (part)
                part.hasSequence = false;
        }, function () {
        });
    };

    var entry = Entry(sessionId);

    $scope.addLink = function (part) {

        var modalInstance = $modal.open({
            templateUrl:'/views/modal/add-link-modal.html',
            controller:function ($scope, $http, $modalInstance, $cookieStore) {
                $scope.mainEntry = part;
                var sessionId = $cookieStore.get("sessionId");
                var originalLinks = angular.copy($scope.mainEntry.linkedParts);
                $scope.getEntriesByPartNumber = function (val) {
                    return $http.get('/rest/parts/autocomplete/partid', {
                        headers:{'X-ICE-Authentication-SessionId':sessionId},
                        params:{
                            token:val
                        }
                    }).then(function (res) {
                            return res.data;
                        });
                };

                $scope.addExistingPartLink = function ($item, $model, $label) {
                    if ($item.id == $scope.mainEntry.id)
                        return;

                    var found = false;
                    angular.forEach($scope.mainEntry.linkedParts, function (t) {
                        if (t.id === $item.id) {
                            found = true;
                        }
                    });

                    if (found)
                        return;
                    $scope.mainEntry.linkedParts.push($item);
                    $scope.addExistingPartNumber = undefined;
                };

                $scope.removeExistingPartLink = function (link) {
                    var i = $scope.mainEntry.linkedParts.indexOf(link);
                    if (i < 0)
                        return;

                    $scope.mainEntry.linkedParts.splice(i, 1);
                };

                $scope.processLinkAdd = function () {
                    entry.update($scope.mainEntry, function (result) {
                        entry.query({partId:result.id}, function (result) {
                            $modalInstance.close(result);
                        }, function (error) {
                            console.error(error);
                        })
                    }, function (error) {
                        console.error(error);
                    })
                };

                $scope.cancelAddLink = function () {
                    $scope.mainEntry.linkedParts = originalLinks;
                    $modalInstance.close();
                }
            },
            backdrop:"static"
        });

        modalInstance.result.then(function (entry) {
            if (entry) {
                part = entry;
            }
        }, function () {
        });
    };

    var partDefaults = {
        type:$scope.createType,
        links:[
            {}
        ],
        selectionMarkers:[
            {}
        ],
        bioSafetyLevel:'1',
        status:'Complete',
        creator:$scope.user.firstName + ' ' + $scope.user.lastName,
        creatorEmail:$scope.user.email
    };

    $scope.part = angular.copy(partDefaults);

    $scope.sbolShowHide = function () {
        $scope.showSBOL = !$scope.showSBOL;
    };

    $scope.entryFields = undefined;
    $scope.entry = undefined;

    if (!isNaN($stateParams.id)) {
        entry.query({partId:$stateParams.id},
            function (result) {
                $rootScope.$broadcast("EntryRetrieved", result);
                $scope.entry = EntryService.convertToUIForm(result);
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());

                entry.statistics({partId:$stateParams.id}, function (stats) {
                    $scope.entryStatistics = stats;
                });
            }, function (error) {
                console.error(error);
            });
    }

    var menuSubDetails = $scope.subDetails = [
        {url:'/views/entry/general-information.html', display:'General Information', isPrivileged:false, icon:'fa-exclamation-circle'},
        {id:'sequences', url:'/views/entry/sequence-analysis.html', display:'Sequence Analysis', isPrivileged:false, countName:'traceSequenceCount', icon:'fa-search-plus'},
        {id:'comments', url:'/views/entry/comments.html', display:'Comments', isPrivileged:false, countName:'commentCount', icon:'fa-comments-o'},
        {id:'samples', url:'/views/entry/samples.html', display:'Samples', isPrivileged:false, countName:'sampleCount', icon:'fa-flask'},
        {id:'history', url:'/views/entry/history.html', display:'History', isPrivileged:true, countName:'historyCount', icon:'fa-history'},
        {id:'experiments', url:'/views/entry/experiments.html', display:'Experimental Data', isPrivileged:false, countName:'experimentalDataCount', icon:'fa-magic'}
    ];

    $scope.showSelection = function (index) {
        angular.forEach(menuSubDetails, function (details) {
            details.selected = false;
        });
        menuSubDetails[index].selected = true;
        $scope.selection = menuSubDetails[index].url;
        if (menuSubDetails[index].id) {
            $location.path("/entry/" + $stateParams.id + "/" + menuSubDetails[index].id);
        } else {
            $location.path("/entry/" + $stateParams.id);
        }
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

    $scope.edit = function (type, val) {
        $scope[type] = val;
    };

    $scope.quickEditEntry = function (field) {
        // dirty is used to flag that the field's value has been modified to
        // prevent saving unchanged values on blur

        field.errorUpdating = false;

        if (!field.dirty) {
            return;
//            field.edit = false;
        }

        field.updating = true;

        entry.update($scope.entry, function (result) {
            field.edit = false;

            if (result)
                $scope.entry = result;

            field.dirty = false;
            field.updating = false;
        }, function (error) {
            console.error(error);
            field.updating = false;
            field.errorUpdating = true;
        });
    };

    var folders = Folders();
    $scope.nextEntryInContext = function () {
        $scope.context.offset += 1;
        $scope.context.callback($scope.context.offset, function (result) {
            $location.path("/entry/" + result);
        });
    };

    $scope.prevEntryInContext = function () {
        $scope.context.offset -= 1;
        $scope.context.callback($scope.context.offset, function (result) {
            $location.path("/entry/" + result);
        });
    };

    $scope.backTo = function () {
        $location.path($scope.context.back);
    };

    $scope.removeLink = function (mainEntry, linkedEntry) {
        entry.removeLink({partId:mainEntry.id, linkId:linkedEntry.id}, function (result) {
            var idx = mainEntry.linkedParts.indexOf(linkedEntry);
            if (idx != -1) {
                mainEntry.linkedParts.splice(idx, 1);
            }
        }, function (error) {
            console.error(error);
        });
    };

    // file upload
    var uploader = $scope.sequenceFileUpload = $fileUploader.create({
        scope:$scope, // to automatically update the html. Default: $rootScope
        url:"/rest/file/sequence",
        method:'POST',
        removeAfterUpload:true,
        headers:{"X-ICE-Authentication-SessionId":sessionId},
        autoUpload:true,
        queueLimit:1 // can only upload 1 file
    });

    uploader.bind('progress', function (event, item, progress) {
        $scope.serverError = undefined;

        if (progress != "100")  // isUploading is always true until it returns
            return;

        // upload complete. have processing
        $scope.processingFile = item.file.name;
    });

    uploader.bind('success', function (event, xhr, item, response) {
        $scope.entry.hasSequence = true;
    });

    uploader.bind('completeall', function (event, items) {
        $scope.processingFile = undefined;
    });

    uploader.bind('beforeupload', function (event, item) {
        item.formData.push({entryType:$scope.entry.type});
        item.formData.push({entryRecordId:$scope.entry.recordId});
    });

    uploader.bind('error', function (event, xhr, item, response) {
        console.info('Error', xhr, item, response);
//        item.remove();
        $scope.serverError = true;
    });
});

iceControllers.controller('FolderPermissionsController', function ($scope, $modalInstance, $cookieStore, Folders, Permission, User, folder) {
    var sessionId = $cookieStore.get("sessionId");
    var panes = $scope.panes = [];
    $scope.folder = folder;
    $scope.userFilterInput = undefined;

    $scope.activateTab = function (pane) {
        angular.forEach(panes, function (pane) {
            pane.selected = false;
        });
        pane.selected = true;
        if (pane.title === 'Read')
            $scope.activePermissions = angular.copy($scope.readPermissions);
        else
            $scope.activePermissions = angular.copy($scope.writePermissions);

        angular.forEach($scope.users, function (item) {
            for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                item.selected = (item.id !== undefined && item.id === $scope.activePermissions[i].articleId);
            }
        });
    };

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
        Folders().removePermission({folderId:folder.id, permissionId:permissionId},
            function (result) {
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    if (permissionId != $scope.activePermissions[i].id)
                        continue;

                    $scope.activePermissions.splice(i, 1);
                    break;
                }
            });
    };

    $scope.addRemovePermission = function (permission) {
        permission.selected = !permission.selected;
        if (!permission.selected) {
            removePermission(permission.id);
            return;
        }

        // add permission
        var type;
        angular.forEach(panes, function (pane) {
            if (pane.selected) {
                type = pane.title.toUpperCase() + "_FOLDER";
            }
        });
        permission.typeId = folder.id;
        permission.type = type;

        Folders().addPermission({folderId:folder.id}, permission, function (result) {
            // result is the permission object
//            $scope.entry.id = result.typeId;
            $scope.activePermissions.push(result);
            permission.permissionId = result.id;
        });
    };

    // retrieve permissions for folder
    Folders().permissions({folderId:folder.id}, function (result) {
        $scope.readPermissions = [];
        $scope.writePermissions = [];

        angular.forEach(result, function (item) {
            if (item.type === 'WRITE_FOLDER')
                $scope.writePermissions.push(item);
            else
                $scope.readPermissions.push(item);
        });

        $scope.panes.push({title:'Read', count:$scope.readPermissions.length, selected:true});
        $scope.panes.push({title:'Write', count:$scope.writePermissions.length});

        $scope.activePermissions = angular.copy($scope.readPermissions);
    });

    $scope.deletePermission = function (index, permission) {
        Folders().removePermission({folderId:folder.id, permissionId:permission.id},
            function (result) {
                $scope.activePermissions.splice(index, 1);
            });
    };

    $scope.filter = function (val) {
        if (!val) {
            $scope.accessPermissions = undefined;
            return;
        }

        $scope.filtering = true;
        Permission().filterUsersAndGroups({limit:10, val:val},
            function (result) {
                $scope.accessPermissions = result;
                $scope.filtering = false;
            }, function (error) {
                $scope.filtering = false;
                $scope.accessPermissions = undefined;
            });
    };
});
