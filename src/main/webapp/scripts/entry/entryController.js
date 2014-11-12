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
            headers:{
                "X-ICE-Authentication-SessionId":sid
            }
        });

        uploader.bind('success', function (event, xhr, item, response) {
            response.description = desc;
            attachment.create({
                    partId:$stateParams.id
                }, response,
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

        attachment.get({
            partId:$stateParams.id
        }, function (result) {
            $scope.attachments = result;
        });

        $scope.downloadAttachment = function (attachment) {
            $window.open("/rest/file/attachment/" + attachment.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };

        $scope.deleteAttachment = function (index, att) {
            attachment.delete({
                partId:$stateParams.id,
                attachmentId:att.id
            }, function (result) {
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

        entry.comments({
            partId:entryId
        }, function (result) {
            $scope.entryComments = result;
        });

        entry.samples({
            partId:entryId
        }, function (result) {
            $scope.entrySamples = result;
        });

        $scope.createComment = function () {
            entry.createComment({
                partId:entryId
            }, $scope.newComment, function (result) {
                $scope.entryComments.splice(0, 0, result);
                $scope.addComment = false;
                $scope.entryStatistics.commentCount = $scope.entryComments.length;
            }, function (error) {
                console.error("comment create error", error);
            });
        };

        $scope.updateComment = function (comment) {
            entry.updateComment({
                partId:entryId,
                commentId:comment.id
            }, comment, function (result) {
                if (result) {
                    comment.edit = false;
                    comment.modified = result.modified;
                }
            }, function (error) {
                console.error(error);
            })
        }
    })
    .controller('EntrySampleController', function ($rootScope, $scope, $modal, $cookieStore, $stateParams, Entry, Samples) {
        $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        var samples = Samples(sessionId);
        var partId = $stateParams.id;

        // retrieve samples for partId
        entry.samples({
            partId:partId
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

        $scope.openAddToCart = function () {
            var modalInstance = $modal.open({
                templateUrl:'/views/modal/sample-request.html'
            });

            modalInstance.result.then(function (selected) {
                var sampleSelection = {
                    requestType:selected,
                    partData:{
                        id:$scope.entry.id
                    }
                };

                // add selection to shopping cart
                samples.addRequestToCart({}, sampleSelection, function (result) {
                    $scope.$emit("SamplesInCart", result);
                    setInCart(result);
                });
            }, function () {
                // dismiss callback
            });
        };

        $scope.newSample = {
            open:{},
            depositor:{
                id:$scope.user.id,
                email:$scope.user.email
            },
            main:{}
        };

        $scope.format = "M/d/yyyy h:mm a";
        // add sample 96 well plate click
        $scope.cellBarcodeClick = function (row, col) {
            var rc = row + (10 + col + '').slice(-2);
            $scope.newSample.open = {
                cell:rc
            };
        };

        $scope.submitBarcode = function () {
            $scope.newSample.code = $scope.newSample.open.cell;
            $scope.newSample.main.child = {
                display:$scope.newSample.open.cell,
                type:'WELL'
            };

            if ($scope.newSample.open.barcode) {
                $scope.newSample.main.child.child = {
                    display:$scope.newSample.open.barcode,
                    type:'TUBE'
                }
            }

            $scope.newSample.open = {};
        };

        $scope.createNewSample = function () {
            $scope.newSample.main.type = "PLATE96";

            // create sample
            entry.addSample({partId:partId}, $scope.newSample, function (result) {
                $scope.samples = result;
                $scope.newSample = {
                    open:{},
                    depositor:{
                        id:$scope.user.id,
                        email:$scope.user.email
                    },
                    main:{}
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

            var recurse = $scope.newSample.main;
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
            var recurse = $scope.newSample.main;
            while (recurse != null) {
                if (recurse.display == rc)
                    return true;

                recurse = recurse.child;
            }
            return false;

//            newSample.open.cell === row + (10+col+'').slice(-2)
//            "main":{"child":{"display":"C04","type":"WELL","child":{"type":"TUBE"}}}
        }
    })
    .controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, $fileUploader, Entry) {
        var entryId = $stateParams.id;
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);
        $scope.traceUploadError = undefined;

        entry.traceSequences({
            partId:entryId
        }, function (result) {
            $scope.traceSequences = result;
        });

        var uploader = $scope.traceSequenceUploader = $fileUploader.create({
            scope:$scope, // to automatically update the html. Default: $rootScope
            url:"/rest/parts/" + entryId + "/traces",
            method:'POST',
            removeAfterUpload:true,
            headers:{
                "X-ICE-Authentication-SessionId":sid
            },
            autoUpload:true,
            queueLimit:1, // can only upload 1 file
            formData:[
                {
                    entryId:entryId
                }
            ]
        });

        uploader.bind('success', function (event, xhr, item, response) {
            console.log("response", response);
            entry.traceSequences({
                partId:entryId
            }, function (result) {
                $scope.traceSequences = result;
                $scope.showUploadOptions = false;
            });
        });

        uploader.bind('error', function (event, xhr, item, response) {
            console.error('Error', xhr, item, response);
            $scope.traceUploadError = true;
        });

        $scope.deleteTraceSequenceFile = function (fileId) {
            var foundTrace;
            var foundIndex;

            for (var i = 0; i < $scope.traceSequences.length; i++) {
                var trace = $scope.traceSequences[i];
                if (trace.fileId === fileId && trace.fileId != undefined) {
                    foundTrace = trace;
                    foundIndex = i;
                    break;
                }
            }

            if (foundTrace != undefined) {
                entry.deleteTraceSequence({
                    partId:entryId,
                    traceId:foundTrace.id
                }, function (result) {
                    $scope.traceSequences.splice(foundIndex, 1);
                    $scope.entryStatistics.traceSequenceCount = $scope.traceSequences.length;
                }, function (error) {
                    console.log(error);
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

        entry.experiments({
            partId:entryId
        }, function (result) {
            $scope.entryExperiments = result;
        });

        $scope.createExperiment = function () {
            if ($scope.experiment === undefined ||
                $scope.experiment.url === undefined ||
                $scope.experiment.url === '' ||
                $scope.experiment.url.lastIndexOf('http', 0) !== 0) {
                $scope.urlMissing = true;
                return;
            }

            entry.createExperiment({
                partId:entryId
            }, $scope.experiment, function (result) {
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

        entry.history({
            partId:entryId
        }, function (result) {
            $scope.history = result;
        });

        $scope.deleteHistory = function (history) {
            console.log(history);

            entry.deleteHistory({partId:entryId, historyId:history.id}, function (result) {
                var idx = $scope.history.indexOf(history);
                if (idx == -1)
                    return;

                $scope.history.splice(idx, 1);
            });
        }
    });