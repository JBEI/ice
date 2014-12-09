'use strict';

/* Services */
var iceServices = angular.module('iceApp.services', ['ngCookies', 'ngResource']);

iceServices.factory('Group', function ($resource, $cookieStore) {
    return function () {

        var sessionId = $cookieStore.get("sessionId");

        return $resource('/rest/groups', {groupId:'@groupId'}, {
            members:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:"/rest/groups/:groupId/members",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'PUT',
                responseType:'json',
                url:"/rest/groups/:groupId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Permission', function ($resource, $cookieStore) {
    return function () {

        var sessionId = $cookieStore.get("sessionId");

        return $resource('/rest/permission', {}, {
            filterUsersAndGroups:{
                method:'GET',
                responseType:"json",
                isArray:true,
                url:"/rest/permission/autocomplete",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('User', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/users', {userId:'@userId', preferenceKey:'@preferenceKey'}, {
            query:{
                method:'GET',
                responseType:"json",
                url:"/rest/users/:userId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'POST',
                responseType:"json",
                url:"/rest/users/:userId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            list:{
                method:'GET',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            filter:{
                method:'GET',
                url:'/rest/users/autocomplete',
                isArray:true,
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getGroups:{
                method:'GET',
                url:"/rest/users/:userId/groups",
                responseType:'json',
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createGroup:{
                method:'PUT',
                url:"/rest/users/:userId/groups",
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createUser:{
                method:'PUT',
                url:'/rest/users/',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getEntries:{
                method:'GET',
                url:'/rest/users/:userId/entries',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getPreferences:{
                method:'GET',
                url:'/rest/users/:userId/preferences',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            updatePreference:{
                method:'POST',
                url:'/rest/users/:userId/preferences/:preferenceKey',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            resetPassword:{
                method:'POST',
                url:'/rest/users/password',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            changePassword:{
                method:'PUT',
                url:'/rest/users/password',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            samples:{
                method:'GET',
                url:'/rest/users/:userId/samples',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Message', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/messages', {messageId:'@id'}, {
            query:{
                method:'GET',
                responseType:"json",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Samples', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/samples', {userId:'@userId', requestId:'@requestId', status:'@status', type:'@type'}, {
            requests:{
                method:'GET',
                responseType:'json',
                url:"/rest/samples/requests",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'PUT',
                responseType:'json',
                url:"/rest/samples/requests/:requestId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            submitRequests:{
                method:'PUT',
                url:"/rest/samples/requests",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            userRequests:{
                method:'GET',
                responseType:'json',
                url:"/rest/samples/requests/:userId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            removeRequestFromCart:{
                method:'DELETE',
                url:"/rest/samples/requests/:requestId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addRequestToCart:{
                method:'POST',
                responseType:'json',
                isArray:true,
                url:"/rest/samples/requests",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getStorageType:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:"/rest/samples/storage/:type",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        })
    }
});

iceServices.factory('Attachment', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/parts/:partId/attachments', {partId:'@partId', attachmentId:'@attachmentId'}, {
            create:{
                method:'POST',
                responseType:"json",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            get:{
                method:'GET',
                responseType:"json",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            delete:{
                method:'DELETE',
                url:'/rest/parts/:partId/attachments/:attachmentId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Entry', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/parts/', {partId:'@id', traceId:'@traceId', permissionId:'@permissionId', commentId:'@commentId', sampleId:'@sampleId', linkId:'@linkId', historyId:'@historyId'}, {
            query:{
                method:'GET',
                responseType:"json",
                url:"/rest/parts/:partId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            tooltip:{
                method:'GET',
                responseType:'json',
                url:"/rest/parts/:partId/tooltip",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            create:{
                method:'PUT',
                responseType:'json',
                url:'/rest/parts',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'PUT',
                responseType:'json',
                url:'/rest/parts/:partId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            statistics:{
                method:'GET',
                responseType:'json',
                url:'/rest/parts/:partId/statistics',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            comments:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/comments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            experiments:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/experiments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            permissions:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/permissions',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addPermission:{
                method:'POST',
                responseType:'json',
                url:'/rest/parts/:partId/permissions',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            removePermission:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/parts/:partId/permissions/:permissionId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createComment:{
                method:'POST',
                responseType:'json',
                url:'/rest/parts/:partId/comments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            updateComment:{
                method:'PUT',
                responseType:'json',
                url:'/rest/parts/:partId/comments/:commentId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createExperiment:{
                method:'POST',
                responseType:'json',
                url:'/rest/parts/:partId/experiments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            samples:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/samples',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addSample:{
                method:'POST',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/samples',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteSample:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/parts/:partId/samples/:sampleId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            traceSequences:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/traces',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            history:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/parts/:partId/history',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteHistory:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/parts/:partId/history/:historyId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteTraceSequence:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/parts/:partId/traces/:traceId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addTraceSequence:{
                method:'POST',
                url:'/rest/parts/:partId/traces',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            sequence:{
                method:'GET',
                responseType:'json',
                url:'/rest/parts/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteSequence:{
                method:'DELETE',
                url:'/rest/parts/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addSequenceAsString:{
                method:"POST",
                url:'/rest/parts/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            moveEntriesToTrash:{
                method:'POST',
                url:'/rest/parts/trash',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            delete:{
                method:'DELETE',
                url:'/rest/parts/:partId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            enablePublicRead:{
                method:'PUT',
                url:'/rest/parts/:partId/permissions/public',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            disablePublicRead:{
                method:'DELETE',
                url:'/rest/parts/:partId/permissions/public',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            removeLink:{
                method:'DELETE',
                url:'/rest/parts/:partId/links/:linkId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Upload', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/upload/:importId', {importId:'@id', type:'@type', entryId:'@entryId'}, {
            get:{
                method:'GET',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            create:{
                method:'PUT',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            updateStatus:{
                method:'PUT',
                url:'/rest/upload/:importId/status',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createEntry:{
                method:'PUT',
                url:'/rest/upload/:importId/entry',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            updateEntry:{
                method:'POST',
                url:'/rest/upload/:importId/entry/:entryId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            rename:{
                method:'PUT',
                url:'/rest/upload/:importId/name',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            fileUpload:{
                method:'POST',
                url:'rest/upload/file',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            updateList:{
                method:'PUT',
                url:'rest/upload/:importId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteSequence:{
                method:'DELETE',
                url:'rest/upload/:importId/entry/:entryId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteAttachment:{
                method:'DELETE',
                url:'rest/upload/:importId/entry/:entryId/attachment',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Settings', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/config', {key:'@key'}, {
            get:{
                method:'GET',
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getSetting:{
                method:'GET',
                url:'/rest/config/:key'
                // this does not require authentication
//                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getGeneralSettings:{
                method:'GET',
                isArray:true,
                url:'/rest/config/general',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'PUT',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            rebuildLucene:{
                method:'PUT',
                url:'/rest/config/lucene',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            rebuildBlast:{
                method:'PUT',
                url:'/rest/config/blast',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            version:{
                method:'GET',
                url:'/rest/config/version'
            }
        });
    }
});

iceServices.factory('Remote', function ($resource, $cookieStore) {
    return function () {
        return $resource('/rest/remote/:id', {id:'@id', email:'@email', partId:'@partId', folderId:'@folderId'}, {
            publicFolders:{
                method:'GET',
                responseType:'json',
                url:'/rest/remote/:id/available',
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            publicEntries:{
                method:'GET',
                responseType:'json',
                url:'/rest/remote/:id/entries',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            getUser:{
                method:'GET',
                responseType:'json',
                url:'/rest/remote/:id/users/:email',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            getFolderEntries:{
                method:'GET',
                responseType:'json',
                url:'/rest/remote/:id/folders/:folderId',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            samples:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/remote/:id/parts/:partId/samples',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            comments:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/remote/:id/parts/:partId/comments',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            traces:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/remote/:id/parts/:partId/traces',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            }
        });
    }
});

iceServices.factory('Files', function ($resource, $cookieStore) {
    return function () {
        return $resource('/rest/file', {fileId:'@fileId'}, {
            getTraceSequenceFile:{
                method:'GET',
                url:'/rest/file/trace/:fileId',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            getCSV:{
                method:'POST',
                url:'/rest/file/csv',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
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

        return $resource('/rest/folders', {folderId:'@id', folderName:'@folderName', folderType:'@folderType'}, {

            //get all sub folders by type
            getByType:{
                method:'GET',
                responseType:"json",
                url:"/rest/folders/:folderType",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            // get all counts (todo: instead of hard coding the folder types on the ui,
            // have this method also return the names)
            query:{
                method:'GET',
                responseType:"json",
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            create:{
                method:'PUT',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            addEntriesToFolders:{
                method:'POST',
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            removeEntriesFromFolder:{
                method:'PUT',
                url:'/rest/folders/:folderId/entries',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            // retrieves folder contents. folderId could be a string such as "personal"
            // "available", "shared", "upload"
            folder:{
                method:'GET',
                url:"/rest/folders/:folderId/entries",
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            delete:{
                method:'DELETE',
                url:'/rest/folders/:folderId',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            update:{
                method:'PUT',
                url:'/rest/folders/:folderId',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            permissions:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/folders/:folderId/permissions',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            addPermission:{
                method:'POST',
                responseType:'json',
                url:'/rest/folders/:folderId/permissions',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            removePermission:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/folders/:folderId/permissions/:permissionId',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            enablePublicReadAccess:{
                method:'PUT',
                responseType:'json',
                url:'/rest/folders/:folderId/permissions/public',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            disablePublicReadAccess:{
                method:'DELETE',
                url:'/rest/folders/:folderId/permissions/public',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            }
        });
    }
});

iceServices.factory('AccessToken', function ($resource) {
    return function () {
        return $resource('/rest/accesstoken', {}, {
            createToken:{
                method:'POST',
                responseType:'json'
            }
        })
    }
});

iceServices.factory('Authentication',
    function ($resource, $cookieStore, $http, $rootScope, $location, $cookies, AccessToken) {
        return {
            // logs in user to ice
            login:function (username, password) {
                var token = AccessToken();
                token.createToken({}, {email:username, password:password},
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
                    },
                    function (error) {
                        console.error(error);
                        $rootScope.errMsg = "Login failed";
                    }
                );
            },

            // checks if the session is valid
            isSessionValid:function () {
//                console.log("check for valid session", who);
                var sid = $cookieStore.get('sessionId');
                if (sid === undefined) {
                    if ($location.path() !== '/login')
                        $cookies.loginDestination = $location.path();
                    $location.path('/login');
                    return;
                }

                return $http.get('/rest/accesstoken',
                    {headers:{'X-ICE-Authentication-SessionId':sid}})
                    .success(function (data) {
                        if (data.sessionId === undefined) {
                            $cookieStore.remove('userId');
                            $cookieStore.remove('sessionId');
                            if ($location.path() !== '/login')
                                $cookies.loginDestination = $location.path();
                            $location.path('/login');
                        }
                        $rootScope.user = data;
                    })
                    .error(function (data, status) {
                        if (status === 401) {
                            if ($location.path() !== '/login')
                                $cookies.loginDestination = $location.path();
                            $location.path('/login');
                        }
                        console.log("ERROR", data);
                    });
            },

            // todo : use a parameter in isSessionValid to check
            isAdmin:function (User) {
                this.isSessionValid().then(function (result) {
                    if (!result || !result.data || !result.data.isAdmin) {
                        $location.path("/folders/personal");
                        return false;
                    }
                });
            },

            // logs out user by invalidating the session id
            logout:function () {
                var sid = $cookieStore.get("sessionId");
                return $http.delete('/rest/accesstoken', {headers:{'X-ICE-Authentication-SessionId':sid}}).
                    success(function () {
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
        getContext:function () {
            return context;
        },

        setContextCallback:function (callback, available, offset, back) {
            context = {};
            context.callback = callback;
            context.available = available;
            context.offset = offset;
            context.back = back;
        }
    }
});
