'use strict';

angular.module('ice.common.service', [])
    .factory('Util', function ($rootScope, $location, $cookieStore, $cookies, $resource) {
        return {
            handleError: function (response) {
                var type;
                var errorMsg = response.data.errorMessage;

                switch (response.status) {
                    case 401:
                        if ($location.path() != '/login') {
                            $cookieStore.remove('user');
                            $rootScope.user = undefined;
                            $cookies.loginDestination = $location.path();
                            $location.path('/login');
                            errorMsg = "Your session has expired. Please login again";
                        } else {
                            errorMsg = response.data.errorMessage;
                        }
                        break;

                    case 403:
                        errorMsg = "Access to requested resource has been denied";
                        type = "warning";
                        break;

                    case 404:
                        errorMsg = "The requested resource could not be found";
                        type = "warning";
                        break;

                    case 500:
                        type = "danger";
                        break;

                    default:
                        errorMsg = "Unknown server error";
                        type = "danger";
                }

                if (errorMsg == undefined) {
                    errorMsg = "Unknown server error";
                    type = "danger";
                }

                $rootScope.serverFeedback = {message: errorMsg, type: type};
            },

            /**
             * sets the feedback for display to the user
             * @param message message to display to user. make as brief as possible
             * @param type type of alert. one of 'success', 'info' (default), 'warning', 'danger'
             */
            setFeedback: function (message, type) {
                if (!type)
                    type = 'info';

                if (type == 'error')
                    type = "danger";

                $rootScope.serverFeedback = {type: type, message: message};
            },

            clearFeedback: function () {
                $rootScope.serverFeedback = undefined;
            },

            get: function (url, successHandler, queryParams, errorHandler) {
                var errorCallback = this.handleError;
                if (errorHandler)
                    errorCallback = errorHandler;

                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                //queryParams.sid = $cookieStore.get("sessionId");
                $resource(url, queryParams, {
                    'get': {
                        method: 'GET',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).get(successHandler, errorCallback);
            },

            // difference between this and get is "isArray"
            list: function (url, successHandler, queryParams, errorHandler) {
                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                var errorCallback = this.handleError;
                if (errorHandler)
                    errorCallback = errorHandler;

                //queryParams.sid = $cookieStore.get('sessionId');
                $resource(url, queryParams, {
                    'list': {
                        method: 'GET',
                        isArray: true,
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).list(successHandler, errorCallback);
            },

            post: function (url, obj, successHandler, params, errHandler) {
                var errorCallback = this.handleError;
                if (errHandler)
                    errorCallback = errHandler;

                if (!params)
                    params = {};
                //params.sid = $cookieStore.get('sessionId');
                $resource(url, params, {
                    'post': {
                        method: 'POST',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).post(obj, successHandler, errorCallback);
            },

            update: function (url, obj, params, successHandler, failureHandler) {
                var errorCallback = this.handleError;
                if (failureHandler)
                    errorCallback = failureHandler;

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
                }).update(obj, successHandler, errorCallback);
            },

            remove: function (url, params, successHandler, errHandler) {
                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                var errorCallback = this.handleError;
                if (errHandler)
                    errorCallback = errHandler;

                $resource(url, params, {
                    'delete': {
                        method: 'DELETE',
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get('sessionId')}
                    }
                }).delete(successHandler, errorCallback)
            }
        }
    });
