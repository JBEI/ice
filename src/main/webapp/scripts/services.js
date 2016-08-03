'use strict';

/* Services */
angular.module('iceApp.services', ['ngCookies', 'ngResource'])

    .factory('Authentication', function ($resource, $cookieStore, $http, $rootScope, $location, $cookies, Util) {
        return {
            // logs in user to ice
            login: function (username, password) {
                Util.post("/rest/accesstokens", {email: username, password: password},
                    function (success) {
                        if (success && success.sessionId) {
                            $rootScope.user = success;
                            $cookieStore.put('userId', success.email);
                            $cookieStore.put('sessionId', success.sessionId);
                            var loginDestination = $cookies.loginDestination || '/';
                            $cookies.loginDestination = null;
                            $location.path(loginDestination);
                        } else {
                            $cookieStore.remove('userId');
                            $cookieStore.remove('sessionId');
                        }
                    });
            },

            getLoggedInUser: function () {
                if ($rootScope.user) {
                    return $rootScope.user;
                }

                var sid = $cookieStore.get('sessionId');
                return $http.get('rest/accesstokens',
                    {headers: {'X-ICE-Authentication-SessionId': sid}})
                    .success(function (data) {
                        $rootScope.user = data;
                    })
                    .error(function (data, status) {
                        if (status === 401) {
                            $cookieStore.remove('userId');
                            $cookieStore.remove('sessionId');
                            if ($location.path() !== '/login')
                                $cookies.loginDestination = $location.path();
                            $location.path('/login');
                        }
                    });
            },

            isAdmin: function () {
                if ($rootScope.user) {
                    if (!$rootScope.user.isAdmin)
                        $location.path('/folders/personal');
                } else {
                    Util.get('rest/accesstokens', function (result) {
                        if (!result || !result.isAdmin) {
                            $location.path('/folders/personal');
                        }
                    });
                }
            },

            // logs out user by invalidating the session id
            logout: function () {
                Util.remove("rest/accesstokens", {}, function (result) {
                    $rootScope.user = undefined;
                    $cookieStore.remove('userId');
                    $cookieStore.remove('sessionId');
                    $location.path('/login');
                });
            }
        };
    })

    .factory('EntryContextUtil', function () {
        // default search filters
        var context;

        return {
            getContext: function () {
                return context;
            },

            resetContext: function () {
                context = undefined;
            },

            setContextCallback: function (callback, available, offset, back, sort) {
                context = {};
                context.callback = callback;
                context.available = available;
                context.offset = offset;
                context.back = back;
                context.sort = sort;
            }
        }
    });
