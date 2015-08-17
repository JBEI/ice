'use strict';

angular.module('ice.entry.sample.controller', [])
    .controller('DisplaySampleController', function ($rootScope, $scope) {
        $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        $scope.canDelete = function () {
            return !$scope.remote && $rootScope.user && $rootScope.user.isAdmin;
        }
    })
    .controller('EntrySampleController', function ($location, $rootScope, $scope, $modal, $cookieStore, $stateParams, Entry, Samples) {
        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        var partId = $stateParams.id;

        $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        // retrieve samples for partId
        entry.samples({
            partId: partId
        }, function (result) {
            $scope.samples = result;
        });

        // marks the sample object "inCart" field if the data
        // contains the entry id of current part being viewed
        var setInCart = function (data) {
            if (!data || !data.length) {
                $scope.samples[0].inCart = false;
                return;
            }

            // check specific values added to cart
            for (var idx = 0; idx < data.length; idx += 1) {
                // using "==" instead of "===" since partId is a string
                if (data[idx].partData.id == partId) {
                    $scope.samples[0].inCart = true;
                    return;
                }
            }

            // assuming not found
            $scope.samples[0].inCart = false;
        };

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
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/sample-request.html',
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
                        Samples(sessionId).addRequestToCart({}, sampleSelection, function (result) {
                            $rootScope.$emit("SamplesInCart", result);
                            setInCart(result);
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
        $scope.cellBarcodeClick = function (row, col) {
            var rc = row + (10 + col + '').slice(-2);
            $scope.newSample.open = {
                cell: rc
            };
        };

        $scope.delete = function (sample) {
            entry.deleteSample({partId: partId, sampleId: sample.id}, function (result) {
                console.log(result);
                var idx = $scope.samples.indexOf(sample);
                $scope.samples.splice(idx, 1);
                console.log("deleted", sample, idx);
            }, function (error) {
                console.log(error);
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
            $scope.createNewSample();
        };

        $scope.createNewSample = function () {
            // create sample
            entry.addSample({partId: partId}, $scope.newSample, function (result) {
                $scope.samples = result;
                $scope.newSample = {
                    open: {},
                    depositor: {
                        id: $scope.user.id,
                        email: $scope.user.email
                    },
                    location: {}
                };
            }, function (error) {
                console.error(error);
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


