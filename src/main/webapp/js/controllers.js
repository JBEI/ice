'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap', 'angularFileUpload', 'vr.directives.slider', 'angularMoment']);

iceControllers.controller('WebOfRegistriesController', function ($scope, $modal, $cookieStore, $stateParams, WebOfRegistries) {
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
    }
});

iceControllers.controller('EntrySampleController', function ($scope, $modal, $cookieStore, $stateParams, Entry) {
    $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
    $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

    $scope.openAddToCart = function () {
        var modalInstance = $modal.open({
            templateUrl:'/views/modal/sample-request.html'
        });

        modalInstance.result.then(function (selected) {
            console.log("selected", selected);
            $scope.$emit("SampleTypeSelected", selected);
            // "liquid" or "streak"

        }, function () {
            // dismiss callback
        });
    };

    var sessionId = $cookieStore.get("sessionId");
    var entry = Entry(sessionId);
    entry.samples({partId:$stateParams.id}, function (result) {
        $scope.samples = result;
    });
});

iceControllers.controller('ActionMenuController', function ($scope, $rootScope, $location, $cookieStore, Folders, Entry) {
    $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;

    // reset all on state change
    $rootScope.$on('$stateChangeStart',
        function (event, toState, toParams, fromState, fromParams) {
            $scope.editDisabled = $scope.addToDisabled = $scope.removeDisabled = $scope.moveToDisabled = $scope.deleteDisabled = true;
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
            $scope.userFolders = data;
        });
    };

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
        // todo : consider using post to "move to trash"
        Entry(sid).moveEntriesToTrash(selectedEntries,
            function (result) {
                console.log(result);
            }, function (error) {
                console.log(error);
            })
    };

    $scope.$on("EntrySelection", function (event, data) {
        selectedEntries = [];

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
        $scope.deleteDisabled = ($scope.user.email != $scope.entry.ownerEmail && $scope.user.accountType.toLowerCase() !== "admin");
        // only owners or admins can delete
    });

    $scope.editEntry = function () {
        $location.path('/entry/edit/' + $scope.entry.id);
        $scope.editDisabled = true;
    };
});

iceControllers.controller('RegisterController', function ($scope, $resource, $location) {
    $scope.errMsg = undefined;

    $scope.submit = function () {
        var User = $resource("/rest/profile");

        // todo User.get first to ensure email does not exist
        User.save({email:$scope.email, firstName:$scope.firstName, lastName:$scope.lastName, institution:$scope.institution, description:$scope.about}, function (data) {
            if (data.length != 0)
                $location.path("/login");
            else
                $scope.errorMsg = "Could not create account";
        });
    };

    $scope.cancel = function () {
        $location.path("/login");
    }
});

iceControllers.controller('ForgotPasswordController', function ($scope, $resource, $location) {
    $scope.errMsg = undefined;

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
    var user = User($cookieStore.get("sessionId"));
    user.list(function (result) {
        $scope.userList = result;
    });

    $scope.createProfile = function () {
        console.log("create profile");
    }
});

