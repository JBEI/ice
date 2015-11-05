'use strict';

angular.module('ice.common.service', [])
    .factory('Util', function ($rootScope, $location, $cookieStore, $resource) {
        return {
            handleError: function (response) {
                var errorMsg;

                switch (response.status) {
                    case 401:
                        if ($location.path() != '/login') {
                            $cookieStore.remove('user');
                            $rootScope.user = undefined;
                            $location.path('/login');
                            errorMsg = "Your session has expired. Please login again";
                        } else {
                            errorMsg = response.data.errorMessage;
                        }
                        break;

                    case 404:
                        errorMsg = "The requested resource could not be found";
                        break;

                    case 500:
                        errorMsg = response.data.errorMessage;
                        break;

                    default:
                        errorMsg = "Unknown server error";
                }
                $rootScope.serverFeedback = {message: errorMsg};
            },

            setFeedback: function (message, type) {
                if (!type)
                    type = 'info';

                $rootScope.serverFeedback = {type: type, message: message};
            },

            get: function (url, successHandler, queryParams) {
                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                queryParams.sid = $cookieStore.get("sessionId");
                $resource(url, queryParams, {
                    'get': {
                        method: 'GET',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).get(successHandler, this.handleError);
            },

            // difference between this and get is "isArray"
            list: function (url, successHandler, queryParams) {
                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                queryParams.sid = $cookieStore.get('sessionId');
                $resource(url, queryParams, {
                    'list': {
                        method: 'GET',
                        isArray: true,
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).list(successHandler, this.handleError);
            },

            post: function (url, obj, successHandler, params) {
                if (!params)
                    params = {};
                params.sid = $cookieStore.get('sessionId');
                $resource(url, params, {
                    'post': {
                        method: 'POST',
                        headers: {'X-ICE-Authentication-SessionId': params.sid}
                    }
                }).post(obj, successHandler, this.handleError);
            },

            update: function (url, obj, params, successHandler) {
                if (!params)
                    params = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                $resource(url, params, {
                    'update': {
                        method: 'PUT',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).update(obj, successHandler, this.handleError);
            },

            remove: function (url, params, successHandler) {
                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                $resource(url, params, {
                    'delete': {
                        method: 'DELETE',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).delete(successHandler, this.handleError)
            }
        }
    });
