'use strict';

angular.module('ice.common.service', [])
    .factory('Util', function ($rootScope, $location, $cookieStore, $resource) {
        return {
            handleError: function (response) {
                switch (response.status) {
                    case 401:
                        if ($location.path() != '/login') {
                            $cookieStore.remove('user');
                            $rootScope.user = undefined;
                            $location.path('/login');
                            $rootScope.error = "Your session has expired. Please login again";
                        } else {
                            $rootScope.error = response.data.errorMessage;
                        }
                        break;

                    case 404:
                        $rootScope.error = "The requested resource could not be found";
                        break;

                    case 500:
                        $rootScope.error = response.data.errorMessage;
                        break;

                    default:
                        $rootScope.error = "Unknown server error";
                }
            },

            get: function (url, successHandler, queryParams) {
                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                queryParams.sid = $cookieStore.get("sessionId");
                return $resource(url).get(queryParams, successHandler, this.handleError);
            },

            // difference between this and get is "isArray"
            list: function (url, successHandler, queryParams) {
                if (!queryParams)
                    queryParams = {};

                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }

                queryParams.sid = $cookieStore.get("sessionId");
                return $resource(url).query(queryParams, successHandler, this.handleError);
            },

            post: function (url, obj, successHandler, params) {
                if (!params)
                    params = {};
                params.sid = $cookieStore.get("sessionId");
                $resource(url, params).save(obj, successHandler, this.handleError);
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
                        headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
                    }
                }).update(obj, successHandler, this.handleError);
            },

            remove: function (url, successHandler) {
                if (!successHandler) {
                    successHandler = function (resp) {
                    }
                }
                $resource(url, {sid: $cookieStore.get("sessionId")}).remove(successHandler, this.handleError)
            }
        }
    })
;
