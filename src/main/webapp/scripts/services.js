'use strict';

/* Services */
var iceServices = angular.module('iceApp.services', ['ngCookies', 'ngResource']);

iceServices.factory('Group', function ($resource, $cookieStore) {
    return function () {

        var sessionId = $cookieStore.get("sessionId");

        return $resource('rest/groups', {groupId: '@groupId'}, {
            members: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: "rest/groups/:groupId/members",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            update: {
                method: 'PUT',
                responseType: 'json',
                url: "rest/groups/:groupId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            }
        });
    }
});

iceServices.factory('User', function ($resource) {
    return function (sessionId) {
        return $resource('rest/users', {userId: '@userId', preferenceKey: '@preferenceKey', sendEmail: '@sendEmail'}, {
            query: {
                method: 'GET',
                responseType: "json",
                url: "rest/users/:userId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            update: {
                method: 'POST',
                responseType: "json",
                url: "rest/users/:userId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            list: {
                method: 'GET',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            filter: {
                method: 'GET',
                url: 'rest/users/autocomplete',
                isArray: true,
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            getGroups: {
                method: 'GET',
                url: "rest/users/:userId/groups",
                responseType: 'json',
                isArray: true,
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            createGroup: {
                method: 'PUT',
                url: "rest/users/:userId/groups",
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            createUser: {
                method: 'PUT',
                url: 'rest/users/',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            getEntries: {
                method: 'GET',
                url: 'rest/users/:userId/entries',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            getPreferences: {
                method: 'GET',
                url: 'rest/users/:userId/preferences',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            updatePreference: {
                method: 'POST',
                url: 'rest/users/:userId/preferences/:preferenceKey',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            resetPassword: {
                method: 'POST',
                url: 'rest/users/password',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            changePassword: {
                method: 'PUT',
                url: 'rest/users/:userId/password',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            samples: {
                method: 'GET',
                url: 'rest/users/:userId/samples',
                responseType: 'json',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            }
        });
    }
});

iceServices.factory('Samples', function ($resource) {
    return function (sessionId) {
        return $resource('rest/samples', {
            userId: '@userId',
            requestId: '@requestId',
            status: '@status',
            type: '@type'
        }, {
            requests: {
                method: 'GET',
                responseType: 'json',
                url: "rest/samples/requests",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            update: {
                method: 'PUT',
                responseType: 'json',
                url: "rest/samples/requests/:requestId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            submitRequests: {
                method: 'PUT',
                url: "rest/samples/requests",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            removeRequestFromCart: {
                method: 'DELETE',
                url: "rest/samples/requests/:requestId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            addRequestToCart: {
                method: 'POST',
                responseType: 'json',
                isArray: true,
                url: "rest/samples/requests",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            getStorageType: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: "rest/samples/storage/:type",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            }
        })
    }
});

iceServices.factory('Attachment', function ($resource) {
    return function (sessionId) {
        return $resource('rest/parts/:partId/attachments', {partId: '@partId', attachmentId: '@attachmentId'}, {
            create: {
                method: 'POST',
                responseType: "json",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            get: {
                method: 'GET',
                responseType: "json",
                isArray: true,
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            delete: {
                method: 'DELETE',
                url: 'rest/parts/:partId/attachments/:attachmentId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            }
        });
    }
});

iceServices.factory('Entry', function ($resource) {
    return function (sessionId) {
        return $resource('rest/parts/', {
            partId: '@id',
            traceId: '@traceId',
            permissionId: '@permissionId',
            commentId: '@commentId',
            sampleId: '@sampleId',
            linkId: '@linkId',
            historyId: '@historyId'
        }, {
            query: {
                method: 'GET',
                responseType: "json",
                url: "rest/parts/:partId",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            tooltip: {
                method: 'GET',
                responseType: 'json',
                url: "rest/parts/:partId/tooltip",
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            create: {
                method: 'POST',
                responseType: 'json',
                url: 'rest/parts',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            update: {
                method: 'PUT',
                responseType: 'json',
                url: 'rest/parts/:partId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            statistics: {
                method: 'GET',
                responseType: 'json',
                url: 'rest/parts/:partId/statistics',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            comments: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/comments',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            permissions: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/permissions',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            addPermission: {
                method: 'POST',
                responseType: 'json',
                url: 'rest/parts/:partId/permissions',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            removePermission: {
                method: 'DELETE',
                responseType: 'json',
                url: 'rest/parts/:partId/permissions/:permissionId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            createComment: {
                method: 'POST',
                responseType: 'json',
                url: 'rest/parts/:partId/comments',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            updateComment: {
                method: 'PUT',
                responseType: 'json',
                url: 'rest/parts/:partId/comments/:commentId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            samples: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/samples',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            addSample: {
                method: 'POST',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/samples',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            deleteSample: {
                method: 'DELETE',
                responseType: 'json',
                url: 'rest/parts/:partId/samples/:sampleId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            shotgunSequences: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/shotgunsequences',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            traceSequences: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/traces',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            history: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/parts/:partId/history',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            deleteHistory: {
                method: 'DELETE',
                responseType: 'json',
                url: 'rest/parts/:partId/history/:historyId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            deleteTraceSequence: {
                method: 'DELETE',
                responseType: 'json',
                url: 'rest/parts/:partId/traces/:traceId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            addTraceSequence: {
                method: 'POST',
                url: 'rest/parts/:partId/traces',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            sequence: {
                method: 'GET',
                responseType: 'json',
                url: 'rest/parts/:partId/sequence',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            deleteSequence: {
                method: 'DELETE',
                url: 'rest/parts/:partId/sequence',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            addSequenceAsString: {
                method: "POST",
                url: 'rest/parts/:partId/sequence',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            delete: {
                method: 'DELETE',
                url: 'rest/parts/:partId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            enablePublicRead: {
                method: 'PUT',
                url: 'rest/parts/:partId/permissions/public',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            disablePublicRead: {
                method: 'DELETE',
                url: 'rest/parts/:partId/permissions/public',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            // adds a new link to the referenced part. the link could be a parent or child
            addLink: {
                method: 'POST',
                url: 'rest/parts/:partId/links',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            removeLink: {
                method: 'DELETE',
                url: 'rest/parts/:partId/links/:linkId',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            updateEntryList: {
                method: 'POST',
                responseType: 'json',
                url: 'rest/parts',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            }
        });
    }
});

iceServices.factory('Settings', function ($resource) {
    return function (sessionId) {
        return $resource('rest/config', {key: '@key'}, {
            get: {
                method: 'GET',
                isArray: true,
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            getSetting: {
                method: 'GET',
                url: 'rest/config/:key'
            },

            getGeneralSettings: {
                method: 'GET',
                isArray: true,
                url: 'rest/config/general',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            update: {
                method: 'PUT',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            rebuildLucene: {
                method: 'PUT',
                url: 'rest/config/lucene',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            rebuildBlast: {
                method: 'PUT',
                url: 'rest/config/blast',
                headers: {'X-ICE-Authentication-SessionId': sessionId}
            },

            version: {
                method: 'GET',
                url: 'rest/config/version'
            }
        });
    }
});

iceServices.factory('Remote', function ($resource, $cookieStore) {
    return function () {
        return $resource('rest/remote/:id', {id: '@id', email: '@email', partId: '@partId', folderId: '@folderId'}, {
            //publicFolders: {
            //    method: 'GET',
            //    responseType: 'json',
            //    url: 'rest/remote/:id/available',
            //    isArray: true,
            //    headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            //},

            publicEntries: {
                method: 'GET',
                responseType: 'json',
                url: 'rest/remote/:id/entries',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            getUser: {
                method: 'GET',
                responseType: 'json',
                url: 'rest/remote/:id/users/:email',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            getFolderEntries: {
                method: 'GET',
                responseType: 'json',
                url: 'rest/remote/:id/folders/:folderId',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            samples: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/remote/:id/parts/:partId/samples',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            comments: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/remote/:id/parts/:partId/comments',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            traces: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/remote/:id/parts/:partId/traces',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            }
        });
    }
});

iceServices.factory('Files', function ($resource, $cookieStore) {
    return function () {
        return $resource('rest/file', {fileId: '@fileId'}, {
            getTraceSequenceFile: {
                method: 'GET',
                url: 'rest/file/trace/:fileId',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            }
        });
    }
});

iceServices.factory('Folders', function ($resource, $cookieStore) {
    return function () {

//        Each key value in the parameter object is first bound to url template
//        if present and then any excess keys are appended to the url search query after the ?.
//        Given a template /path/:verb and parameter {verb:'greet', salutation:'Hello'}
//        results in URL /path/greet?salutation=Hello.

//        If the parameter value is prefixed with @ then the value of that parameter is extracted
//        from the data object (useful for non-GET operations).

        return $resource('rest/folders', {folderId: '@id', folderName: '@folderName', folderType: '@folderType'}, {

            //get all sub folders by type
            getByType: {
                method: 'GET',
                responseType: "json",
                url: "rest/collections/:folderType/folders",
                isArray: true,
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            query: {
                method: 'GET',
                responseType: "json",
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            create: {
                method: 'POST',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            addSelectionToFolders: {
                method: 'PUT',
                isArray: true,
                url: 'rest/folders',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            removeEntriesFromFolder: {
                method: 'PUT',
                url: 'rest/folders/:folderId/entries',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            folder: {
                method: 'GET',
                url: "rest/folders/:folderId/entries",
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            delete: {
                method: 'DELETE',
                url: 'rest/folders/:folderId',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            update: {
                method: 'PUT',
                url: 'rest/folders/:folderId',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            permissions: {
                method: 'GET',
                responseType: 'json',
                isArray: true,
                url: 'rest/folders/:folderId/permissions',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            addPermission: {
                method: 'POST',
                responseType: 'json',
                url: 'rest/folders/:folderId/permissions',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            removePermission: {
                method: 'DELETE',
                responseType: 'json',
                url: 'rest/folders/:folderId/permissions/:permissionId',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            enablePublicReadAccess: {
                method: 'PUT',
                responseType: 'json',
                url: 'rest/folders/:folderId/permissions/public',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            },

            disablePublicReadAccess: {
                method: 'DELETE',
                url: 'rest/folders/:folderId/permissions/public',
                headers: {'X-ICE-Authentication-SessionId': $cookieStore.get("sessionId")}
            }
        });
    }
});

iceServices.factory('Authentication',
    function ($resource, $cookieStore, $http, $rootScope, $location, $cookies, Util) {
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
    });

iceServices.factory('EntryContextUtil', function () {
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