iceControllers.controller('AdminController', function ($rootScope, $location, $scope, $stateParams, $cookieStore, Settings) {
    var generalSettingKeys = [
        'TEMPORARY_DIRECTORY',
        'PROJECT_NAME',
        'PART_NUMBER_DIGITAL_SUFFIX',
        'PART_NUMBER_DELIMITER',
        'NEW_REGISTRATION_ALLOWED',
        'PROFILE_EDIT_ALLOWED',
        'PASSWORD_CHANGE_ALLOWED',
        'JOIN_WEB_OF_REGISTRIES',
        'PART_NUMBER_PREFIX',
        'URI_PREFIX'
    ];

    var emailSettingKeys = [
        'ERROR_EMAIL_EXCEPTION_PREFIX',
        'ADMIN_EMAIL',
        'SMTP_HOST',
        'SEND_EMAIL_ON_ERRORS'
    ];

    // retrieve general setting
    $scope.getSetting = function () {
        var sessionId = $cookieStore.get("sessionId");

        // retrieve site wide settings
        var settings = Settings(sessionId);
        settings.get(function (result) {
            $rootScope.settings = result;

            $scope.generalSettings = [];
            $scope.emailSettings = [];

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

iceControllers.controller('ProfileGroupsController', function ($scope, $location, $cookieStore, $stateParams, User) {
    var profileId = $stateParams.id;
    $location.path("/profile/" + profileId + "/groups", false);
    $scope.selectedUsers = [];

    var user = User($cookieStore.get('sessionId'));
    user.getGroups({userId:profileId}, function (result) {
        $scope.userGroups = result;
    });

    user.list(function (result) {
        $scope.users = result;
    });

    $scope.selectUser = function (user) {
        $scope.selectedUsers.push(user);
        var index = $scope.users.indexOf(user);
        if (index != -1)
            $scope.users.splice(index, 1);
    }
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
iceControllers.controller('CollectionController', function ($scope, $state, $location, $cookieStore, $rootScope, Folders, Settings, sessionValid, Search) {
    console.log("CollectionController");

    // todo : set on all
    // $location.search('q', null);

    if (sessionValid === undefined || sessionValid.data.sessionId === undefined) {
        return;
    }

    var sessionId = $cookieStore.get("sessionId");
    $scope.searchFilters = {};

    // retrieve site wide settings
    var settings = Settings(sessionId);
    settings.get(function (result) {
        $rootScope.settings = result;
    });

    // retrieve user settings


    // default list of collections
    $scope.collectionList = [
        { name:'available', display:'Available', icon:'fa-folder', iconOpen:'fa-folder-open'},
        { name:'personal', display:'Personal', icon:'fa-folder', iconOpen:'fa-folder-open'},
        { name:'shared', display:'Shared', icon:'fa-folder', iconOpen:'fa-folder-open'},
        { name:'bulkUpload', display:'Drafts', icon:'fa-edit', iconOpen:'fa-edit'},
        { name:'deleted', display:'Deleted', icon:'fa-trash-o', iconOpen:'fa-trash-o'}
    ];

    // entry items that can be created
    $scope.items = [
        {name:"Plasmid", type:"plasmid"},
        {name:"Strain", type:"strain"},
        {name:"Part", type:"part"},
        {name:"Arabidopsis Seed", type:"seed"}
    ];

    if ($location.path() === "/") {
        // change state
        $location.path("/folders/personal");
//        // a bit of a hack. the folders are a child state so when
//        // url/folder/personal is accessed, this code is still executed (stateParams do not help here)
//        // so that causes personal folder to be retrieved twice
//        $scope.folder = undefined; // should already be undefined
//        console.log("CC - retrieving personal entries");
//
//        var folders = Folders;
//        folders.folder({folderId:'personal'}, function (result) {
//            $scope.folder = result;
//        });
    }

    // selected entries
    $scope.selection = [];

    // search
    $scope.runUserSearch = function () {
        var search = Search();

        console.log("running", $scope.searchFilters);

        $scope.loading = true;
        $scope.searchResults = undefined;
        search.runSearch($scope.searchFilters,
            function (result) {
                $scope.searchResults = result;
                $scope.loadingPage = false;
                console.log(result);
            },
            function (error) {
                $scope.loadingPage = false;
            }
        );
    };

    // table
    $scope.alignmentGraph = function (searchResult) {
        var ptsPerPixel = searchResult.queryLength / 100;
        var start = Number.MAX_VALUE;
        var end = Number.MIN_VALUE;
        var stripes = {};

        var started = false;
        var matchDetails = searchResult.matchDetails;

        for (var i = 0; i < matchDetails.length; i++) {
            var line = matchDetails[i].replace(/"/g, ' ').trim();

            if (line.lastIndexOf("Query", 0) === 0) {
                started = true;
                var l = [];
                l = line.split(" ");
                var tmp = (l[2]);
                if (tmp < start)
                    start = tmp;

                // check end
                tmp = (l[l.length - 1]);
                if (tmp > end)
                    end = tmp;
            } else if (line.lastIndexOf("Score", 0) === 0) { // starting a new line
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

        return results;
    };
});

iceControllers.controller('ImportController', function ($rootScope, $scope, $modal, $cookieStore, $resource, $stateParams, $fileUploader, $http, Upload) {
    var sid = $cookieStore.get("sessionId");
    var upload = Upload(sid);

    $scope.bulkUpload = {};
    $scope.bulkUpload.entryIdData = [];
    $scope.bulkUpload.name = "untitled";
    $scope.uploadNameEditMode = false;

    $scope.setNameEditMode = function (value) {
        $scope.uploadNameEditMode = value;
    };

    var uploader = $fileUploader.create({
        scope:$scope, // to automatically update the html. Default: $rootScope
        url:"/rest/file/sequence",
        method:'POST',
        removeAfterUpload:true,
        headers:{"X-ICE-Authentication-SessionId":sid},
        autoUpload:true,
        queueLimit:1 // can only upload 1 file
    });

    uploader.bind('beforeupload', function (event, item) {
        console.log("beforeupload");
        item.formData.push({entryType:$scope.importType});
        item.formData.push({entryRecordId:$scope.bulkUpload.entryIdData[row]});
    });

    uploader.bind('progress', function (event, item, progress) {
        if (progress != "100")  // isUploading is always true until it returns
            return;

        // upload complete. have processing
        $scope.processingFile = item.file.name;
    });

    $scope.onFileSelect = function ($files) {
        console.log("fileSelect");
    };

    $scope.createSheet = function () {
        var availableWidth, availableHeight, $window = $(window), $dataTable = $("#dataTable");
        var plasmidHeaders, strainHeaders, seedHeaders;
        var columns = [];

        // headers
        // part
        var partHeaders = ["Principal Investigator <span class='required'>*</span>"
            , "PI Email <i class='pull-right opacity_hover fa fa-question-circle' title='tooltip'></i>"
            , "Funding Source"
            , "Intellectual Property"
            , "BioSafety Level <span class='required'>*</span>"
            , "Name <span class='required'>*</span>"
            , "Alias"
            , "Keywords"
            , "Summary <span class='required'>*</span>"
            , "Notes"
            , "References"
            , "Links"
            , "Status <span class='required'>*</span>"
            , "Creator <span class='required'>*</span>"
            , "Creator Email <span class='required'>*</span>"
            // other headers are inserted here
            , "Sequence FileName"
            , "Attachment FileName"];

        var dataSchema;
        switch ($scope.importType) {
            case "strain":
                strainHeaders = angular.copy(partHeaders);
                strainHeaders.splice.apply(strainHeaders, [15, 0].concat(["Parental Strain", "Genotype or Phenotype", "Plasmids",
                    "Selection Markers"]));
                dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, parentStrain:null, genotypePhenotype:null, plasmids:null, selectionMarkers:null, sequenceFilename:null, attachmentFilename:null};
                break;

            case "plasmid":
                plasmidHeaders = angular.copy(partHeaders);
                plasmidHeaders.splice.apply(plasmidHeaders, [15, 0].concat(["Circular", "Backbone", "Promoters", "Replicates In",
                    "Origin of Replication", "Selection Markers"]));
                dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, circular:null, backbone:null, promoters:null, replicatesIn:null, originOfReplication:null, selectionMarkers:null, sequenceFilename:null, attachmentFilename:null};
                break;

            case "seed":
                seedHeaders = angular.copy(partHeaders);
                seedHeaders.splice.apply(seedHeaders, [15, 0].concat(["Homozygosity", "Ecotype", "Harvest Date", "Parents",
                    "Plant Type", "Generation", "Sent to ABRC?"]));
                dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, homozygosity:null, ecotype:null, harvestDate:null, parents:null, plantType:null, generation:null, sentToAbrc:null, sequenceFilename:null, attachmentFilename:null};
                break;

            case "part":
                dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, sequenceFilename:null, attachmentFilename:null};
                break;
        }

        // cell renderer for file upload
        var fileUploadRenderer = function (instance, td, row, col, prop, value, cellProperties) {
//            console.log(instance, td, row, col, prop, value, cellProperties);
            var escaped = Handsontable.helper.stringify(value);

            var $up = $('<span class="fileUpload"><i class="fa fa-upload opacity_hover opacity_4"></i> Upload '
                + '<input type="file" ng-model="sequenceFiles" ng-file-select="onFileSelect($files)" class="upload" />');

            $up.on("change", function (event) {
                console.log("change", $scope.sequenceFiles);
            });

            $(td).empty().append($up);
            return td;
        };

        var autoComplete = function (field, query, process) {
            $http.get('/rest/part/autocomplete', {
                headers:{'X-ICE-Authentication-SessionId':sid},
                params:{
                    val:query,
                    field:field
                }
            }).then(function (res) {
                    return process(res.data);
                });
        };

        for (var prop in dataSchema) {
            if (dataSchema.hasOwnProperty(prop)) {
                var object = {};
                object.data = prop;

                switch (prop) {
                    case 'circular':
                    case 'sentToAbrc':
                        object.type = 'checkbox';
                        break;

                    case 'bioSafetyLevel':
                        object.type = 'autocomplete';
                        object.source = ['Level 1', 'Level 2', ''];
                        break;

                    case 'selectionMarkers':
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('SELECTION_MARKERS', query, process);
                        };
                        break;

                    case "promoters":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('PROMOTERS', query, process);
                        };
                        break;

                    case "replicatesIn":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('REPLICATES_IN', query, process);
                        };
                        break;

                    case "originOfReplication":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('ORIGIN_OF_REPLICATION', query, process);
                        };
                        break;

                    case 'sequenceFilename':
                    case "attachmentFilename":
                        object.renderer = fileUploadRenderer;
                        object.type = 'text';
                        object.readOnly = true;  // file cells are readonly. all data is set programmatically
                        object.copyable = false; // file cells cannot be copied
                        break;
                }

                columns.push(object);
            }
        }

        var getSheetHeaders = function (index) {
            switch ($scope.importType) {
                case "strain":
                    return strainHeaders[index];

                case "plasmid":
                    return plasmidHeaders[index];

                case "part":
                    return partHeaders[index];

                case "seed":
                    return seedHeaders[index];
            }
        };

        var calculateSize = function () {
            var offset = $dataTable.offset();
            availableWidth = $window.width() - offset.left + $window.scrollLeft();
            availableHeight = $window.height() - offset.top + $window.scrollTop();
            $dataTable.handsontable('render');
        };

        var widthFunction = function () {
            if (availableWidth === void 0) {
                calculateSize();
            }
            return availableWidth;
        };

        var heightFunction = function () {
            if (availableHeight === void 0) {
                calculateSize();
            }
            return availableHeight - 87;
        };

        $window.on('resize', calculateSize);

        var createOrUpdateEntry = function (data) {
            var row = data[0];
            var objectProperty = data[1];
            var value = data[3];
            var entryIdDataIndex = $scope.bulkUpload.entryIdData[row];

            // if no entry associated with row and now data, skip
            if (value.trim() === "" && !entryIdDataIndex)
                return;

            var object = {};

            object[objectProperty] = value;
            if (entryIdDataIndex) {
                object['id'] = entryIdDataIndex;
            }

            if ($scope.bulkUpload.id === undefined) {
                // create draft of specified type
                upload.create({type:$scope.importType})
                    .$promise
                    .then(function (result) {
                        // create entry.
                        console.log("created new bulk upload", result);
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $scope.bulkUpload.name = result.name;

                        upload.createEntry({importId:result.id}, object,
                            function (createdEntry) {
                                $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                                $scope.saving = false;
                                console.log("created entry", $scope.bulkUpload);
                            },
                            function (error) {
                                console.error(error);
                                $scope.saving = false;
                            });
                    });
            } else {
                // check if row being updated has existing entry
                if (!object['id']) {
                    // create new entry for existing upload
                    upload.createEntry({importId:$scope.bulkUpload.id}, object,
                        function (createdEntry) {
                            $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                            console.log("NEW ENTRY", createdEntry, $scope.bulkUpload.entryIdData.length);
                            $scope.bulkUpload.lastUpdate = createdEntry.modificationTime;
                            $scope.saving = false;
                        });
                } else {
                    // update entry for existing upload
                    object.recordType = $scope.importType;

                    upload.updateEntry({importId:$scope.bulkUpload.id, entryId:object.id}, object,
                        function (updatedEntry) {
                            console.log("UPDATE", updatedEntry);
                            if (!$scope.bulkUpload.entryIdData[row] === updatedEntry.id) {
                                console.error("Returned id does not match");
                                return;
                            }
                            $scope.bulkUpload.lastUpdate = updatedEntry.modificationTime;
                            $scope.saving = false;
                        },
                        function (error) {
                            // todo : this should revert the change in the ui and display a message
                            console.error(error);
                            $scope.saving = false;
                        });
                }
            }
        };

        // callback for change to the sheet to save/update the information on the server
        var afterChange = function (change, source) {
            // "setMyData" is intended to be used when setting data in code to prevent updates
            if (source === 'loadData' || source === 'setMyData') {
                return; //data load, not need to save
            }

            $scope.saving = true;
            // single cell edit

            if (source === "edit") {
                createOrUpdateEntry(change[0]);
            } else if (source === "autofill") {
                // click and drag
                console.log($scope.bulkUpload, change);
                for (var i = 0; i < change.length; i += 1) {
                    createOrUpdateEntry(change[i]);
                }
            } else if (source === "paste") {
                // todo
                // paste from copy
            }
//            console.log("change", change, "source", source);
        };

        var options = {
            data:[],
            dataSchema:dataSchema,
            startRows:50, // comes into effect only if no data is provided
            minRows:50,
//        startCols: plasmidHeaders.length, // ignored because of "columns"
            colHeaders:getSheetHeaders,
            rowHeaders:true,
            colWidths:150,
            stretchH:'all',
            minSpareRows:1,
            enterMoves:{row:0, col:1}, // move right on enter instead of down
            autoWrapRow:true,
            autoWrapCol:true,
            columns:columns,
            width:widthFunction,
            height:heightFunction,
            afterChange:afterChange
        };

        $dataTable.handsontable(options);
        $scope.spreadSheet = $dataTable.data('handsontable');

        $scope.fileUploadModal = function () {
            var modalInstance = $modal.open({
                templateUrl:'views/modal/file-upload.html',
                controller:'BulkUploadModalController',
                backdrop:'static',
                resolve:{
                    addType:function () {
                        return $stateParams.type;
                    }
                }
            });
        };

        $scope.confirmResetFormModal = function () {
            var resetModalInstance = $modal.open({
                templateUrl:'views/modal/reset-bulk-upload-sheet.html',
                controller:'BulkUploadModalController',
                backdrop:'static',
                resolve:{
                    addType:function () {
                        return $stateParams.type;
                    }
                }
            });
        };

        $scope.showBulkUploadRenameModal = function () {
            var modalInstance = $modal.open({
                templateUrl:'views/modal/rename-bulk-upload-sheet.html',
                controller:function ($scope, $modalInstance, uploadName) {
                    $scope.newBulkUploadName = uploadName;
                },
                backdrop:'static',
                resolve:{
                    uploadName:function () {
                        return $scope.bulkUpload.name;
                    }
                }
            });

            modalInstance.result.then(function (newName) {
                // update name on the server if a bulk upload has already been created
                if ($scope.bulkUpload.id) {
                    var tmp = {id:$scope.bulkUpload.id, name:newName};
                    console.log($scope.bulkUpload, tmp);
                    Upload(sid).bulkUpdate({importId:$scope.bulkUpload.id}, tmp, function (result) {
                        $scope.bulkUpload.name = result.name;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $rootScope.$broadcast("BulkUploadNameChange", tmp);
                    });
                } else {
                    // just update display name
                    $scope.bulkUpload.name = newName;
                }
            }, function () {
                // dismiss callback
            });
        };

        $scope.sheetCreated = true;
    };

    var asyncLoop = function (loopFunction) {

        // loop function
        var loop = function (start) {
            loopFunction.functionToLoop(loop, start);
        };

        loop(0);//init
    };

    // retrieve
    $scope.uploadEntries = [];

    if (!isNaN($stateParams.type)) {
        asyncLoop({
            functionToLoop:function (loop, start) {
                upload.get(
                    {importId:$stateParams.type, offset:start, limit:40},
                    function (result) {
                        $scope.bulkUpload.name = result.name;
                        $scope.importType = result.type.toLowerCase();
                        if (start === 0)
                            $scope.createSheet();
                        // else render on append data
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = new Date(result.lastUpdate);
                        var l = $scope.bulkUpload.entryIdData.length;

                        if (result.entryList && result.entryList.length) {
                            for (var i = 0; i < result.entryList.length; i += 1) {
                                $scope.bulkUpload.entryIdData[l + i] = result.entryList[i].id;    // todo index here is starting from 0 again
//                                console.log(l+i, $scope.bulkUpload.entryIdData[l+i]);
                                $scope.uploadEntries.push(result.entryList[i]);
                            }
                        }
                        $scope.spreadSheet.loadData(angular.copy($scope.uploadEntries));
                        if ($scope.uploadEntries.length < result.count) {
                            loop(start + result.entryList.length);
                        }
                    });
            }
        });
    } else {
        $scope.importType = $stateParams.type;
        $scope.createSheet();
    }
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

iceControllers.controller('SearchInputController', function ($scope, $rootScope, $http, $cookieStore, $location) {
    console.log("SearchInputController", $scope.searchFilters);
    $scope.searchTypes = {all:true, strain:true, plasmid:true, part:true, arabidopsis:true};

    $scope.check = function (selection) {
        var allTrue = true;
        for (var type in $scope.searchTypes) {
            if ($scope.searchTypes.hasOwnProperty(type) && type !== 'all') {
                if (selection === 'all')
                    $scope.searchTypes[type] = $scope.searchTypes.all;
                allTrue = (allTrue && $scope.searchTypes[type] === true);
            }
        }
        $scope.searchTypes.all = allTrue;
    };

    $scope.search = function () {
        console.log("searching");

        $scope.searchFilters.q = $scope.queryText;
        $scope.searchFilters.s = $scope.sequenceText;
        $scope.searchFilters.sort = 'relevance';
        $scope.searchFilters.asc = false;
        $scope.searchFilters.entryTypes = [];
        $scope.searchFilters.sp = "BLAST_N";
        for (var type in $scope.searchTypes) {
            if ($scope.searchTypes.hasOwnProperty(type) && type !== 'all') {
                if ($scope.searchTypes[type])
                    $scope.searchFilters.entryTypes.push(type);
            }
        }

        $scope.loadingPage = true;
        $location.path('/search');
        $location.search('q', $scope.queryText);

        $scope.runUserSearch();
    };

    $scope.reset = function () {
    };
});

iceControllers.controller('SearchController', function ($scope, $http, $cookieStore, $location) {
    console.log("SearchController", $scope.searchFilters);
    var sessionId = $cookieStore.get("sessionId");
    var queryString = $location.search().q;
    $scope.queryString = queryString;

    // param defaults
    if (!$scope.loadingPage) {
        $scope.searchFilters.q = queryString;
        $scope.searchFilters.sort = 'relevance';
        $scope.searchFilters.asc = false;
        $scope.searchFilters.limit = 15;
        $scope.searchFilters.entryTypes = ['strain', 'plasmid', 'arabidopsis', 'part'];
        $scope.searchFilters.sp = "BLAST_N";
        $scope.runUserSearch();
    }

    $scope.setSearchResultPage = function (pageNo) {
        $scope.loadingPage = true;
        $scope.searchFilters.offset = (pageNo - 1) * 15;
        $scope.runUserSearch();
    };

    // tODO : sort

    $scope.maxSize = 5;  // number of clickable pages to show in pagination
    $scope.currentPage = 1;

    $scope.getType = function (relScore) {
        if (relScore === undefined)
            return 'info';

        if (relScore >= 70)
            return 'success';
        if (relScore >= 30 && relScore < 70)
            return 'warning';
        if (relScore < 30)
            return 'danger';
        return 'info';
    }
});

iceControllers.controller('FullScreenFlashController', function ($scope, $location) {
    console.log("FullScreenFlashController");
    $scope.entryId = $location.search().entryId;
    $scope.sessionId = $location.search().sessionId;
    $scope.entry = {'recordId':$scope.entryId};
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
        $scope.loadingPage = true;
        if ($scope.params.folderId === undefined)
            $scope.params.folderId = 'personal';
        $scope.params.offset = (pageNo - 1) * 15; // TODO : make sure it is a number
        console.log("page#", pageNo, "queryParams", $scope.params);
        folders.folder($scope.params, function (result) {
            $scope.folder = result;
            $scope.loadingPage = false;
        });
    };

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

// controller for <ice.menu.collections> directive
iceControllers.controller('CollectionMenuController', function ($cookieStore, $scope, $modal, $rootScope, $location, $stateParams, Folders) {
    var sessionId = $cookieStore.get("sessionId");
    var folders = Folders();

    // default is personal folder
    console.log("CollectionMenuController");
    console.log("selected collection", $stateParams.collection);

    //
    // initialize
    //

    // folders contained in the selected folder (default selected to personal)
    $scope.selectedCollectionFolders = undefined;
    $scope.selectedFolder = $stateParams.collection === undefined ? 'personal' : $stateParams.collection;

    // retrieve collections contained in the selected folder
    console.log("retrieving sub collections", $scope.selectedFolder);
    folders.getByType({folderType:$scope.selectedFolder},
        function (result) {
            $scope.selectedCollectionFolders = result;
        }, function (error) {
            console.error(error);
        });

    console.log("CMC - retrieving folder counts");
    folders.query(function (result) {
        if (result === undefined || $scope.collectionList === undefined)
            return;

        for (var i = 0; i < $scope.collectionList.length; i += 1) {
            var item = $scope.collectionList[i];
            item.count = result[item.name];
        }
    });
    //
    // end initialize
    //

    // called from collections-menu-details.html when a collection's folder is selected
    // simply changes state to folder and allows the controller for that to handle it
    $scope.selectCollectionFolder = function (folder) {
        console.log($scope.selectedCollectionFolders);
        console.log("selectCollectionFolder(TODO)", folder, $scope.selectedFolder);
        console.log("/" + folder.type + "/" + folder.id);

        // type on server is PUBLIC, PRIVATE, SHARED, UPLOAD
        var type = folder.type.toLowerCase();
        if (type !== "upload")
            type = "folders";

        $location.path("/" + type + "/" + folder.id);
        $scope.folder = undefined;   // this forces "Loading..." to be shown
    };

    // called when a collection is selected. Collections are pre-defined ['Available', 'Deleted', etc]
    // and some allow folders and when that is selected then the selectCollectionFolder() is called
    $scope.selectCollection = function (name) {
        console.log("selectCollection()", name);
        $location.path("/folders/" + name);
        $scope.selectedFolder = name;
        $scope.selectedCollectionFolders = undefined;

        // retrieve sub folders for selected collection
        folders.getByType({folderType:$scope.selectedFolder, folderId:name},
            function (result) {
                $scope.selectedCollectionFolders = result;
            },
            function (error) {
                console.error(error);
            });
    };

    $scope.currentPath = function (param) {
        if ($stateParams.collection === undefined && param === 'personal')
            return true;
        return $stateParams.collection === param;
    };

    // BulkUploadNameChange handler
    $scope.$on("BulkUploadNameChange", function (event, data) {
        if (data === undefined || $scope.selectedFolder !== "bulkUpload" || $scope.selectedCollectionFolders === undefined) // todo : use vars
            return;

        for (var i = 0; i < $scope.selectedCollectionFolders.length; i += 1) {
            var subFolder = $scope.selectedCollectionFolders[i];
            if (subFolder.id !== data.id)
                continue;

            $scope.selectedCollectionFolders[i].folderName = data.name;
            break;
        }
    });
});


iceControllers.controller('BulkUploadModalController', function ($scope, $location, $cookieStore, $routeParams, $modalInstance, $fileUploader, addType) {
    var sid = $cookieStore.get("sessionId");
    $scope.addType = addType;

    var uploader = $scope.importUploader = $fileUploader.create({
//        scope: $scope, // to automatically update the html. Default: $rootScope
        url:"/rest/file/bulk-import",
        method:'POST',
//        removeAfterUpload:true,
        headers:{"X-ICE-Authentication-SessionId":sid},
        formData:[
            { type:addType }
        ]
//        filters: [
//            function (item) {                    // user defined filter example
//                console.info('filter1', item);   // added to queue if it returns true
//                return true;
//            }
//        ]
    });

    uploader.bind('success', function (event, xhr, item, response) {
        var info = response;
        console.log("success", response);
        $scope.modalClose = "Close";
        $scope.processing = false;
//
        if (!isNaN(response)) {
            $modalInstance.close();
            $location.path("/upload/" + response);
        }
    });

    uploader.bind('error', function (event, xhr, item, response) {
        console.info('Error', xhr, item, response);
        $scope.uploadError = response;
        $scope.processing = false;
    });

    uploader.bind('complete', function (event, xhr, item, response) {
        console.info('Complete', xhr, item, response);
        $scope.processing = false;
    });

    $scope.ok = function () {
        $modalInstance.close($scope.selected.item);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.uploadFile = function () {
        uploader.uploadAll();
    };

    // example of event binding
    uploader.bind('afteraddingfile', function (event, item) {
        console.info('After adding a file', item);
//        item.upload();
    });

    uploader.bind('progress', function (event, item, progress) {
        console.info('progress', item, progress);
        if (progress !== '100')
            return;

        $scope.processing = true;
        item.remove();
    });
});

iceControllers.controller('UserController', function ($scope, $routeParams, Entry) {
//    $scope.entry = Entry.query({partId:$routeParams.id});
});

iceControllers.controller('LoginController', function ($scope, $location, $cookieStore, $cookies, $rootScope, Authentication) {
    $scope.submit = function () {
        Authentication.login($scope.userId, $scope.userPassword);
    };

    $scope.goToRegister = function () {
        $location.path("/register");
    }
});

iceControllers.controller('EditEntryController', function ($scope, $location, $cookieStore, $rootScope, $stateParams, Entry) {
    console.log("EditEntryController");

    var entry = Entry($cookieStore.get("sessionId"));

    entry.query({partId:$stateParams.id}, function (result) {
        $scope.entry = result;
    });

    $scope.editEntry = function () {
        entry.update($scope.entry, function (result) {
            $location.path("/entry/" + result.id);
        });
    };
});

iceControllers.controller('CreateEntryController', function ($http, $scope, $modal, $rootScope, $fileUploader, $location, $stateParams, $cookieStore, Entry) {
    console.log("CreateEntryController", $stateParams.type, $scope.part);
    $scope.createType = $stateParams.type;
    $scope.showMain = true;

    var partFields = [
        {label:"Name", required:true, schema:'name', help:'Help Text', placeHolder:'e.g. JBEI-0001', inputType:'short'},
        {label:"Alias", schema:'alias', inputType:'short'},
        {label:"Principal Investigator", required:true, schema:'principalInvestigator', inputType:'withEmail', bothRequired:'false'},
        {label:"Funding Source", schema:'fundingSource', inputType:'short'},
        {label:"Status", schema:'status', options:[
            {value:"Complete", text:"Complete"},
            {value:"In Progress", text:"In Progress"},
            {value:"Abandoned", text:"Abandoned"},
            {value:"Planned", text:"Planned"}
        ]},
        {label:"Bio Safety Level", schema:'bioSafetyLevel', options:[
            {value:"1", text:"Level 1"},
            {value:"2", text:"Level 2"}
        ]},
        {label:"Creator", required:true, schema:'creator', inputType:'withEmail', bothRequired:'true'},
        {label:"Links", schema:'links', inputType:'add'},
        {label:"Summary", required:true, schema:'shortDescription', inputType:'long'},
        {label:"References", schema:'references', inputType:'long'},
        {label:"Intellectual Property", schema:'intellectualProperty', inputType:'long'}
    ];

    var plasmidFields = [
        {label:"Backbone", schema:'backbone', inputType:'medium'},
        {label:"Origin of Replication", schema:'originOfReplication', inputType:'autoComplete',
            autoCompleteField:'ORIGIN_OF_REPLICATION'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'},
        {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER'},
        {label:"Promoters", schema:'promoters', inputType:'autoComplete', autoCompleteField:'PROMOTERS'},
        {label:"Replicates In", schema:'replicatesIn', inputType:'autoComplete', autoCompleteField:'REPLICATES_IN'}
    ];

    var seedFields = [
        {label:"Sent To ABRC", schema:'sentToABRC', help:"Help Text", inputType:'bool'},
        {label:"Plant Type", schema:'plantType', options:[
            {value:"EMS", text:"EMS"},
            {value:"OVER_EXPRESSION", text:"Over Expression"},
            {value:"RNAI", text:"RNAi"},
            {value:"REPORTER", text:"Reporter"},
            {value:"T_DNA", text:"T-DNA"},
            {value:"OTHER", text:"Other"}
        ]},
        {label:"Generation", schema:'generation', options:[
            {value:"UNKNOWN", text:"UNKNOWN"},
            {value:"F1", text:"F1"},
            {value:"F2", text:"F2"},
            {value:"F3", text:"F3"},
            {value:"M0", text:"M0"},
            {value:"M1", text:"M1"},
            {value:"M2", text:"M2"},
            {value:"T0", text:"T0"},
            {value:"T1", text:"T1"},
            {value:"T2", text:"T2"},
            {value:"T3", text:"T3"},
            {value:"T4", text:"T4"},
            {value:"T5", text:"T5"}
        ]},
        {label:"Harvest Date", schema:'harvestDate', inputType:'date'},
        {label:"Homozygosity", schema:'backbone', inputType:'medium'},
        {label:"Ecotype", schema:'backbone', inputType:'medium'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'}
    ];

    var strainFields = [
        {label:"Parent Strain", schema:'parentalStrain', placeHolder:"Part Number", inputType:'autoComplete',
            autoCompleteField:'PLASMID_PART_NUMBER'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'},
        {label:"Genotype/Phenotype", schema:'genotypePhenotype', inputType:'long'},
        {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER'}
    ];

    var partDefaults = {
        recordType:$scope.createType,
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
    $scope.part.linkedParts = [];
    $scope.activePart = $scope.part;

    var getFieldsForType = function (type) {
        var fields = angular.copy(partFields);
        switch (type) {
            case 'strain':
                fields.splice.apply(fields, [7, 0].concat(strainFields));
                return fields;

            case 'seed':
                fields.splice.apply(fields, [7, 0].concat(seedFields));
                return fields;

            case 'plasmid':
                fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                return fields;

            case 'part':
            default:
                return fields;
        }
    };

    $scope.selectedFields = getFieldsForType($scope.createType);

    $scope.addLink = function (schema, index) {
        $scope.part[schema].splice(index + 1, 0, {value:''});
    };

    $scope.removeLink = function (schema, index) {
        $scope.part[schema].splice(index, 1);
        $scope.colLength = 11 - $scope.part.linkedParts.length;
    };

    $scope.addNewPartLink = function (type) {
        $scope.selectedFields = getFieldsForType(type);

        var newLink = angular.copy(partDefaults);
        newLink.recordType = type;
        $scope.part.linkedParts.push(newLink);

        $scope.colLength = 11 - $scope.part.linkedParts.length;
        $scope.active = $scope.part.linkedParts.length - 1;
        $scope.activePart = $scope.part.linkedParts[$scope.active];
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

//        var type = $model.type;
//        $scope.selectedFields = getFieldsForType(type);
//
//        var newLink = angular.copy(partDefaults);
//        newLink.recordType = type;
//        $scope.part.linkedParts.push(newLink);
//
//        $scope.colLength = 11 - $scope.part.linkedParts.length;
//        $scope.active = $scope.part.linkedParts.length - 1;
//        $scope.activePart = $scope.part.linkedParts[$scope.active];
//        $scope.activePart.isExistingPart = true;
//        $scope.addExisting = false;
    };

    $scope.deleteNewPartLink = function (index) {
        // todo : not working
        console.log("delete part link at index", index);
        $scope.part.linkedParts.splice(index, 1);
        if ($scope.active === index) {
            // set new active
            console.log("set new active");
            if (index + 1 < $scope.part.linkedParts.length)
                $scope.active = index + 1;
            else {
                if ($scope.part.linkedParts.length === 0)
                // not really needed since when main will not be shown if no other tabs present
                    $scope.active = 'main';
                else
                    $scope.active = index - 1;
            }
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
        $scope.selectedFields = getFieldsForType($scope.activePart.recordType);
    };

    var sid = $cookieStore.get("sessionId");

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

    var entry = Entry(sid);

    var toStringArray = function (objArray) {
        var result = [];
        angular.forEach(objArray, function (object) {
            if (!object || !object.value || object.value === "")
                return;
            result.push(object.value);
        });
        return result;
    };

    var validateFields = function (part) {
        var canSubmit = true;

        // main type
        var mainFields = getFieldsForType(part.type);
        angular.forEach(mainFields, function (field) {
            if (!field.required)
                return;

            if (field.inputType === 'add' || field.inputType === 'autoCompleteAdd') {
                if (part[field.schema].length == 0) {
                    field.invalid = true;
                }
                else {
                    for (var i = 0; i < part[field.schema].length; i += 1) {
                        var fieldValue = part[field.schema][i].value;
                        field.invalid = !fieldValue || fieldValue === '';
                    }
                }
            } else {
                field.invalid = (!part[field.schema] || part[field.schema] === '');
            }

            if (canSubmit) {
                canSubmit = !field.invalid;
            }
        });
        return canSubmit;
    };

    $scope.submitPart = function () {

        // validate main
        var canSubmit = validateFields($scope.part);

        // validate components if any
        if ($scope.part.linkedParts && $scope.part.linkedParts.length) {
            for (var idx = 0; idx < $scope.part.linkedParts.length; idx += 1) {
                var canSubmitLinked = validateFields($scope.part.linkedParts[idx]);
                if (!canSubmitLinked) {
                    // show icon in tab
                    // todo
                    console.log("not  valid");
                    canSubmit = canSubmitLinked;
                }
            }
        }

        if (!canSubmit) {
            $("body").animate({scrollTop:130}, "slow");
            return;
        }

        // convert arrays of objects to array strings
        $scope.part.links = toStringArray($scope.part.links);
        $scope.part.selectionMarkers = toStringArray($scope.part.selectionMarkers);

        for (var i = 0; i < $scope.part.linkedParts.length; i += 1) {
            $scope.part.linkedParts[i].links = toStringArray($scope.part.linkedParts[i].links);
            $scope.part.linkedParts[i].selectionMarkers = toStringArray($scope.part.linkedParts[i].selectionMarkers);
        }

        if ($scope.part.id) {
            entry.update({partId:$scope.part.id}, $scope.part, function (result) {
                $location.path('/entry/' + result.id);
            });
        } else {
            entry.create($scope.part, function (result) {
                console.log("created entry", result);
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
        console.info('Before upload', item);
        item.formData.push({entryType:"plasmid"}); // todo
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

iceControllers.controller('EntryPermissionController', function ($scope, $cookieStore, User, Entry, filterFilter) {
    console.log("EntryPermissionController");
    var sessionId = $cookieStore.get("sessionId");
    var user = User(sessionId);
    var entry = Entry(sessionId);
    var panes = $scope.panes = [];

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

    $scope.showAddPermissionOptionsClick = function (pane) {
        $scope.showPermissionInput = true;

        // TODO : consider, instead of retrieving all and filtering, try on the server first
        user.list(function (result) {
            $scope.users = result;
            $scope.filteredUsers = angular.copy(result);

            angular.forEach($scope.users, function (item) {
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    if (item.id == $scope.activePermissions[i].articleId && $scope.activePermissions[i].article === 'ACCOUNT') {
                        item.selected = true;
                        item.permissionId = $scope.activePermissions[i].id;
                        break;
                    }
                }
            });
        });
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
    $scope.addRemovePermission = function (user) {
        user.selected = !user.selected;
        if (user.selected) {
            var type;
            angular.forEach(panes, function (pane) {
                if (pane.selected) {
                    type = pane.title.toUpperCase() + "_ENTRY";
                }
            });
            var permission = {article:'ACCOUNT', type:type, typeId:$scope.entry.id, articleId:user.id};
            entry.addPermission({partId:$scope.entry.id}, permission, function (result) {
                // result is the permission object
                $scope.entry.id = result.typeId;
                $scope.activePermissions.push(result);
                user.permissionId = result.id;
            });
        } else {
            removePermission(user.permissionId);
        }
    };

    $scope.enablePublicRead = function (entry) {
        $scope.entry.publicRead = true;
        $scope.activePermissions.splice(0, 0, ({article:'GLOBAL', articleId:user.id, display:"Public"}));
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

iceControllers.controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $rootScope, $fileUploader, Entry, Folders) {
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

    var partFields = [
        {label:"Part ID", required:true, schema:'partId', canEdit:false, help:''},
        {label:"Name", required:true, schema:'name', help:'Help Text', placeHolder:'e.g. JBEI-0001', inputType:'short'},
        {label:"Alias", schema:'alias', inputType:'short'},
        {label:"Principal Investigator", required:true, schema:'principalInvestigator', inputType:'withEmail', bothRequired:'false'},
        {label:"Funding Source", schema:'fundingSource', inputType:'short'},
        {label:"Status", schema:'status', options:[
            {value:"Complete", text:"Complete"},
            {value:"In Progress", text:"In Progress"},
            {value:"Abandoned", text:"Abandoned"},
            {value:"Planned", text:"Planned"}
        ]},
        {label:"Bio Safety Level", schema:'bioSafetyLevel', options:[
            {value:"1", text:"Level 1"},
            {value:"2", text:"Level 2"}
        ]},
        {label:"Creator", required:true, schema:'creator', inputType:'withEmail', bothRequired:'true'},
        {label:"Links", schema:'links', inputType:'add'},
        {label:"Summary", required:true, schema:'shortDescription', inputType:'long'},
        {label:"References", schema:'references', inputType:'long'},
        {label:"Intellectual Property", schema:'intellectualProperty', inputType:'long'}
    ];

    var plasmidFields = [
        {label:"Backbone", schema:'backbone', inputType:'medium'},
        {label:"Origin of Replication", schema:'originOfReplication', inputType:'autoComplete',
            autoCompleteField:'ORIGIN_OF_REPLICATION'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'},
        {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER'},
        {label:"Promoters", schema:'promoters', inputType:'autoComplete', autoCompleteField:'PROMOTERS'},
        {label:"Replicates In", schema:'replicatesIn', inputType:'autoComplete', autoCompleteField:'REPLICATES_IN'}
    ];

    var seedFields = [
        {label:"Sent To ABRC", schema:'sentToABRC', help:"Help Text", inputType:'bool'},
        {label:"Plant Type", schema:'plantType', options:[
            {value:"EMS", text:"EMS"},
            {value:"OVER_EXPRESSION", text:"Over Expression"},
            {value:"RNAI", text:"RNAi"},
            {value:"REPORTER", text:"Reporter"},
            {value:"T_DNA", text:"T-DNA"},
            {value:"OTHER", text:"Other"}
        ]},
        {label:"Generation", schema:'generation', options:[
            {value:"UNKNOWN", text:"UNKNOWN"},
            {value:"F1", text:"F1"},
            {value:"F2", text:"F2"},
            {value:"F3", text:"F3"},
            {value:"M0", text:"M0"},
            {value:"M1", text:"M1"},
            {value:"M2", text:"M2"},
            {value:"T0", text:"T0"},
            {value:"T1", text:"T1"},
            {value:"T2", text:"T2"},
            {value:"T3", text:"T3"},
            {value:"T4", text:"T4"},
            {value:"T5", text:"T5"}
        ]},
        {label:"Harvest Date", schema:'harvestDate', inputType:'date'},
        {label:"Homozygosity", schema:'backbone', inputType:'medium'},
        {label:"Ecotype", schema:'backbone', inputType:'medium'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'}
    ];

    var strainFields = [
        {label:"Parent Strain", schema:'parentalStrain', placeHolder:"Part Number", inputType:'autoComplete',
            autoCompleteField:'PLASMID_PART_NUMBER'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'},
        {label:"Genotype/Phenotype", schema:'genotypePhenotype', inputType:'long'},
        {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER'}
    ];

    var partDefaults = {
        recordType:$scope.createType,
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

    var getFieldsForType = function (type) {
        var fields = angular.copy(partFields);
        switch (type) {
            case 'strain':
                fields.splice.apply(fields, [7, 0].concat(strainFields));
                return fields;

            case 'seed':
                fields.splice.apply(fields, [7, 0].concat(seedFields));
                return fields;

            case 'plasmid':
                fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                return fields;

            case 'part':
            default:
                return fields;
        }
    };

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
    entry.query({partId:$stateParams.id},
        function (result) {
            $rootScope.$broadcast("EntryRetrieved", result);
            $scope.entry = result;
            $scope.entryFields = getFieldsForType(result.type.toLowerCase());

            entry.statistics({partId:$stateParams.id}, function (stats) {
                $scope.entryStatistics = stats;
            });
        }, function (error) {
            console.error(error);
        });

    var menuSubDetails = $scope.subDetails = [
        {url:'/views/entry/general-information.html', display:'General Information', selected:true, icon:'fa-exclamation-circle'},
        {url:'/views/entry/sequence-analysis.html', display:'Sequence Analysis', selected:false, countName:'traceSequenceCount', icon:'fa-search-plus'},
        {url:'/views/entry/comments.html', display:'Comments', selected:false, countName:'commentCount', icon:'fa-comments-o'},
        {url:'/views/entry/samples.html', display:'Samples', selected:false, countName:'sampleCount', icon:'fa-flask'},
        {url:'/views/entry/history.html', display:'History', selected:false, countName:'historyCount', icon:'fa-history'},
        {url:'/views/entry/experiments.html', display:'Experimental Data', selected:false, count:'eddCount', icon:'fa-magic'}
    ];

    $scope.showSelection = function (index) {
        angular.forEach(menuSubDetails, function (details) {
            details.selected = false;
        });
        menuSubDetails[index].selected = true;
        $scope.selection = menuSubDetails[index].url;
    };

    $scope.selection = menuSubDetails[0].url;

    $scope.edit = function (type, val) {
        $scope[type] = val;
    };

    $scope.quickEditEntry = function (field) {
        if (!field.dirty) {
            return;
//            field.edit = false;
        }
        field.updating = true;

        entry.update($scope.entry, function (result) {
            if (field.inputType !== 'withEmail')
                field.edit = false;

            field.dirty = false;
            field.updating = false;
        }, function (error) {
            console.error(error);
            field.updating = false;
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

iceControllers.controller('EntryAttachmentController', function ($scope, $window, $cookieStore, $stateParams, $fileUploader, Attachment) {
    console.log("EntryAttachmentController");

    // create a uploader with options

    var sid = $cookieStore.get("sessionId");
    var attachment = Attachment(sid);

    var desc = "";
    $scope.$watch('attachmentDescription', function () {
        desc = $scope.attachmentDescription;
    });

    var uploader = $scope.uploader = $fileUploader.create({
        scope:$scope, // to automatically update the html. Default: $rootScope
        url:"/rest/file/attachment",
        method:'POST',
        removeAfterUpload:true,
        headers:{"X-ICE-Authentication-SessionId":sid}
//        formData:[
//            { description:desc}
//        ]
//        filters:[
//            function (item) {                    // user defined filter example
//                console.info('filter1', item);   // added to queue if it returns true
//                console.info('filter1', $scope.attachmentDescription);   // added to queue if it returns true
//                return true;
//            }
//        ]
    });

//    uploader.bind('beforeupload', function (event, item) {
//        item.uploader.url = item.url = 'upload?type=attachment&eid=' + $scope.entry.id;
//    });

    // example of event binding
//    uploader.bind('afteraddingfile', function (event, item) {
//        console.info('After adding a file', item);
//    });

    uploader.bind('success', function (event, xhr, item, response) {
        response.description = desc;
        attachment.create({partId:$stateParams.id}, response,
            function (result) {
                $scope.attachments.push(result);
                $scope.cancel();
            });
    });

    uploader.bind('error', function (event, xhr, item, response) {
        console.error('Error', xhr, item, response);
    });

//    uploader.bind('complete', function (event, xhr, item, response) {
//        console.info('Complete', xhr, item, response);
//    });

//    uploader.bind('cancel', function (event, xhr, item) {
//        console.info('cancel', xhr, item);
//    });

    $scope.cancel = function () {
        $scope.uploader.cancelAll();
        $scope.uploader.clearQueue();
        $scope.showAttachmentInput = false;
        $scope.attachmentDescription = undefined;
    };

    attachment.get({partId:$stateParams.id}, function (result) {
        $scope.attachments = result;
    });

    $scope.downloadAttachment = function (attachment) {
        $window.open("/rest/file/attachment/" + attachment.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
    };

    $scope.deleteAttachment = function (index, att) {
        attachment.delete({partId:$stateParams.id, attachmentId:att.id}, function (result) {
            confirmObject[index] = false;
            $scope.attachments.splice(index, 1);
        });
    };

    var confirmObject = {};
    $scope.confirmDelete = function (idx) {
        return confirmObject[idx];
    };

    $scope.setConfirmDelete = function (idx, value) {
        confirmObject[idx] = value;
    }
});

iceControllers.controller('EntryCommentController', function ($scope, $cookieStore, $stateParams, Entry) {
    var entryId = $stateParams.id;
    var entry = Entry($cookieStore.get("sessionId"));

    entry.comments({partId:entryId}, function (result) {
        $scope.entryComments = result;
    });

    entry.samples({partId:entryId}, function (result) {
        $scope.entrySamples = result;
    });

    $scope.createComment = function () {
        entry.createComment({partId:entryId}, $scope.newComment, function (result) {
            $scope.entryComments.splice(0, 0, result);
            $scope.addComment = false;
            $scope.entryStatistics.commentCount = $scope.entryComments.length;
        }, function (error) {
            console.error("comment create error", error);
        });
    };
});

iceControllers.controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, Entry) {
    var entryId = $stateParams.id;
    var entry = Entry($cookieStore.get("sessionId"));

    entry.traceSequences({partId:entryId}, function (result) {
        $scope.traceSequences = result;
    });

    $scope.deleteTraceSequenceFile = function (fileId) {
        var foundTrace = undefined;
        var foundIndex = undefined;

        for (var i = 0; i < $scope.traceSequences.length; i++) {
            var trace = $scope.traceSequences[i];
            if (trace.fileId === fileId && trace.fileId != undefined) {
                foundTrace = trace;
                foundIndex = i;
                break;
            }
        }

        if (foundTrace != undefined) {
            entry.deleteTraceSequence({partId:entryId, traceId:foundTrace.id}, function (result) {
                $scope.traceSequences.splice(foundIndex, 1);
                $scope.entryStatistics.traceSequenceCount = $scope.traceSequences.length;
            });
        }
    };

    $scope.downloadTraceFile = function (trace) {
        $window.open("/rest/file/trace/" + trace.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
    };
});

iceControllers.controller('FolderPermissionsController', function ($scope, $modalInstance, $cookieStore, Folders, User, folder) {
    console.log("FolderPermissionsController");

    var sessionId = $cookieStore.get("sessionId");
    var panes = $scope.panes = [];
    $scope.folder = folder;
    var user = User(sessionId);

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

    $scope.showAddPermissionOptionsClick = function (pane) {
        // TODO : instead of retrieving all and filtering, try on the server first
        user.list(function (result) {
            $scope.users = result;

            angular.forEach($scope.users, function (item) {
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    if (item.id == $scope.activePermissions[i].articleId) {
                        item.selected = true;
                        item.permissionId = $scope.activePermissions[i].id;
                        break;
                    }
                }
            });
        });
    };

    // retrieve permissions for folder
    Folders().permissions({folderId:folder.id}, function (result) {
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
    })
});
