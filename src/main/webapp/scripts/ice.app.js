'use strict';

// Declare app level module which depends on filters, and services, etc
angular
    .module('iceApp', ['ice.dependencies'])

    .run(function (Authentication, $route, $location, $rootScope, Util) {
        // change path without re-loading
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

        $rootScope.logout = function () {
            Authentication.logout();
        };

        $rootScope.siteSettings = {
            logo: "img/logo.png",
            loginMessage: "views/institution.html",
            footer: "views/footer.html"
        };

        Util.get("rest/config/site", function (result) {
            if (!result || !result.assetName)
                return;

            if (result.hasLogo)
                $rootScope.siteSettings.logo = "rest/file/" + result.assetName + "/logo.png";

            if (result.hasLoginMessage)
                $rootScope.siteSettings.loginMessage = "rest/file/" + result.assetName + "/institution.html";

            if (result.hasFooter)
                $rootScope.siteSettings.footer = "rest/file/" + result.assetName + "/footer.html";

            $rootScope.siteSettings.version = result.version;
        });

        // check user on rootscope (which is cleared when the page is refreshed)
        if (!$rootScope.user) {
            Util.get("rest/accesstokens", function (result) {
                $rootScope.user = result;
            });
        }
    })

    // this is run first
    .config(function ($locationProvider, $stateProvider, $urlRouterProvider, localStorageServiceProvider) {
        $locationProvider.html5Mode(true);
        localStorageServiceProvider.setPrefix('ice');
        $urlRouterProvider.otherwise('/');

        // angular ui
        $stateProvider
            .state('main', {
                url: '/',
                templateUrl: 'views/folder.html',
                controller: 'CollectionController'
            })
            .state('login', {
                url: '/login',
                controller: 'LoginController',
                templateUrl: 'views/login.html'
            })
            .state('register', {
                url: '/register',
                controller: 'RegisterController',
                templateUrl: 'views/register.html'
            })
            .state('forgot-password', {
                url: '/forgot-password',
                controller: 'ForgotPasswordController',
                templateUrl: 'views/forgot-password.html'
            })
            .state('main.folder', {
                url: 'folders/:collection',
                templateUrl: 'scripts/collection/collection-selection.html'
            })
            .state('main.web', {
                url: 'web',
                templateUrl: 'scripts/wor/index.html'
            })
            .state('main.web.list', {
                url: '/:partner',
                templateUrl: 'scripts/wor/wor-contents.html',
                controller: 'WorContentController'
            })
            .state('main.web.entry', {
                url: '/:partner/entry/:entryId',
                templateUrl: 'scripts/wor/entry.html',
                controller: 'WorEntryController'
            })

            .state('main.web_folder', {
                url: 'web/:partner/folder/:folderId',
                templateUrl: 'scripts/wor/wor-folder-contents.html',
                controller: 'WorFolderContentController'
            })
            .state('main.search', {
                url: 'search?q&w',
                templateUrl: 'scripts/search/search-results.html',
                controller: 'SearchController'
            })
            .state('main.edit', {
                url: 'entry/edit/:id',
                controller: 'EditEntryController',
                templateUrl: 'scripts/entry/edit.html'
            })
            .state('main.entry', {
                url: 'entry/:id',
                templateUrl: 'scripts/entry/entry.html'
            })
            .state('main.entry.option', {
                url: '/:option',
                templateUrl: 'scripts/entry/sequence-analysis.html'
            })
            .state('main.create', {
                url: 'create/:type',
                controller: 'CreateEntryController',
                templateUrl: 'scripts/entry/create-entry.html'
            })
            .state('main.profile', {
                url: 'profile/:id',
                templateUrl: 'scripts/profile/profile.html'
            })
            .state('main.profile.option', {
                url: '/:option',
                templateUrl: 'scripts/profile/groups.html'
            })
            .state('main.admin', {
                url: 'admin',
                templateUrl: 'scripts/admin/admin.html',
                resolve: {
                    isAdmin: function (Authentication) {
                        return Authentication.isAdmin();
                    }
                }
            })
            .state('main.admin.option', {
                url: '/:option',
                templateUrl: 'scripts/admin/settings.html'

            })
            .state('main.upload', {
                url: 'upload/:type',
                controller: 'UploadController',
                templateUrl: 'scripts/upload/import.html'
            })
            .state('flash', {
                url: '/static/swf/:shortHand/:swfName?entryId&sessionId&url',
                controller: 'FullScreenFlashController',
                templateUrl: 'scripts/entry/fullscreen-flash.html'
            })
            // for backward compatibility with older ice version urls where links were submitted with publications
            .state('redirect', {
                url: '/page=collections;id=:id',
                controller: function ($stateParams, $location) {
                    $location.path("folders/" + $stateParams.id);
                }
            });
    });
