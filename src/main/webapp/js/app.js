'use strict';


// Declare app level module which depends on filters, and services, etc
var iceApp = angular.module('iceApp', [
    'ngRoute',
    'iceApp.filters',
    'iceApp.services',
    'iceApp.directives',
    'iceApp.controllers'
]);


iceApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/view1',
        {
            controller:'MyCtrl1',
            templateUrl:'partials/partial1.html'
        })
        .when('/view2',
        {
            controller:'MyCtrl2',
            templateUrl:'partials/partial2.html'
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

