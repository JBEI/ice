'use strict';

angular.module('ice.admin.sample.controller', [])
    .controller('AdminSampleRequestController', function ($scope, $location, $rootScope, $uibModal, Util, AdminSettings) {
        $rootScope.error = undefined;

        $scope.selectOptions = ['ALL', 'PENDING', 'FULFILLED', 'REJECTED'];
        $scope.selectedRequests = [];
        $scope.maxSize = 5;
        $scope.params = {
            sort: 'requested',
            asc: false,
            currentPage: 1,
            status: 'ALL',
            limit: 15,
            hstep: [15, 30, 50, 100]
        };

        $scope.exportSelectedSamples = function () {
            const selectedIds = [];
            for (let i = 0; i < $scope.selectedRequests.length; i += 1) {
                selectedIds.push($scope.selectedRequests[i].id);
            }

            const clickEvent = new MouseEvent("click", {
                "view": window,
                "bubbles": true,
                "cancelable": false
            });

            Util.download("rest/samples/requests/file", selectedIds).$promise.then(function (result) {
                var url = URL.createObjectURL(new Blob([result.data]));
                var a = document.createElement('a');
                a.href = url;
                a.download = result.filename();
                a.target = '_blank';
                a.dispatchEvent(clickEvent);
                $scope.selectedRequests = [];
            });
        };

        $scope.selectRequest = function (request) {
            var i = $scope.selectedRequests.indexOf(request);
            if (i === -1) {
                $scope.selectedRequests.push(request);
            } else {
                $scope.selectedRequests.splice(i, 1);
            }
        };

        $scope.getBSLLabel = function (bslValue) {
            switch (bslValue) {
                default:
                    return "";

                case 1:
                case 2:
                    return "BSL " + bslValue;

                case -1:
                    return "RESTRICTED";
            }
        };

        $scope.requestSamples = function () {
            $scope.loadingPage = true;
            const params = angular.copy($scope.params);

            Util.get("rest/samples/requests", function (result) {
                console.log(result);

                $scope.sampleRequests = result;
                $scope.loadingPage = false;
                $scope.indexStart = ($scope.currentPage - 1) * $scope.params.limit;
            }, params, function (error) {
                $scope.loadingPage = false;
            });
        };

        $scope.samplesFilterTemplate = "scripts/admin/popover/sample-filter-template.html";
        $scope.params.status = ['FULFILLED', 'IN_CART', 'PENDING', 'REJECTED'];
        $scope.samplesRequesterNameTemplate = "scripts/admin/popover/sample-requester-name-filter-template.html";

        $scope.sampleFilterChecked = function (filter) {
            var idx = $scope.params.status.indexOf(filter);
            if (idx === -1)
                $scope.params.status.push(filter);
            else
                $scope.params.status.splice(idx, 1);
            $scope.requestSamples();
        };

        // initial sample request (uses default paging values)
        $scope.requestSamples();

        $scope.sampleRequestPageChanged = function () {
            $scope.params.offset = ($scope.params.currentPage - 1) * $scope.params.limit;
            if ($scope.filter) {
                $scope.params.filter = $scope.filter;
            }

            $scope.requestSamples();
        };

        $scope.exportSampleFolder = function (folderId) {
            const clickEvent = new MouseEvent("click", {
                "view": window,
                "bubbles": true,
                "cancelable": false
            });

            Util.download("/rest/folders/" + folderId + "/file").$promise.then(function (result) {
                let url = URL.createObjectURL(new Blob([result.data]));
                let a = document.createElement('a');
                a.href = url;
                a.download = result.filename();
                a.target = '_blank';
                a.dispatchEvent(clickEvent);
            });
        };

        $scope.sortFolderRequests = function (sort) {
            $scope.folderRequests.params.asc = ($scope.folderRequests.params.sort === sort ? !$scope.folderRequests.params.asc : false);
            $scope.folderRequests.params.currentPage = 1;
            $scope.folderRequests.params.sort = sort;
            $scope.folderRequestPageChanged();
        };

        $scope.folderRequestPageChanged = function () {
            $scope.folderRequests.params.offset = ($scope.folderRequests.params.currentPage - 1) * $scope.folderRequests.params.limit;

            Util.get("rest/samples/requests", function (result) {
                $scope.folderRequests.available = result.count;
                $scope.folderRequests.results = result.requests;
            }, $scope.folderRequests.params);
        };

        $scope.initFolderRequests = function () {
            $scope.folderRequests = {
                available: 0,
                results: [],
                params: {
                    asc: false,
                    sort: 'requested',
                    limit: 15,
                    currentPage: 1,
                    hstep: [15, 30, 50, 100],
                    isFolder: true
                },
            };
            $scope.folderRequestPageChanged();
        };

        $scope.retrieveSampleSettings = function () {
            Util.list("rest/samples/requests/settings", function (result) {
                angular.forEach(result, function (setting) {
                    if (AdminSettings.getSampleRequestKeys().indexOf(setting.key) !== -1) {
                        $scope.sampleRequestSettings.push({
                            'originalKey': setting.key,
                            'key': (setting.key.replace(/_/g, ' ')).toLowerCase(),
                            'value': setting.value,
                            'editMode': false,
                            'type': AdminSettings.getKeyType(setting.key)
                        });
                    }
                });
            }, {}, function (error) {
                console.log(error);
            })
        };

        $scope.updateStatus = function (request, newStatus, isFolder) {
            Util.update("rest/samples/requests/" + request.id + "?status=" + newStatus, {}, {isFolder: isFolder ? isFolder : false}, function (result) {
                if (result === undefined || result.id !== request.id)
                    return;

                if (!isFolder) {
                    let i = $scope.sampleRequests.requests.indexOf(request);
                    if (i !== -1) {
                        $scope.sampleRequests.requests[i].status = result.status;
                    }
                } else {
                    const i = $scope.folderRequests.results.indexOf(request);
                    if (i !== -1) {
                        $scope.folderRequests.results[i].status = result.status;
                    }
                }
            });
        };

        $scope.sort = function (field) {
            if ($scope.loadingPage)
                return;

            $scope.loadingPage = true;
            if ($scope.params.sort === field)
                $scope.params.asc = !$scope.params.asc;
            else
                $scope.params.asc = false;

            $scope.params.sort = field;
            $scope.requestSamples();
        };

        $scope.searchSampleLocations = function () {
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/sample-location-search.html',
                controller: "AdminSampleLocationSearch",
                backdrop: "static"
            });
        };

        $scope.importSamples = function () {
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/import-samples.html',
                controller: 'AdminSampleImport',
                backdrop: "static",
                keyboard: false,
                size: 'lg'
            })
        };

        $scope.sanitizeCsv = function () {
            $uibModal.open({
                templateUrl: 'scripts/admin/modal/create-samples.html',
                controller: 'AdminSampleCreate',
                backdrop: "static",
                size: 'lg'
            })
        };

        $scope.getPrimarySample = function (locations) {
            if (locations.length != 3)
                return locations[0];

            for (var i = 0; i < locations.length; i += 1) {
                if (locations[i].label.indexOf("backup") == -1)
                    return locations[i];
            }
            return locations[0];
        }
    })
    .controller('AdminSampleCreate', function ($scope, FileUploader, Authentication, $window) {
        $scope.sampleUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/samples/file",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        $scope.sampleUploader.onProgressItem = function (item, progress) {
            $scope.progress = progress;
        };

        $scope.sampleUploader.onSuccessItem = function (item, response, status, header) {
            $scope.processingFile = false;
            $scope.result = {success: true, data: response};
            $window.open("rest/file/tmp/" + response.fileId + "?filename=data.csv", "_self");
        };

        $scope.sampleUploader.onAfterAddingFile = function () {
            $scope.processingFile = false;
            $scope.result = {success: false};
        };

        $scope.sampleUploader.onBeforeUploadItem = function () {
            $scope.processingFile = true;
        };

        $scope.sampleUploader.onCompleteAll = function () {
            $scope.processingFile = false;
        };
    })
    .controller('AdminSampleLocationSearch', function ($scope, $uibModalInstance, Util) {
        $scope.sample = {};

        $scope.searchSampleLocation = function () {
            $scope.sample.searching = true;
            $scope.sample.notFound = false;
            $scope.sample.partId = undefined;

            Util.list("rest/samples/" + $scope.sample.location, function (result) {
                $scope.sample.searching = false;

                if (result && result.length) {
                    $scope.sample.partId = result[0].partId;
                } else {
                    $scope.sample.notFound = true;
                }
            }, {}, function (error) {
                $scope.sample.searching = false;
                $scope.sample.notFound = true;
            })
        }
    })
    .controller('AdminSampleImport', function ($scope, $uibModalInstance, Util, FileUploader, Authentication, Upload) {
        $scope.progress = 0;
        $scope.selectRequestEntries = undefined;
        $scope.plates = [{id: 0, locationBarcodes: []}, {id: 1}, {id: 2}];
        $scope.currentPlate = $scope.plates[0];

        $scope.folderRequestPageChanged = function () {
            $scope.folderRequests.params.offset = ($scope.folderRequests.params.currentPage - 1) * $scope.folderRequests.params.limit;

            Util.get("rest/samples/requests", function (result) {
                $scope.folderRequests.available = result.count;
                $scope.folderRequests.results = result.requests;
            }, $scope.folderRequests.params);
        };

        const initFolderRequests = function () {
            $scope.folderRequests = {
                available: 0,
                results: [],
                params: {
                    asc: false,
                    sort: 'requested',
                    limit: 8,
                    currentPage: 1,
                    isFolder: true
                },
            };
            $scope.folderRequestPageChanged();
        };

        initFolderRequests();

        $scope.selectSample = function (index) {
            $scope.currentPlate = $scope.plates[index];
        };

        $scope.keys = function (object) {
            if (!object)
                return [];

            return Object.keys(object);
        };

        const assignPartNumbers = function (result) {
            if ($scope.currentPlate.id !== 0)
                return;

            $scope.currentPlate.locationBarcodes = angular.copy($scope.currentPlate.cleanLocationBarcodes);

            for (let i = 0; i < result.entries.length; i += 1) {
                const entry = result.entries[i];

                for (const key of Object.keys($scope.currentPlate.locationBarcodes).sort()) {
                    const tube = $scope.currentPlate.locationBarcodes[key];
                    if (!tube.barcodeAvailable)
                        continue;

                    if (!tube || !tube.barcode || tube.barcode === 'No Read' || tube.barcode === 'No Tube')
                        continue;

                    if (tube.partId || !tube.barcode)
                        continue;

                    tube.partId = entry.partId;
                    break;
                }
            }
        };

        $scope.selectFolderForSample = function (folder) {
            console.log("selected folder", folder);

            Util.get("rest/folders/" + folder.folderDetails.id + "/entries", function (result) {
                // $scope.currentPlate.folder = folder;
                $scope.plates[0].folder = folder;

                if (!$scope.plates[0].cleanLocationBarcodes)
                    return;

                assignPartNumbers(result);
            }, {fields: ['part_number']});
        };

        $scope.showFolders = function () {
            $scope.plates[0].folder = undefined;
        };

        $scope.uploadSelectedFile = function (file) {
            if (!file)
                return;

            $scope.uploadingFile = true;
            Upload.upload({
                url: "rest/samples/map",
                data: {
                    file: file
                },
                method: 'POST',
                headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()}
            }).then(function (resp) {
                $scope.uploadingFile = undefined;
                $scope.messages = undefined;

                if (resp.status !== 200)
                    Util.setFeedback("Error uploading file", "error");

                $scope.currentPlate.locationBarcodes = resp.data.locationBarcodes;
                if (resp.data.name && !$scope.currentPlate.name)
                    $scope.currentPlate.name = resp.data.name;

                if ($scope.currentPlate.id === 0) {
                    $scope.currentPlate.cleanLocationBarcodes = angular.copy(resp.data.locationBarcodes);

                    // check if user uploaded part Ids
                    if (resp.data.hasUserSpecifiedPartIds) {
                        $scope.currentPlate.folder = {folderDetails: {folderName: "User Specified"}}
                    } else {
                        // if a folder has already been selected then assign the part numbers
                        if ($scope.currentPlate.folder)
                            $scope.selectFolderForSample($scope.currentPlate.folder);
                    }
                }

            }, null, function (evt) {
                console.log(evt.loaded, evt.total);
            });
        };

        $scope.submitPlateInformation = function () {
            // validate
            $scope.messages = {processing: "Validating data"};

            // working copy validation
            const workingCopy = $scope.plates[0];
            if (!workingCopy.locationBarcodes) {
                $scope.messages.error = "No barcodes detected. Please upload file for working copy";
            }

            if (!workingCopy.name) {
                $scope.messages.error = "Please enter a plate name for working copy";
            }

            if ($scope.messages.error) {
                $scope.messages.processing = undefined;
                return;
            }

            // get all valid barcodes and part Ids
            const plateToUpload = {name: workingCopy.name, locationBarcodes: {}};
            const keys = $scope.keys(workingCopy.locationBarcodes);
            for (let i = 0; i < keys.length; i += 1) {
                const well = keys[i];
                const tube = workingCopy.locationBarcodes[well];
                if (!tube.barcodeAvailable)
                    continue;

                if (!tube.partId)
                    continue;

                plateToUpload.locationBarcodes[well] = tube;
            }

            const uploadPlateKeys = Object.keys(plateToUpload.locationBarcodes);
            const sampleCount = uploadPlateKeys.length;
            if (!sampleCount) {
                $scope.messages = {warning: "Please select a folder to assign part ids"};
                return;
            }

            // send data to the server
            // create working copy samples
            $scope.messages = {processing: "Creating " + sampleCount + " samples for working copy"};
            plateToUpload.name = $scope.plates[1].name;
            Util.post("rest/samples", plateToUpload, function () {

                if (!$scope.plates[1].locationBarcodes) {
                    $scope.messages = {success: "Samples created successfully for working copy only"};
                    return;
                }

                // process backup 1 copies if available
                let backup1CopiesAvailable = false;
                for (let i = 0; i < uploadPlateKeys.length; i += 1) {
                    const backupWell = uploadPlateKeys[i];
                    const backupTube = $scope.plates[1].locationBarcodes[backupWell];
                    backup1CopiesAvailable = backupTube !== undefined;
                    if (!backup1CopiesAvailable) {
                        $scope.messages = {success: "Samples created successfully for working copy only"};
                        return;
                    }

                    plateToUpload.locationBarcodes[backupWell].barcode = backupTube.barcode;
                }

                // on success for working copy create backup copy 1
                $scope.messages = {processing: "Creating " + sampleCount + " samples for backup 1"};
                plateToUpload.name = $scope.plates[2].name;
                Util.post("rest/samples", plateToUpload, function () {

                    if (!$scope.plates[2].locationBarcodes) {
                        $scope.messages = {success: "Samples created successfully for working copy & backup 1"};
                        return;
                    }

                    // process backup 1 copies if available
                    let backup2CopiesAvailable = false;
                    for (let i = 0; i < uploadPlateKeys.length; i += 1) {
                        const backupWell = uploadPlateKeys[i];
                        const backupTube = $scope.plates[2].locationBarcodes[backupWell];
                        backup2CopiesAvailable = backupTube !== undefined;
                        if (!backup2CopiesAvailable) {
                            $scope.messages = {success: "Samples created successfully for working copy & backup 1"};
                            return;
                        }

                        plateToUpload.locationBarcodes[backupWell].barcode = backupTube.barcode;
                    }

                    $scope.messages = {processing: "Creating " + sampleCount + " samples for backup 2"};
                    Util.post("rest/samples", plateToUpload, function () {
                        $scope.messages = {success: "Samples created successfully for working copy, backups 1 and 2"};
                    });
                });
            }, {}, function (error) {
                console.log(error);
                $scope.messages = {error: "Server error processing data"};
            })

        };
    });
