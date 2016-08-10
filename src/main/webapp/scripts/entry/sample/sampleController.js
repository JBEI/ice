'use strict';

angular.module('ice.entry.sample.controller', [])
    .controller('DisplaySampleController', function ($rootScope, $scope, SampleService) {
        $scope.Plate96Rows = SampleService.getPlate96Rows();
        $scope.Plate96Cols = SampleService.getPlate96Cols();

        $scope.canDelete = function () {
            return !$scope.remote && $rootScope.user && $rootScope.user.isAdmin;
        }
    })
    .controller('EntrySampleController', function ($location, $rootScope, $scope, $uibModal, $cookieStore, $stateParams,
                                                   Util, SampleService) {
        var sessionId = $cookieStore.get("sessionId");
        var partId = $stateParams.id;

        $scope.Plate96Rows = SampleService.getPlate96Rows();
        $scope.Plate96Cols = SampleService.getPlate96Cols();

        // retrieve samples for partId
        Util.list('rest/parts/' + partId + '/samples', function (result) {
            $scope.samples = result;
        });

        // marks the sample object "inCart" field if the data
        // contains the entry id of current part being viewed
        //var setInCart = function (data) {
        //    if (!data || !data.length) {
        //        $scope.samples[0].inCart = false;
        //        return;
        //    }
        //
        //    // check specific values added to cart
        //    for (var idx = 0; idx < data.length; idx += 1) {
        //        // using "==" instead of "===" since partId is a string
        //        if (data[idx].partData.id == partId) {
        //            $scope.samples[0].inCart = true;
        //            return;
        //        }
        //    }
        //
        //    // assuming not found
        //    $scope.samples[0].inCart = false;
        //};

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
                    $scope.sampleTemp = $scope.tempRange[0];

                    $scope.hasComments = function () {
                        for (var i = 0; i < $scope.samples.length; i += 1) {
                            if ($scope.samples[i].comments.length)
                                return true;
                        }
                        return false;
                    };

                    $scope.addSampleToCart = function (type, tmp) {
                        var sampleSelection = {
                            requestType: type,
                            growthTemperature: tmp.value,
                            partData: {
                                id: entryId
                            }
                        };

                        // add selection to shopping cart
                        Util.post("rest/samples/requests", sampleSelection, function () {
                            $rootScope.$emit("SamplesInCart");
                            modalInstance.close('');
                        });
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
                var idx = $scope.samples.indexOf(sample);
                $scope.samples.splice(idx, 1);
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

        // has either well or t
        $scope.hasContent = function (row, col) {
            var rc = row + (10 + col + '').slice(-2);
            var recurse = $scope.newSample.location;
            while (recurse != null) {
                if (recurse.display == rc)
                    return true;

                recurse = recurse.child;
            }
            return false;
        }
    });


