'use strict';

angular.module('ice.profile.controller', [])
    .controller('ProfileEntryController', function ($scope, $location, $cookieStore, $stateParams, User, Entry) {
        var user = User($cookieStore.get("sessionId"));
        var profileId = $stateParams.id;

        $location.path("profile/" + profileId + "/entries", false);
        $scope.maxSize = 5;
        $scope.params = {userId: profileId, sort: "created", asc: false, currentPage: 1};

        user.getEntries($scope.params, function (result) {
            $scope.folder = result;
        });

        $scope.sort = function (sortType) {
            $scope.folder = null;
            // only change if switching to different sort
            $scope.params.asc = $scope.params.sort === sortType ? !$scope.params.asc : false;
            $scope.params.sort = sortType;
            $scope.params.offset = 0;
            user.getEntries($scope.params, function (result) {
                $scope.folder = result;
                $scope.params.currentPage = 1;
            }, function (error) {
                console.error(error);
            });
        };

        $scope.profileEntryPopupTemplate = "views/folder/template.html";

        $scope.tooltipDetails = function (entry) {
            $scope.currentTooltip = undefined;
            var sessionId = $cookieStore.get("sessionId");

            Entry(sessionId).tooltip({partId: entry.id},
                function (result) {
                    $scope.currentTooltip = result;
                }, function (error) {
                    console.error(error);
                });
        };

        $scope.userEntriesPageChanged = function () {
            $scope.loadingPage = true;
            $scope.params.offset = ($scope.params.currentPage - 1) * 15;
            user.getEntries($scope.params, function (result) {
                $scope.folder = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
            });
        };
    })
    .controller('ProfileController', function ($scope, $location, $cookieStore, $rootScope, $stateParams, User, Settings) {
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
                url: 'scripts/profile/profile-information.html',
                display: 'Profile',
                selected: true,
                icon: 'fa-user',
                open: true
            },
            {
                id: 'prefs',
                url: 'scripts/profile/preferences.html',
                display: 'Preferences',
                selected: false,
                icon: 'fa-cog'
            },
            {id: 'groups', url: 'scripts/profile/groups.html', display: 'Groups', selected: false, icon: 'fa-group'},
            {
                id: 'messages',
                url: 'scripts/profile/messages.html',
                display: 'Messages',
                selected: false,
                icon: 'fa-envelope-o'
            },
            {
                id: 'samples',
                url: 'scripts/profile/samples.html',
                display: 'Samples',
                selected: false,
                icon: 'fa-shopping-cart'
            },
            {
                id: 'entries',
                url: 'scripts/profile/entries.html',
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
                $location.path("profile/" + profileId + "/" + selectedOption.id);
            } else {
                $location.path("profile/" + profileId);
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
    })
    .controller('ProfileSamplesController', function ($scope, $cookieStore, $location, $stateParams, User) {
        $scope.maxSize = 15;
        $scope.params = {currentPage: 1};
        $scope.pendingSampleRequests = undefined;

        var user = User($cookieStore.get("sessionId"));
        var profileId = $stateParams.id;
        user.samples({userId: profileId},
            function (result) {
                $scope.userSamples = result;
            }, function (error) {
                console.error(error);
            });

        $scope.profileSamplesPageChanged = function () {
            $scope.loadingPage = true;
            $scope.offset = ($scope.params.currentPage - 1) * 15;
            user.samples({offset: $scope.offset}, {userId: profileId},
                function (result) {
                    $scope.userSamples = result;
                    $scope.loadingPage = false;
                }, function (error) {
                    console.error(error);
                    $scope.loadingPage = false;
                });
        }
    })
    .controller('ProfileGroupsController', function ($rootScope, $scope, $location, $cookieStore, $stateParams, User, Group) {
        var profileId = $stateParams.id;
        $location.path("profile/" + profileId + "/groups", false);
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
    })
;