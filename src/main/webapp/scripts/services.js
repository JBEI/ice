'use strict';

/* Services */
var iceServices = angular.module('iceApp.services', ['ngCookies', 'ngResource']);

//iceServices.factory('Search', function ($resource, $cookieStore) {
//    return function () {
//
//        var sessionId = $cookieStore.get("sessionId");
//
//        return $resource('/rest/search', {}, {
//            runSearch:{
//                method:'GET',
//                responseType:"json",
//                headers:{'X-ICE-Authentication-SessionId':sessionId}
//            }
//        });
//    }
//});

iceServices.factory('EntryService', function () {
    var toStringArray = function (objArray) {
        var result = [];
        angular.forEach(objArray, function (object) {
            if (!object || !object.value || object.value === "")
                return;
            result.push(object.value);
        });
        return result;
    };

    // commons fields to all the different types of parts supported by the system
    var partFields = [
        {label:"Name", required:true, schema:'name', help:'Help Text', placeHolder:'e.g. JBEI-0001', inputType:'short'},
        {label:"Alias", schema:'alias', inputType:'short'},
        {label:"Principal Investigator", required:true, schema:'principalInvestigator', inputType:'withEmail', bothRequired:'false'},
        {label:"Funding Source", schema:'fundingSource', inputType:'short'},
        {label:"Status", schema:'status', options:[
            {value:"Complete", text:"Complete"},
            {value:"In Progress", text:"In Progress"},
            {value:"Abandoned", text:"Abandoned"},
            {value:"Planned", text:"Planned"}
        ]},
        {label:"Bio Safety Level", schema:'bioSafetyLevel', options:[
            {value:"1", text:"Level 1"},
            {value:"2", text:"Level 2"}
        ]},
        {label:"Creator", required:true, schema:'creator', inputType:'withEmail', bothRequired:'true'},
        {label:"Links", schema:'links', inputType:'add'},
        {label:"Summary", required:true, schema:'shortDescription', inputType:'long'},
        {label:"References", schema:'references', inputType:'long'},
        {label:"Intellectual Property", schema:'intellectualProperty', inputType:'long'}
    ];

    // fields peculiar to plasmids
    var plasmidFields = [
        {label:"Backbone", schema:'backbone', subSchema:'plasmidData', inputType:'medium'},
        {label:"Origin of Replication", schema:'originOfReplication', inputType:'autoComplete',
            autoCompleteField:'ORIGIN_OF_REPLICATION', subSchema:'plasmidData'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'},
        {label:"Promoters", schema:'promoters', subSchema:'plasmidData', inputType:'autoComplete', autoCompleteField:'PROMOTERS'},
        {label:"Replicates In", schema:'replicatesIn', subSchema:'plasmidData', inputType:'autoComplete', autoCompleteField:'REPLICATES_IN'}
    ];

    // fields peculiar to arabidopsis seeds
    var seedFields = [
        {label:"Sent To ABRC", schema:'sentToABRC', help:"Help Text", inputType:'bool', subSchema:'arabidopsisSeedData'},
        {label:"Plant Type", schema:'plantType', subSchema:'arabidopsisSeedData', options:[
            {value:"EMS", text:"EMS"},
            {value:"OVER_EXPRESSION", text:"Over Expression"},
            {value:"RNAI", text:"RNAi"},
            {value:"REPORTER", text:"Reporter"},
            {value:"T_DNA", text:"T-DNA"},
            {value:"OTHER", text:"Other"}
        ]},
        {label:"Generation", schema:'generation', subSchema:'arabidopsisSeedData', options:[
            {value:"UNKNOWN", text:"UNKNOWN"},
            {value:"F1", text:"F1"},
            {value:"F2", text:"F2"},
            {value:"F3", text:"F3"},
            {value:"M0", text:"M0"},
            {value:"M1", text:"M1"},
            {value:"M2", text:"M2"},
            {value:"T0", text:"T0"},
            {value:"T1", text:"T1"},
            {value:"T2", text:"T2"},
            {value:"T3", text:"T3"},
            {value:"T4", text:"T4"},
            {value:"T5", text:"T5"}
        ]},
        {label:"Harvest Date", schema:'harvestDate', subSchema:'arabidopsisSeedData', inputType:'date'},
        {label:"Homozygosity", schema:'backbone', subSchema:'arabidopsisSeedData', inputType:'medium'},
        {label:"Ecotype", schema:'backbone', subSchema:'arabidopsisSeedData', inputType:'medium'},
        {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
            autoCompleteField:'SELECTION_MARKERS'}
    ];

    // fields peculiar to seeds
    var strainFields = [
        {label:"Selection Markers", required:true, schema:'selectionMarkers',
            inputType:'autoCompleteAdd', autoCompleteField:'SELECTION_MARKERS'},
        {label:"Genotype/Phenotype", schema:'genotypePhenotype', inputType:'long', subSchema:'strainData'},
        {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER', subSchema:'strainData'}
    ];

    var generateLinkOptions = function (type) {
        switch (type.toLowerCase()) {
            case 'plasmid':
                return [
                    {type:'part', display:'Part'},
                    {type:'plasmid', display:'Plasmid'}
                ];

            case 'part':
                return [
                    {type:'part', display:'Part'}
                ];

            case 'strain':
                return [
                    {type:'part', display:'Part'},
                    {type:'plasmid', display:'Plasmid'},
                    {type:'strain', display:'Strain'}
                ];

            case 'arabidopsis':
                return [
                    {type:'part', display:'Part'},
                    {type:'arabidopsis', display:'Arabidopsis Seed'}
                ];
        }
    };

    var validateFields = function (part, fields) {
        var canSubmit = true;

        // main type
        angular.forEach(fields, function (field) {
            if (!field.required)
                return;

            if (field.inputType === 'add' || field.inputType === 'autoCompleteAdd') {
                if (part[field.schema].length == 0) {
                    field.invalid = true;
                }
                else {
                    for (var i = 0; i < part[field.schema].length; i += 1) {
                        var fieldValue = part[field.schema][i].value;
                        field.invalid = !fieldValue || fieldValue === '';
                    }
                }
            } else {
                field.invalid = (part[field.schema] === undefined || part[field.schema] === '');
            }

            if (canSubmit) {
                canSubmit = !field.invalid;
            }
        });
        return canSubmit;
    };

    var getFieldsForType = function (type) {
        var fields = angular.copy(partFields);
        type = type.toLowerCase();
        switch (type) {
            case 'strain':
                fields.splice.apply(fields, [7, 0].concat(strainFields));
                return fields;

            case 'arabidopsis':
                fields.splice.apply(fields, [7, 0].concat(seedFields));
                return fields;

            case 'plasmid':
                fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                return fields;

            case 'part':
            default:
                return fields;
        }
    };

    return {
        toStringArray:function (obj) {
            return toStringArray(obj);
        },

        linkOptions:function (type) {
            return generateLinkOptions(type);
        },

        getFieldsForType:function (type) {
            return getFieldsForType(type);
        },

        // converts to a form that the backend can work with
        getTypeData:function (entry) {
            var type = entry.type.toLowerCase();
            var fields = getFieldsForType(type);
            angular.forEach(fields, function (field) {
                if (field.subSchema) {
                    if (entry[field.subSchema] === undefined)
                        entry[field.subSchema] = {};
                    entry[field.subSchema][field.schema] = entry[field.schema];
                }
            });

            return entry;
        },

        // inverse of the above. converts to form ui can work with
        convertToUIForm:function (entry) {
            var type = entry.type.toLowerCase();
            var fields = getFieldsForType(type);

            angular.forEach(fields, function (field) {
                if (field.subSchema && entry[field.subSchema]) {
                    entry[field.schema] = entry[field.subSchema][field.schema];
                }
            });

            return entry;
        },

        validateFields:function (part, fields) {
            return validateFields(part, fields);
        }
    }
});

iceServices.factory('Pigeon', function ($http) {
    return {
        fetch:function (script) {
            $http.post("cidar1.bu.edu:5801/pigeon.php", {"specification":script})
                .success(function (data, status, headers, config) {
                    console.log("SUCCESS", data, status);
                })
                .error(function (data, status, headers, config) {
                    console.log("ERROR", data, status);
                });
        }
    }
});

iceServices.factory('Group', function ($resource, $cookieStore) {
    return function () {

        var sessionId = $cookieStore.get("sessionId");

        return $resource('/rest/groups', {userId:'@userId'}, {
            getUserGroups:{
                method:'GET',
                responseType:"json",
                isArray:true,
                url:"/rest/groups/:userId",
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
        return $resource('/rest/samples', {userId:'@userId'}, {
            requests:{
                method:'GET',
                responseType:'json',
                url:"/rest/samples/requests",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            userRequests:{
                method:'GET',
                responseType:'json',
                url:"/rest/samples/requests/:userId",
                isArray:true,
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addRequestToCart:{
                method:'POST',
                responseType:'json',
                isArray:true,
                url:"/rest/samples/requests",
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

            tooltip:{
                method:'GET',
                responseType:'json',
                url:"/rest/part/:partId/tooltip",
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

            experiments:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/experiments',
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

            createExperiment:{
                method:'POST',
                responseType:'json',
                url:'/rest/part/:partId/experiments',
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

            history:{
                method:'GET',
                responseType:'json',
                isArray:true,
                url:'/rest/part/:partId/history',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteTraceSequence:{
                method:'DELETE',
                responseType:'json',
                url:'/rest/part/:partId/traces/:traceId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addTraceSequence:{
                method:'POST',
                url:'/rest/part/:partId/traces',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            sequence:{
                method:'GET',
                responseType:'json',
                url:'/rest/part/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            deleteSequence:{
                method:'DELETE',
                url:'/rest/part/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            addSequenceAsString:{
                method:"POST",
                url:'/rest/part/:partId/sequence',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            moveEntriesToTrash:{
                method:'POST',
                url:'/rest/part/trash',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            delete:{
                method:'DELETE',
                url:'/rest/part/:partId',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            enablePublicRead:{
                method:'PUT',
                url:'/rest/part/:partId/permissions/public',
                headers:{'X-ICE-Authentication-SessionId':sessionId}
            },

            disablePublicRead:{
                method:'DELETE',
                url:'/rest/part/:partId/permissions/public',
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
        return $resource('/rest/remote/:id', {id:'@id', email:'@email', folderId:'@folderId'}, {
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
            }
        });
    }
});

iceServices.factory('WebOfRegistries', function ($resource, $cookieStore) {
    return function () {
        return $resource('/rest/web', {url:'@url'}, {
            query:{
                method:'GET',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            addPartner:{
                method:'POST',
                url:'/rest/web/partner',
                responseType:'json',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            removePartner:{
                method:'DELETE',
                url:'/rest/web/partner/:url',
                headers:{'X-ICE-Authentication-SessionId':$cookieStore.get("sessionId")}
            },

            updatePartner:{
                method:'PUT',
                url:'/rest/web/partner/:url',
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
            }
        });
    }
});

iceServices.factory('AccessToken', function ($resource, $cookieStore, $http, $rootScope, $location, $cookies) {
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
            isSessionValid:function (who) {
                console.log("check for valid session", who);
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

            isAdmin:function () {
//                console.log($rootScope.user);
//                 if(!$rootScope.user || !$rootScope.user.isAdmin) {
//                     $location.path("/");
//                     return false;
//                 }
                return true;
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
