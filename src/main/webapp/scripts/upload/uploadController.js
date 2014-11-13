'use strict';

angular.module('ice.upload.controller', [])
    .controller('UploadController', function ($rootScope, $location, $scope, $modal, $cookieStore, $resource, $stateParams, $fileUploader, $http, Upload, UploadUtil, EntryService) {
        var sid = $cookieStore.get("sessionId");
        var upload = Upload(sid);
        var sheetData = [
            []
        ];

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type.toUpperCase();

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = []; // maintains the ids of the main entrys (row indexed)
        $scope.bulkUpload.linkedEntryIdData = []; // maintains the ids of the linked entry
        $scope.bulkUpload.name = "untitled";
        $scope.uploadNameEditMode = false;
        var linkedHeaders = undefined;
        var linkedDataSchema = undefined;
        $scope.linkedSelection = undefined;
        var linkedImportType;

        $scope.setNameEditMode = function (value) {
            $scope.uploadNameEditMode = value;
        };

        //
        // add link of specified type to selected bulk upload
        // this will be allowed as long as the user has not entered any data into the linked portion
        //
        $scope.addNewPartLink = function (type) {
            linkedImportType = type;
            $scope.linkedSelection = type.charAt(0).toUpperCase() + type.substring(1);
            var ht = $("#dataTable").handsontable('getInstance');
            linkedHeaders = UploadUtil.getSheetHeaders(type);
            linkedDataSchema = UploadUtil.getDataSchema(type);
            ht.alter('insert_col', undefined, linkedHeaders.length);
        };

        //
        // uses xmlHttpRequest to upload files
        //
        var transport = function (item, row, col) {
            var xhr = item._xhr = new XMLHttpRequest();
            var form = new FormData();

            item.formData.forEach(function (obj) {
                angular.forEach(obj, function (value, key) {
                    form.append(key, value);
                });
            });

            form.append(item.alias, item.file);

            xhr.upload.onprogress = function (event) {
                var progress = event.lengthComputable ? event.loaded * 100 / event.total : 0;
                console.log(Math.round(progress));
            };

            var transformResponse = function (response) {
                $http.defaults.transformResponse.forEach(function (transformFn) {
                    response = transformFn(response);
                });
                return response;
            };

            xhr.onload = function () {
                var response = transformResponse(xhr.response);
                if ((xhr.status >= 200 && xhr.status < 300) || xhr.status === 304) {
                    sheetData[row][col] = response.filename;
                } else {
                    console.log("error");
                }
            };

            xhr.onerror = function () {
                // error
            };

            xhr.onabort = function () {
                // canceled
            };

            xhr.open(item.method, item.url, true);

            angular.forEach(item.headers, function (value, name) {
                xhr.setRequestHeader(name, value);
            });

            xhr.send(form);
        };

        //
        // handles file uploads when user selects a file in the bulk upload interface
        //
        $scope.onFileSelect = function (files, row, col) {
            var id = $scope.bulkUpload.id;
            var url = "/rest/upload/" + id + "/" + UploadUtil.indexToRestResource($scope.importType, col);

            var item = {
                method:'POST',
                url:url,
                file:files[0],
                alias:"file",
                formData:[
                    {entryType:$scope.importType, entryId:$scope.bulkUpload.entryIdData[row]}
                ],
                headers:{"X-ICE-Authentication-SessionId":sid}
            };

            transport(item, row, col);
        };

        // delete file in row, col
        $scope.onFileDelete = function (row, col) {
            var entryId = $scope.bulkUpload.entryIdData[row];
            var id = $scope.bulkUpload.id;

            console.log(id);

            if (UploadUtil.indexToRestResource($scope.importType, col) === "attachment") {
                Upload(sid).deleteAttachment({importId:id, entryId:entryId},
                    function (success) {
                        sheetData[row][col] = undefined;
                    }, function (error) {
                        console.error(error);
                    });
            } else {
                //delete sequence
                Upload(sid).deleteSequence({importId:$scope.bulkUpload.id, entryId:entryId},
                    function (success) {
                        sheetData[row][col] = undefined;
                    }, function (error) {
                        console.error(error);
                    });
            }
        };

        //
        // creates new sheet interface
        //
        var createSheet = function () {
            var availableWidth, availableHeight, $window = $(window), $dataTable = $("#dataTable");

            // cell renderer for file upload
            var fileUploadRenderer = function (instance, td, row, col, prop, value, cellProperties) {
                if (value) {
                    var $del = $('<i class="fa fa-trash-o delete_icon"></i>');
                    $del.on("click", function (event) {
//                        sheetData[row][col] = undefined;
                        angular.element(this).scope().onFileDelete(row, col);
                    });
                    $(td).empty().append(value).append("&nbsp;").append($del);
                } else {
                    var $up = $('<span class="fileUpload"><i class="fa fa-upload opacity_hover opacity_4"></i> Upload '
                        + '<input type="file" class="upload" /></span>');

                    $up.on("change", function (event) {
                        console.log("change", event);
                        angular.element(this).scope().onFileSelect(this.getElementsByTagName("input")[0].files, row, col);
                    });

                    $(td).empty().append($up);
                }
                return td;
            };

            var autoComplete = function (field, query, process) {
                $http.get('/rest/parts/autocomplete', {
                    headers:{'X-ICE-Authentication-SessionId':sid},
                    params:{
                        val:query,
                        field:field
                    }
                }).then(function (res) {
                        return process(res.data);
                    });
            };

            var getCellProperties = function (row, col, prop) {
                var object = {};
                var fieldType;

                if (linkedImportType) {
                    var newIndex = col - sheetHeaders.length;
                    fieldType = UploadUtil.getTypeField(linkedImportType, newIndex);
                } else
                    fieldType = UploadUtil.getTypeField($scope.importType, col);

                switch (fieldType) {

                    case 'circular':
                    case 'sentToAbrc':
                        object.type = 'checkbox';
                        break;

                    case 'status':
                        object.type = 'autocomplete';
                        object.source = ['Complete', 'In Progress', 'Planned', ''];
                        object.allowInvalid = false;
                        object.validator = function (value, callback) {
                            callback(value == 'Complete' || value == 'In Progress' || value == 'Planned' || value == '');
                        };
                        break;

                    case 'bioSafetyLevel':
                        object.type = 'autocomplete';
                        object.source = ['1', '2', ''];
                        object.validator = function (value, callback) {
                            callback(value == 1 || value == 2);
                        };
                        object.allowInvalid = false;
                        break;

                    case 'selectionMarkers':
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('SELECTION_MARKERS', query, process);
                        };
                        break;

                    case "promoters":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('PROMOTERS', query, process);
                        };
                        break;

                    case "replicatesIn":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('REPLICATES_IN', query, process);
                        };
                        break;

                    case "originOfReplication":
                        object.type = 'autocomplete';
                        object.strict = false;
                        object.source = function (query, process) {
                            autoComplete('ORIGIN_OF_REPLICATION', query, process);
                        };
                        break;

                    case 'sequenceFileName':
                    case "attachments":
                    case "sequenceTrace":
                        object.renderer = fileUploadRenderer;
                        object.type = 'text';
                        object.readOnly = true;  // file cells are readonly. all data is set programmatically
                        object.copyable = false; // file cells cannot be copied
                        break;
                }

                return object;
            };

            // headers for the current selection and initialize first row
            var sheetHeaders = UploadUtil.getSheetHeaders($scope.importType);
            for (var i = 0; i < sheetHeaders.length; i += 1) {
                sheetData[0][i] = null;
            }

            //
            // function to display the header at the specified index. A special case is when a link
            // has been added in which case we need to check for the linked selection's headers as appropriate
            //
            var getSheetHeaders = function (index) {
                if (index >= sheetHeaders.length && $scope.linkedSelection) {
                    var newIndex = index - sheetHeaders.length;
                    return $scope.linkedSelection + " " + linkedHeaders[newIndex];
                }
                return sheetHeaders[index];
            };

            var getColWidth = function (index) {
                return 150;
            };

            var calculateSize = function () {
                var offset = $dataTable.offset();
                availableWidth = $window.width() - offset.left + $window.scrollLeft();
                availableHeight = $window.height() - offset.top + $window.scrollTop();
                $dataTable.handsontable('render');
            };

            var widthFunction = function () {
                if (availableWidth === void 0) {
                    calculateSize();
                }
                return availableWidth;
            };

            var heightFunction = function () {
                if (availableHeight === void 0) {
                    calculateSize();
                }
                return availableHeight - 87;
            };

            $window.on('resize', calculateSize);

            //
            // called by the callback handler when a user edits a cell or a number of cells
            // and a save to the server is required
            //
            var createOrUpdateEntry = function (data) {
                var row = data[0];
                var object = getEntryObject(row, data[1], data[3]);
                if (!object)
                    return;

                $scope.saving = true;
                if ($scope.bulkUpload.id === undefined) {
                    // create draft of specified type
                    upload.create({type:$scope.importType})
                        .$promise
                        .then(function (result) {
//                            console.log("created new bulk upload", result);
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            $scope.bulkUpload.name = result.name;
//                            $location.path("/upload/" + result.id, false);

                            // then create entry and associate with draft
                            createEntry(result.id, object, row);
                        });
                } else {
                    // check if row being updated has existing entry
                    if (!object['id']) {
                        // create new entry for existing upload
                        createEntry($scope.bulkUpload.id, object, row);
                    } else {
                        // update entry for existing upload
                        upload.updateEntry({importId:$scope.bulkUpload.id, entryId:object.id}, object,
                            function (updatedEntry) {
                                $scope.bulkUpload.lastUpdate = updatedEntry.modificationTime;

                                // todo : this will be an actual problem if there is a different value; undefined is ok
                                if ($scope.bulkUpload.entryIdData[row] != updatedEntry.id) {
                                    $scope.bulkUpload.entryIdData[row] = updatedEntry.id;
                                }

                                if (updatedEntry.linkedParts && updatedEntry.linkedParts.length) {
                                    var linkedId = updatedEntry.linkedParts[0].id;
                                    if (linkedId) {
//                                console.log("created link");
                                        $scope.bulkUpload.linkedEntryIdData[row] = linkedId;
                                    }
                                }
                                $scope.saving = false;
                            },
                            function (error) {
                                // todo : this should revert the change in the ui and display a message
                                console.error(error);
                                $scope.saving = false;
                            });
                    }
                }
            };

            // upload entries associated with a bulk upload
            var updateEntryList = function (objects) {
                if ($scope.bulkUpload.id === undefined) {
                    console.error("cannot update upload list. no bulk upload object");
                    return;
                }

                var entryList = [];
                for (var idx = 0; idx < objects.length; idx += 1) {
                    var o = objects[idx];
                    if (!o)
                        continue;
                    entryList[entryList.length + 1] = o;
                }

                upload.updateList({importId:$scope.bulkUpload.id}, {entryList:entryList}, function (success) {
                    for (var j = 0; j < success.entryList.length; j += 1) {
                        var part = success.entryList[j];

                        $scope.bulkUpload.entryIdData[part.index] = part.id;
                        if (part.linkedParts && part.linkedParts.length) {
                            var linkedId = part.linkedParts[0].id;
                            if (linkedId) {
                                $scope.bulkUpload.linkedEntryIdData[part.index] = linkedId;
                            }
                        }
                    }
                    $scope.saving = false;
                }, function (error) {
                    console.error(error);
                    $scope.saving = false;
                });
            };

            // bulk create or update from autofill or paste
            var bulkCreateOrUpdate = function (change) {
                $scope.saving = true;

                // array of objects that will be created or updated
                var objects = [];

                for (var i = 0; i < change.length; i += 1) {
                    var data = change[i];
                    var row = data[0];
                    var existing = undefined;

                    if (objects.length > row) {
                        existing = objects[row];
                    }

                    var object = getEntryObject(row, data[1], data[3]);

                    if (existing) {
                        for (var attrname in object) {
                            if (object.hasOwnProperty(attrname)) {
                                existing[attrname] = object[attrname];
                            }
                        }
                        objects[row] = existing;
                    } else {
                        objects[row] = object;
                    }
                }

                if (objects.length === 0)
                    return;

                if ($scope.bulkUpload.id === undefined) {
                    // first create bulk upload
                    upload.create({type:$scope.importType}, function (result) {
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $scope.bulkUpload.name = result.name;
                        //                            $location.path("/upload/" + result.id, false);

                        // then update the list
                        updateEntryList(objects);
                    }, function (error) {
                        console.error("error creating bulk upload", error);
                    });
                } else {
                    updateEntryList(objects);
                }
            };

            var getEntryObject = function (row, col, value) {
                // entry indexes
                var entryIdDataIndex = $scope.bulkUpload.entryIdData[row];

                // if no object to update
                if (value === "" && !entryIdDataIndex)
                    return undefined;

                var object = {id:entryIdDataIndex, type:$scope.importType.toUpperCase(), strainData:{}, plasmidData:{}, arabidopsisSeedData:{}};

                // check if there is a linked object being updated
                if (col >= sheetHeaders.length) {   // or if ($scope.linkedSelection?
                    var linkedEntryIdDataIndex = $scope.bulkUpload.linkedEntryIdData[row];

                    if (value === "" && !linkedEntryIdDataIndex)
                        return undefined;

                    // set property for linked object and add it to link
                    var newIndex = col - sheetHeaders.length;
                    // todo : same treatment as object above
                    var linkedObject = {id:linkedEntryIdDataIndex, type:$scope.linkedSelection.toUpperCase()};
                    linkedObject[linkedDataSchema[newIndex]] = value;
                    object.linkedParts = [linkedObject];
                } else {
                    object = UploadUtil.setDataValue($scope.importType.toUpperCase(), col, object, value);
                }

                object.index = row;
                return object;
            };

            var createEntry = function (importId, object, row) {
                upload.createEntry({importId:importId}, object,
                    function (createdEntry) {
                        $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                        if (createdEntry.linkedParts && createdEntry.linkedParts.length) {
                            var linkedId = createdEntry.linkedParts[0].id;
                            if (linkedId) {
//                                console.log("created link");
                                $scope.bulkUpload.linkedEntryIdData[row] = linkedId;
                            }
                        }

                        $scope.saving = false;
//                        console.log("created entry", $scope.bulkUpload);
                    },
                    function (error) {
                        console.error(error);
                        $scope.saving = false;
                    });
            };

            //
            // callback for change to the sheet to save/update the information on the server
            //
            var afterChange = function (change, source) {
                // "setMyData" is intended to be used when setting data in code to prevent updates
                if (source === 'loadData' || source === 'setMyData' || source === 'external') {
                    return; //data load, no need to save
                }

                // single cell edit
                if (source === "edit") {   // single cell edit, change expected to contain single array
                    createOrUpdateEntry(change[0]);
                } else if (source === "autofill" || source === "paste") {
                    bulkCreateOrUpdate(change);
                }
            };

            var options = {
                data:sheetData,
                startRows:50, // comes into effect only if no data is provided
                minRows:50,
                colHeaders:getSheetHeaders,
                rowHeaders:true, // use default of 1, 2, 3 for row headers
                colWidths:getColWidth,
                stretchH:'all',
                minSpareRows:1,
                enterMoves:{row:0, col:1}, // move right on enter instead of down
                autoWrapRow:true,
                autoWrapCol:true,
                cells:getCellProperties,
                width:widthFunction,
                height:heightFunction,
                afterChange:afterChange,
                manualColumnResize:true,
                columnSorting:true
            };

            $dataTable.handsontable(options);
            $scope.spreadSheet = $dataTable.data('handsontable');

            $scope.fileUploadModal = function () {
                var modalInstance = $modal.open({
                    templateUrl:'views/modal/file-upload.html',
                    controller:'BulkUploadModalController',
                    backdrop:'static',
                    resolve:{
                        addType:function () {
                            return $scope.importType;
                        },

                        linkedAddType:function () {
                            return $scope.linkedSelection;
                        }
                    }
                });
            };

            $scope.confirmResetFormModal = function () {
                var resetModalInstance = $modal.open({
                    templateUrl:'views/modal/reset-bulk-upload-sheet.html',
                    controller:'BulkUploadModalController',
                    backdrop:'static',
                    resolve:{
                        addType:function () {
                            return $scope.importType;
                        },

                        linkedAddType:function () {
                            return $scope.linkedSelection;
                        }
                    }
                });
            };

            $scope.confirmRejectUploadModal = function () {
                var resetModalInstance = $modal.open({
                    templateUrl:'scripts/upload/modal/reject-upload.html',
                    controller:'BulkUploadRejectModalController',
                    backdrop:'static',
                    resolve:{
                        upload:function () {
                            return $scope.bulkUpload;
                        }
                    }
                });
            };

            $scope.submitImportForApproval = function () {
                $scope.submitting = true;
                var requiresApproval = $scope.bulkUpload.status && $scope.bulkUpload.status == 'PENDING_APPROVAL';

                // validate the contents;
                var tmp = {id:$scope.bulkUpload.id};
                if (requiresApproval)
                    tmp.status = 'APPROVED';
                else
                    tmp.status = 'PENDING_APPROVAL';
                $scope.requestError = undefined;

                Upload(sid).updateStatus({importId:$scope.bulkUpload.id}, tmp, function (result) {
                    $scope.submitting = false;
                    $location.path('/folders/personal');
                }, function (error) {
                    console.log(error, error.status === 400);

                    $scope.submitting = false;
                    if (error.status === 400) {
                        $scope.requestError = "Error: validation failed";
                    } else {
                        $scope.requestError = "Unknown server error";
                    }

                    var resetModalInstance = $modal.open({
                        templateUrl:'views/modal/upload-submit-alert.html',
                        controller:function ($scope, msg, isError) {
                            $scope.requestError = msg;
                        },
                        backdrop:'static',
                        resolve:{
                            msg:function () {
                                return $scope.requestError;
                            },

                            isError:function () {
                                return true;
                            }
                        }
                    });
                });
            };

            $scope.showBulkUploadRenameModal = function () {
                var modalInstance = $modal.open({
                    templateUrl:'views/modal/rename-bulk-upload-sheet.html',
                    controller:function ($scope, $modalInstance, uploadName) {
                        $scope.newBulkUploadName = uploadName;
                    },
                    backdrop:'static',
                    resolve:{
                        uploadName:function () {
                            return $scope.bulkUpload.name;
                        }
                    }
                });

                modalInstance.result.then(function (newName) {
                    // update name on the server if a bulk upload has already been created
                    if ($scope.bulkUpload.id) {
                        var tmp = {id:$scope.bulkUpload.id, name:newName};
                        console.log($scope.bulkUpload, tmp);
                        Upload(sid).rename({importId:$scope.bulkUpload.id}, tmp, function (result) {
                            $scope.bulkUpload.name = result.name;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            $scope.$emit("BulkUploadNameChange", tmp);
                        });
                    } else {
                        // just update display name
                        $scope.bulkUpload.name = newName;
                    }
                }, function () {
                    // dismiss callback
                });
            };

            $scope.sheetCreated = true;
        };

        var asyncLoop = function (loopFunction) {

            // loop function
            var loop = function (start) {
                loopFunction.functionToLoop(loop, start);
            };

            loop(0);//init
        };

        // retrieve
        $scope.uploadEntries = [];

        var generateLinkOptions = function (type) {
            switch (type) {
                case 'plasmid':
                    $scope.linkOptions = [
                        {type:'part', display:'Part'},
                        {type:'Plasmid', display:'Plasmid'}
                    ];
                    break;

                case 'part':
                    $scope.linkOptions = [
                        {type:'part', display:'Part'}
                    ];
                    break;

                case 'strain':
                    $scope.linkOptions = [
                        {type:'part', display:'Part'},
                        {type:'Plasmid', display:'Plasmid'},
                        {type:'strain', display:'Strain'}
                    ];
                    break;

                case 'arabidopsis':
                    $scope.linkOptions = [
                        {type:'part', display:'Part'},
                        {type:'arabidopsis', display:'Arabidopsis Seed'}
                    ];
                    break;
            }
        };

        // retrieve the contents of an import if parameter is a number
        if (!isNaN($stateParams.type)) {
            $scope.importType = undefined;
            asyncLoop({
                functionToLoop:function (loop, start) {
                    upload.get(
                        {importId:$stateParams.type, offset:start, limit:40},
                        function (result) {
                            $scope.bulkUpload.name = result.name;
                            $scope.bulkUpload.status = result.status;
                            $scope.importType = result.type.toLowerCase();
                            generateLinkOptions($scope.importType);

                            if (start === 0)
                                createSheet();

                            // else render on append data
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            var l = $scope.bulkUpload.entryIdData.length;    // number of existing entries

                            if (result.entryList && result.entryList.length) {
                                var dataSchema = UploadUtil.getDataSchema($scope.importType);

                                // for each entry object (row)
                                for (var i = 0; i < result.entryList.length; i += 1) {
                                    var entry = result.entryList[i];
                                    $scope.bulkUpload.entryIdData.push(entry.id);

                                    // ensure capacity
                                    if (!sheetData[l + i])
                                        sheetData[l + i] = [];

                                    // display [for each field in the object]
                                    for (var j = 0; j < dataSchema.length; j += 1) {
                                        var val = UploadUtil.getEntryValue($scope.importType, entry, j);
//                                        entry[dataSchema[j]];
                                        if (val === undefined)
                                            val = '';

                                        // currently for attachments only
                                        if (val instanceof Array && dataSchema[j] === "attachments") {
                                            if (val.length) {
                                                val = val[0].filename;
                                            } else {
                                                val = ""
                                            }
                                        }

                                        // "i" is for the new data added append to existing
                                        sheetData[l + i][j] = val;
                                    }

                                    // check if there is a linked entry
                                    if (entry.linkedParts && entry.linkedParts.length) {
                                        var linkedPart = entry.linkedParts[0];

                                        // check if there is a linked type and the link on the ui has not been created
                                        if (linkedDataSchema === undefined || linkedDataSchema.length === 0) {
                                            $scope.addNewPartLink(linkedPart.type.toLowerCase());
                                        }

                                        $scope.bulkUpload.linkedEntryIdData.push(linkedPart.id);

                                        // linkedDataSchema is created when addNewPartLink is called
                                        var dataSchemaLength = dataSchema.length;
                                        for (var k = 0; k < linkedDataSchema.length; k += 1) {
                                            val = linkedPart[linkedDataSchema[k]];
                                            if (val === undefined)
                                                val = '';

                                            // currently for attachments only
                                            if (val instanceof Array && dataSchema[j] === "attachments") {
                                                if (val.length) {
                                                    val = val[0].filename;
                                                } else {
                                                    val = ""
                                                }
                                            }

                                            // "i" is for the new data added append to existing
                                            sheetData[l + i][k + dataSchemaLength] = val;
                                        }
                                    }

                                    $scope.uploadEntries.push(entry);
                                }
                            }

                            if ($scope.uploadEntries.length < result.count) {
                                loop(start + result.entryList.length);
                            }
                        });
                }
            });
        } else {
            //
            $scope.importType = $stateParams.type;
            generateLinkOptions($scope.importType);
            createSheet();
        }
    })
    .controller('BulkUploadRejectModalController', function ($scope, $cookieStore, $location, $modalInstance, upload, Upload) {
        $scope.rejectUpload = function () {
            $scope.submitting = true;
            var sid = $cookieStore.get("sessionId");

            Upload(sid).updateStatus({importId:upload.id}, {id:upload.id, status:'IN_PROGRESS'}, function (result) {
                $location.path('/folders/pending');
                $modalInstance.close();
                $scope.submitting = false;
                // todo : send optional message if any
            }, function (error) {
                console.error(error);
                $scope.submitting = false;
            })
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    })
    .controller('BulkUploadModalController', function ($window, $scope, $location, $cookieStore, $routeParams, $modalInstance, $fileUploader, addType, linkedAddType) {
        var sid = $cookieStore.get("sessionId");
        $scope.addType = addType;

        var uploader = $scope.importUploader = $fileUploader.create({
//        scope: $scope, // to automatically update the html. Default: $rootScope
            url:"/rest/upload/file",
            method:'POST',
//        removeAfterUpload:true,
            headers:{"X-ICE-Authentication-SessionId":sid},
            formData:[
                { type:addType }
            ]
        });

        uploader.bind('success', function (event, xhr, item, response) {
            var info = response;
            console.log("success", response);
            $scope.modalClose = "Close";
            $scope.processing = false;
//
            if (!isNaN(response)) {
                $modalInstance.close();
                $location.path("/upload/" + response);
            } else {
                $scope.uploadError = response;
            }
        });

        uploader.bind('error', function (event, xhr, item, response) {
            console.info('Error', xhr, item, response);
            $scope.uploadError = response;
            $scope.processing = false;
        });

        uploader.bind('complete', function (event, xhr, item, response) {
            console.info('Complete', xhr, item, response);
            $scope.processing = false;
        });

        $scope.ok = function () {
            $modalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.uploadFile = function () {
            uploader.uploadAll();
        };

        // example of event binding
        uploader.bind('afteraddingfile', function (event, item) {
            console.info('After adding a file', item);
//        item.upload();
        });

        uploader.bind('progress', function (event, item, progress) {
            console.info('progress', item, progress);
            if (progress !== '100')
                return;

            $scope.processing = true;
            item.remove();
        });

        $scope.downloadCSVTemplate = function () {
            var url = "/rest/file/upload/" + $scope.addType;
            if (linkedAddType)
                url += "?link=" + linkedAddType;
            $window.open(url, "_self");
        }
    });