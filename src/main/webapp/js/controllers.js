'use strict';

var iceControllers = angular.module('iceApp.controllers', ['iceApp.services']);

iceControllers.controller('RegisterController', ['$scope', '$resource', '$location', function ($scope, $resource, $location) {
    $scope.submit = function () {
        var User = $resource("/rest/profile");
        User.save({email:$scope.email, firstName:$scope.firstName, lastName:$scope.lastName, institution:$scope.institution, description:$scope.about}, function (data) {
            console.log(data);
            if (data.length != 0)
                $location.path("/login");
            else
                $scope.errorMsg = "Could not create account";
        });
    }
}]);

iceControllers.controller('CollectionController', ['$scope', '$routeParams', '$location', '$cookieStore', 'Folders', 'Authentication', function ($scope, $routeParams, $location, $cookies, Folders, Authentication) {
    Authentication.isSessionValid()
        .then(function (response) {
            if (response.data.length == 0) {
                $location.path("/login");
                return;
            }

            var folderType;
            if ($routeParams.id === undefined)
                folderType = 'personal';
            else
                folderType = $routeParams.id;

            var sessionId = $cookies.get("sessionId");
            var folders = new Folders(sessionId);
            console.log(folderType);

            switch (folderType) {
                case "all":
                    folders.all(function (data) {
                        $scope.selectedCollection = data;
                    });
                    break;

                case "personal":
                    // retrieve user folders
                    folders.userEntries(function (result) {
                        $scope.folder = result;
                    });
                    break
            }
        });
}]);


iceControllers.controller('EntryController', ['$scope', '$routeParams', 'Entry', function ($scope, $routeParams, Entry) {
    $scope.entry = Entry.query({partId:$routeParams.id});
}]);

iceControllers.controller('CollectionDetailController', [ '$scope', function ($scope) {
    // eg of listening
//    $scope.$on("FOO", function (event, va) {
//        console.log("received ", va, " on broadcast");
//    });

}]);

iceControllers.controller('FolderController', ['$cookieStore', '$scope', '$rootScope', '$location', 'Folders', 'Authentication', function ($cookieStore, $scope, $rootScope, $location, Folders, Authentication) {
    Authentication.isSessionValid()
        .then(function (response) {
            if (response.data.length == 0) {
                $location.path("/login");
                return;
            }

            var sessionId = $cookieStore.get("sessionId");
            var folders = new Folders(sessionId);

            folders.query(function (data) {
//                console.log(JSON.stringify(data));
                $scope.collection = data;
            });

//            folders.all(function (data) {
////                console.log(JSON.stringify(data));
//                $scope.personalFolders = data;
//            });

            folders.personal(function (result) {
                $scope.personalFolders = result;
//                $scope.$broadcast("FO", "bar"); // example broadcast
            });
        });

    $scope.createCollection = function ($event) {
        if ($event.keyCode != 13) {
            return;
        }
    };

    $scope.selection = function (name) {
        console.log(name);
        $scope.folder = undefined;   // this forces "Loading..." to be shown
        var sessionId = $cookieStore.get("sessionId");
        Folders(sessionId).folder({folderId:name.id}, function (result) {
            $scope.folder = result;
        });
    };

    $scope.selectCollection = function (name) {
        $scope.collectionSelection = name;
        $location.path("/folders/" + name);
    };

    $scope.collectionList =
        [
            { name:'Available', icon:'fa-folder', path:'all'},
            { name:'Personal', icon:'fa-folder', path:'personal'},
            { name:'Shared', icon:'fa-folder', path:'shared'},
            { name:'Deleted', icon:'fa-trash-o', path:'deleted'}
        ];

    $scope.collectionSelection = 'personal';

    $scope.currentPath = function (param) {
        return $scope.collectionSelection === param;
    };
}]);

iceControllers.controller('UserController', ['$scope', '$routeParams', 'Entry', function ($scope, $routeParams, Entry) {
//    $scope.entry = Entry.query({partId:$routeParams.id});
}]);

iceControllers.controller('LoginController', ['$scope', '$location', '$cookieStore', '$rootScope', 'Authentication', function ($scope, $location, $cookieStore, $rootScope, Authentication) {
    $scope.submit = function () {
        Authentication.login($scope.userId, $scope.userPassword);
    }
}]);
