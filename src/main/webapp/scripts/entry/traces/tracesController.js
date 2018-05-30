'use strict';

angular.module('ice.entry.traces.controller', [])
    .controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, FileUploader, $uibModal, Util, Authentication) {
        var entryId = $stateParams.id;

        $scope.traceUploadError = undefined;
        $scope.maxSize = 5;
        $scope.tracesParams = {limit: 5, currentPage: 1, start: 0};

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

            Util.download("rest/parts/" + entryId + "/traces/all?sid=" + Authentication.getSessionId()).$promise.then(function (result) {
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
            $window.open("rest/file/trace/" + trace.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };

        var alignmentTracks = function (alignedSequence, referenceSequence) {
            var alignment = {
                id: "iceAlignment",
                pairwiseAlignments: []
            };

            for (var i = 0; i < alignedSequence.length; i += 1) {
                if (!alignedSequence[i].traceSequenceAlignment)
                    continue;

                if (alignedSequence[i].traceSequenceAlignment.queryStart > alignedSequence[i].traceSequenceAlignment.queryEnd) {
                    console.log(alignedSequence[i].traceSequenceAlignment);
                    continue;
                }

                if (alignedSequence[i].traceSequenceAlignment.subjectStart > alignedSequence[i].traceSequenceAlignment.subjectEnd) {
                    console.log(alignedSequence[i].traceSequenceAlignment);
                    continue;
                }

                alignment.pairwiseAlignments.push(
                    [
                        // reference sequence
                        {
                            sequenceData: {
                                id: i + 1, // refSequence.identifier
                                name: referenceSequence.name,
                                sequence: referenceSequence.sequence // raw sequence
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
                                matchEnd: alignedSequence[i].traceSequenceAlignment.subjectEnd
                            }
                        }
                    ]
                );
            }

            return alignment;
        };

        $scope.fetchSequenceTraces = function () {
            Util.get("rest/parts/" + entryId + "/traces", function (result) {
                if (result && result.data) {
                    Util.get("rest/parts/" + entryId + "/sequence", function (sequenceData) {
                        $scope.loadSequenceChecker(alignmentTracks(result.data, sequenceData));
                    })
                }
            })
        };

        $scope.loadSequenceChecker = function (alignments) {
            $scope.checkerEditor = $window.createAlignmentView(document.getElementById("sequence-checker-root"),
                alignments);
        }
    })
    .controller('TraceSequenceUploadModalController', function ($scope, FileUploader, $uibModalInstance, entryId, Authentication) {
        $scope.cancelAddSangerTrace = function () {
            $uibModalInstance.dismiss('cancel');
        };

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
