'use strict';

// Declare app level module which depends on filters, and services, etc
var iceApp = angular.module('iceApp', ['ice.dependencies']);

iceApp.run(['$route', '$rootScope', '$location', function ($route, $rootScope, $location) {
    var original = $location.path;
    $location.path = function (path, reload) {
        if (reload === false) {
            var lastRoute = $route.current;
            var un = $rootScope.$on('$locationChangeSuccess', function () {
                $route.current = lastRoute;
                un();
            });
        }
        return original.apply($location, [path]);
    };
}]);

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
        .state('main', {
            url:'/',
            templateUrl:'/views/folder.html',
            controller:'CollectionController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid('main');
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
        .state('main.web', {
            url:'web',
            templateUrl:'/views/wor/index.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.web.list', {
            url:'/:partner',
            templateUrl:'/views/wor/wor-contents.html',
            controller:'WorContentController'
        })
        .state('main.web.entry', {
            url:'/:partner/entry/:entryId',
            templateUrl:'/views/wor/entry.html',
            controller:'WorEntryController'
        })

        .state('main.web_folder', {
            url:'web/:partner/folder/:folderId',
            templateUrl:'/views/wor/wor-folder-contents.html',
            controller:'WorFolderContentController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.search', {
            url:'search?q&w',
            templateUrl:'scripts/search/search-results.html',
            controller:'SearchController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.edit', {
            url:'entry/edit/:id',
            controller:'EditEntryController',
            templateUrl:'/views/entry/edit.html'
        })
        .state('main.entry', {
            url:'entry/:id',
            templateUrl:'/views/entry.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.entry.option', {
            url:'/:option',
            templateUrl:'/views/entry/sequence-analysis.html'
        })
        .state('main.create', {
            url:'create/:type',
            controller:'CreateEntryController',
            templateUrl:'/views/entry/create-entry.html'
        })
        .state('main.profile', {
            url:'profile/:id',
            templateUrl:'/views/profile.html'
        })
        .state('main.profile.option', {
            url:'/:option',
            templateUrl:'/views/profile/groups.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('main.admin', {
            url:'admin',
            templateUrl:'/scripts/admin/admin.html',
            controller:'AdminController',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid() && Authentication.isAdmin();
                }
            }
        })
        .state('main.admin.option', {
            url:'/:option',
            templateUrl:'/scripts/admin/settings.html'

        })
        .state('main.upload', {
            url:'upload/:type',
            controller:'UploadController',
            templateUrl:'/scripts/upload/import.html',
            resolve:{
                sessionValid:function (Authentication) {
                    return Authentication.isSessionValid();
                }
            }
        })
        .state('flash', {
            url:'/static/swf/:shortHand/:swfName?entryId&sessionId&url',
            controller:'FullScreenFlashController',
            templateUrl:'/views/entry/fullscreen-flash.html'
        })
    ;
});
