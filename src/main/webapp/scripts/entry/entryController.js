'use strict';

angular.module('ice.entry.controller', [])
    .controller('EntryAttachmentController', function ($scope, $window, $cookieStore, $stateParams, $fileUploader, Attachment) {

        // create a uploader with options
        var sid = $cookieStore.get("sessionId");
        var attachment = Attachment(sid);

        var desc = "";
        $scope.$watch('attachmentDescription', function () {
            desc = $scope.attachmentDescription;
        });

        var uploader = $scope.uploader = $fileUploader.create({
            scope:$scope, // to automatically update the html. Default: $rootScope
            url:"/rest/file/attachment",
            method:'POST',
            removeAfterUpload:true,
            headers:{"X-ICE-Authentication-SessionId":sid}
        });


        uploader.bind('success', function (event, xhr, item, response) {
            response.description = desc;
            attachment.create({partId:$stateParams.id}, response,
                function (result) {
                    $scope.attachments.push(result);
                    $scope.cancel();
                });
        });

        uploader.bind('error', function (event, xhr, item, response) {
            console.error('Error', xhr, item, response);
        });

        $scope.cancel = function () {
            $scope.uploader.cancelAll();
            $scope.uploader.clearQueue();
            $scope.showAttachmentInput = false;
            $scope.attachmentDescription = undefined;
        };

        attachment.get({partId:$stateParams.id}, function (result) {
            $scope.attachments = result;
        });

        $scope.downloadAttachment = function (attachment) {
            $window.open("/rest/file/attachment/" + attachment.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };

        $scope.deleteAttachment = function (index, att) {
            attachment.delete({partId:$stateParams.id, attachmentId:att.id}, function (result) {
                confirmObject[index] = false;
                $scope.attachments.splice(index, 1);
            });
        };

        var confirmObject = {};
        $scope.confirmDelete = function (idx) {
            return confirmObject[idx];
        };

        $scope.setConfirmDelete = function (idx, value) {
            confirmObject[idx] = value;
        }
    })
    .controller('EntryCommentController', function ($scope, $cookieStore, $stateParams, Entry) {
        var entryId = $stateParams.id;
        var entry = Entry($cookieStore.get("sessionId"));

        entry.comments({partId:entryId}, function (result) {
            $scope.entryComments = result;
        });

        entry.samples({partId:entryId}, function (result) {
            $scope.entrySamples = result;
        });

        $scope.createComment = function () {
            entry.createComment({partId:entryId}, $scope.newComment, function (result) {
                $scope.entryComments.splice(0, 0, result);
                $scope.addComment = false;
                $scope.entryStatistics.commentCount = $scope.entryComments.length;
            }, function (error) {
                console.error("comment create error", error);
            });
        };
    })
    .controller('EntrySampleController', function ($scope, $modal, $cookieStore, $stateParams, Entry) {
        $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        $scope.openAddToCart = function () {
            var modalInstance = $modal.open({
                templateUrl:'/views/modal/sample-request.html'
            });

            modalInstance.result.then(function (selected) {
                console.log("selected", selected);
                $scope.$emit("SampleTypeSelected", selected);
                // "liquid" or "streak"

            }, function () {
                // dismiss callback
            });
        };

        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        entry.samples({partId:$stateParams.id}, function (result) {
            $scope.samples = result;
        });
    })
    .controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, $fileUploader, Entry) {
        var entryId = $stateParams.id;
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);

        entry.traceSequences({partId:entryId}, function (result) {
            $scope.traceSequences = result;
        });

        var uploader = $scope.traceSequenceUploader = $fileUploader.create({
            scope:$scope, // to automatically update the html. Default: $rootScope
            url:"/rest/part/" + entryId + "/traces",
            method:'POST',
            removeAfterUpload:true,
            headers:{"X-ICE-Authentication-SessionId":sid},
            autoUpload:true,
            queueLimit:1, // can only upload 1 file
            formData:[
                { entryId:entryId}
            ]
        });

        $scope.deleteTraceSequenceFile = function (fileId) {
            var foundTrace = undefined;
            var foundIndex = undefined;

            for (var i = 0; i < $scope.traceSequences.length; i++) {
                var trace = $scope.traceSequences[i];
                if (trace.fileId === fileId && trace.fileId != undefined) {
                    foundTrace = trace;
                    foundIndex = i;
                    break;
                }
            }

            if (foundTrace != undefined) {
                entry.deleteTraceSequence({partId:entryId, traceId:foundTrace.id}, function (result) {
                    $scope.traceSequences.splice(foundIndex, 1);
                    $scope.entryStatistics.traceSequenceCount = $scope.traceSequences.length;
                });
            }
        };

        $scope.downloadTraceFile = function (trace) {
            $window.open("/rest/file/trace/" + trace.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };
    })
    .controller('EntryExperimentController', function ($scope, $cookieStore, $stateParams, Entry) {
        var entryId = $stateParams.id;
        var entry = Entry($cookieStore.get("sessionId"));
        $scope.experiment = {};
        $scope.addExperiment = false;

        entry.experiments({partId:entryId}, function (result) {
            $scope.entryExperiments = result;
        });

        $scope.createExperiment = function () {
            if ($scope.experiment === undefined || $scope.experiment.url === undefined || $scope.experiment.url === ''
                || $scope.experiment.url.lastIndexOf('http', 0) !== 0) {
                $scope.urlMissing = true;
                return;
            }

            entry.createExperiment({partId:entryId}, $scope.experiment, function (result) {
                $scope.entryExperiments.splice(0, 0, result);
                $scope.addExperiment = false;
                $scope.entryStatistics.experimentalDataCount = $scope.entryExperiments.length;
            }, function (error) {
                console.error("experiment create error", error);
            });
        };
    })
    .controller('PartHistoryController', function ($scope, $window, $cookieStore, $stateParams, Entry) {
        var entryId = $stateParams.id;
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);

        entry.history({partId:entryId}, function (result) {
            $scope.history = result;
        });

        $scope.deleteHistory = function (history) {
            // todo : delete
        }
    });