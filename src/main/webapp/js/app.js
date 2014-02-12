'use strict';

// Declare app level module which depends on filters, and services, etc
var iceApp = angular.module('iceApp', [
    'ngRoute',
    'ngCookies',
    'iceApp.filters',
    'iceApp.services',
    'iceApp.directives',
    'iceApp.controllers'
]);

iceApp.run(function (Authentication, $location, $rootScope) {
    $rootScope.logout = function () {
        Authentication.logout();
    };
//    if ($location.$$path === "/login")
//        return;
//
//    console.log("init");
//    if (Authentication.isSessionValid().length == 0)
//        $location.path("/login");
});

iceApp.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
    $locationProvider.html5Mode(true);

//    var resolve = function($location) {
//        console.log("resolve");
//        Authentication.isSessionValid({}, function success(value, headers) {
//            console.log("IceCont: Is session valid call returned ", JSON.stringify(value));
//        if (value.sessionId == undefined) {
//
//            console.log(JSON.stringify(headers, null, 3));
//            $location.path("/login");
//        }
//        else {
//            console.log(JSON.stringify(value, null, 3));
//            $rootScope.user = value;
//            $cookieStore.put('userId', value.email);
//            $cookieStore.put('sessionId', value.sessionId);
//        }
//        }, function error(httpResponse) {
//            alert("error");
//        });
//    };

    $routeProvider
        .when("/", {
            controller:'CollectionController',
            templateUrl:'/views/folder.html'
//            resolve: resolve
        })
        .when("/login", {
            controller:'LoginController',
            templateUrl:'/views/login.html'
        })
        .when('/entry/:id', {
            controller:'EntryController',
            templateUrl:'/views/entry.html'
        })
        .when('/profile/:profileId', {
            controller:'UserController',
            templateUrl:'/views/profile.html'
        })
        .when('/register', {
            controller:'RegisterController',
            templateUrl:'/views/register.html'
        })
        .when('/folders/:collection', {
            controller:'CollectionController',
            templateUrl:'/views/folder.html'
        })
        .when('/folders/:collection/:id', {
            controller:'CollectionFolderController',
            templateUrl:'/views/folder.html'
        })
        .when('/test/upload/:id', {
            controller:'UploadController',
            templateUrl:'/views/upload.html'
        })
        .when('/search', {
            controller:'SearchController'
        })
        .otherwise({redirectTo:'/'});
}]);
