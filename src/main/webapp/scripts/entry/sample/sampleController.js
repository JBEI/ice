'use strict';

angular.module('ice.entry.sample.controller', [])
    .controller('DisplaySampleController', function ($rootScope, $scope, $uibModal, SampleService, Util) {
        //
        // init (fetch all samples on the plate)
        //
        if ($scope.sample.location.type == 'PLATE96') {
            Util.get("rest/samples/location/" + $scope.sample.location.id, function (result) {
                $scope.sampleMap = result.sampleMap;
            });
        }

        $scope.userIsAdmin = function () {
            return $rootScope.user.isAdmin;
        };

        $scope.selectSample = function (data) {
            $scope.selected = angular.copy(data);
            if (data.location.type == "PLATE96") {
                if (data.location.child.child)
                    $scope.selected.location = data.location.child.child;
                else
                    $scope.selected.location = data.location.child;
                $scope.selected.location.name = data.location.child.display;
            }
        };

        $scope.plateNumber = $scope.sample.location.display;

        $scope.selectSample($scope.sample); // select sample by default to show details
        $scope.Plate96Rows = SampleService.getPlate96Rows();
        $scope.Plate96Cols = SampleService.getPlate96Cols();

        $scope.canDelete = function () {
            return !$scope.remote && $rootScope.user && $rootScope.user.isAdmin;
        };

        // relies on line 20 setting the actual location (e.g. A01) to the name field
        $scope.isSelectedColumn = function (location, col) {
            if (location.name[1] == "0")
                return col == location.name[2];
            return col == location.name.substring(1);
        };

        $scope.isSelectedRow = function (location, row) {
            return location.name[0] == row;
        };

        $scope.uploadSampleInformation = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/entry/modal/upload-sample-information.html',
                controller: "UploadSampleInformationController",
                backdrop: "static",
                resolve: {
                    plateNumber: function () {
                        return $scope.plateNumber;
                    },
                    existing: function () {
                        return $scope.sampleMap;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                if (result) {
                    $scope.$broadcast("UpdateCollectionCounts");
                    $scope.updateSelectedCollectionFolders();
                    Selection.reset();
                }
            });
        }
    })
    .controller('UploadSampleInformationController', function ($scope, plateNumber, existing,
                                                               $uibModalInstance, FileUploader, Authentication) {
        $scope.plateNumber = plateNumber;
        $scope.sampleMap = existing;

        $scope.sampleInformationUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/samples/file/model",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": Authentication.getSessionId()
            },
            autoUpload: true,
            queueLimit: 1
        });

        $scope.sampleInformationUploader.onSuccessItem = function (item, response, status, headers) {
            if (status != "200") {
                $scope.sampleUploadError = true;
                return;
            }

            $scope.errors = response;
        };

        $scope.sampleInformationUploader.onErrorItem = function (item, response, status, headers) {
            $scope.sampleUploadError = true;
        };

        $scope.sampleInformationUploader.onAfterAddingFile = function () {
            $scope.errors = undefined;
            $scope.isSuccess = false;
        };

        $scope.sampleInformationUploader.onProgressItem = function (item, progress) {
            $scope.progress = progress;
        };

        $scope.errorDetails = function (code) {
            switch (code.trim()) {
                case "-s":
                    return "No storage locations for barcode";

                case "+s":
                    return "Multiple storage locations for barcode";

                case "-p":
                    return "No parent well for tube";

                case "-ws":
                    return "Wrong sample for barcode";

                case "-b":
                    return "Mismatched barcode";

                case "-w":
                    return "Mismatched well";

                case "-e":
                    return "Invalid entry";
            }

            return "Unknown error"
        }
    })
    .controller('EntrySampleController', function ($rootScope, $scope, $uibModal, $stateParams, Util, SampleService) {
        var partId = $stateParams.id;

        $scope.Plate96Rows = SampleService.getPlate96Rows();
        $scope.Plate96Cols = SampleService.getPlate96Cols();
        $scope.addToCartDefaultLocal = undefined;

        Util.get('rest/config/ADD_TO_CART_DEFAULT_SET_TO_LOCAL', function (result) {
            $scope.addToCartDefaultLocal = result.value.toUpperCase() === "YES";
        });

        // retrieve samples for partId and all samples for relevant plates
        var refreshSamples = function () {
            Util.list('rest/parts/' + partId + '/samples', function (result) {
                $scope.totalSamples = result.length;
                $scope.samples = result;
                $scope.selected = null;
            });
        };
        refreshSamples();

        $scope.isAddGene = function (samples) {
            if (!samples || !samples.length)
                return false;

            for (var i = 0; i < samples.length; i += 1) {
                if (samples[i].location.type == 'ADDGENE')
                    return true;
            }

            return false;
        };

        $scope.requestFromAddGene = function (samples) {
            for (var i = 0; i < samples.length; i += 1) {
                if (samples[i].location.type == 'ADDGENE') {
                    window.open("https://www.addgene.org/" + samples[i].location.display, "_blank");
                    return;
                }
            }
        };

        $scope.openAddToCart = function (entryId, samples) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/entry/sample/modal-sample-request.html',
                controller: function ($scope, samples) {
                    $scope.samples = samples;
                    $scope.tempRange = [{value: 30}, {value: 37}];
                    $scope.plateInformationOptions = [{value: ""},
                        {value: "LB"},
                        {value: "LB Apr50"},
                        {value: "LB Carb100"},
                        {value: "LB Chlor25"},
                        {value: "LB Gent30 Kan50 Rif100"},
                        {value: "LB Kan50"},
                        {value: "LB Kan50 Rif100 Tet 5"},
                        {value: "LB Spect100"},
                        {value: "YPD 1000"},
                        {value: "CSM -HIS"},
                        {value: "CSM -HIS -LEU -URA"},
                        {value: "CSM -LEU"},
                        {value: "CSM -TRP"},
                        {value: "CSM -URA"},
                        {value: "1/2 MS Hygro50"},
                        {value: "Other"}];

                    $scope.sampleTemp = $scope.tempRange[0];
                    $scope.userData = {
                        plateDescription: $scope.plateInformationOptions[0],
                        plateDescriptionText: undefined
                    };

                    $scope.hasComments = function () {
                        for (var i = 0; i < $scope.samples.length; i += 1) {
                            if ($scope.samples[i].comments.length)
                                return true;
                        }
                        return false;
                    };

                    $scope.addSampleToCart = function () {
                        var sampleSelection = {
                            requestType: $scope.userData.sampleType,
                            growthTemperature: $scope.sampleTemp.value,
                            partData: {
                                id: entryId
                            },
                            plateDescription: $scope.userData.plateDescription.value == "Other" ?
                                $scope.userData.plateDescriptionText : $scope.userData.plateDescription.value
                        };

                        // add selection to shopping cart
                        Util.post("rest/samples/requests", sampleSelection, function () {
                            $rootScope.$emit("SamplesInCart");
                            modalInstance.close('');
                        });
                    };

                    $scope.disableAddToCart = function () {
                        return !$scope.userData.sampleType || !$scope.userData.plateDescription.value ||
                            ($scope.userData.plateDescription.value == 'Other' && !$scope.userData.plateDescriptionText);
                    }
                },
                resolve: {
                    samples: function () {
                        return samples;
                    }
                }
            });
        };

        $scope.newSample = {
            open: {},
            depositor: {
                id: $scope.user.id,
                email: $scope.user.email
            },
            location: {}
        };

        $scope.format = "M/d/yyyy h:mm a";
        $scope.newSampleTemplate = "scripts/entry/sample/barcode-popover.html";
        $scope.enablePopup = function (row, col) {
            return $scope.newSample.open.cell === row + (10 + col + '').slice(-2);
        };

