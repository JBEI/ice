'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services', 'ui.bootstrap']);

iceControllers.controller('RegisterController', function ($scope, $resource, $location) {
    $scope.submit = function () {
        var User = $resource("/rest/profile");
        User.save({email:$scope.email, firstName:$scope.firstName, lastName:$scope.lastName, institution:$scope.institution, description:$scope.about}, function (data) {
            if (data.length != 0)
                $location.path("/login");
            else
                $scope.errorMsg = "Could not create account";
        });
    }
});

iceControllers.controller('CollectionController', function ($scope, $routeParams, $location, $cookieStore, Folders, Authentication) {
    Authentication.isSessionValid()
        .then(function (response) {
            if (response.data.length == 0) {
                $location.path("/login");
                return;
            }

            var folderType;
            if ($routeParams.collection === undefined)
                folderType = 'personal';
            else
                folderType = $routeParams.collection;

            var sessionId = $cookieStore.get("sessionId");
            var folders = new Folders(sessionId);
            $scope.folder = undefined;   // this forces "Loading..." to be shown
            console.log(folderType);

            switch (folderType) {
                case "all":
                    folders.all(function (data) {
                        $scope.selectedCollection = data;
                    });
                    folders.available(function (result) {
                        $scope.folder = result;
                    });
                    break;

                case "personal":
                    folders.personal(function (data) {
                        $scope.selectedCollection = data;
                    });

                    // retrieve user folders
                    folders.userEntries(function (result) {
                        $scope.folder = result;
                    });
                    break;
            }
        });
});

// deals with sub collections e.g. /folders/all/5
iceControllers.controller('CollectionFolderController', function ($rootScope, $scope, $cookieStore, $routeParams, Folders) {
    var sessionId = $cookieStore.get("sessionId");
    var folders = new Folders(sessionId);

    // TODO : duplicated logic. move to function
    // retrieve sub folders based on collection selection
    switch ($routeParams.collection) {
        case "all":
            folders.all(function (data) {
                $scope.$broadcast("SelectedCollection", data);
            });
            break;

        case "personal":
            folders.personal(function (data) {
                $scope.$broadcast("SelectedCollection", data);
            });
            break;
    }

    // now retrieve entries for selected folder
    folders.folder({folderId:$routeParams.id}, function (result) {
        $scope.folder = result;
    });

});

iceControllers.controller('EntryController', function ($scope, $routeParams, Entry) {
    $scope.entry = Entry.query({partId:$routeParams.id});
});

iceControllers.controller('UploadController', function ($scope, $resource, $routeParams, Import) {
//    Object.size = function(obj) {
//        var size = 0, key;
//        for (key in obj) {
//            if (obj.hasOwnProperty(key)) size++;
//        }
//        return size;
//    };
    $scope.updates = []; //new Array(35);

    $scope.doSomething = function (val) {
        if (val === undefined)
            return;
        console.log(val);
    };

    $scope.retrieveRows = function () {
        $scope.busy = true;
        Import.list($routeParams.id, 10, function (data) {
            $scope.updates = $scope.updates.concat(data.updates);
            $scope.headers = data.headers;
            $scope.busy = false;

            if ($scope.updates.length < 35) {
                for (var i = $scope.updates.length; i < 35; i += 1) {
                    $scope.updates.push(new Object);
                }
            }

//        var Upload = $resource("/rest/import/fields/" + data.type);
//        Import.headers({type:data.type}, function (result) {
//            $scope.typeHeaders = result;
//            console.log(result[0] + "");
//        });
        });
    };

    $scope.init = function () {
        $scope.rowCount = new Array(35);
    };
    $scope.init();

//    Import.query({importId:$routeParams.id}, function (data) {
//        $scope.updates = $scope.updates.concat(data.updates);
//        console.log($scope.updates);
//        $scope.headers = data.headers;
//
////        var Upload = $resource("/rest/import/fields/" + data.type);
////        Import.headers({type:data.type}, function (result) {
////            $scope.typeHeaders = result;
////            console.log(result[0] + "");
////        });
//    });

//    Import.headers(function (data) {
//        console.log(data);
//    });

    $scope.getColumnIndex = function (keyValue, i) {
        if (keyValue === undefined)
            return '';

        switch (i) {
            case 0:
                return keyValue.PI;

            case 5:
                return keyValue.ALIAS;

            case 3:
                return keyValue.BIOSAFETY_LEVEL;

            case 4:
                return keyValue.NAME;

            case 7:
                return keyValue.SUMMARY;

            default:
                return '';
        }
    };

    $scope.scroll = function () {

    };
});

