'use strict';

angular.module('ice.upload', [])
    .controller('UploadController', function ($rootScope, $location, $scope, $modal, $cookieStore, $resource, $stateParams, $fileUploader, $http, Upload) {
        var sid = $cookieStore.get("sessionId");
        var upload = Upload(sid);

        if (!$scope.importType && $stateParams.type)
            $scope.importType = $stateParams.type;

        $scope.bulkUpload = {};
        $scope.bulkUpload.entryIdData = [];
        $scope.bulkUpload.name = "untitled";
        $scope.uploadNameEditMode = false;

        $scope.setNameEditMode = function (value) {
            $scope.uploadNameEditMode = value;
        };

        $scope.addNewPartLink = function (type) {
            $scope.linkedSelection = type;
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
            var plasmidHeaders, strainHeaders, seedHeaders;
            var columns = [];

            // headers
            // part
            var partHeaders = ["Principal Investigator <span class='required'>*</span>"
                , "PI Email <i class='opacity_hover fa fa-question-circle' title='tooltip' style='margin-left: 20px'></i>"
                , "Funding Source"
                , "Intellectual Property"
                , "BioSafety Level <span class='required'>*</span>"
                , "Name <span class='required'>*</span>"
                , "Alias"
                , "Keywords"
                , "Summary <span class='required'>*</span>"
                , "Notes"
                , "References"
                , "Links"
                , "Status <span class='required'>*</span>"
                , "Creator <span class='required'>*</span>"
                , "Creator Email <span class='required'>*</span>"
                // other headers are inserted here
                , "Sequence File"
                , "Attachment File"];

            var dataSchema;
            switch ($scope.importType) {
                case "strain":
                    strainHeaders = angular.copy(partHeaders);
                    strainHeaders.splice.apply(strainHeaders, [15, 0].concat(["Parental Strain", "Genotype or Phenotype", "Plasmids",
                        "Selection Markers"]));
                    dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, parentStrain:null, genotypePhenotype:null, plasmids:null, selectionMarkers:null, sequenceFilename:null, attachmentFilename:null};
                    break;

                case "plasmid":
                    plasmidHeaders = angular.copy(partHeaders);
                    plasmidHeaders.splice.apply(plasmidHeaders, [15, 0].concat(["Circular", "Backbone", "Promoters", "Replicates In",
                        "Origin of Replication", "Selection Markers"]));
                    dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, circular:null, backbone:null, promoters:null, replicatesIn:null, originOfReplication:null, selectionMarkers:null, sequenceFilename:null, attachmentFilename:null};
                    break;

                case "arabidopsis":
                    seedHeaders = angular.copy(partHeaders);
                    seedHeaders.splice.apply(seedHeaders, [15, 0].concat(["Homozygosity", "Ecotype", "Harvest Date", "Parents",
                        "Plant Type", "Generation", "Sent to ABRC?"]));
                    dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, homozygosity:null, ecotype:null, harvestDate:null, parents:null, plantType:null, generation:null, sentToAbrc:null, sequenceFilename:null, attachmentFilename:null};
                    break;

                case "part":
                    dataSchema = {principalInvestigator:null, principalInvestigatorEmail:null, fundingSource:null, intellectualProperty:null, bioSafetyLevel:null, name:null, alias:null, keywords:null, shortDescription:null, longDescription:null, references:null, links:null, status:null, creator:null, creatorEmail:null, sequenceFilename:null, attachmentFilename:null};
                    break;
            }

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

            var bslValidator = function (value, callback) {
                callback(value == 1 || value == 2);
            }

            for (var prop in dataSchema) {
                if (dataSchema.hasOwnProperty(prop)) {
                    var object = {};
                    object.data = prop;

                    switch (prop) {
                        case 'circular':
                        case 'sentToAbrc':
                            object.type = 'checkbox';
                            break;

                        case 'bioSafetyLevel':
                            object.type = 'autocomplete';
                            object.source = ['1', '2', ''];
                            object.validator = bslValidator;
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

                    columns.push(object);
                }
            }

            var getSheetHeaders = function (index) {
                switch ($scope.importType) {
                    case "strain":
                        return strainHeaders[index];

                    case "plasmid":
                        return plasmidHeaders[index];

                    case "part":
                        return partHeaders[index];

                    case "arabidopsis":
                        return seedHeaders[index];
                }
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

            var createOrUpdateEntry = function (data) {
                var row = data[0];
                var objectProperty = data[1];
                var value = data[3];
                var entryIdDataIndex = $scope.bulkUpload.entryIdData[row];

                // if no entry associated with row and now data, skip
                if (value === "" && !entryIdDataIndex)
                    return;

                var object = {};

                object[objectProperty] = value;
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
//                        $location.path("/upload/" + result.id, false);

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
                        object.type = $scope.importType;

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
                if (source === 'loadData' || source === 'setMyData') {
                    return; //data load, not need to save
                }

                $scope.saving = true;
                // single cell edit

                if (source === "edit") {
                    createOrUpdateEntry(change[0]);
                } else if (source === "autofill") {
                    // click and drag
                    console.log($scope.bulkUpload, change);
                    for (var i = 0; i < change.length; i += 1) {
                        createOrUpdateEntry(change[i]);
                    }
                } else if (source === "paste") {
                    // todo
                    // paste from copy
                }
//            console.log("change", change, "source", source);
            };

            var options = {
                data:[],
                dataSchema:dataSchema,
                startRows:50, // comes into effect only if no data is provided
                minRows:50,
//        startCols: plasmidHeaders.length, // ignored because of "columns"
                colHeaders:getSheetHeaders,
                rowHeaders:true,
                colWidths:150,
                stretchH:'all',
                minSpareRows:1,
                enterMoves:{row:0, col:1}, // move right on enter instead of down
                autoWrapRow:true,
                autoWrapCol:true,
                columns:columns,
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

            $scope.submitImportForApproval = function () {
                var tmp = {id:$scope.bulkUpload.id, status:'PENDING_APPROVAL'};
                $scope.submitting = true;
                Upload(sid).updateStatus({importId:$scope.bulkUpload.id}, tmp, function (result) {
                    $scope.submitting = false;
                    $location.path('/folders/bulkUpload');
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
                            var l = $scope.bulkUpload.entryIdData.length;

                            if (result.entryList && result.entryList.length) {
                                for (var i = 0; i < result.entryList.length; i += 1) {
                                    $scope.bulkUpload.entryIdData[l + i] = result.entryList[i].id;    // todo index here is starting from 0 again
                                    $scope.uploadEntries.push(result.entryList[i]);
                                }
                            }
                            $scope.spreadSheet.loadData(angular.copy($scope.uploadEntries));
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
;