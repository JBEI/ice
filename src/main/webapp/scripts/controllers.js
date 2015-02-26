'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload',
    'vr.directives.slider', 'angularMoment']);

iceControllers.controller('ActionMenuController', function ($scope, $window, $rootScope, $location, $cookieStore, Folders, Entry, WebOfRegistries, Files, Selection, Upload, FolderSelection) {
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
        var entries = Selection.getSelectedEntries();
        var updateFolders = [];

        // add entries to folders for update
        angular.forEach($scope.selectedFolders, function (folder) {
            folder.entries = angular.copy(entries);
            updateFolders.push(folder);
        });

        folders.addEntriesToFolders(updateFolders,
            function (result) {
                $scope.updatePersonalCollections();
                Selection.reset();
            }, function (error) {
                console.error(error);
            });
    };

    $scope.removeEntriesFromFolder = function () {
        // remove selected entries from the current folder
        var entryIds = [];
        var entries = Selection.getSelectedEntries();
        angular.forEach(entries, function (entry) {
            entryIds.push(entry.id);
        });

        folders.removeEntriesFromFolder({folderId:$scope.collectionFolderSelected.id}, entryIds,
            function (result) {
                if (result) {
                    $scope.$broadcast("RefreshAfterDeletion");
                    $scope.$broadcast("UpdateCollectionCounts");
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
            upload.create({name:"Bulk Edit", type:type, status:'BULK_EDIT', entryList:selectedEntries}, function (result) {
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
        var selectedIds = [];
        var entries = Selection.getSelectedEntries();

        for (var i = 0; i < entries.length; i += 1) {
            selectedIds.push(parseInt(entries[i].id));
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
        var entries = Selection.getSelectedEntries();
        // if selected.selected
        var selectedIds = [];
        for (var i = 0; i < entries.length; i += 1) {
            selectedIds.push(parseInt(entries[i].id));
        }

        var files = Files();

        // retrieve from server
        files.getCSV(selectedIds,
            function (result) {
                if (result && result.value) {
                    $window.open("/rest/file/tmp/" + result.value, "_self");
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

iceControllers.controller('ProfileController', function ($scope, $location, $cookieStore, $rootScope, $stateParams, User, Settings) {
    $scope.showChangePassword = false;
    $scope.showEditProfile = false;
    $scope.showSendMessage = false;
    $scope.changePass = {};
    $scope.passwordChangeAllowed = false;

    // get settings
    Settings().getSetting({key:'PASSWORD_CHANGE_ALLOWED'}, function (result) {
        $scope.passwordChangeAllowed = (result.value == 'yes');
    });

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
        {id:'samples', url:'/views/profile/samples.html', display:'Samples', selected:false, icon:'fa-shopping-cart'},
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
        { name:'available', description:'', display:'Featured', icon:'fa-certificate', iconOpen:'fa-certificate orange', alwaysVisible:true},
        { name:'personal', description:'', display:'Personal', icon:'fa-folder', iconOpen:'fa-folder-open dark_blue', alwaysVisible:true},
        { name:'shared', description:'Folders & Entries shared with you', display:'Shared', icon:'fa-share-alt', iconOpen:'fa-share-alt green', alwaysVisible:false},
        { name:'drafts', description:'', display:'Drafts', icon:'fa-pencil', iconOpen:'fa-edit brown', alwaysVisible:false},
        { name:'pending', description:'', display:'Pending Approval', icon:'fa-support', iconOpen:'fa-support purple', alwaysVisible:false},
        { name:'deleted', description:'', display:'Deleted', icon:'fa-trash-o', iconOpen:'fa-trash red', alwaysVisible:false}
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

    // search
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

    $rootScope.$on('SamplesInCart', function (event, data) {
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

iceControllers.controller('CollectionDetailController', function ($scope, $cookieStore, Folders, $stateParams, $location) {
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
        console.log("DELETE folder", folder);

        // expected folders that can be deleted have type "PRIVATE" and "UPLOAD"
        folders.delete({folderId:folder.id, type:folder.type}, function (result) {
            var l = $scope.selectedCollectionFolders.length;
            for (var j = 0; j < l; j += 1) {
                if ($scope.selectedCollectionFolders[j].id === result.id) {
                    $scope.selectedCollectionFolders.splice(j, 1);
                    break;
                }
            }

            // if the deleted folder is one user is currently on, re-direct to personal collection
            if (folder.id == $stateParams.collection) {
                $location.path("/folders/personal");
            }
        }, function (error) {
            console.error(error);
        });
    }
});

// deals with sub collections e.g. /folders/:id
// retrieves the contents of folders
iceControllers.controller('CollectionFolderController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, Folders, Entry, EntryContextUtil, Selection) {
    var sessionId = $cookieStore.get("sessionId");
    var folders = Folders();
    var entry = Entry(sessionId);

    // param defaults
    $scope.params = {'asc':false, 'sort':'created'};
    var subCollection = $stateParams.collection;   // folder id or one of the defined collections (Shared etc)

    // retrieve folder contents. all folders are redirected to /folder/{id} which triggers this
    if (subCollection !== undefined) {
        $scope.folder = undefined;
        $scope.params.folderId = subCollection;

        // retrieve contents of collection (e,g, "personal")
        folders.folder($scope.params, function (result) {
            $scope.loadingPage = false;
            $scope.folder = result;
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
//        console.log("queryParams", $scope.params);
        folders.folder($scope.params, function (result) {
            $scope.folder = result;
            $scope.currentPage = 1;
        });
    };

    var allSelection = {all:false};

    $scope.selectAllClass = function () {
        if (allSelection.all)
            return 'fa-check-square-o';

        for (var k in allSelection) {
            if (!allSelection.hasOwnProperty(k))
                continue;

            if (k === "all")
                continue;

            if (allSelection[k])
                return 'fa-minus-square';
        }

        if (Selection.hasSelection())
            return 'fa-minus-square';
        return 'fa-square-o';
    };

    $scope.setType = function (type) {
        for (var k in allSelection) {
            if (!allSelection.hasOwnProperty(k))
                continue;

            allSelection[k] = false;
        }

        if (!type) {
            Selection.reset();
            return;
        }

        allSelection[type] = true;
    };

    $scope.selectAll = function () {
        allSelection.all = !allSelection.all;
        Selection.setAllSelection(allSelection);
    };

    $scope.isSelected = function (entry) {
        if (!entry)
            return false;

        if (allSelection.all)
            return true;

        for (var k in allSelection) {
            if (!allSelection.hasOwnProperty(k))
                continue;

            if (k === entry.type.toLowerCase()) {
                return allSelection[k];
            }
        }

        return Selection.searchEntrySelected(entry);
    };

    $scope.select = function (entry) {
        Selection.selectEntry(entry);
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

    $scope.getDisplay = function (permission) {
        if (permission.article === 'ACCOUNT')
            return permission.display.replace(/[^A-Z]/g, '');

        // group
        return permission.display;
    };

    $scope.shareText = function (permission) {
        var display = "";
        if (permission.article === 'GROUP')
            display = "Members of ";

        display += permission.display;

        if (permission.type.lastIndexOf("WRITE", 0) === 0)
            display += " can edit";
        else
            display += " can read";
        return display;
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
        var partLinks;
        $scope.entry = undefined;

        entry.query({partId:$stateParams.id}, function (result) {
            $scope.entry = EntryService.convertToUIForm(result);
            partLinks = angular.copy($scope.entry.linkedParts);
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

            //for (var i = 0; i < $scope.entry.linkedParts.length; i += 1) {
            //    $scope.entry.linkedParts[i].links = EntryService.toStringArray($scope.entry.linkedParts[i].links);
            //    $scope.entry.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.entry.linkedParts[i].selectionMarkers);
            //}

            // convert the part to a form the server can work with
            $scope.entry = EntryService.getTypeData($scope.entry);
            $scope.entry.linkedParts = partLinks;

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
                    $scope.part = EntryService.setNewEntryFields($scope.part);
                    $scope.part.linkedParts = [];
                    $scope.activePart = $scope.part;
                    $scope.selectedFields = EntryService.getFieldsForType($scope.createType);
                } else {
                    var newPart = result;
                    newPart = EntryService.setNewEntryFields(newPart);
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
                if (!$scope.activePart.parameters)
                    $scope.activePart.parameters = [];
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

            if ($scope.part.harvestDate)
                $scope.part.harvestDate = new Date($scope.part.harvestDate).getTime();

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

        $scope.clear = function () {
            $scope.dt = null;
        };

        $scope.addCustomParameter = function () {
            $scope.activePart.parameters.push({key:'', value:''});
        };

        $scope.removeCustomParameter = function (index) {
            $scope.activePart.parameters.splice(index, 1);
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
            $scope.activePermissions = $scope.readPermissions;
        else
            $scope.activePermissions = $scope.writePermissions;
    };

    // retrieve permissions
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

        $scope.activePermissions = $scope.readPermissions;
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

    $scope.closePermissionOptions = function () {
        $scope.showPermissionInput = false;
    };

    var removePermission = function (permissionId) {
        entry.removePermission({partId:$scope.entry.id, permissionId:permissionId},
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
                    if (permissionId == $scope.activePermissions[idx].id) {
                        i = idx;
                        break;
                    }
                }

                if (i == -1) {
                    console.log("not found");
                    return;
                }

                $scope.activePermissions.splice(i, 1);
                pane.count = $scope.activePermissions.length;
            });
    };

    //
    // when user clicks on the check box, removes permission if exists or adds if not
    //
    $scope.addRemovePermission = function (permission) {
        permission.selected = !permission.selected;
        if (!permission.selected) {
            removePermission(permission.id);
            return;
        }

        // add permission
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
            if (result.type == 'READ_ENTRY') {
                $scope.readPermissions.push(result);
                $scope.activePermissions = $scope.readPermissions;
            }
            else {
                $scope.writePermissions.push(result);
                $scope.activePermissions = $scope.writePermissions;
            }

            permission.id = result.id;
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
        removePermission(permission.id);
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
    $scope.vectorEditor = $sce.trustAsHtml('<object type="application/x-shockwave-flash" data="/swf/ve/VectorEditor.swf?entryId=' + $scope.entryId + '&sessionId=' + $scope.sessionId + '" id="vectoreditor" width="100%" height="' + ($(window).height() - 100) + 'px"><param name="wmode" value="opaque" /></object>');

    $(window).resize(function () {
        var height = $(this).height();
        $('#vectoreditor').height(height - 100);
    });
});

iceControllers.controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $modal, $rootScope, $fileUploader, Entry, Folders, EntryService, EntryContextUtil, Selection) {
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
    $scope.notFound = undefined;
    $scope.noAccess = undefined;

    entry.query({partId:$stateParams.id},
        function (result) {
            Selection.reset();
            Selection.selectEntry(result);

            $scope.entry = EntryService.convertToUIForm(result);
            $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());

            entry.statistics({partId:$stateParams.id}, function (stats) {
                $scope.entryStatistics = stats;
            });
        }, function (error) {
            if (error.status === 404)
                $scope.notFound = true;
            else if (error.status === 403)
                $scope.noAccess = true;
        });

    var menuSubDetails = $scope.subDetails = [
        {url:'/scripts/entry/general-information.html', display:'General Information', isPrivileged:false, icon:'fa-exclamation-circle'},
        {id:'sequences', url:'/scripts/entry/sequence-analysis.html', display:'Sequence Analysis', isPrivileged:false, countName:'traceSequenceCount', icon:'fa-search-plus'},
        {id:'comments', url:'/scripts/entry/comments.html', display:'Comments', isPrivileged:false, countName:'commentCount', icon:'fa-comments-o'},
        {id:'samples', url:'/scripts/entry/samples.html', display:'Samples', isPrivileged:false, countName:'sampleCount', icon:'fa-flask'},
        {id:'history', url:'/scripts/entry/history.html', display:'History', isPrivileged:true, countName:'historyCount', icon:'fa-history'},
        {id:'experiments', url:'/scripts/entry/experiments.html', display:'Experimental Data', isPrivileged:false, countName:'experimentalDataCount', icon:'fa-magic'}
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

    $scope.quickEdit = {};

    $scope.quickEditEntry = function (field) {
        // dirty is used to flag that the field's value has been modified to
        // prevent saving unchanged values on blur

        field.errorUpdating = false;
        if (!field.dirty) {
            return;
        }

        field.updating = true;

        // update the main entry with quickEdit (which is the model)
        $scope.entry[field.schema] = $scope.quickEdit[field.schema];
        if (field.inputType === 'withEmail') {
            $scope.entry[field.schema + 'Email'] = $scope.quickEdit[field.schema + 'Email'];
        }

        $scope.entry = EntryService.getTypeData($scope.entry);

        entry.update($scope.entry, function (result) {
            field.edit = false;

            if (result)
                $scope.entry = EntryService.convertToUIForm(result);

            field.dirty = false;
            field.updating = false;
        }, function (error) {
            field.updating = false;
            field.errorUpdating = true;
        });
    };

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
        Selection.reset();
        console.log($scope.context.back);
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
    folders.permissions({folderId:folder.id}, function (result) {
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
        folders.removePermission({folderId:folder.id, permissionId:permissionId},
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
        folders.update({folderId:folder.id}, folder, function (result) {

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

        folders.addPermission({folderId:folder.id}, permission, function (result) {
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
        Folders().enablePublicReadAccess({id:folder.id}, function (result) {
            folder.publicReadAccess = true;
        }, function (error) {

        });
    };

    $scope.disablePublicRead = function (folder) {
        Folders().disablePublicReadAccess({folderId:folder.id}, function (result) {
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
