'use strict';

angular.module('ice.upload.controller', [])
    .controller('UploadController', function ($rootScope, $location, $scope, $modal, $cookieStore, $resource, $stateParams, $fileUploader, $http, Upload, UploadUtil) {
        var sid = $cookieStore.get("sessionId");
        var upload = Upload(sid);
        var sheetData = [
            []
        ];

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type.toUpperCase();

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = [];
        $scope.bulkUpload.name = "untitled";
        $scope.uploadNameEditMode = false;

        $scope.setNameEditMode = function (value) {
            $scope.uploadNameEditMode = value;
        };

        $scope.addNewPartLink = function (type) {
            $scope.linkedSelection = type;
            console.log("add new part link", type);
            var ht = $("#dataTable").handsontable('getInstance');
            ht.alter('insert_col', 1, 5);
//            ht.setDataAtCell(0, 0, 'new value');
        };

        var uploader = $fileUploader.create({
            scope:$scope, // to automatically update the html. Default: $rootScope
            url:"/rest/file/sequence",
            method:'POST',
            removeAfterUpload:true,
            headers:{"X-ICE-Authentication-SessionId":sid},
            autoUpload:true,
            queueLimit:1 // can only upload 1 file
        });

        uploader.bind('beforeupload', function (event, item) {
            console.log("beforeupload");
            item.formData.push({entryType:$scope.importType});
            item.formData.push({entryRecordId:$scope.bulkUpload.entryIdData[row]});
        });

        uploader.bind('progress', function (event, item, progress) {
            if (progress != "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        });

        $scope.onFileSelect = function ($files) {
            console.log("fileSelect");
        };

        $scope.createSheet = function () {
            var availableWidth, availableHeight, $window = $(window), $dataTable = $("#dataTable");

            // cell renderer for file upload
            var fileUploadRenderer = function (instance, td, row, col, prop, value, cellProperties) {
//            console.log(instance, td, row, col, prop, value, cellProperties);
                var escaped = Handsontable.helper.stringify(value);

                var $up = $('<span class="fileUpload"><i class="fa fa-upload opacity_hover opacity_4"></i> Upload '
                    + '<input type="file" ng-model="sequenceFiles" ng-file-select="onFileSelect($files)" class="upload" />');

                $up.on("change", function (event) {
                    console.log("change", $scope.sequenceFiles);
                });

                $(td).empty().append($up);
                return td;
            };

            var autoComplete = function (field, query, process) {
                $http.get('/rest/part/autocomplete', {
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

                switch (UploadUtil.getTypeField($scope.importType, col)) {
//                    case 'circular':
//                    case 'sentToAbrc':
//                        object.type = 'checkbox';
//                        break;

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

                    case 'sequenceFilename':
                    case "attachmentFilename":
                        object.renderer = fileUploadRenderer;
                        object.type = 'text';
                        object.readOnly = true;  // file cells are readonly. all data is set programmatically
                        object.copyable = false; // file cells cannot be copied
                        break;
                }

                return object;
            };

            var getSheetHeaders = function (index) {
                return UploadUtil.getSheetHeaders($scope.importType)[index];
            };

            var getColWidth = function (index) {
                return 150;
            };

            var headers = UploadUtil.getSheetHeaders($scope.importType);

            // initialize first row
            for (var i = 0; i < headers.length; i += 1)
                sheetData[0][i] = '';

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

            var createOrUpdateEntry = function (data) {
                var row = data[0];
                var objectPropertyIndex = data[1];
                var value = data[3];
                var entryIdDataIndex = $scope.bulkUpload.entryIdData[row];

                // if no entry associated with row and no data, skip
                if (value === "" && !entryIdDataIndex)
                    return;

                var object = {};
                var dataSchema = UploadUtil.getDataSchema($scope.importType);

                object[dataSchema[objectPropertyIndex]] = value;
                if (entryIdDataIndex) {
                    object['id'] = entryIdDataIndex;
                }

                if ($scope.bulkUpload.id === undefined) {
                    // create draft of specified type
                    upload.create({type:$scope.importType})
                        .$promise
                        .then(function (result) {
                            // create entry.
                            console.log("created new bulk upload", result);
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = result.lastUpdate;
                            $scope.bulkUpload.name = result.name;
                            $location.path("/upload/" + result.id, false);

                            upload.createEntry({importId:result.id}, object,
                                function (createdEntry) {
                                    $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                                    $scope.saving = false;
                                    console.log("created entry", $scope.bulkUpload);
                                },
                                function (error) {
                                    console.error(error);
                                    $scope.saving = false;
                                });
                        });
                } else {
                    // check if row being updated has existing entry
                    if (!object['id']) {
                        // create new entry for existing upload
                        upload.createEntry({importId:$scope.bulkUpload.id}, object,
                            function (createdEntry) {
                                $scope.bulkUpload.entryIdData[row] = createdEntry.id;
                                console.log("NEW ENTRY", createdEntry, $scope.bulkUpload.entryIdData.length);
                                $scope.bulkUpload.lastUpdate = createdEntry.modificationTime;
                                $scope.saving = false;
                            });
                    } else {
                        // update entry for existing upload
                        object.type = $scope.importType.toUpperCase();

                        upload.updateEntry({importId:$scope.bulkUpload.id, entryId:object.id}, object,
                            function (updatedEntry) {
                                console.log("UPDATE", updatedEntry);
                                if (!$scope.bulkUpload.entryIdData[row] === updatedEntry.id) {
                                    console.error("Returned id does not match");
                                    return;
                                }
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

            // callback for change to the sheet to save/update the information on the server
            var afterChange = function (change, source) {
                // "setMyData" is intended to be used when setting data in code to prevent updates
                if (source === 'loadData' || source === 'setMyData' || source === 'external') {
                    return; //data load, no need to save
                }

                $scope.saving = true;

                // single cell edit

                if (source === "edit") {   // single cell edit, change expected to contain single array
                    createOrUpdateEntry(change[0]);
                } else if (source === "autofill") {
                    // click and drag
//                    console.log($scope.bulkUpload, change);
                    for (var i = 0; i < change.length; i += 1) {
                        createOrUpdateEntry(change[i]);
                    }
                } else if (source === "paste") {
                    // paste from copy may contain multiple arrays as in auto fill
                    for (var j = 0; j < change.length; j += 1) {
                        createOrUpdateEntry(change[j]);
                    }
                }
//            console.log("change", change, "source", source);
            };

            var getDataSchema = function () {
                return UploadUtil.getDataSchema($scope.importType);
            };

            var options = {
                data:sheetData,
                dataSchema:getDataSchema,
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
                columnSorting:true,
                observeChanges:true
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

            $scope.submitImportForApproval = function () {
                $scope.submitting = true;

                // validate the contents;
                var tmp = {id:$scope.bulkUpload.id, status:'PENDING_APPROVAL'};

                Upload(sid).updateStatus({importId:$scope.bulkUpload.id}, tmp, function (result) {
                    $scope.submitting = false;
                    $location.path('/folders/personal');
                }, function (error) {
                    $scope.submitting = false;
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

        if (!isNaN($stateParams.type)) {
            $scope.importType = undefined;
            asyncLoop({
                functionToLoop:function (loop, start) {
                    upload.get(
                        {importId:$stateParams.type, offset:start, limit:40},
                        function (result) {
                            $scope.bulkUpload.name = result.name;
                            $scope.importType = result.type.toLowerCase();
                            generateLinkOptions($scope.importType);

                            if (start === 0)
                                $scope.createSheet();

                            // else render on append data
                            $scope.bulkUpload.id = result.id;
                            $scope.bulkUpload.lastUpdate = new Date(result.lastUpdate);
                            var l = $scope.bulkUpload.entryIdData.length;    // number of existing entries

                            if (result.entryList && result.entryList.length) {
                                var dataSchema = UploadUtil.getDataSchema($scope.importType);
                                for (var i = 0; i < result.entryList.length; i += 1) {   // for each entry object
                                    var entry = result.entryList[i];
                                    $scope.bulkUpload.entryIdData[l + i] = entry.id;    // todo index here is starting from 0 again

                                    // for each field in the object
                                    for (var j = 0; j < dataSchema.length; j += 1) {
                                        var val = entry[dataSchema[j]];
                                        if (val === undefined || (val instanceof  Array))
                                            val = '';
                                        sheetData[l + i][j] = val;
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
            $scope.importType = $stateParams.type;
            generateLinkOptions($scope.importType);
            $scope.createSheet();
        }
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
            console.log($scope.addType, linkedAddType);
            var url = "/rest/file/upload/" + $scope.addType;
            if (linkedAddType)
                url += "?link=" + linkedAddType;
            $window.open(url, "_self");
        }
    })

;