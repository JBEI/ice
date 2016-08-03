'use strict';

angular.module('ice.upload.controller', [])
    .controller('UploadController', function ($rootScope, $location, $scope, $uibModal, $cookieStore, $resource,
                                              $stateParams, FileUploader, $http, UploadUtil, Util) {
        var sid = $cookieStore.get("sessionId");
        //var upload = Upload(sid);
        var sheetData = [
            []
        ];

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type.toUpperCase();

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = []; // maintains the ids of the main entrys (row indexed)
        $scope.bulkUpload.linkedEntryIdData = []; // maintains the ids of the linked entry
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
            var ht = angular.element('#dataTable').handsontable('getInstance');

            // check if there is already a link
            if (linkedHeaders) {
                var length = UploadUtil.getSheetHeaders($scope.importType).length;
                ht.alter('remove_col', length - 1, linkedHeaders.length);
            }

            linkedImportType = type;
            $scope.linkedSelection = type.charAt(0).toUpperCase() + type.substring(1);
            linkedHeaders = UploadUtil.getSheetHeaders(type);
            linkedDataSchema = UploadUtil.getDataSchema(type);

            // add linkedHeaders.length columns after the last column
            ht.alter('insert_col', undefined, linkedHeaders.length);
        };

        //
        // add a part_id column to enable linking to existing entries
        //
        $scope.addExistingPart = function () {
            linkedImportType = $scope.linkedSelection = "Existing";
            var ht = angular.element('#dataTable').handsontable('getInstance');

            // check if there is already a link
            if (linkedHeaders) {
                var length = UploadUtil.getSheetHeaders($scope.importType).length;
                ht.alter('remove_col', length - 1, linkedHeaders.length);
            }

            linkedHeaders = ["Part Number"];
            linkedDataSchema = ["partId"];
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

            xhr.onload = function () {
                var response = JSON.parse(xhr.responseText);

                if (response && response.filename) {
                    var ht = angular.element('#dataTable').handsontable('getInstance')
                    sheetData[row][col] = response.filename;
                    ht.setDataAtCell(row, col, response.filename, 'loadData');
                } else {
                    Util.setFeedback("Error uploading file", "error");
                }
            };

            xhr.onerror = function () {
                // error
                console.log("error uploading");
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
        // creates new sheet interface
        //
        var createSheet = function () {

            //
            // handles file uploads when user selects a file in the bulk upload interface
            //
            $scope.onFileSelect = function (files, row, col) {
                var uploadFile = function () {
                    var id = $scope.bulkUpload.id;
                    var file = files[0];
                    var url = "rest/uploads/" + id + "/";
                    var formDataType;
                    var actualEntryId;

                    // check if there is a link
                    if ($scope.linkedSelection) {
                        var sheetHeaders = UploadUtil.getSheetHeaders($scope.importType);
                        var index = col - sheetHeaders.length;
                        formDataType = $scope.linkedSelection;
                        url += UploadUtil.indexToRestResource(formDataType, index);
                        actualEntryId = $scope.bulkUpload.linkedEntryIdData[row];
                    } else {
                        url += UploadUtil.indexToRestResource($scope.importType, col);
                        formDataType = $scope.importType;
                        actualEntryId = $scope.bulkUpload.entryIdData[row];
                    }

                    var item = {
                        method: 'POST',
                        url: url,
                        file: file,
                        alias: "file",
                        formData: [
                            {entryType: formDataType, entryId: actualEntryId}
                        ],
                        headers: {"X-ICE-Authentication-SessionId": sid}
                    };

                    transport(item, row, col);
                };

                createOrUpdateEntry([row, col], uploadFile);
            };

            // delete file in row, col
            $scope.onFileDelete = function (row, col) {
                var entryId = $scope.bulkUpload.entryIdData[row];
                var id = $scope.bulkUpload.id;

                if (UploadUtil.indexToRestResource($scope.importType, col) === "attachment") {
                    Util.remove("rest/uploads/" + id + "/entry/" + entryId + "/attachment", {}, function (result) {
                        sheetData[row][col] = undefined;
                        ht.setDataAtCell(row, col, undefined, 'loadData');
                    });
                } else {
                    //delete sequence
                    Util.remove("'rest/uploads/" + id + "/entry/" + entryId + "/sequence", {}, function (result) {
                        sheetData[row][col] = undefined;
                        ht.setDataAtCell(row, col, undefined, 'loadData');
                    });
                }
            };

            var availableHeight, $window = $(window), $dataTable = $("#dataTable");

            //
            // cell renderer for file upload
            //
            var fileUploadRenderer = function (instance, td, row, col, prop, value, cellProperties) {
                if (value) {
                    var $del = $('<i class="fa fa-trash-o delete_icon"></i>');
                    $del.on("click", function (event) {
                        angular.element(this).scope().onFileDelete(row, col);
                    });
                    $(td).empty().append(value).append("&nbsp;").append($del);
                } else {
                    var $up = $('<span class="fileUpload"><i class="fa fa-upload opacity_hover opacity_4"></i> Upload '
                        + '<input type="file" class="upload" /></span>');

                    $up.on("change", function (event) {
                        angular.element(this).scope().onFileSelect(this.getElementsByTagName("input")[0].files, row, col);
                    });

                    $(td).empty().append($up);
                }
                return td;
            };

            var autoComplete = function (field, query, process) {
                $http.get('rest/search/filter', {
                    headers: {'X-ICE-Authentication-SessionId': sid},
                    params: {
                        token: query,
                        field: field
                    }
                }).then(function (res) {
                    return process(res.data);
                });
            };

            var getCellProperties = function (row, col, prop) {
                var object = {};
                var fieldType;

                if (linkedImportType && col >= sheetHeaders.length) {
                    if (linkedImportType === "Existing")
                        fieldType = "partNumber";
                    else {
                        var newIndex = col - sheetHeaders.length;
                        fieldType = UploadUtil.getTypeField(linkedImportType, newIndex);
                    }
                } else
                    fieldType = UploadUtil.getTypeField($scope.importType, col);

                switch (fieldType) {
                    case 'partNumber':
                        object.type = 'autocomplete';
                        object.strict = true;
                        object.source = function (query, process) {
                            $http.get('rest/uploads/partNumbers', {
                                headers: {'X-ICE-Authentication-SessionId': sid},
                                params: {
                                    token: query,
                                    // field: field
                                    type: $scope.importType.toUpperCase()
                                }
                            }).then(function (res) {
                                console.log(res, process);
                                return process(res.data);
                            });
                        };
                        break;

                    case 'circular':
                    case 'sentToAbrc':
                        object.type = 'checkbox';
                        break;

                    case 'status':
                        object.type = 'autocomplete';
                        object.source = ['Complete', 'In Progress', 'Planned', ''];
                        object.allowInvalid = false;
                        object.validator = function (value, callback) {
                            callback(object.source.indexOf(value) != -1);
                        };
                        break;

                    case 'bioSafetyLevel':
                        object.type = 'autocomplete';
                        object.source = ['1', '2', ''];
                        object.validator = function (value, callback) {
                            callback(object.source.indexOf(value) != -1);
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

                    case "harvestDate":
                        object.type = "date";
                        object.dateFormat = "MM/DD/YYYY";
                        object.correctFormat = true;
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
                var fieldType;

                if (linkedImportType) {
                    var newIndex = index - sheetHeaders.length;
                    fieldType = UploadUtil.getTypeField(linkedImportType, newIndex);
                } else
                    fieldType = UploadUtil.getTypeField($scope.importType, index);

                switch (fieldType) {
                    case "circular":
                        return 60;

                    default:
                        return 150;
                }
            };

            var calculateSize = function () {
                var offset = $dataTable.offset();
                availableHeight = $window.height() - offset.top + $window.scrollTop();
                $dataTable.handsontable('render');
            };

            var heightFunction = function () {
                if (availableHeight === void 0) {
                    calculateSize();
                }
                return availableHeight - 67;
            };

            $window.on('resize', calculateSize);

            var isRowEmpty = function (rowData) {
                for (var col = 0; col < rowData.length; col++) {
                    var content = rowData[col];

                    if (typeof content === "string" && content.trim()) {
                        return false;
                    }
                }

                return true;
            };

            //
            // called by the callback handler when a user edits a cell or a number of cells
            // and a save to the server is required
            //
            var createOrUpdateEntry = function (data, callback) {
                var row = data[0];
                var object = getEntryObject(row, data[1], data[3]);
                if (!object)
                    return;

                $scope.saving = true;
                if ($scope.bulkUpload.id === undefined) {
                    // create draft of specified type
                    Util.update("rest/uploads", {type: $scope.importType}, {}, function (result) {
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $scope.bulkUpload.name = result.name;

                        console.log($scope.bulkUpload.id);
                        $location.path("upload/" + $scope.bulkUpload.id, false);
                        // then create entry and associate with draft
                        createEntry(result.id, object, row, callback);
                    });
                } else {
                    // check if row being updated has existing entry
                    if (!object['id']) {
                        // create new entry for existing upload
                        createEntry($scope.bulkUpload.id, object, row, callback);
                    } else if (isRowEmpty(sheetData[row])) {
                        Util.remove('rest/uploads/' + $scope.bulkUpload.id + '/entry/' + $scope.bulkUpload.entryIdData[row], {}, function (result) {
                            $scope.saving = false;
                        }, function (error) {
                            $scope.saving = false;
                        });
                    } else {
                        // update entry for existing upload
                        Util.post('rest/uploads/' + $scope.bulkUpload.id + '/entry/' + object.id, object,
                            function (updatedEntry) {
                                $scope.bulkUpload.lastUpdate = updatedEntry.modificationTime;

                                // todo : this will be an actual problem if there is a different value; undefined is ok
                                if ($scope.bulkUpload.entryIdData[row] != updatedEntry.id) {
                                    $scope.bulkUpload.entryIdData[row] = updatedEntry.id;
                                }

                                if (updatedEntry.linkedParts && updatedEntry.linkedParts.length) {
                                    var linkedId = updatedEntry.linkedParts[0].id;
                                    if (linkedId) {
                                        $scope.bulkUpload.linkedEntryIdData[row] = linkedId;
                                    }
                                }
                                $scope.saving = false;
                                if (callback)
                                    callback();
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

                Util.update('rest/uploads/' + $scope.bulkUpload.id, {entryList: entryList}, {}, function (success) {
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

            // bulk create or update from auto-fill or paste
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
                    Util.update('rest/uploads', {type: $scope.importType}, {}, function (result) {
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $scope.bulkUpload.name = result.name;
                        $location.path("upload/" + $scope.bulkUpload.id, false);

                        // then update the list
                        updateEntryList(objects);
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

                var object = {
                    id: entryIdDataIndex,
                    type: $scope.importType.toUpperCase(),
                    strainData: {},
                    plasmidData: {},
                    arabidopsisSeedData: {}
                };

                // check if there is a linked object being updated
                if (col >= sheetHeaders.length) {   // or if ($scope.linkedSelection?
                    var linkedEntryIdDataIndex = $scope.bulkUpload.linkedEntryIdData[row];

                    if (value === "" && !linkedEntryIdDataIndex)
                        return undefined;

                    // set property for linked object and add it to link
                    var newIndex = col - sheetHeaders.length;
                    // todo : same treatment as object above
                    var linkedObject = {
                        id: linkedEntryIdDataIndex,
                        type: $scope.linkedSelection.toUpperCase(),
                        strainData: {},
                        plasmidData: {},
                        arabidopsisSeedData: {}
                    };

                    if ($scope.linkedSelection.toUpperCase() == "EXISTING")
                        linkedObject.partId = value;
                    else
                        linkedObject = UploadUtil.setDataValue($scope.linkedSelection.toUpperCase(), newIndex, linkedObject, value);
                    object.linkedParts = [linkedObject];
                } else {
                    object = UploadUtil.setDataValue($scope.importType.toUpperCase(), col, object, value);
                }

                object.index = row;
                return object;
            };

            var createEntry = function (importId, object, row, callback) {
                Util.update('rest/uploads/' + importId + '/entry', object, {},
                    function (createdEntry) {
                        $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                        if (createdEntry.linkedParts && createdEntry.linkedParts.length) {
                            var linkedId = createdEntry.linkedParts[0].id;
                            if (linkedId) {
                                $scope.bulkUpload.linkedEntryIdData[row] = linkedId;
                            }
                        }

                        $scope.saving = false;
                        if (callback)
                            callback();
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
                data: sheetData,
                startRows: 50, // comes into effect only if no data is provided
                minRows: 50,
                colHeaders: getSheetHeaders,
                rowHeaders: true, // use default of 1, 2, 3 for row headers
                colWidths: getColWidth,
                //stretchH: 'all',
                minSpareRows: 1,
                enterMoves: {row: 0, col: 1}, // move right on enter instead of down
                autoWrapRow: true,
                autoWrapCol: true,
                cells: getCellProperties,
                //width: widthFunction,
                height: heightFunction,
                afterChange: afterChange,
                manualColumnResize: true,
                columnSorting: false,
                contextMenu: true,
                afterRemoveRow: function (row) {
                    Util.remove("rest/uploads/" + $scope.bulkUpload.id + "/entry/" + $scope.bulkUpload.entryIdData[row],
                        {});
                }
            };

            $dataTable.handsontable(options);
            var ht = $dataTable.handsontable('getInstance');
            ht.updateSettings({
                contextMenu: {
                    items: {
                        "remove_row": {
                            name: 'Remove row'
                        }
                    }
                }
            });
            $scope.spreadSheet = $dataTable.data('handsontable');

            $scope.fileUploadModal = function () {
                var modalInstance = $uibModal.open({
                    templateUrl: 'scripts/upload/modal/file-upload.html',
                    controller: 'BulkUploadModalController',
                    backdrop: 'static',
                    resolve: {
                        addType: function () {
                            return $scope.importType;
                        },

                        linkedAddType: function () {
                            return $scope.linkedSelection;
                        },

                        uploadId: function () {
                            return $scope.bulkUpload.id;
                        }
                    }
                });
            };

            $scope.confirmResetFormModal = function () {
                var resetModalInstance = $uibModal.open({
                    templateUrl: 'scripts/upload/modal/reset-bulk-upload-sheet.html',
                    controller: 'BulkUploadModalController',
                    backdrop: 'static',
                    resolve: {
                        addType: function () {
                            return $scope.importType;
                        },

                        linkedAddType: function () {
                            return $scope.linkedSelection;
                        },

                        uploadId: function () {
                            return $scope.bulkUpload.id;
                        }
                    }
                });
            };

            $scope.confirmRejectUploadModal = function () {
                var resetModalInstance = $uibModal.open({
                    templateUrl: 'scripts/upload/modal/reject-upload.html',
                    controller: 'BulkUploadRejectModalController',
                    backdrop: 'static',
                    resolve: {
                        upload: function () {
                            return $scope.bulkUpload;
                        }
                    }
                });
            };

            $scope.setPermissionsModal = function () {
                var modelInstance = $uibModal.open({
                    templateUrl: 'scripts/upload/modal/permissions.html',
                    controller: 'BulkUploadPermissionsController',
                    backdrop: 'static',
                    resolve: {
                        upload: function () {
                            return $scope.bulkUpload;
                        }
                    }
                })
            };

            $scope.submitImportForApproval = function () {
                $scope.submitting = true;
                var requiresApproval = $scope.bulkUpload.status && $scope.bulkUpload.status == 'PENDING_APPROVAL';

                // validate the contents;
                var tmp = {id: $scope.bulkUpload.id, status: $scope.bulkUpload.status};
                if (requiresApproval)
                    tmp.status = 'APPROVED';
                else if ($scope.bulkUpload.status != "BULK_EDIT")
                    tmp.status = 'PENDING_APPROVAL';
                $scope.requestError = undefined;

                Util.update("rest/uploads/" + $scope.bulkUpload.id + "/status", tmp, {},
                    function (result) {
                        if (result.success) {
                            $scope.submitting = false;
                            $location.path('/folders/personal');
                        }
                    }, function (error) {
                        $scope.requestError = error.data;
                        $scope.submitting = false;

                        var resetModalInstance = $uibModal.open({
                            templateUrl: 'scripts/upload/modal/upload-submit-alert.html',
                            controller: function ($scope, msg, isError) {
                                $scope.requestError = msg;
                            },
                            backdrop: 'static',
                            resolve: {
                                msg: function () {
                                    return $scope.requestError;
                                },

                                isError: function () {
                                    return true;
                                }
                            }
                        });
                    });
            };

            $scope.showBulkUploadRenameModal = function () {
                var modalInstance = $uibModal.open({
                    templateUrl: 'scripts/upload/modal/rename-bulk-upload-sheet.html',
                    controller: function ($scope, $uibModalInstance, uploadName) {
                        $scope.newBulkUploadName = uploadName;
                    },
                    backdrop: 'static',
                    resolve: {
                        uploadName: function () {
                            return $scope.bulkUpload.name;
                        }
                    }
                });

                modalInstance.result.then(function (newName) {
                    // update name on the server if a bulk upload has already been created
                    if ($scope.bulkUpload.id) {
                        var tmp = {id: $scope.bulkUpload.id, name: newName};
                        Util.update('rest/uploads/' + $scope.bulkUpload.id + '/name', tmp, {}, function (result) {
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

        // retrieve the contents of an import if parameter is a number
        if (!isNaN($stateParams.type)) {
            $scope.importType = undefined;
            asyncLoop({
                functionToLoop: function (loop, start) {
                    Util.get("rest/uploads/" + $stateParams.type,
                        function (result) {
                            $scope.bulkUpload.name = result.name;
                            $scope.bulkUpload.status = result.status;
                            $scope.importType = result.type.toLowerCase();
                            $scope.linkOptions = UploadUtil.generateLinkOptions($scope.importType);

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
                                        var linkType = linkedPart.type.toLowerCase();

                                        // check if there is a linked type and the link on the ui has not been created
                                        if (linkedDataSchema === undefined || linkedDataSchema.length === 0) {
                                            if (linkedPart.visible === "OK")
                                                $scope.addExistingPart();
                                            else
                                                $scope.addNewPartLink(linkType);
                                        }

                                        $scope.bulkUpload.linkedEntryIdData.push(linkedPart.id);

                                        // linkedDataSchema is created when addNewPartLink is called
                                        var dataSchemaLength = dataSchema.length;
                                        for (var k = 0; k < linkedDataSchema.length; k += 1) {
                                            if ($scope.linkedSelection.toLowerCase() === "existing") {
                                                val = linkedPart.partId;
                                            } else {
                                                val = UploadUtil.getEntryValue(linkType, linkedPart, k);

                                                if (val === undefined)
                                                    val = '';

                                                // currently for attachments only
                                                if (val instanceof Array && linkedDataSchema[k] === "attachments") {
                                                    if (val.length) {
                                                        val = val[0].filename;
                                                    } else {
                                                        val = ""
                                                    }
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
                            angular.element("#dataTable").handsontable('render');
                        }, {offset: start, limit: 40});
                }
            });
        } else {
            //
            $scope.importType = $stateParams.type;
            $scope.linkOptions = UploadUtil.generateLinkOptions($scope.importType);
            createSheet();
        }
    })
    .controller('BulkUploadRejectModalController', function ($scope, $cookieStore, $location, $uibModalInstance,
                                                             upload, Util) {
        $scope.rejectUpload = function () {
            $scope.submitting = true;

            Util.update('rest/uploads/' + upload.id + '/status', {
                id: upload.id,
                status: 'IN_PROGRESS'
            }, function (result) {
                $location.path('/folders/pending');
                $uibModalInstance.close();
                $scope.submitting = false;
            }, function (error) {
                $scope.submitting = false;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    })
    .controller('BulkUploadModalController', function ($window, $scope, $location, $cookieStore, $routeParams, uploadId,
                                                       $uibModalInstance, FileUploader, addType, linkedAddType, Util) {
        var sid = $cookieStore.get("sessionId");
        $scope.addType = addType;

        //
        // reset the current bulk upload. involves deleting all entries and showing user new upload form
        //
        $scope.resetBulkUpload = function () {
            // expected folders that can be deleted have type "PRIVATE" and "UPLOAD"
            Util.remove("rest/folders/" + uploadId, {folderId: uploadId, type: "UPLOAD"}, function () {
                $location.path("/upload/" + addType);
                $uibModalInstance.dismiss('cancel');
            }, function (error) {
                console.error(error);
            });
        };

        $scope.retryUpload = function () {
            $scope.uploadError = undefined;
            createUploader();
        };

        var createUploader = function () {
            if ($scope.importUploader) {
                $scope.importUploader.cancelAll();
                $scope.importUploader.clearQueue();
                $scope.importUploader.destroy();
            }

            $scope.importUploader = new FileUploader({
                url: "rest/uploads/file",
                method: 'POST',
                removeAfterUpload: true,
                headers: {"X-ICE-Authentication-SessionId": sid},
                formData: [
                    {type: addType}
                ]
            });
        };

        createUploader();

        $scope.importUploader.onSuccessItem = function (item, response, status, headers) {
            $scope.modalClose = "Close";
            $scope.processing = false;
            if (response.success && response.uploadInfo.id) {
                $uibModalInstance.close();
                $location.path("upload/" + response.uploadInfo.id);
            } else {
                $scope.uploadError = "Unknown server error";
            }
        };

        $scope.importUploader.onErrorItem = function (item, response, status, headers) {
            $scope.processing = false;
            $scope.uploadError = response;

            if (status == 400) {
                $scope.uploadError.message = "Validation error processing file \'" + item.file.name + "\'";
            } else {
                $scope.uploadError.message = "Unknown server error";
            }
        };

        $scope.importUploader.onCompleteItem = function (item, response, status, headers) {
            $scope.processing = false;
        };

        $scope.importUploader.onProgressItem = function (event, item, progress) {
            if (progress !== '100')
                return;

            $scope.processing = true;
            item.remove();
        };

        $scope.ok = function () {
            $uibModalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.downloadCSVTemplate = function () {
            var url = "rest/file/upload/" + $scope.addType;
            if (linkedAddType)
                url += "?link=" + linkedAddType;
            $window.open(url, "_self");
        }
    }).controller('BulkUploadPermissionsController', function ($scope, $cookieStore, $location, $uibModalInstance,
                                                               upload, Util) {
        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        // init
        var panes = $scope.panes = [];
        $scope.readPermissions = [];
        $scope.writePermissions = [];
        $scope.upload = upload;
        $scope.userFilterInput = undefined;

        // if upload.id exists, then retrieve the permissions for the upload
        if (upload.id) {
            Util.list('rest/uploads/' + upload.id + '/permissions', function (result) {
                angular.forEach(result, function (item) {
                    if (item.type == 'WRITE_UPLOAD')
                        $scope.writePermissions.push(item);
                    else if (item.type == 'READ_UPLOAD')
                        $scope.readPermissions.push(item);
                });

                $scope.panes.push({title: 'Read', count: $scope.readPermissions.length, selected: true});
                $scope.panes.push({title: 'Write', count: $scope.writePermissions.length});
                $scope.activePermissions = $scope.readPermissions;

            });
        }

        $scope.activateTab = function (pane) {
            angular.forEach(panes, function (pane) {
                pane.selected = false;
            });
            pane.selected = true;
            if (pane.title === 'Read')
                $scope.activePermissions = $scope.readPermissions;
            else
                $scope.activePermissions = $scope.writePermissions;

            angular.forEach($scope.users, function (item) {
                for (var i = 0; i < $scope.activePermissions.length; i += 1) {
                    item.selected = (item.id !== undefined && item.id === $scope.activePermissions[i].articleId);
                }
            });
        };

        $scope.closeModal = function () {
            $uibModalInstance.close('cancel'); // todo : pass object to inform if folder is shared or cleared
        };

        $scope.showAddPermissionOptionsClick = function () {
            $scope.showPermissionInput = true;
        };

        $scope.closePermissionOptions = function () {
            $scope.users = undefined;
            $scope.showPermissionInput = false;
        };

        var removePermission = function (permissionId) {
            Util.remove('rest/uploads/' + $scope.upload.id + '/permissions/' + permissionId, {}, function (result) {
                if (!result)
                    return;
                // check which pane is selected
                var pane;
                if ($scope.panes[0].selected)
                    pane = $scope.panes[0];
                else
                    pane = $scope.panes[1];

                var i = -1;

                for (var idx = 0; idx < $scope.activePermissions.length; idx += 1) {
                    if (permissionId == $scope.activePermissions[idx].id) {
                        i = idx;
                        break;
                    }
                }

                if (i == -1)
                    return;

                $scope.activePermissions.splice(i, 1);
                pane.count = $scope.activePermissions.length;
            });
        };

        $scope.addRemovePermission = function (permission) {
            permission.selected = !permission.selected;
            if (!permission.selected) {
                removePermission(permission.id);
                return;
            }

            // add permission
            var pane;
            for (var i = 0; i < panes.length; i += 1) {
                if (panes[i].selected) {
                    permission.type = panes[i].title.toUpperCase() + "_UPLOAD";
                    pane = panes[i];
                    break;
                }
            }

            // todo : if no upload identified
            Util.post('rest/uploads/' + $scope.upload.id + '/permissions', permission, function (result) {
                if (!result) {
                    console.error("Permission creation error");
                    return;
                }

                console.log(result);
                if (result.type == 'READ_UPLOAD') {
                    $scope.readPermissions.push(result);
                    $scope.activePermissions = $scope.readPermissions;
                } else if (result.type == 'WRITE_UPLOAD') {
                    $scope.writePermissions.push(result);
                    $scope.activePermissions = $scope.writePermissions;
                }
                permission.id = result.id;
                pane.count = $scope.activePermissions.length;
            });
        };

        $scope.enablePublicRead = function (folder) {
        };

        $scope.disablePublicRead = function (folder) {
        };

        $scope.deletePermission = function (index, permission) {
            removePermission(permission.id);
        };

        $scope.filter = function (val) {
            if (!val) {
                $scope.accessPermissions = undefined;
                return;
            }

            $scope.filtering = true;
            // todo
            //Permission().filterUsersAndGroups({limit: 10, val: val},
            //    function (result) {
            //        $scope.accessPermissions = result;
            //        $scope.filtering = false;
            //    }, function (error) {
            //        console.error(error);
            //        $scope.filtering = false;
            //        $scope.accessPermissions = undefined;
            //    });
        };
    });