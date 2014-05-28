'use strict';

/* Services */
var iceServices = angular.module('iceApp.services', ['ngCookies', 'ngResource']);

iceServices.factory('Pigeon', function ($http) {
    return {
        fetch:function (script) {
            $http.post("cidar1.bu.edu:5801/pigeon.php", {"desc":script})
                .success(function (data, status, headers, config) {
                    console.log("SUCCESS", data, status);
                })
                .error(function (data, status, headers, config) {
                    console.log("ERROR", data, status);
                });
        }
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
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            getGroups:{
                method:'GET',
                url:"/rest/users/:userId/groups",
                responseType:'json',
                isArray:true,
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
        return $resource('/rest/samples', {}, {
            requests:{
                method:'GET',
                responseType:'json',
                url:"/rest/samples/requests",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        })
    }
});

iceServices.factory('Attachment', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/part/:partId/attachments', {partId:'@partId', attachmentId:'@attachmentId'}, {
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
                url:'/rest/part/:partId/attachments/:attachmentId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Entry', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/part/', {partId:'@id', traceId:'@traceId', permissionId:'@permissionId'}, {
            query:{
                method:'GET',
                responseType:"json",
                url:"/rest/part/:partId",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            create:{
                method:'PUT',
                responseType:'json',
                url:'/rest/part',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            update:{
                method:'PUT',
                responseType:'json',
                url:'/rest/part/:partId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            statistics:{
                method:'GET',
                responseType:'json',
                url:'/rest/part/:partId/statistics',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            comments:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/comments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            permissions:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/permissions',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addPermission:{
                method:'POST',
                responseType:'json',
                url:'/rest/part/:partId/permissions',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            removePermission:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/part/:partId/permissions/:permissionId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            createComment:{
                method:'POST',
                responseType:'json',
                url:'/rest/part/:partId/comments',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            samples:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/samples',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            traceSequences:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/traces',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteTraceSequence:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/part/:partId/traces/:traceId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            sequence:{
                method:'GET',
                responseType:'json',
                url:'/rest/part/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteEntries:{
                method:'DELETE',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            delete:{
                method:'DELETE',
                url:'/rest/part/:partId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Upload', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/upload/:importId', {importId:'@id', type:'@type', name:'@name', entryId:'@entryId'}, {
            get:{
                method:'GET',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            create:{
                method:'PUT',
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

            bulkUpdate:{
                method:'POST',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Search', function ($resource, $cookieStore) {
    return function () {

        var sessionId = $cookieStore.get("sessionId");

        return $resource('/rest/search', {}, {
            runSearch:{
                method:'GET',
                responseType:"json",
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            }
        });
    }
});

iceServices.factory('Settings', function ($resource) {
    return function (sessionId) {
        return $resource('/rest/config', {}, {
            get:{
                method:'GET',
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
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
            }
        });
    }
});

iceServices.factory('WebOfRegistries', function ($resource, $cookieStore) {
    return function () {
        return $resource('/rest/web', {}, {
            query:{
                method:'GET',
                responseType:'json',
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
            }
        });
    }
});

iceServices.factory('Authentication', function ($resource, $cookieStore, $http, $rootScope, $location) {
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
                    $location.path('/');
                }).
                error(function (data, status, headers, config) {
                    console.log(data, status);
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                });
        },

        // checks if the session is valid
        isSessionValid:function () {
            var sid = $cookieStore.get('sessionId');
            if (sid === undefined) {
                $location.path('/login');
                return;
            }

            return $http.get('/rest/accesstoken',
                {headers:{'X-ICE-Authentication-SessionId':sid}})
                .success(function (data) {
                    if (data.sessionId === undefined) {
                        $cookieStore.remove('userId');
                        $cookieStore.remove('sessionId');
                        $location.path('/login');
                    }
                    $rootScope.user = data;
                })
                .error(function (data, status) {
                    if (status === 401) {
                        $location.path('/login');
                    }
                    console.log("ERROR", data);
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

    // example using resource
//    return $resource('/rest/accesstoken', {}, {
//        'login':{ method:"POST" },
//        "isSessionValid":{ method:"GET", headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")} }
//    });
});
