'use strict';

angular.module('ice.profile.controller', [])
    .controller('MessageController', function ($scope, $uibModal, $stateParams, Util) {
        $scope.selectedMessage = undefined;

        $scope.selectMessage = function (message) {
            Util.get("rest/messages/" + message.id, function (result) {
                message.selected = true;
                //result.message = result.message.replace(/(?:\r\n|\r|\n)/g, '<br />');
                $scope.selectedMessage = result;
            });
        };

        // get all messages
        Util.get("rest/messages", function (result) {
            $scope.messages = result;
            if (result.data.length) {
                $scope.selectMessage(result.data[0]);
            }
        });

        $scope.replyMessage = function () {
        };

        $scope.openCreateMessageModal = function () {
            $uibModal.open({
                templateUrl: 'scripts/profile/modal/create-message.html',
                backdrop: "static",
                keyboard: false,
                controller: 'CreateMessageController'
            })
        };
    })
    .controller('CreateMessageController', function ($scope, $uibModalInstance, $http, $cookieStore, Util) {
        $scope.newMessage = {accounts: [], userGroups: []};

        $scope.createNewMessage = function () {
            Util.post("rest/messages", $scope.newMessage, function (result) {
                $uibModalInstance.close();
            })
        };

        $scope.closeGroupModal = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.setMessageRecipient = function (a, b, c) {
            $scope.newMessage.accounts.push(a);
            $scope.addedUser = undefined;
        };

        $scope.removeMessageRecipient = function (account, group) {
            if (account) {
                var accountIdx = $scope.newMessage.accounts.indexOf(account);
                if (accountIdx != -1)
                    $scope.newMessage.accounts.splice(accountIdx, 1);
            }

            if (group) {
                var groupIdx = $scope.newMessage.userGroups.indexOf(gr);
                if (groupIdx != -1)
                    $scope.newMessage.userGroups.splice(groupIdx, 1);
            }
        };

        $scope.filter = function (val) {
            return $http.get('rest/users/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")},
                params: {
                    val: val
                }
            }).then(function (res) {
                return res.data;
            });
        };
    })
    .controller('ApiKeysController', function ($scope, $uibModal, Util) {
        $scope.apiKeys = undefined;

        // retrieve existing api keys for current user
        $scope.retrieveProfileApiKeys = function () {
            Util.get("rest/api-keys", function (result) {
                $scope.apiKeys = result.data;
            });
        };

        $scope.retrieveProfileApiKeys();

        $scope.openApiKeyRequest = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/profile/modal/api-key-request.html',
                controller: 'GenerateApiKeyController',
                backdrop: "static"
            });

            modalInstance.result.then(function (result) {
                $scope.retrieveProfileApiKeys();
            });
        };

        $scope.deleteAPIKey = function (key) {
            Util.remove("rest/api-keys/" + key.id, key, function (result) {
                var idx = $scope.apiKeys.indexOf(key);
                if (idx >= 0)
                    $scope.apiKeys.splice(idx, 1);
            });
        }
    })
    .controller('GenerateApiKeyController', function ($scope, $uibModalInstance, Util) {
        $scope.apiKey = undefined;
        $scope.clientIdValidationError = undefined;
        $scope.errorCreatingKey = undefined;
        $scope.client = {};

        $scope.cancel = function () {
            $uibModalInstance.close();
        };

        $scope.generateToken = function () {
            if (!$scope.client.id) {
                $scope.clientIdValidationError = true;
                return;
            }

            var queryParams = {client_id: $scope.client.id};
            Util.post("rest/api-keys", null, function (result) {
                $scope.apiKey = result;
            }, queryParams);
        }
    })
    .controller('ProfileEntryController', function ($scope, $location, $cookieStore, $stateParams, Util) {
        var profileId = $stateParams.id;
        $scope.profileEntryPopupTemplate = "scripts/folder/template.html";
        $scope.maxSize = 5;
        $scope.params = {userId: profileId, sort: "created", asc: false, currentPage: 1};

        Util.get("rest/users/" + profileId + "/entries", function (result) {
            $scope.folder = result;
        }, $scope.params);

        $scope.sort = function (sortType) {
            $scope.folder = null;
            // only change if switching to different sort
            $scope.params.asc = $scope.params.sort === sortType ? !$scope.params.asc : false;
            $scope.params.sort = sortType;
            $scope.params.offset = 0;

            Util.get("rest/users/" + profileId + "/entries", function (result) {
                $scope.folder = result;
                $scope.params.currentPage = 1;
            }, $scope.params);
        };

        $scope.tooltipDetails = function (entry) {
            $scope.currentTooltip = undefined;
            Util.get("rest/parts/" + entry.id + "/tooltip", function (result) {
                $scope.currentTooltip = result;
            });
        };

        $scope.userEntriesPageChanged = function () {
            $scope.loadingPage = true;
            $scope.params.offset = ($scope.params.currentPage - 1) * 15;
            Util.get("rest/users/" + profileId + "/entries", function (result) {
                $scope.folder = result;
                $scope.loadingPage = false;
            }, $scope.params);
        };
    })
    .controller('ProfileController', function ($scope, $location, $rootScope, $stateParams, Util, ProfileService) {
        $scope.showChangePassword = false;
        $scope.showEditProfile = false;
        $scope.showSendMessage = false;
        $scope.changePass = {};
        $scope.passwordChangeAllowed = false;

        // get settings
        Util.get("rest/config/PASSWORD_CHANGE_ALLOWED", function (result) {
            $scope.passwordChangeAllowed = (result.value.toLowerCase() === 'yes');
        });

        $scope.preferenceEntryDefaults = ProfileService.preferenceEntryDefaults();
        $scope.preferences = {};

        var profileOption = $stateParams.option;
        var profileId = $scope.userId = $stateParams.id;

        $scope.savePreference = function (pref) {
            Util.post("rest/users/" + profileId + "/preferences/" + pref.id, {}, function (result) {
                pref.edit = false;
            }, {value: $scope.preferences[pref.id]});
        };

        var menuOptions = $scope.profileMenuOptions = ProfileService.profileMenuOptions();

        $scope.showSelection = function (index) {
            var selectedOption = menuOptions[index];
            if (!selectedOption)
                return;

            var canViewSelected = selectedOption.open || $rootScope.user.isAdmin || ($scope.profile.email === $rootScope.user.email);
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
        Util.get("rest/users/" + profileId, function (result) {
            $scope.profile = result;
            Util.get("rest/users/" + profileId + "/preferences", function (prefs) {
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

            //if (!$scope.changePass || $scope.changePass.current === undefined || !$scope.changePass.current.length) {
            //    $scope.changePasswordError = "Please enter your current password";
            //    $scope.currentError = true;
            //    return;
            //}

            // check new password value
            if (pass.new === undefined || pass.new.length === 0) {
                $scope.changePasswordError = "Please enter a new password";
                $scope.newPassError = true;
                return;
            }

            // check for new password confirm value
            if (pass.new2 === undefined || pass.new2.length === 0) {
                $scope.changePasswordError = "Please confirm the new password";
                $scope.newPass2Error = true;
                return;
            }

            // check for matching password values
            if (pass.new2 !== pass.new) {
                $scope.changePasswordError = "Passwords do not match";
                $scope.newPassError = true;
                $scope.newPass2Error = true;
                return;
            }

            // validate existing password
            $scope.passwordChangeSuccess = undefined;
            $scope.changePasswordError = undefined;

            Util.update("rest/users/" + $stateParams.id + "/password", {password: pass.new}, {},
                function (success) {
                    if (!success) {
                        $scope.changePasswordError = "There was an error changing the password";
                    } else {
                        $scope.passwordChangeSuccess = true;
                    }
                }, function (error) {
                    $scope.changePasswordError = "There was an error changing your password";
                });
        };

        $scope.updateProfile = function () {
            Util.update("rest/users/" + profileId, $scope.editProfile, {}, function (result) {
                $scope.profile = result;
                $scope.editClick(false, false, false);
            });
        };

        $scope.switchtoEditMode = function () {
            $scope.editProfile = angular.copy($scope.profile);
        }
    })
    .controller('ProfileSamplesController', function ($scope, $cookieStore, $location, $stateParams, Util) {
        $scope.maxSize = 15;
        $scope.params = {currentPage: 1};
        $scope.pendingSampleRequests = undefined;

        Util.get('rest/users/' + $stateParams.id + '/samples', function (result) {
            $scope.userSamples = result;
        });

        $scope.profileSamplesPageChanged = function () {
            $scope.loadingPage = true;
            $scope.offset = ($scope.params.currentPage - 1) * 15;
            Util.get('rest/users/' + $stateParams.id + '/samples', function (result) {
                $scope.userSamples = result;
                $scope.loadingPage = false;
            }, {offset: $scope.offset}, function (error) {
                $scope.loadingPage = false;
            });
        }
    })
    .controller('ProfileGroupsController', function ($rootScope, $scope, $location, $cookieStore, $stateParams,
                                                     $uibModal, Util) {
        var profileId = $stateParams.id;
        $location.path("profile/" + profileId + "/groups", false);
        $scope.selectedUsers = [];
        $scope.selectedRemoteUsers = [];
        $scope.enteredUser = undefined;
        $scope.privateGroupsParams = {offset: 0, limit: 10, currentPage: 1, maxSize: 5};

        // init: retrieve groups user belongs to and created
        var getGroups = function () {
            Util.get("rest/users/" + profileId + "/groups", function (result) {
                $scope.userGroups = result;
            });
        };
        getGroups();

        $scope.switchToEditMode = function (selectedGroup) {
            selectedGroup.edit = true;
            Util.list("rest/groups/" + selectedGroup.id + "/members", function (result) {
                selectedGroup.members = result;
            }, {}, function (error) {
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
            Util.list('rest/users/autocomplete', function (result) {
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
            Util.update("rest/users/" + profileId + "/groups", $scope.newGroup, {}, function (result) {
                $scope.userGroups.data.splice(0, 0, result);
                $scope.showCreateGroup = false;
            })
        };

        $scope.updateGroup = function (selectedGroup) {
            Util.update("rest/groups/" + selectedGroup.id, selectedGroup, {}, function (result) {
                selectedGroup.memberCount = selectedGroup.members.length;
                selectedGroup.edit = false;
            });
        };

        $scope.openCreateGroupModal = function (group) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/profile/modal/edit-group.html',
                controller: 'ProfileGroupsModalController',
                backdrop: "static",
                keyboard: false,
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

                Util.setFeedback("Group successfully created", "success");
                getGroups();
            })
        };

        $scope.deleteUserGroup = function (group) {
            Util.remove("rest/groups/" + group.id, {}, function (result) {
                var i = $scope.userGroups.data.indexOf(group);
                if (i >= 0)
                    $scope.userGroups.data.splice(i, 1);
                console.log(result);
            })
        }
    })
    .controller('ProfileGroupsModalController', function ($scope, $http, Util, currentGroup, $cookieStore, $uibModalInstance) {
        $scope.headerMessage = currentGroup ? "Update \"" + currentGroup.label + "\"" : "Create New Group";
        $scope.webPartners = [];
        $scope.placeHolder = 'User name or email';

        $scope.closeGroupModal = function () {
            $uibModalInstance.dismiss('cancel');
        };

        if (currentGroup && currentGroup.id) {
            Util.get("rest/groups/" + currentGroup.id + "/members", function (result) {
                currentGroup.type = 'ACCOUNT';
                $scope.newGroup = currentGroup;

                angular.forEach(result.members, function (member) {
                    member.type = 'ACCOUNT';
                    $scope.newGroup.members.push(member);
                });

                angular.forEach(result.remoteMembers, function (member) {
                    member.type = 'REMOTE';
                    $scope.newGroup.members.push(member);
                });
            });
        } else {
            $scope.newGroup = {members: [], type: 'ACCOUNT'};
        }

        $scope.getWebPartners = function () {
            $scope.placeHolder = 'Enter remote user\'s email';
            Util.list("rest/partners", function (result) {
                $scope.webPartners = result;
            });
        };

        $scope.filter = function (val) {
            if ($scope.newGroup.type == 'REMOTE')
                return;

            return $http.get('rest/users/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")},
                params: {
                    val: val
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.userSelectionForGroupAdd = function ($item, $model, $label) {
            if ($scope.newGroup.type == 'REMOTE') {
                $scope.newGroup.members.push({
                    type: $scope.newGroup.type,
                    email: $item,
                    partner: $scope.newGroup.partner
                });
            } else {
                $item.type = $scope.newGroup.type;
                $scope.newGroup.members.push($item);
            }

            // reset
            $scope.newUserName = undefined;
            $scope.newGroup.partner = undefined;
            $scope.newGroup.type = 'ACCOUNT';
        };

        $scope.createOrUpdateGroup = function () {
            var members = [];
            var remoteMembers = [];

            for (var i = 0; i < $scope.newGroup.members.length; i += 1) {
                var member = $scope.newGroup.members[i];
                if (member.type == 'REMOTE') {
                    remoteMembers.push({user: {email: member.email}, partner: member.partner});
                } else {
                    members.push(member);
                }
            }

            $scope.newGroup.members = members;
            $scope.newGroup.remoteMembers = remoteMembers;

            if ($scope.newGroup.id) {
                Util.update("rest/groups/" + $scope.newGroup.id, $scope.newGroup, {}, function (result) {
                    $uibModalInstance.close(result);
                });
            } else {
                Util.post("rest/groups", $scope.newGroup, function (result) {
                    $uibModalInstance.close(result);
                })
            }
        };

        $scope.selectPartnerForGroupAdd = function (partner) {
            $scope.newGroup.partner = {id: partner.id, url: partner.url};
        };
    })
;