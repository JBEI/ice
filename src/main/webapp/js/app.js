'use strict';

// Declare app level module which depends on filters, and services, etc
var iceApp = angular.module('iceApp', [
    'ngRoute',
    'ngCookies',
    'iceApp.filters',
    'iceApp.services',
    'iceApp.directives',
    'iceApp.controllers',
    'ui.router'
]);

iceApp.run(function (Authentication, $rootScope) {
    $rootScope.logout = function () {
        Authentication.logout();
    };

//    $rootScope.$on('$stateChangeStart',
//        function (event, toState, toParams, fromState, fromParams) {
//            if (toState.name === 'logout' || toState.name === "login")
//                return;
//
//            var sid = $cookieStore.get("sessionId");
//            if (sid === undefined) {
//                console.log("sid undefined");
//                $rootScope.user = undefined;
//                $location.path('/login');
//                return;
//            }
//
//            Authentication.isSessionValid(sid)
//                .success(function (data) {
//                    if (data.length == 0) {
//                        console.log(data);
//                        console.log("session invalid");
//                        // clear all stored information
//                        $rootScope.user = undefined;
//                        $location.path('/login');
//                    }
//
//                    $rootScope.user = data;
//                })
//                .error(function (data) {
//                });
//        })
});

iceApp.config(function ($locationProvider, $stateProvider, $urlRouterProvider) {
    $locationProvider.html5Mode(true);

    $urlRouterProvider.otherwise('/');

    // angular ui
    $stateProvider
        .state('test', {
            url:'/test',
            templateUrl:'/views/test.html',
            controller:'TestController'
        })
        .state('main', {
            url:'/',
            templateUrl:'/views/folder.html',
            controller:'CollectionController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('login', {
            url:'/login',
            controller:'LoginController',
            templateUrl:'/views/login.html'
        })
        .state('register', {
            url:'/register',
            controller:'RegisterController',
            templateUrl:'/views/register.html'
        })
        .state('forgot-password', {
            url:'/forgot-password',
            controller:'ForgotPasswordController',
            templateUrl:'/views/forgot-password.html'
        })
        .state('main.folder', {
            url:'folders/:collection',
//            controller: 'CollectionFolderController',
            templateUrl:'/views/collection-selection.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.search', {
            url:'search?q&w',
            templateUrl:'/views/search-results.html',
            controller:'SearchController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.entry', {
            url:'entry/:id',
            controller:'EntryController',
            templateUrl:'/views/entry.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.create', {
            url:'create/:type',
//            controller: 'CreateEntryController',
            templateUrl:'/views/entry/create-entry.html'
            // illustrates using parameter for
//            templateUrl: function (stateParams) {
//                return '/views/entry/create-' + stateParams.type + '.html';
//            }
        })
        .state('main.edit', {
            url:'entry/edit/:id',
            controller:'EditEntryController',
            templateUrl:'/views/entry/edit.html'
        })
        .state('main.profile', {
            url:'profile/:id',
            templateUrl:'/views/profile.html'
        })
        .state('main.profile.option', {
            url:'/:option',
            templateUrl:'/views/profile/groups.html'
        })
        .state('main.admin', {
            url:'admin',
            templateUrl:'/views/admin.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
                // TODO : also is admin
            }
        })
        .state('main.upload', {
            url:'upload/:type',
            controller:'ImportController',
            templateUrl:'/views/import.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('flash', {
            url:'/static/swf/:shortHand/:swfName?entryId&sessionId',
            controller:'FullScreenFlashController',
            templateUrl:'/views/entry/fullscreen-flash.html'
        })
        .state("otherwise", { url:'/'})
    ;
});