// add sample 96 well plate click
        $scope.cellBarcodeClick = function (row, col) { //todo: prevent the popover from opening for multiple wells
            var rc = row + (10 + col + '').slice(-2);
            $scope.newSample.open = {
                cell: rc
            };
        };

        $scope.delete = function (sample) {
            Util.remove('rest/parts/' + partId + '/samples/' + sample.id, {}, function () {
                refreshSamples();
            });
        };

        $scope.submitBarcode = function () {
            $scope.newSample.code = $scope.newSample.open.cell;
            $scope.newSample.location.child = {
                display: $scope.newSample.open.cell,
                type: 'WELL'
            };

            if ($scope.newSample.open.barcode) {
                $scope.newSample.location.child.child = {
                    display: $scope.newSample.open.barcode,
                    type: 'TUBE'
                }
            }
        };

        $scope.createNewSample = function () {
            // create sample
            Util.post('rest/parts/' + partId + '/samples', $scope.newSample, function (result) {
                $scope.samples = result.data;
                $scope.newSample = {
                    open: {},
                    depositor: {
                        id: $scope.user.id,
                        email: $scope.user.email
                    },
                    location: {}
                };
                refreshSamples();
            });
        };

        $scope.hasTube = function (row, col) {
            return check("TUBE", row, col);
        };

        $scope.hasWell = function (row, col) {
            return check("WELL", row, col);
        };

        var check = function (type, row, col) {
            var rc = row + (10 + col + '').slice(-2);
            if ($scope.newSample.code != rc)
                return false;

            var recurse = $scope.newSample.location;
            while (recurse != null) {
                if (recurse.type != type) {
                    recurse = recurse.child;
                    continue;
                }

                return true;
            }
            return false;
        };

        $scope.hasContent = function (row, col) {
            var rc = row + (10 + col + '').slice(-2);
            var recurse = $scope.newSample.location;
            while (recurse != null) {
                if (recurse.display == rc)
                    return true;

                recurse = recurse.child;
            }
            return false;
        };
    });
