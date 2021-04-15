'use strict';

angular.module('ice.entry.traces.controller', [])
    .controller('TraceSequenceController', function ($scope, $window, $cookies, $stateParams, FileUploader, $uibModal, Util) {
            var entryId = $stateParams.id;

            $scope.traceUploadError = undefined;
            $scope.maxSize = 5;
            $scope.tracesParams = { limit: 5, currentPage: 1, start: 0 };

            Util.get("/rest/parts/" + entryId + "/traces", function (result) {
                $scope.traces = result;
            }, $scope.tracesParams);

            $scope.tracesPageChanged = function () {
                $scope.tracesParams.start = ($scope.tracesParams.currentPage - 1) * $scope.tracesParams.limit;
                Util.get("/rest/parts/" + entryId + "/traces", function (result) {
                    $scope.traces = result;
                }, $scope.tracesParams);
            };

            $scope.showAddSangerTraceModal = function () {
                var modalInstance = $uibModal.open({
                    templateUrl: "scripts/entry/modal/add-sanger-trace.html",
                    controller: 'TraceSequenceUploadModalController',
                    backdrop: 'static',
                    resolve: {
                        entryId: function () {
                            return $stateParams.id;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    $scope.tracesParams.start = 0;

                    Util.get("/rest/parts/" + entryId + "/traces", function (result) {
                        Util.setFeedback("", "success");
                        $scope.traces = result;
                        $scope.showUploadOptions = false;
                        $scope.traceUploadError = false;
                    });
                });
            };

            $scope.downloadAllTraces = function () {
                var clickEvent = new MouseEvent("click", {
                    "view": window,
                    "bubbles": true,
                    "cancelable": false
                });

                Util.download("rest/parts/" + entryId + "/traces/all").$promise.then(function (result) {
                    var url = URL.createObjectURL(new Blob([result.data]));
                    var a = document.createElement('a');
                    a.href = url;
                    a.download = result.filename();
                    a.target = '_blank';
                    a.dispatchEvent(clickEvent);
                    $scope.selectedRequests = [];
                });
            };

            $scope.deleteTraceSequenceFile = function (fileId) {
                var foundTrace;
                var foundIndex;

                for (var i = 0; i < $scope.traces.data.length; i++) {
                    var trace = $scope.traces.data[i];
                    if (trace.fileId === fileId && trace.fileId != undefined) {
                        foundTrace = trace;
                        foundIndex = i;
                        break;
                    }
                }

                if (foundTrace != undefined) {
                    Util.remove("rest/parts/" + entryId + "/traces/" + foundTrace.id, {}, function (result) {
                        $scope.traces.data.splice(foundIndex, 1);
                        $scope.entryStatistics.sequenceCount = $scope.traces.data.length;
                    });
                }
            };

            $scope.downloadTraceFile = function (trace) {
                $window.open("rest/file/trace/" + trace.fileId + "?sid=" + $cookies.get("sessionId"), "_self");
            };

            var convertFeaturedDNASequence = function (result) {
                var features = [];

                for (var i = 0; i < result.features.length; i += 1) {
                    var feature = result.features[i];
                    if (!feature.locations.length)
                        continue;

                    var notes = feature.notes.length ? feature.notes[0].value : "";

                    for (var j = 0; j < feature.locations.length; j += 1) {
                        var location = feature.locations[j];

                        var featureObject = {
                            start: location.genbankStart - 1,
                            end: location.end - 1,
                            fid: feature.id,
                            forward: feature.strand == 1,
                            type: feature.type,
                            name: feature.name,
                            notes: notes,
                            annotationType: feature.type
                        };

                        features.push(featureObject);
                    }
                }

                return features;
            };


            var alignmentTracks = function (alignedSequence, referenceSequence) {
//            function magicDownload(text, fileName) {
//                let blob = new Blob([text], {
//                    type: "text/csv;charset=utf8;"
//                });
//
//// create hidden link
//                let element = document.createElement("a");
//                document.body.appendChild(element);
//                element.setAttribute("href", window.URL.createObjectURL(blob));
//                element.setAttribute("download", fileName);
//                element.style.display = "";
//
//                element.click();
//
//                document.body.removeChild(element);
//            }


                const alignment = {
                    id: "iceAlignment",
                    alignmentName: 'Untitled', // todo
                    alignmentAnnotationVisibility: {
                        "features": true,
                    },
                    pairwiseAlignments: []
                };

                // get reference sequences features
                var features = convertFeaturedDNASequence(referenceSequence);

                for (var i = 0; i < alignedSequence.length; i += 1) {
                    if (!alignedSequence[i].traceSequenceAlignment)
                        continue;

                    var pairwiseAlignment = [
                        // reference sequence
                        {
                            sequenceData: {
                                id: i + 1, // refSequence.identifier
                                name: referenceSequence.name,
                                sequence: referenceSequence.sequence, // raw sequence
                                features: features
                            },
                            alignmentData: {
                                id: i + 1,
                                sequence: alignedSequence[i].traceSequenceAlignment.queryAlignment,
                                matchStart: alignedSequence[i].traceSequenceAlignment.queryStart,
                                matchEnd: alignedSequence[i].traceSequenceAlignment.queryEnd
                            }
                        },
                        // alignment sequence
                        {
                            sequenceData: {
                                id: i + 1,
                                name: alignedSequence[i].filename,
                                sequence: alignedSequence[i].sequence     // raw sequence
                            },
                            alignmentData: {
                                id: i + 1,
                                sequence: alignedSequence[i].traceSequenceAlignment.subjectAlignment,
                                matchStart: alignedSequence[i].traceSequenceAlignment.subjectStart,
                                strand: alignedSequence[i].traceSequenceAlignment.strand,
                                matchEnd: alignedSequence[i].traceSequenceAlignment.subjectEnd
                            }
                        }
                    ];

                    alignment.pairwiseAlignments.push(pairwiseAlignment);

                    //if (alignedSequence[i].traceSequenceAlignment.subjectStart > alignedSequence[i].traceSequenceAlignment.subjectEnd) {
                    //    magicDownload(JSON.stringify(pairwiseAlignment), 'alignedSequence.json');
                    //}
                }

                return alignment;
            };

            $scope.fetchSequenceTraces = function () {
                $scope.matchingAlignmentsFound = true;
                Util.get("rest/parts/" + entryId + "/traces", function (result) {
                    if (result && result.data) {
                        Util.get("rest/parts/" + entryId + "/sequence", function (sequenceData) {
                            const alignments = alignmentTracks(result.data, sequenceData);
                            if (!alignments.pairwiseAlignments.length) {
                                $scope.matchingAlignmentsFound = false;
                                return;
                            }

                            $scope.loadSequenceChecker(alignments);

                        }, {}, function (error) {
                            console.error(error);
                            Util.setFeedback("Error retrieving alignments", "error");
                            $scope.matchingAlignmentsFound = false;
                        });
                    } else {
                        $scope.matchingAlignmentsFound = false;
                    }
                })
            };

            $scope.loadSequenceChecker = function (alignments) {
                $scope.checkerEditor = $window.createAlignmentView(document.getElementById("sequence-checker-root"),
                    alignments);
            }
        }
    )
    .controller('TraceSequenceUploadModalController', function ($scope, FileUploader, $uibModalInstance, entryId, Authentication) {
        $scope.traceSequenceUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/parts/" + entryId + "/traces",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": Authentication.getSessionId()
            },
            autoUpload: true,
            queueLimit: 1, // can only upload 1 file
            formData: [
                {
                    entryId: entryId
                }
            ]
        });

        $scope.traceSequenceUploader.onSuccessItem = function (item, response, status, headers) {
            if (status != "200") {
                $scope.traceUploadError = true;
                return;
            }

            $uibModalInstance.close();
        };

        $scope.traceSequenceUploader.onErrorItem = function (item, response, status, headers) {
            $scope.traceUploadError = true;
        };
    });
