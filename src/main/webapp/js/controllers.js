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

iceControllers.controller('EntryController', ['$scope', '$routeParams', 'Entry', function ($scope, $routeParams, Entry) {
    $scope.entry = Entry.query({partId:$routeParams.id});
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

            folders.all(function (data) {
//                console.log(JSON.stringify(data));
                $scope.personalFolders = data;
            });
        });
}]);

iceControllers.controller('UserController', ['$scope', '$routeParams', 'Entry', function ($scope, $routeParams, Entry) {
//    $scope.entry = Entry.query({partId:$routeParams.id});
}]);

iceControllers.controller('IceController', ['$scope', '$location', '$rootScope', '$cookieStore', 'Authentication', function ($scope, $location, $rootScope, $cookieStore, Authentication) {
    $rootScope.logout = function () {
        Authentication.logout();
    }
//    var sid = $cookieStore.get("sessionId");
//    if (sid == undefined) {
//        console.log("session id is undefined. redirecting user to login page");
//        $location.path("/login");
//        return;
//    }
//
//    Authentication.isSessionValid({}, function(value, headers) {
//        console.log("IceCont: Is session valid call returned ", JSON.stringify(value));
//        if (value.$resolved && value.$promise.length == 0) {
//            console.log(JSON.stringify(headers, null, 3));
//            $location.path("/login");
//        }
//        else {
//            console.log(JSON.stringify(value, null, 3));
//            $rootScope.user = value;
//            $cookieStore.put('userId', value.email);
//            $cookieStore.put('sessionId', value.sessionId);
//        }
//    }, function error(httpResponse) {
//        alert("error");
//    });
}]);

iceControllers.controller('LoginController', ['$scope', '$location', '$cookieStore', '$rootScope', 'Authentication', function ($scope, $location, $cookieStore, $rootScope, Authentication) {
    $scope.submit = function () {
        Authentication.login($scope.userId, $scope.userPassword);
    }
}]);
