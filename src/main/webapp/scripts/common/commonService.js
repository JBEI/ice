'use strict';

angular.module('ice.common.service', [])
    .factory('Util', function ($rootScope, $location, $cookies, $resource) {
        return {
            handleError: function (response) {
                let type;
                let errorMsg = response.data ? response.data.errorMessage : "Unknown error";

                switch (response.status) {
                    case 401:
                        if ($location.path() !== '/login') {
                            $cookies.remove('user');
                            $rootScope.user = undefined;
                            $cookies.loginDestination = $location.path();
                            $location.path('/login');
                            errorMsg = "Expired session. Please login again";
                        } else {
                            errorMsg = response.data.errorMessage;
                        }
                        break;

                    case 403:
                        errorMsg = "Access to requested resource denied";
                        type = "warning";
                        break;

                    case 404:
                        errorMsg = "Requested resource not found";
                        type = "warning";
                        break;

                    case 500:
                        type = "danger";
                        break;

                    case 503:
                        $location.path('/config');
                        break;

                    default:
                        errorMsg = "Unknown server error";
                        type = "danger";
                }

                if (!errorMsg) {
                    console.log("error", response);
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

                if (type === 'error')
                    type = "danger";

                $rootScope.serverFeedback = {type: type, message: message};
            },

            clearFeedback: function () {
                $rootScope.serverFeedback = undefined;
            },

            get: function (url, successHandler, queryParams, errorHandler) {
                let errorCallback = this.handleError;
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
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')}
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

                let errorCallback = this.handleError;
                if (errorHandler)
                    errorCallback = errorHandler;

                //queryParams.sid = $cookieStore.get('sessionId');
                $resource(url, queryParams, {
                    'list': {
                        method: 'GET',
                        isArray: true,
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')}
                    }
                }).list(successHandler, errorCallback);
            },

            post: function (url, obj, successHandler, params, errHandler) {
                let errorCallback = this.handleError;
                if (errHandler)
                    errorCallback = errHandler;

                if (!params)
                    params = {};
                //params.sid = $cookieStore.get('sessionId');
                $resource(url, params, {
                    'post': {
                        method: 'POST',
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')}
                    }
                }).post(obj, successHandler, errorCallback);
            },

            update: function (url, obj, params, successHandler, failureHandler) {
                let errorCallback = this.handleError;
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
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')}
                    }
                }).update(obj, successHandler, errorCallback);
            },

            remove: function (url, params, successHandler, errHandler) {
                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }
                let errorCallback = this.handleError;
                if (errHandler)
                    errorCallback = errHandler;

                $resource(url, params, {
                    'delete': {
                        method: 'DELETE',
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')}
                    }
                }).delete(successHandler, errorCallback)
            },

            download: function (url, postData) {
                let m = postData ? "POST" : "GET";
                let down = $resource(url, {}, {
                    download: {
                        method: m,
                        headers: {'X-ICE-Authentication-SessionId': $cookies.get('sessionId')},
                        responseType: 'arraybuffer',
                        transformResponse: function (data, headers) {
                            return {
                                data: data,
                                filename: function () {
                                    let header = headers('content-disposition');
                                    let result = header.split(';')[1].trim().split('=')[1];
                                    return result.replace(/"/g, '');
                                }
                            }
                        }
                    }
                });

                return down.download(postData);
            },

            constants: function () {
                return {
                    REMOTE_ENTRY_SELECTED: "EntrySelected"
                }
            }
        }
    });
