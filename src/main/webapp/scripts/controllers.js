'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'vr.directives.slider', 'angularMoment']);

iceControllers.controller('ActionMenuController', function ($scope, $window, $rootScope, $location, $cookieStore, Folders, Entry, WebOfRegistries) {
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
    $scope.selectedFolders = [];

    // retrieve personal list of folders user can add or move parts to
    $scope.retrieveUserFolders = function () {
        $scope.userFolders = undefined;
        folders.getByType({folderType:"personal"}, function (data) {
            if (data.length)
                $scope.userFolders = data;
        });
    };

    // select a folder in the pull down
    $scope.select = function (folder) {
        var i = $scope.selectedFolders.indexOf(folder);
        if (i == -1) {
            $scope.selectedFolders.push(folder);
        } else {
            $scope.selectedFolders.splice(i, 1);
        }
    };

    $scope.addEntriesToFolders = function () {
        var updateFolders = [];
        angular.forEach($scope.selectedFolders, function (folder) {
            folder.entries = angular.copy(selectedEntries);
            updateFolders.push(folder);
        });

        folders.addEntriesToFolders(updateFolders,
            function (result) {
                // todo : send message to update the counts
                // emit
            });
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

        $scope.entrySelected = data.length > 0;

        // is reading it so can add to any
        if (data.length == 0) {
            $scope.addToDisabled = true;
            $scope.editDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
        } else {
            // need read permission but assuming it already exists if can read and select it
            $scope.addToDisabled = false;
        }

        // can delete if all have canEdit=true
        var entryType = 'None';
        for (var i = 0; i < data.length; i += 1) {
            var entry = data[i];
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

        $scope.editDisabled = $scope.deleteDisabled || entryType === 'None' || entryType === undefined;
    });

    $rootScope.$on("EntryRetrieved", function (event, data) {
        $scope.entry = data;
        $scope.editDisabled = !data.canEdit;
        $scope.entrySelected = true;
        var isAdmin = $scope.user.accountType === undefined ? false : $scope.user.accountType.toLowerCase() === "admin";
        $scope.deleteDisabled = ($scope.user.email != $scope.entry.ownerEmail && !isAdmin);
        // only owners or admins can delete
    });

    // function that handles "edit" click
    $scope.editEntry = function () {
        $location.path('/entry/edit/' + $scope.entry.id);
        $scope.editDisabled = true;
    };

    $scope.retrieveRegistryPartners = function () {
        WebOfRegistries().query({}, function (result) {
            $scope.registryPartners = result;
        });
    };

    $scope.csvExport = function () {
        // todo : if selectedEntries.length?
        $window.open("/rest/part/" + $scope.entry.id + "/csv?sid=" + $cookieStore.get("sessionId"), "_self");
    }
});

iceControllers.controller('RegisterController', function ($scope, $resource, $location, User) {
    $scope.errMsg = undefined;
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
                $location.path("/login");
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

    var samples = Samples($cookieStore.get("sessionId"));
    samples.requests(function (result) {
        $scope.pendingSampleRequests = result;
    }, function (data) {
        if (data.status === 401) {
            $location.path('/login');
            return;
        }

        $rootScope.error = data;
    });
});

