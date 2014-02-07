'use strict';

/* Services */
var iceServices = angular.module('iceApp.services', ['ngCookies', 'ngResource']);

iceServices.factory('Entry', ['$resource', '$cookieStore', function ($resource, $cookieStore) {
//    method:'GET',
//        url:'/rest/part/' + $routeParams.id,
//        headers:
    return $resource('/rest/part/:partId', {partId:'@id'}, {
        query:{
            method:'GET',
            headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
        }
    });
}]);

iceServices.factory('Folders', ['$resource', '$cookieStore', function ($resource, $cookieStore) {
//    var User = $resource('/rest/accesstoken', {}, {headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}});
//    var user = User.get(function(value, headers) {
//        if (value.$resolved && value.$promise.length == 0) {
//            console.log(JSON.stringify(headers, null, 3));
//            $location.path("/login");
//            return;
//        }
    return function (sessionId) {

        return $resource('/rest/folders', {folderId:'@id'}, {
            query:{
                method:'GET',
                responseType:"json",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            all:{
                method:'GET',
                url:"/rest/folders/all",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            personal:{
                method:'GET',
                url:"/rest/folders/user",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            userEntries:{
                method:'GET',
                url:"/rest/folders/personal",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            folder:{
                method:'GET',
                url:"/rest/folders/:folderId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
}]);

iceServices.factory('Authentication', ['$resource', '$cookieStore', '$http', '$rootScope', '$location', function ($resource, $cookieStore, $http, $rootScope, $location) {
    return {
        // logs in user to ice
        login:function (username, password) {
            return $http({
                url:"/rest/accesstoken",
                method:"POST",
                data:"{email:" + username + ", password:" + password + "}",
                dataType:"json" }).
                success(function (data, status, headers, config) {
                    if (data.length == 0) {
                        $rootScope.errMsg = "Login failed.";
                        $cookieStore.remove('userId');
                        $cookieStore.remove('sessionId');
                        return;
                    }
                    $rootScope.user = data;
                    $cookieStore.put('userId', data.email);
                    $cookieStore.put('sessionId', data.sessionId);
                    console.log($cookieStore.get("sessionId"));
                    $location.path('/');
                }).
                error(function (data, status, headers, config) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
        },

        // checks if the session is valid
        isSessionValid:function () {
            var sid = $cookieStore.get("sessionId");
            if (sid == undefined)
                sid = "undefined";
            return $http.get('/rest/accesstoken',
                {headers:{'X-ICE-Authentication-SessionId':sid}}).
                success(function (data) {
                    if (data.length == 0) {
                        // clear all stored information
                        $rootScope.user = undefined;
                        $cookieStore.remove('userId');
                        $cookieStore.remove('sessionId');
                        $location.path('/login');
                        return;
                    }
                    // a bit redundant
                    $rootScope.user = data;
                }).
                error(function (data, status) {
                    console.log(status);
                });
        },

        // logs out user by invalidating the session id
        logout:function () {
            var sid = $cookieStore.get("sessionId");
            return $http.delete('/rest/accesstoken', {headers:{'X-ICE-Authencation-SessionId':sid}}).
                success(function () {
                    $rootScope.user = undefined;
                    cookieStore.remove('userId');
                    $cookieStore.remove('sessionId');
                    $location.path('/login');
                });
        }
    };

    // example using resource
//    return $resource('/rest/accesstoken', {}, {
//        'login':{ method:"POST" },
//        "isSessionValid":{ method:"GET", headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")} }
//    });
}]);
