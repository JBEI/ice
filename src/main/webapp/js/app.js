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

iceApp.run(function (Authentication, $location) {
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
            controller:'IceController',
            templateUrl:'/partials/main.htm'
//            resolve: resolve
        })
        .when("/login", {
            controller:'LoginController',
            templateUrl:'/partials/login.html'
        })
        .when('/entry/:id', {
            controller:'EntryController',
            templateUrl:'/partials/entry.html'
        })
        .when('/profile/:profileId', {
            controller:'UserController',
            templateUrl:'/partials/profile.html'
        })
        .when('/register', {
            controller:'RegisterController',
            templateUrl:'/partials/register.html'
        })
        .otherwise({redirectTo:'/'});
}]);

//    LOGIN("login"),
//    MAIN("main"),
//    COLLECTIONS("collections"),
//    ADD_ENTRY("add"),
//    BULK_IMPORT("bulk"),
//    ENTRY_VIEW("entry"),
//    PROFILE("profile"),
//    ADMIN("admin"),
//    QUERY("query"),
//    LOGOUT("logout"),
//    NEWS("news");

// collection/entry/view/id