iceControllers.controller('CollectionDetailController', function ($scope, $location) {
});

iceControllers.controller('SearchController', function ($scope, $http, $cookieStore) {
    $scope.search = function () {
        console.log($scope.queryText);
        var sessionId = $cookieStore.get("sessionId");
        $http.get('/rest/search/',
            {headers:{'X-ICE-Authentication-SessionId':sessionId}, params:{'query':$scope.queryText, 'searchWeb':false}}).
            success(
            function (data) {
                console.log(data);
            }).
            error(function (data, status) {
                console.log(status);
            });
    };
});

iceControllers.controller('FolderController', function ($cookieStore, $scope, $rootScope, $location, $routeParams, Folders, Authentication) {
    Authentication.isSessionValid()
        .then(function (response) {
            if (response.data.length == 0) {
                $location.path("/login");
                return;
            }

            var sessionId = $cookieStore.get("sessionId");
            var folders = new Folders(sessionId);

            folders.personal(function (result) {
                $scope.selectedCollection = result;
//                $scope.$broadcast("FO", "bar"); // example broadcast
            });
        });

    $scope.$on("SelectedCollection", function (event, value) {
        console.log(value);
        $scope.selectedCollection = value;
    });

    $scope.createCollection = function ($event) {
        if ($event.keyCode != 13) {
            return;
        }
    };

    $scope.selectCollectionFolder = function (value) {
        $scope.folder = undefined;   // this forces "Loading..." to be shown
        var sessionId = $cookieStore.get("sessionId");
        Folders(sessionId).folder({folderId:value}, function (result) {
            $scope.folder = result;
        });
    };

//    $scope.selection = function (name) {
//        $scope.folder = undefined;   // this forces "Loading..." to be shown
//        var sessionId = $cookieStore.get("sessionId");
//        Folders(sessionId).folder({folderId:name.id}, function (result) {
//            $scope.folder = result;
//        });
//    };

    $scope.selectCollection = function (name) {
        $location.path("/folders/" + name);
    };

    $scope.collectionList =
        [
            { name:'Available', icon:'fa-folder', iconOpen:'fa-folder-open', path:'all'},
            { name:'Personal', icon:'fa-folder', iconOpen:'fa-folder-open', path:'personal'},
            { name:'Shared', icon:'fa-folder', iconOpen:'fa-folder-open', path:'shared'},
            { name:'Deleted', icon:'fa-trash-o', iconOpen:'fa-trash-o', path:'deleted'}
        ];

    $scope.currentPath = function (param) {
        if ($routeParams.collection === undefined && param === 'personal')
            return true;
        var current = $routeParams.collection === param;
        return current;
    };

    // returns the class for the open folder icon depending on whether this is collection being viewed
    $scope.currentPathOpenIcon = function (param) {
        var collection = $routeParams.collection;
        if (collection === undefined)
            collection = 'personal';

        if (collection === param.path)
            return param.iconOpen;
        return param.icon;
    };
});

iceControllers.controller('UserController', function ($scope, $routeParams, Entry) {
//    $scope.entry = Entry.query({partId:$routeParams.id});
});

iceControllers.controller('LoginController', function ($scope, $location, $cookieStore, $rootScope, Authentication) {
    $scope.submit = function () {
        Authentication.login($scope.userId, $scope.userPassword);
    }
});
