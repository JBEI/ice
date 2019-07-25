'use strict';

angular.module('ice.upload.controller', ['ngFileUpload'])
    .controller('UploadController', function ($rootScope, $location, $scope, $uibModal, $resource, $http, $stateParams,
                                              FileUploader, UploadUtil, Util, Authentication, Upload, EntryService) {
        let sheetData = [
            []
        ];

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type.toUpperCase();

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = [];         // maintains the ids of the main entrys (row indexed)
        $scope.bulkUpload.linkedEntryIdData = [];   // maintains the ids of the linked entry
        $scope.uploadNameEditMode = false;
        $scope.linkedSelection = undefined;
        let partTypeDefault;
        let linkedPartTypeDefault;
        const FILE_FIELDS_COUNT = 3;

        $scope.setNameEditMode = function (value) {
            $scope.uploadNameEditMode = value;
        };

        //
        // add link of specified type to selected bulk upload
        // this will be allowed as long as the user has not entered any data into the linked portion
        //
        $scope.addNewPartLink = function (type) {
            // todo : if there is already a link
            if ($scope.linkedSelection)
                return;

            // todo : if $scope.bulkUpload.id has not been created yet
            Util.update("rest/uploads/" + $scope.bulkUpload.id + "/link/" + type, {}, {}, function (linkedDefaults) {
                let ht = angular.element('#dataTable').handsontable('getInstance');
                $scope.linkedSelection = type;

                // add linkedHeaders.length number of columns after the last column
                ht.alter('insert_col', undefined, linkedDefaults.fields.length);
            });
        };

        //
        // add a part_id column to enable linking to existing entries
        //
        $scope.addExistingPart = function () {
            // todo : if there is already a link
            // if( $scope.linkedSelection)

            // todo : if $scope.bulkUpload.id has not been created yet
            Util.update("rest/uploads/" + $scope.bulkUpload.id + "/link/existing", {}, {}, function () {
                let ht = angular.element('#dataTable').handsontable('getInstance');
                $scope.linkedSelection = "Existing";

                // add linkedHeaders.length number of columns after the last column
                ht.alter('insert_col', undefined, 1);
            });
        };

        //
        // creates new sheet interface
        //
        const createSheet = function () {
            let availableHeight, $window = $(window), $dataTable = $("#dataTable");
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
                        // add files to available set
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
                let entryId;
                let id = $scope.bulkUpload.id;

                let restEndPoint;
                if (col < partTypeDefault.fields.length) {
                    entryId = $scope.bulkUpload.entryIdData[row];
                    restEndPoint = UploadUtil.indexToRestResource(col - partTypeDefault.fields.length);
                } else {
                    if ($scope.linkedSelection) {
                        entryId = $scope.bulkUpload.linkedEntryIdData[row];
                        const index = col - (partTypeDefault.fields.length + FILE_FIELDS_COUNT) - linkedPartTypeDefault.fields.length;
                        restEndPoint = UploadUtil.indexToRestResource(index);
                    }
                }

                Util.remove("rest/uploads/" + id + "/entry/" + entryId + "/" + restEndPoint, {}, function () {
                    sheetData[row][col] = undefined;
                    $dataTable.handsontable('getInstance').setDataAtCell(row, col, undefined, 'loadData');
                });
            };

            // provide auto complete suggestions for user from backend
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

            const getCellProperties = function (row, col) {
                let field = partTypeDefault.fields[col];
                if (!field) {
                    const fileFieldIndex = col - partTypeDefault.fields.length;
                    if (fileFieldIndex < FILE_FIELDS_COUNT) {
                        field = {inputType: "file"};
                    } else {
                        // deal with existing linked entries
                        if ($scope.linkedSelection && $scope.linkedSelection.toLowerCase() === "existing") {
                            const cellProperties = {};
                            cellProperties.type = 'autocomplete';
                            cellProperties.strict = true;
                            cellProperties.source = function (query, process) {
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
                        } else {
                            // deal with linked entries
                            const index = col - (FILE_FIELDS_COUNT + partTypeDefault.fields.length);
                            field = linkedPartTypeDefault.fields[index];

                            if (!field) {
                                const linkedFileFieldIndex = index - linkedPartTypeDefault.fields.length;
                                if (linkedFileFieldIndex < FILE_FIELDS_COUNT) {
                                    field = {inputType: "file"}
                                }
                            } else {
                                return UploadUtil.getCellProperties(field, autoComplete, $scope.uploadedFiles);
                            }
                        }
                    }
                }

                return UploadUtil.getCellProperties(field, autoComplete, $scope.uploadedFiles);
            };

            // Callback
            // function to display the header at the specified index. A special case is when a link
            // has been added in which case we need to check for the linked selection's headers as appropriate
            //
            const getSheetHeaders = function (index) {
                const mainHeadersSize = partTypeDefault.fields.length + FILE_FIELDS_COUNT;

                if (index < mainHeadersSize) {
                    let headerString = UploadUtil.getHeaderForIndex(partTypeDefault.fields, index);
                    if (!headerString) {
                        console.log("Could not retrieve header string for index " + index);
                        return "";
                    }
                    return headerString;
                } else {
                    // dealing with link
                    if (!$scope.linkedSelection) {
                        console.log("Could not retrieve header string for index " + index);
                        return "";
                    }

                    if ($scope.linkedSelection.toLowerCase() === "existing")
                        return "Part Number";

                    const linkIndex = index - mainHeadersSize;
                    let headerString = UploadUtil.getHeaderForIndex(linkedPartTypeDefault.fields, linkIndex);
                    if (!headerString) {
                        console.log("Could not retrieve header string for index " + linkIndex);
                        return "";
                    }
                    return headerString;
                }
            };

            // row headers for the sheet. Starts at "1". Header is bold if the row has an entry
            const getRowHeaders = function (index) {
                const rowHeader = index + 1;
                if ($scope.bulkUpload.entryIdData[index] > 0 || $scope.bulkUpload.linkedEntryIdData[index] > 0)
                    return "<b>" + rowHeader + "</b>";

                return "<span class='text-muted'>" + rowHeader + "</span>";
            };

            // calculates the column width for each header type
            const getColWidth = function (index) {
                const mainHeadersSize = partTypeDefault.fields.length + FILE_FIELDS_COUNT;
                if (index < mainHeadersSize)
                    return UploadUtil.getColumnWidth(partTypeDefault.fields, index);

                return UploadUtil.getColumnWidth(linkedPartTypeDefault.fields, (index - mainHeadersSize));
            };

            const calculateSize = function () {
                let offset = $dataTable.offset();
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
                for (let col = 0; col < rowData.length; col++) {
                    let content = rowData[col];

                    if (typeof content === "string" && content.trim()) {
                        return false;
                    }
                }

                return true;
            };

            // todo :
            const dealWithFileField = function (row, col, filename, oldFilename) {
                let formDataType, actualEntryId, resource;

                // check if there is a link
                if ($scope.linkedSelection) {
                    formDataType = $scope.linkedSelection;

                    let index;
                    if (col < partTypeDefault.fields.length + FILE_FIELDS_COUNT) {
                        index = col - partTypeDefault.fields.length;
                        actualEntryId = $scope.bulkUpload.entryIdData[row];
                    } else {
                        index = col - (partTypeDefault.fields.length + FILE_FIELDS_COUNT) - linkedPartTypeDefault.fields.length;
                        actualEntryId = $scope.bulkUpload.linkedEntryIdData[row];
                    }
                    resource = UploadUtil.indexToRestResource(index);
                } else {
                    resource = UploadUtil.indexToRestResource(col - partTypeDefault.fields.length);
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

            //
            // called by the callback handler when a user edits a cell or a number of cells
            // and a save to the server is required
            // data: 4 element array [row, col, oldValue, newValue]
            //
            const createOrUpdateEntry = function (data) {

                // check if it is a file upload field
                const col = data[1];
                if (col > partTypeDefault.fields.length && col < partTypeDefault.fields.length + FILE_FIELDS_COUNT) {
                    dealWithFileField(data[0], data[1], data[3], data[2]);
                    return;
                }
                // todo : linked

                const row = data[0];

                // retrieve object at specified row
                let object = getEntryObject(row, data[1], data[3]);
                if (!object)
                    return;

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
                        Util.remove('rest/uploads/' + $scope.bulkUpload.id + '/entry/' + $scope.bulkUpload.entryIdData[row], {}, function () {
                            $scope.saving = false;
                        }, function (error) {
                            console.error(error);
                            $scope.saving = false;
                        });
                    } else {
                        // update entry for existing upload
                        // check if we are updating main or linked
                        if (col >= partTypeDefault.fields.length + FILE_FIELDS_COUNT)
                            object = object.linkedParts[0];

                        Util.post('rest/uploads/' + $scope.bulkUpload.id + '/entry/' + object.id, object,
                            function (updatedEntry) {
                                $scope.bulkUpload.lastUpdate = updatedEntry.modificationTime;
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

            // upload set of entries associated with an upload, in bulk
            const updateEntryList = function (objects) {
                if ($scope.bulkUpload.id === undefined) {
                    console.error("cannot update upload list. no bulk upload object"); // todo : need to create?
                    return;
                }

                let entryList = [];
                for (let idx = 0; idx < objects.length; idx += 1) {
                    let o = objects[idx];
                    if (!o)
                        continue;

                    entryList[entryList.length] = UploadUtil.cleanBSL(o);
                }

                Util.update('rest/uploads/' + $scope.bulkUpload.id, {entryList: entryList}, {}, function (success) {
                    for (let j = 0; j < success.entryList.length; j += 1) {
                        let part = success.entryList[j];

                        $scope.bulkUpload.entryIdData[part.index] = part.id;
                        if (part.linkedParts && part.linkedParts.length) {
                            let linkedId = part.linkedParts[0].id;
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
            // change = [[row, col, oldValue, newValue], [....]] {array of arrays}
            const bulkCreateOrUpdate = function (change) {
                $scope.saving = true;

                // array of objects that will be created or updated
                let objects = [];
                let index = 0;

                for (let i = 0; i < change.length; i += 1) {
                    let data = change[i];

                    let row = data[0];
                    let col = data[1];

                    let fields;
                    if (col < partTypeDefault.fields + FILE_FIELDS_COUNT) {
                        fields = partTypeDefault.fields;
                    } else {
                        fields = linkedPartTypeDefault.fields;
                        col = col - (partTypeDefault.fields + FILE_FIELDS_COUNT);
                    }

                    if (UploadUtil.isFileColumn(fields, col)) {
                        console.log("skipping file col", data);
                        continue;
                    }

                    const object = getEntryObject(row, data[1], data[3]);
                    object.index = row;
                    objects[index++] = object;
                }

                if (objects.length === 0)
                    return;

                if ($scope.bulkUpload.id === undefined) {
                    // first create bulk upload object
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
                // $dataTable.handsontable('getInstance').render();
            };

            const getEntryObject = function (row, col, value) {
                // get the main object
                let object = UploadUtil.createPartObject($scope.bulkUpload.entryIdData[row], $scope.importType);

                // determine if we are updating the main or link (using col)
                if (col < partTypeDefault.fields.length) {
                    // updating main entry
                    UploadUtil.setDataValue($scope.importType.toUpperCase(), col, object, value, partTypeDefault);
                } else if ($scope.linkedSelection) {
                    // updating linked entry
                    const linkedObject = UploadUtil.createPartObject($scope.bulkUpload.linkedEntryIdData[row], $scope.linkedSelection);
                    if ($scope.linkedSelection.toUpperCase() === "EXISTING")
                        linkedObject.partId = value;
                    else {
                        const newIndex = col - partTypeDefault.fields.length - 3;
                        UploadUtil.setDataValue($scope.linkedSelection.toUpperCase(), newIndex, linkedObject, value, partTypeDefault);
                    }

                    object.linkedParts = [linkedObject];
                } else {
                    // todo : else file
                    console.log("don't know what to do with", row, col, value);
                }

                object.index = row;
                return object;
            };

            //
            // callback : function to call when the entry is created successfully
            //
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
                rowHeaders: getRowHeaders,
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
                        {}, function () {
                            $scope.bulkUpload.entryIdData.splice(row, 1);
                            $dataTable.handsontable('getInstance').render();
                        });
                },
                afterSelection: function (startRow, startCol, endRow, endCol) {
                    // cannot drag cols that handle files
                    const index = partTypeDefault.fields.length - 1; // 0-index
                    let noHandleList = [index + 1, index + 2, index + 3];
                    if (noHandleList.indexOf(endCol) === -1)
                        $dataTable.handsontable('updateSettings', {fillHandle: true});
                    else
                        $dataTable.handsontable('updateSettings', {fillHandle: false});
                }
            };

            const createTable = function () {
                $dataTable.handsontable(options);
                let ht = $dataTable.handsontable('getInstance');
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
            };

            //
            // get headers from server for case when a new bulk upload is created
            //

            if (!partTypeDefault) {
                Util.get("rest/parts/defaults/" + $scope.importType, function (result) {
                    partTypeDefault = EntryService.convertToUIForm(result);
                    $scope.linkedSelection = result.linkType;
                    console.log(result);

                    // headers for the current selection and initialize first row
                    sheetData[0].length = partTypeDefault.fields.length + 3;

                    createTable();
                }, {}, function (error) {
                });
            } else {
                if (!$scope.linkedSelection) {
                    sheetData[0].length = partTypeDefault.fields.length + 3;
                } else {
                    if ($scope.linkedSelection.toLowerCase() === "existing") {
                        sheetData[0].length = partTypeDefault.fields.length + 3 + 1;
                    } else {
                        Util.get("rest/parts/defaults/" + $scope.linkedSelection, function (defaults) {
                            linkedPartTypeDefault = EntryService.convertToUIForm(defaults);
                            sheetData[0].length = partTypeDefault.fields.length + 3 + linkedPartTypeDefault.fields.length + 3;
                        }, {}, function (error) {
                            // todo
                        });
                    }
                }
                createTable();
            }

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
                let requiresApproval = $scope.bulkUpload.status && $scope.bulkUpload.status === 'PENDING_APPROVAL';

                // validate the contents;
                let tmp = {id: $scope.bulkUpload.id, status: $scope.bulkUpload.status};
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
                            controller: function ($scope, msg) {
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
                let modalInstance = $uibModal.open({
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
                        let tmp = {id: $scope.bulkUpload.id, name: newName};
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
        };

        // for retrieving existing entries
        let asyncLoop = function (loopFunction) {

            // loop function
            let loop = function (start, partDefaults) {
                loopFunction.retrieveEntries(loop, partDefaults, start);
            };

            //
            // init : retrieve upload to get information
            //
            Util.get("rest/uploads/" + $stateParams.type, function (result) {
                Util.get("rest/parts/defaults/" + result.type, function (defaults) {
                    partTypeDefault = EntryService.convertToUIForm(defaults);
                    loop(0, partTypeDefault);
                }, {}, function (error) {
                    // todo
                });
            }, {offset: 0, limit: 1});
        };

        // retrieve the contents of an import if parameter is a number
        if (!isNaN($stateParams.type)) {
            $scope.importType = undefined;

            asyncLoop({
                retrieveEntries: function (loop, partDefaults, start) {
                    Util.get("rest/uploads/" + $stateParams.type,
                        function (result) {
                            if (!result || !result.type)
                                return;

                            console.log(result);

                            $scope.bulkUpload.name = result.name;
                            $scope.bulkUpload.status = result.status;
                            $scope.importType = result.type.toLowerCase();
                            $scope.linkedSelection = result.linkType;
                            $scope.linkOptions = UploadUtil.generateLinkOptions($scope.importType);

                            if (start === 0)
                                createSheet();

                            // else render on append data
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            let numberOfExistingEntries = $scope.bulkUpload.entryIdData.length;    // number of existing entries

                            if (result.entryList && result.entryList.length) {

                                // for each entry object (row)
                                for (let i = 0; i < result.entryList.length; i += 1) {
                                    let entry = result.entryList[i];

                                    // store the ids of the entries
                                    $scope.bulkUpload.entryIdData.push(entry.id);
                                    entry = EntryService.convertToUIForm(entry);

                                    // ensure capacity
                                    if (!sheetData[numberOfExistingEntries + i])
                                        sheetData[numberOfExistingEntries + i] = [];

                                    // display [for each field in the object]
                                    for (let j = 0; j < entry.fields.length; j += 1) {
                                        const field = entry.fields[j];
                                        sheetData[numberOfExistingEntries + i][j] = UploadUtil.getPartValue(field, entry);
                                    }

                                    // traces, attachments and sequences
                                    sheetData[numberOfExistingEntries + i][entry.fields.length] = "";
                                    sheetData[numberOfExistingEntries + i][entry.fields.length + 1] = entry.sequenceFileName;
                                    if (entry.hasAttachment) {
                                        const attachment = entry.attachments[0];
                                        sheetData[numberOfExistingEntries + i][entry.fields.length + 2] = attachment.filename;
                                    }

                                    // check if there is a linked entry
                                    if (entry.linkedParts && entry.linkedParts.length) {
                                        let linkedPart = entry.linkedParts[0];
                                        $scope.bulkUpload.linkedEntryIdData.push(linkedPart.id);
                                        linkedPart = EntryService.convertToUIForm(linkedPart);

                                        // linked part fields display
                                        for (let k = 0; k < linkedPart.fields.length; k += 1) {
                                            const linkedField = linkedPart.fields[k];
                                            sheetData[numberOfExistingEntries + i][k + entry.fields.length + FILE_FIELDS_COUNT] = UploadUtil.getPartValue(linkedField, linkedPart);
                                        }

                                        // todo : get traces
                                        const linkedFilesIndex = entry.fields.length + FILE_FIELDS_COUNT + linkedPart.fields.length;
                                        sheetData[numberOfExistingEntries + i][linkedFilesIndex] = '';
                                        sheetData[numberOfExistingEntries + i][linkedFilesIndex + 1] = linkedPart.sequenceFileName;
                                        if (linkedPart.hasAttachment) {
                                            const attachment = linkedPart.attachments[0];
                                            sheetData[numberOfExistingEntries + i][linkedFilesIndex + 2] = attachment.filename;
                                        }
                                    }
                                }
                            }

                            if ($scope.bulkUpload.entryIdData.length < result.count) {
                                loop(start + result.entryList.length, partTypeDefault);
                            }
                            angular.element("#dataTable").handsontable('render');
                        }, {offset: start, limit: 40});
                }
            });
            // }, {}, function (error) {
            //     todo
            // });
        } else {
            // new sheet. create based con type
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
            }, function () {
                $location.path('/folders/pending');
                $uibModalInstance.close();
                $scope.submitting = false;
            }, function (error) {
                console.error(error);
                $scope.submitting = false;
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    })
    .controller('BulkUploadModalController', function ($window, $scope, $location, $cookies, $routeParams, uploadId,
                                                       $uibModalInstance, FileUploader, addType, linkedAddType, Util,
                                                       Authentication) {
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

        let createUploader = function () {

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

        $scope.importUploader.onSuccessItem = function (item, response) {
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

        $scope.importUploader.onErrorItem = function (item, response, status) {
            $scope.processing = false;
            $scope.uploadError = {};
            if (response.userMessage)
                $scope.uploadError.message = response.userMessage;
            else
                $scope.uploadError.message = "Unknown server error";

            if (status === 400) {
                $scope.uploadError.message = "Validation error processing file \'" + item.file.name + "\'";
                if (response.userMessage)
                    $scope.uploadError.details = response.userMessage;
                $scope.uploadError.headers = response.headers;
            }
        };

        $scope.importUploader.onBeforeUploadItem = function () {
            $scope.processing = true;
        };

        $scope.importUploader.onCompleteItem = function () {
            $scope.processing = false;
        };

        $scope.ok = function () {
            $uibModalInstance.close($scope.selected.item);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.downloadCSVTemplate = function () {
            let url = "rest/file/upload/" + $scope.addType;
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
        let panes = $scope.panes = [];
        $scope.readPermissions = [];
        $scope.writePermissions = [];
        $scope.upload = upload;
        $scope.userFilterInput = undefined;

        // if upload.id exists, then retrieve the permissions for the upload
        if (upload.id) {
            Util.list('rest/uploads/' + upload.id + '/permissions', function (result) {
                angular.forEach(result, function (item) {
                    if (item.type === 'WRITE_UPLOAD')
                        $scope.writePermissions.push(item);
                    else if (item.type === 'READ_UPLOAD')
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
                for (let i = 0; i < $scope.activePermissions.length; i += 1) {
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

        let removePermission = function (permissionId) {
            Util.remove('rest/uploads/' + $scope.upload.id + '/permissions/' + permissionId, {}, function (result) {
                if (!result)
                    return;
                // check which pane is selected
                let pane;
                if ($scope.panes[0].selected)
                    pane = $scope.panes[0];
                else
                    pane = $scope.panes[1];

                let i = -1;

                for (let idx = 0; idx < $scope.activePermissions.length; idx += 1) {
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
            let pane;
            for (let i = 0; i < panes.length; i += 1) {
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
