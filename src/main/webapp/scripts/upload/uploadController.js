'use strict';

angular.module('ice.upload.controller', ['ngFileUpload'])
    .controller('UploadController', function ($rootScope, $location, $scope, $uibModal, $resource, $http, $stateParams,
                                              FileUploader, UploadUtil, Util, Authentication, Upload) {
        let sheetData = [
            []
        ];

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type.toUpperCase();

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = []; // maintains the ids of the main entrys (row indexed)
        $scope.bulkUpload.linkedEntryIdData = []; // maintains the ids of the linked entry
        $scope.uploadNameEditMode = false;
        let linkedHeaders = undefined;
        let linkedDataSchema = undefined;
        $scope.linkedSelection = undefined;
        let linkedImportType;

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
            var ht = angular.element('#dataTable').handsontable('getInstance');

            // check if there is already a link
            if (linkedHeaders) {
                var length = UploadUtil.getSheetHeaders($scope.importType).length;
                ht.alter('remove_col', length - 1, linkedHeaders.length);
            }

            linkedImportType = $scope.linkedSelection = "Existing";
            linkedHeaders = ["Part Number"];
            linkedDataSchema = ["partId"];

            // add linkedHeaders.length number of columns after the last column
            ht.alter('insert_col', undefined, linkedHeaders.length);
        };

        //
        // creates new sheet interface
        //
        const createSheet = function () {

            let availableHeight, $window = $(window), $dataTable = $("#dataTable");
            const sheetHeaders = UploadUtil.getSheetHeaders($scope.importType);
            // headers for the current selection and initialize first row
            for (let i = 0; i < sheetHeaders.length; i += 1) {
                sheetData[0][i] = null;
            }

            $scope.uploadedFiles = {
                map: new Map([["", null]]),
                arr: [""]
            };

            $scope.upload = function (files) {
                if (!files || !files.length)
                    return;

                for (let i = 0; i < files.length; i++) {
                    const file = files[i];
                    if (file.$error) {
                        console.log("skipping file due to error", file, "error", file.$error);
                        continue;
                    }

                    if ($scope.uploadedFiles.map.get(file.name)) {
                        // todo : if file already exists then alert user
                    } else {
                        $scope.uploadedFiles.map.set(file.name, file);
                        $scope.uploadedFiles.arr.push(file.name);
                    }
                }

                if ($scope.uploadedFiles.arr.length) {
                    let fileText = "file";
                    if ($scope.uploadedFiles.arr.length > 2)
                        fileText += "s";
                    Util.setFeedback(($scope.uploadedFiles.arr.length - 1) + " " + fileText + " available");
                }
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

            const autoComplete = function (field, query, process) {
                $http.get('rest/search/filter', {
                    headers: {'X-ICE-Authentication-SessionId': Authentication.getSessionId()},
                    params: {
                        token: query,
                        field: field
                    }
                }).then(function (res) {
                    return process(res.data);
                });
            };

            const getCellProperties = function (row, col, prop) {
                let object = {};
                let fieldType;

                if (linkedImportType && col >= sheetHeaders.length) {
                    if (linkedImportType === "Existing") {
                        fieldType = "partNumber";
                    } else {
                        const newIndex = col - sheetHeaders.length;
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
                                headers: {'X-ICE-Authentication-SessionId': Authentication.getSessionId()},
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
                            callback(object.source.indexOf(value) !== -1);
                        };
                        break;

                    case 'bioSafetyLevel':
                        object.type = 'autocomplete';
                        object.source = ['1', 'Level 1', '2', 'Level 2', 'Restricted', ''];
                        object.validator = function (value, callback) {
                            callback(object.source.indexOf(value) !== -1);
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

                    case "sequenceFileName":
                    case "attachments":
                    case "sequenceTrace":
                        object.type = 'autocomplete';
                        object.strict = true;
                        object.copyable = false; // file cells cannot be copied
                        object.source = function (query, process) {
                            if ($scope.uploadedFiles.arr.length > 1)
                                return process($scope.uploadedFiles.arr);
                            else
                                alert("No files available. Drag and drop files to be able to select them")
                        };
                        object.validator = function (value, callback) {
                            if (!value || value.trim() === "")
                                callback(true);
                            else
                                callback($scope.uploadedFiles.map.get(value) !== undefined);
                        };
                        break;

                    case "harvestDate":
                        object.type = "date";
                        object.dateFormat = "MM/DD/YYYY";
                        object.correctFormat = true;
                        break;
                }

                return object;
            };

            //
            // function to display the header at the specified index. A special case is when a link
            // has been added in which case we need to check for the linked selection's headers as appropriate
            //
            const getSheetHeaders = function (index) {
                if (index >= sheetHeaders.length && $scope.linkedSelection) {
                    var newIndex = index - sheetHeaders.length;
                    return $scope.linkedSelection + " " + linkedHeaders[newIndex];
                }
                return sheetHeaders[index];
            };

            //
            // calculates the column width for each header type
            //
            const getColWidth = function (index) {
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

            const calculateSize = function () {
                var offset = $dataTable.offset();
                availableHeight = $window.height() - offset.top + $window.scrollTop();
                $dataTable.handsontable('render');
            };
            $window.on('resize', calculateSize);

            const heightFunction = function () {
                if (availableHeight === void 0) {
                    calculateSize();
                }
                return availableHeight - 67;
            };

            const isRowEmpty = function (rowData) {
                for (var col = 0; col < rowData.length; col++) {
                    var content = rowData[col];

                    if (typeof content === "string" && content.trim()) {
                        return false;
                    }
                }

                return true;
            };

            const dealWithFileField = function (row, col, filename, oldFilename) {
                let formDataType, actualEntryId, resource;

                // check if there is a link
                if ($scope.linkedSelection) {
                    // todo: sequence traces
                    formDataType = $scope.linkedSelection;
                    resource = getFieldProperty(col) === "attachments" ? "attachment" : "sequence";

                    // col determines which id we are operating with
                    if (col < UploadUtil.getSheetHeaders($scope.importType).length) {
                        // retrieve the id for the main entry
                        actualEntryId = $scope.bulkUpload.entryIdData[row];
                    } else {
                        actualEntryId = $scope.bulkUpload.linkedEntryIdData[row];
                    }
                } else {
                    resource = UploadUtil.indexToRestResource($scope.importType, col);
                    formDataType = $scope.importType;
                    actualEntryId = $scope.bulkUpload.entryIdData[row];
                }

                // actual file upload
                const uploadSelectedFile = function () {
                    let url = "rest/uploads/" + $scope.bulkUpload.id;
                    const file = $scope.uploadedFiles.map.get(filename);
                    if (!file)
                        return;

                    Upload.upload({
                        url: url + "/" + resource,
                        data: {
                            entryType: formDataType,
                            entryId: actualEntryId,
                            file: file
                        },
                        method: 'POST',
                        headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()}
                    }).then(function (resp) {
                        if (resp.status !== 200)
                            Util.setFeedback("Error uploading file", "error");
                    }, null, function (evt) {
                        // todo : on failure clear the text field for the file cell
                    });
                };

                // check if user is removing existing file
                if (oldFilename && !filename) {
                    Util.remove("rest/uploads/" + $scope.bulkUpload.id + "/entry/" + actualEntryId + "/" + resource, {});
                    return;
                }

                if (!actualEntryId) {
                    // create new entry
                    const object = getEntryObject(row, col, filename);
                    if (!object)
                        return;

                    // check if we have a bulk upload first
                    if (!$scope.bulkUpload.id) {
                        // create draft of specified type
                        Util.update("rest/uploads", {type: $scope.importType}, {}, function (result) {
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            $scope.bulkUpload.name = result.name;

                            // attempt to change url without reloading page
                            $window.history.pushState("", "Bulk Upload", "/upload/" + $scope.bulkUpload.id);

                            // then create entry and associate with draft
                            createEntry($scope.bulkUpload.id, object, row, uploadSelectedFile);
                        });
                    } else {
                        // need to pass callback function so that file can be uploaded after the entry is created
                        // but we also need the id of the newly created entry
                        createEntry($scope.bulkUpload.id, object, row, uploadSelectedFile);
                    }
                } else {
                    uploadSelectedFile();
                }
            };

            // called by the callback handler when a user edits a cell or a number of cells
            // and a save to the server is required
            // data: 4 element array [row, col, oldValue, newValue]
            //
            const createOrUpdateEntry = function (data) {
                // get col field
                const fieldType = getFieldProperty(data[1]);

                // check if it is a file upload field
                if (fieldType && UploadUtil.isFileField(fieldType)) {
                    dealWithFileField(data[0], data[1], data[3], data[2]);
                    return;
                }

                const row = data[0];

                // retrieve object at specified row
                const object = getEntryObject(row, data[1], data[3]);
                if (!object)
                    return;

                if (object.bioSafetyLevel === "Level 1")
                    object.bioSafetyLevel = 1;
                else if (object.bioSafetyLevel === "Level 2")
                    object.bioSafetyLevel = 2;
                else if (object.bioSafetyLevel === "Restricted")
                    object.bioSafetyLevel = "-1";

                $scope.saving = true;

                // first create a new upload if we are not updating an existing one
                if (!$scope.bulkUpload.id) {
                    // create draft of specified type
                    Util.update("rest/uploads", {type: $scope.importType}, {}, function (result) {
                        $scope.bulkUpload.id = result.id;
                        $scope.bulkUpload.lastUpdate = result.lastUpdate;
                        $scope.bulkUpload.name = result.name;

                        // attempt to change url without reloading page
                        $location.path("upload/" + $scope.bulkUpload.id, false);

                        // then create entry and associate with draft
                        createEntry(result.id, object, row);
                    });
                } else {
                    // check if row being updated has existing entry
                    if (!object.id) {
                        // create new entry for existing upload
                        createEntry($scope.bulkUpload.id, object, row);
                    } else if (isRowEmpty(sheetData[row])) {
                        // last field has been cleared, then delete the row
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
                                if ($scope.bulkUpload.entryIdData[row] !== updatedEntry.id) {
                                    $scope.bulkUpload.entryIdData[row] = updatedEntry.id;
                                }

                                if (updatedEntry.linkedParts && updatedEntry.linkedParts.length) {
                                    var linkedId = updatedEntry.linkedParts[0].id;
                                    if (linkedId) {
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
            const updateEntryList = function (objects) {
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
            const bulkCreateOrUpdate = function (change) {
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

            const getEntryObject = function (row, col, value) {
                // main entry
                const object = {
                    id: $scope.bulkUpload.entryIdData[row],
                    type: $scope.importType.toUpperCase(),
                    strainData: {},
                    plasmidData: {},
                    arabidopsisSeedData: {},
                    proteinData: {}
                };

                // check if there is a linked object being updated
                if ($scope.linkedSelection) {
                    // set property for linked object and add it to link
                    const newIndex = col - sheetHeaders.length;
                    // todo : same treatment as object above

                    const linkedObject = {
                        id: $scope.bulkUpload.linkedEntryIdData[row],
                        type: $scope.linkedSelection.toUpperCase(),
                        strainData: {},
                        plasmidData: {},
                        arabidopsisSeedData: {},
                        proteinData: {}
                    };

                    if ($scope.linkedSelection.toUpperCase() === "EXISTING")
                        linkedObject.partId = value;
                    else
                        UploadUtil.setDataValue($scope.linkedSelection.toUpperCase(), newIndex, linkedObject, value);

                    object.linkedParts = [linkedObject];
                } else {
                    UploadUtil.setDataValue($scope.importType.toUpperCase(), col, object, value);
                }

                object.index = row;
                return object;
            };

            //
            // callback : function to call when the entry is created successfully
            const createEntry = function (importId, object, row, callback) {
                Util.update('rest/uploads/' + importId + '/entry', object, {},
                    function (createdEntry) {
                        $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                        if (createdEntry.linkedParts && createdEntry.linkedParts.length) {
                            const linkedId = createdEntry.linkedParts[0].id;
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

            const getFieldProperty = function (col) {
                let fieldType;

                if (linkedImportType && col >= sheetHeaders.length) {
                    if (linkedImportType === "Existing") {
                        fieldType = "partNumber";
                    } else {
                        const newIndex = col - sheetHeaders.length;
                        fieldType = UploadUtil.getTypeField(linkedImportType, newIndex);
                    }
                } else
                    fieldType = UploadUtil.getTypeField($scope.importType, col);
                return fieldType;
            };

            //
            // callback for change to the sheet to save/update the information on the server
            // change: 2D array containing information about each of the edited cells [[row, prop, oldVal, newVal], ...].
            // source: String that identifies source of hook call
            //
            const afterChange = function (change, source) {
                switch (source) {
                    // "setMyData" is intended to be used when setting data in code to prevent updates
                    // data load so no need to update
                    case "loadData":
                    case "setMyData":
                    case "external":
                        return;

                    // single cell edit
                    case "edit":
                        // no changes in data values so do not respond
                        if (change[0][2] === change[0][3])
                            return;

                        // update entry with information from change array. only looking at / expecting single
                        // change array
                        createOrUpdateEntry(change[0]);
                        break;

                    // click to drag or paste
                    case "Autofill.fill":
                    case "CopyPaste.paste":
                        bulkCreateOrUpdate(change);
                        break;

                    default:
                        console.error("don't know what to do with source " + source);
                        break;
                }
            };

            const options = {
                data: sheetData,
                startRows: 50, // comes into effect only if no data is provided
                minRows: 50,
                colHeaders: getSheetHeaders,
                rowHeaders: true, // use default of 1, 2, 3 for row headers
                colWidths: getColWidth,
                minSpareRows: 1,
                enterMoves: {row: 0, col: 1}, // move right on enter instead of down
                autoWrapRow: true,
                autoWrapCol: true,
                cells: getCellProperties,
                height: heightFunction,
                afterChange: afterChange,
                manualColumnResize: true,
                columnSorting: false,
                contextMenu: true,
                fillHandle: "vertical",
                afterRemoveRow: function (row) {
                    Util.remove("rest/uploads/" + $scope.bulkUpload.id + "/entry/" + $scope.bulkUpload.entryIdData[row],
                        {});
                },
                afterSelection: function (row, col, row2, col2) {
                    var noHandleList = ['attachments', 'sequenceFileName', 'sequenceTrace'];
                    var colName;
                    var importLength = UploadUtil.getSheetHeaders($scope.importType).length;

                    if (col2 >= UploadUtil.getSheetHeaders($scope.importType).length) {
                        colName = UploadUtil.getTypeField(linkedImportType, (col2 - importLength))
                    } else {
                        colName = UploadUtil.getTypeField($scope.importType, col2);
                    }

                    if (noHandleList.indexOf(colName) !== -1)
                        $dataTable.handsontable('updateSettings', {fillHandle: false});
                    else
                        $dataTable.handsontable('updateSettings', {fillHandle: true});
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
                $uibModal.open({
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
                $uibModal.open({
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
                $uibModal.open({
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
                $uibModal.open({
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
                var requiresApproval = $scope.bulkUpload.status && $scope.bulkUpload.status === 'PENDING_APPROVAL';

                // validate the contents;
                var tmp = {id: $scope.bulkUpload.id, status: $scope.bulkUpload.status};
                if (requiresApproval)
                    tmp.status = 'APPROVED';
                else if ($scope.bulkUpload.status !== "BULK_EDIT")
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

                        $uibModal.open({
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
                            if (!result || !result.type)
                                return;

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
    .controller('BulkUploadRejectModalController', function ($scope, $location, $uibModalInstance, upload, Util) {
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
    .controller('BulkUploadModalController', function ($window, $scope, $location, $cookies, $routeParams, uploadId,
                                                       $uibModalInstance, FileUploader, addType, linkedAddType, Util) {
        $scope.addType = addType;
        $scope.processing = false;

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
                headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
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
                $scope.uploadError = {};
                if (response.userMessage)
                    $scope.uploadError.message = response.userMessage;
            }
        };

        $scope.importUploader.onErrorItem = function (item, response, status, headers) {
            $scope.processing = false;
            $scope.uploadError = {};
            if (response.userMessage)
                $scope.uploadError.message = response.userMessage;
            else
                $scope.uploadError.message = "Unknown server error";

            if (status == 400) {
                $scope.uploadError.message = "Validation error processing file \'" + item.file.name + "\'";
                if (response.userMessage)
                    $scope.uploadError.details = response.userMessage;
                $scope.uploadError.headers = response.headers;
            }
        };

        $scope.importUploader.onBeforeUploadItem = function (item) {
            $scope.processing = true;
        };

        $scope.importUploader.onCompleteItem = function (item, response, status, headers) {
            $scope.processing = false;
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
    })
    .controller('BulkUploadPermissionsController', function ($scope, $cookies, $location, $uibModalInstance,
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
                    if (permissionId === $scope.activePermissions[idx].id) {
                        i = idx;
                        break;
                    }
                }

                if (i === -1)
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

                if (result.type === 'READ_UPLOAD') {
                    $scope.readPermissions.push(result);
                    $scope.activePermissions = $scope.readPermissions;
                } else if (result.type === 'WRITE_UPLOAD') {
                    $scope.writePermissions.push(result);
                    $scope.activePermissions = $scope.writePermissions;
                }
                permission.id = result.id;
                pane.count = $scope.activePermissions.length;
            });
        };

        $scope.deletePermission = function (index, permission) {
            removePermission(permission.id);
        };
    });