iceControllers.controller('AdminUserController', function ($rootScope, $scope, $stateParams, $cookieStore, User) {
    $scope.maxSize = 5;
    $scope.currentPage = 1;

    var user = User($cookieStore.get("sessionId"));

    user.list(function (result) {
        $scope.userList = result;
    });

    $scope.setUserListPage = function (pageNo) {
        $scope.loadingPage = true;
        var offset = (pageNo - 1) * 15; // TODO : make sure it is a number
        user.list({offset:offset}, function (result) {
            $scope.userList = result;
            $scope.loadingPage = false;
        });
    };

    $scope.createProfile = function () {
        console.log("create profile");
    }
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
        'ERROR_EMAIL_EXCEPTION_PREFIX',
        'ADMIN_EMAIL',
        'SMTP_HOST',
        'SEND_EMAIL_ON_ERRORS'
    ];

    $scope.generalSettings = [];
    $scope.emailSettings = [];

    // retrieve general setting
    $scope.getSetting = function () {
        var sessionId = $cookieStore.get("sessionId");

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

iceControllers.controller('ProfileGroupsController', function ($rootScope, $scope, $location, $cookieStore, $stateParams, User, Group, WebOfRegistries, Remote) {
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
    var wor = WebOfRegistries();

    group.getUserGroups({userId:profileId}, function (result) {
        angular.forEach(result, function (item) {
            if (item.ownerEmail && item.ownerEmail === $rootScope.user.email)
                $scope.myGroups.push(item);
            else
                $scope.groupsIBelong.push(item);
        });

        $scope.userGroups = result;
    });

    user.list(function (result) {
        $scope.users = result;
        $scope.activeUsers = result;
    });

    // fetch list of registry partners
    $scope.registryPartners = undefined;
    wor.query({}, function (result) {
        console.log(result);
        if (result) {
            $scope.registryPartners = result.partners;
        }
    });

    $scope.selectUser = function (user) {
        var index = $scope.selectedUsers.indexOf(user);
        if (index == -1)
            $scope.selectedUsers.push(user);
        else
            $scope.selectedUsers.splice(index, 1);
    };

    $scope.selectRemoteUser = function (enteredUser, selectedRegistry) {
        Remote().getUser({id:selectedRegistry, email:enteredUser}, function (result) {
            if (result) {
                result.isRemote = true;
                $scope.selectedRemoteUsers.push(result);
                $scope.enteredUser = undefined;
            }
        });
    };

    $scope.selectedRegistryChange = function (selected) {
        if (selected) {
            $scope.activeUsers = $scope.selectedRemoteUsers;
            return;
        }

        $scope.activeUsers = $scope.users;
    };

    $scope.resetSelectedUsers = function () {
        $scope.selectedUsers = [];
    };

    $scope.createGroup = function (groupName, groupDescription) {
        $scope.newGroup = {label:groupName, description:groupDescription};
        user.createGroup({userId:profileId}, $scope.newGroup, function (result) {
            $scope.myGroups.splice(0, 0, result);
            $scope.showCreateGroup = false;
        })
    };
});

iceControllers.controller('ProfileEntryController', function ($scope, $location, $cookieStore, $stateParams, User) {
    $scope.maxSize = 5;
    $scope.currentPage = 1;

    var user = User($cookieStore.get("sessionId"));
    var profileId = $stateParams.id;
    $location.path("/profile/" + profileId + "/entries", false);

    user.getEntries({userId:profileId}, function (result) {
        $scope.folder = result;
    });
});

iceControllers.controller('ProfileController', function ($scope, $location, $cookieStore, $stateParams, User) {
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
        {url:'/views/profile/profile-information.html', display:'Profile', selected:true, icon:'fa-user'},
        {id:'prefs', url:'/views/profile/preferences.html', display:'Preferences', selected:false, icon:'fa-cog'},
        {id:'groups', url:'/views/profile/groups.html', display:'Groups', selected:false, icon:'fa-group'},
        {id:'messages', url:'/views/profile/messages.html', display:'Messages', selected:false, icon:'fa-envelope-o'},
        {id:'samples', url:'/views/profile/samples.html', display:'Requested Samples', selected:false, icon:'fa-shopping-cart'},
        {id:'entries', url:'/views/profile/entries.html', display:'Entries', selected:false, icon:'fa-th-list'}
    ];

    $scope.showSelection = function (index) {
        angular.forEach(menuOptions, function (details) {
            details.selected = false;
        });
        menuOptions[index].selected = true;
        $scope.profileOptionSelection = menuOptions[index].url;
        if (menuOptions[index].id) {
            $location.path("/profile/" + profileId + "/" + menuOptions[index].id);
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

        var user = User();

        // validate existing password
        var userId = $cookieStore.get('userId');
        var userObj = {sessionId:$cookieStore.get("sessionId"), password:$scope.changePass.current, email:userId};
        // authenticate new password
        user.resetPassword({}, userObj, function (result) {
            if (result == null) {
                $scope.changePasswordError = "Current password is invalid";
                $scope.currentError = true;
                return;
            }

            user.changePassword({},
                {email:userId, password:pass.new},
                function (success) {
                    if (success) {
                        $location.path($location.path());
                    } else {
                        $scope.changePasswordError = "There was an error changing your password";
                    }
                }, function (error) {

                });
            //  change password
        }, function (error) {
            $scope.changePasswordError = "There was an error changing your password";
        });
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
iceControllers.controller('CollectionController', function ($scope, $state, $filter, $location, $cookieStore, $rootScope, Folders, Settings, sessionValid, Search) {
    // todo : set on all
    // $location.search('q', null);

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
        { name:'deleted', display:'Deleted', icon:'fa-trash-o', iconOpen:'fa-trash-o', alwaysVisible:false}
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

    // selected entries
    $scope.selection = [];
    $scope.shoppingCartContents = [];
    // todo : retrieve shopping cart contents

    $scope.hidePopovers = function (hide) {
        $scope.openShoppingCart = !hide;
    };

    $scope.submitShoppingCart = function () {
        $scope.shoppingCartContents = [];
        $scope.openShoppingCart = false;
    };

    // search
    $scope.runUserSearch = function () {
        var search = Search();
        $scope.loadingSearchResults = true;

        search.runSearch($scope.searchFilters,
            function (result) {
                $scope.searchResults = result;
                $scope.loadingSearchResults = false;
            },
            function (error) {
                $scope.loadingSearchResults = false;
                $scope.searchResults = undefined;
                console.log(error);
            }
        );
    };

    $scope.$on('SampleTypeSelected', function (event, data) {
        // todo : save to the server
        $scope.shoppingCartContents.push(data);
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

iceControllers.controller('WebOfRegistriesDetailController', function ($scope, $cookieStore, $location, $stateParams) {
    var sessionId = $cookieStore.get("sessionId");

    $scope.selectRemotePartnerFolder = function (folder) {
        console.log(folder, $stateParams.partner);
        $scope.partnerId = $stateParams.partner;
        $location.path('/web/' + $stateParams.partner + "/folder/" + folder.id);
    };
});

iceControllers.controller('WorFolderContentController', function ($scope, $stateParams, Remote) {
    var id;
    if ($stateParams.folderId === undefined)
        id = $scope.partnerId;
    else
        id = $stateParams.folderId;

    Remote().getFolderEntries({folderId:id, id:$stateParams.partner}, function (result) {
        $scope.selectedPartnerFolder = result;
    });
});

iceControllers.controller('FullScreenFlashController', function ($scope, $location) {
    console.log("FullScreenFlashController");
    $scope.entryId = $location.search().entryId;
    $scope.sessionId = $location.search().sessionId;
    $scope.entry = {'recordId':$scope.entryId};
});

iceControllers.controller('WebOfRegistriesController',
    function ($scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote) {
        console.log("WebOfRegistriesController");

        // retrieve web of registries partners
        $scope.wor = undefined;
        var wor = WebOfRegistries();
//    $scope.getPartners = function(approveOnly) {
        wor.query({approved_only:false}, function (result) {
            $scope.wor = result;
        });
//    };

        $scope.newPartner = undefined;
        $scope.addPartner = function () {
            wor.addPartner({}, $scope.newPartner, function (result) {
                $scope.wor = result;
                $scope.showAddRegistryForm = false;
                $scope.newPartner = undefined;
            });
        };

        $scope.removePartner = function (partner, index) {
            wor.removePartner({url:partner.url}, function (result) {
                $scope.wor.partners.splice(index, 1);
            });
        };

        $scope.approvePartner = function (partner, index) {
            partner.status = 'APPROVED';
            wor.updatePartner({url:partner.url}, partner, function (result) {

            });
        };

        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                console.log(result);
                $scope.selectedPartnerFolders = result;
            });
        }
    });

iceControllers.controller('WebOfRegistriesMenuController',
    function ($scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote) {
        // retrieve web of registries partners
        $scope.wor = undefined;
        var wor = WebOfRegistries();
        wor.query({approved_only:true}, function (result) {
            $scope.wor = result;
        });

//        $scope.newPartner = undefined;
//        $scope.addPartner = function () {
//            wor.addPartner({}, $scope.newPartner, function (result) {
//                $scope.wor = result;
//                $scope.showAddRegistryForm = false;
//                $scope.newPartner = undefined;
//            });
//        };
//
//        $scope.removePartner = function (partner, index) {
//            wor.removePartner({url:partner.url}, function (result) {
//                $scope.wor.partners.splice(index, 1);
//            });
//        };
//
//        $scope.approvePartner = function (partner, index) {
//            partner.status = 'APPROVED';
//            wor.updatePartner({url:partner.url}, partner, function (result) {
//
//            });
//        };

        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                console.log(result);
                $scope.selectedPartnerFolders = result;
            });
        }
    });

iceControllers.controller('WorContentController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, Remote) {
    console.log("WorContentController");

    $scope.selectedPartner = $stateParams.partner;
    console.log("wor", $scope.selectedPartner);
    $scope.loadingPage = true;

    Remote().publicEntries({id:$stateParams.partner}, function (result) {
        $scope.loadingPage = false;
        result.count = 894;
        $scope.selectedPartnerEntries = result;
    });

    $scope.tooltipDetails = function (entry) {
        $scope.currentTooltip = entry;
    };
});

// deals with sub collections e.g. /folders/:id
// retrieves the contents of folders
iceControllers.controller('CollectionFolderController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, $http, Folders, Entry) {
    console.log("CollectionFolderController", $stateParams.collection);

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
        if (pageNo == undefined)
            pageNo = 1;

        $scope.loadingPage = true;
        if ($scope.params.folderId === undefined)
            $scope.params.folderId = 'personal';
        $scope.params.offset = (pageNo - 1) * 15; // TODO : make sure it is a number
        console.log("page#", pageNo, "queryParams", $scope.params);
        folders.folder($scope.params, function (result) {
            console.log("folder', result");
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

    // now retrieve entries for selected folder
//    folders.folder({folderId:$stateParams.id}, function (result) {
//        $scope.folder = result;
//    });

    $scope.allSelected = false;
    $scope.selectAll = function () {
        $scope.allSelected = !$scope.allSelected;
        if (!$scope.allSelected) {
            $scope.selection = [];
            $scope.$broadcast("EntrySelection", $scope.selection);
        } else {
            console.log($scope.folder);
        }
    };

    $scope.counter = 0;
    $scope.selectedEntries = [];

    $scope.select = function (entry) {
        var entryId = entry.id;

        var i = $scope.selection.indexOf(entryId);
        if (i != -1) {
            $scope.selection.splice(i, 1);
            $scope.selectedEntries.splice(i, 1);
        } else {
            $scope.selection.push(entryId);
            $scope.selectedEntries.push(entry);
        }
        $scope.allSelected = $scope.selection.length > 0;
        $scope.$emit("EntrySelection", $scope.selectedEntries);
    };

    $scope.showEntryDetails = function (entry, index) {
        if (!$scope.params.offset) {
            $scope.params.offset = index;
        }
        $rootScope.collectionContext = $scope.params;
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
    }
});

iceControllers.controller('UserController', function ($scope, $routeParams, Entry) {
//    $scope.entry = Entry.query({partId:$routeParams.id});
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
    function ($scope, $location, $cookieStore, $rootScope, $stateParams, Entry, EntryService) {

        var entry = Entry($cookieStore.get("sessionId"));
        $scope.entry = undefined;
        entry.query({partId:$stateParams.id}, function (result) {
            $scope.entry = result;

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
        });

        $scope.editEntry = function () {
            if ($scope.activePart.bioSafetyLevel === 'Level 1')
                $scope.activePart.bioSafetyLevel = 1;
            else
                $scope.activePart.bioSafetyLevel = 2;

            // convert arrays of objects to array strings
            $scope.activePart.links = EntryService.toStringArray($scope.activePart.links);
            $scope.activePart.selectionMarkers = EntryService.toStringArray($scope.activePart.selectionMarkers);

            entry.update($scope.activePart, function (result) {
                $location.path("/entry/" + result.id);
            });
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
            return $http.get('/rest/part/autocomplete', {
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
                    console.log("created entry", result);
                    $scope.$emit("UpdateCollectionCounts");
                    $location.path('/entry/' + result.id);
                }, function (error) {
                    console.error(error);
                });
            }
        };

        $scope.format = 'MMM d, yyyy h:mm:ss a';

        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('/rest/part/autocomplete/partid', {
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

        uploader.bind('whenaddingfilefailed', function (event, item) {
            console.info('When adding a file failed', item);
        });

        uploader.bind('afteraddingall', function (event, items) {
            console.info('After adding all files', items);
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
            console.info('Progress: ' + progress, item);
            console.log("process", item.file.name);

            if (progress != "100")  // isUploading is always true until it returns
                return;

            console.log("process", progress == "100");

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        });

        uploader.bind('success', function (event, xhr, item, response) {
            console.log("active scope", $scope.active);

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

//        // TODO : consider, instead of retrieving all and filtering, try on the server first
//        user.list(function (result) {
//            $scope.users = result;
//            $scope.filteredUsers = angular.copy(result);
//
//            angular.forEach($scope.users, function (item) {
//                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
//                    if (item.id == $scope.activePermissions[i].articleId && $scope.activePermissions[i].article === 'ACCOUNT') {
//                        item.selected = true;
//                        item.permissionId = $scope.activePermissions[i].id;
//                        break;
//                    }
//                }
//            });
//        });
//
//        group.getUserGroups({userId:$rootScope.user.id}, function (result) {
//            console.log(result, $scope.activePermissions);
//            $scope.filteredGroups = angular.copy(result);
//
//            angular.forEach($scope.filteredGroups, function (item) {
//                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
//                    if (item.id == $scope.activePermissions[i].articleId && $scope.activePermissions[i].article === 'GROUP') {
//                        item.selected = true;
//                        item.permissionId = $scope.activePermissions[i].id;
//                        break;
//                    }
//                }
//            });
//        });
    };

    $scope.addEmailUser = function () {
        console.log($scope.userFilterInput);
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
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    if (permissionId != $scope.activePermissions[i].id)
                        continue;

                    $scope.activePermissions.splice(i, 1);
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
        angular.forEach(panes, function (pane) {
            if (pane.selected) {
                type = pane.title.toUpperCase() + "_ENTRY";
            }
        });
        permission.typeId = $scope.entry.id;
        permission.type = type;

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
                $scope.activePermissions.splice(index, 1);
            });
    };
});

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

    $scope.test = $sce.trustAsHtml('<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540002" id="VectorEditor" width="100%" height="100%" codebase="https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab"> \
        <param name="movie" value="VectorEditor.swf"> \
            <param name="quality" value="high">  \
                <param name="bgcolor" value="#869ca7"> \
                    <param name="wmode" value="opaque">  \
                        <param name="allowScriptAccess" value="sameDomain"> \
                            <embed src="/swf/ve/VectorEditor.swf?entryId=' + $scope.entryId + '&amp;sessionId=' + $scope.sessionId + '" \
                            quality="high" bgcolor="#869ca7" width="100%" wmode="opaque" height="100%" \
                            name="SequenceChecker" align="middle" play="true" loop="false"  \
                            type="application/x-shockwave-flash" \
                            pluginspage="http://www.adobe.com/go/getflashplayer"> \
                            </object>');
});

iceControllers.controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $rootScope, $fileUploader, Entry, Folders, EntryService) {
    $scope.partIdEditMode = false;
    $scope.showSBOL = true;
    $scope.context = undefined;
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
//        console.log(part, $scope.entry);
        entry.deleteSequence({partId:part.id}, function (result) {
            console.log(result);
            part.hasSequence = false;
        }, function (error) {
            console.error(error);
        })
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

    var entry = Entry(sessionId);

    if ($rootScope.collectionContext) {
        $rootScope.collectionContext.limit = 1;
        $scope.context = $rootScope.collectionContext;

//        folders.folder($scope.context,
//            function (result) {
//                console.log("result", result);
//                $scope.context.count = result.count;
//
//                if (!result.entries || result.entries.length === 0) {
//                    // tODO : show some error msg to user
//                    return;
//                }
//
//                $scope.context.offset = $rootScope.collectionContext.offset;
//                $rootScope.$broadcast("EntryRetrieved", result);
//                $scope.entry = result.entries[0];
//
//                entry.statistics({partId:$scope.entry.id}, function (stats) {
//                    $scope.entryStatistics = stats;
//                });
//            });
    }

    $scope.entryFields = undefined;
    $scope.entry = undefined;

    if (!isNaN($stateParams.id)) {
        entry.query({partId:$stateParams.id},
            function (result) {
                $rootScope.$broadcast("EntryRetrieved", result);
                $scope.entry = result;
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

        folders.folder($scope.context,
            function (result) {
                $scope.context.count = result.count;

                if (!result.entries || result.entries.length === 0) {
                    // TODO : show some error msg
                    return;
                }

                $location.path("/entry/" + result.entries[0].id);
            });
    };

    $scope.prevEntryInContext = function () {
        $scope.context.offset -= 1;

        folders.folder($scope.context,
            function (result) {
                $scope.context.count = result.count;

                if (!result.entries || result.entries.length === 0) {
                    // tODO : show some error msg
                    return;
                }

                $location.path("/entry/" + result.entries[0].id);
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
