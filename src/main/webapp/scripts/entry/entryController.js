'use strict';

angular.module('ice.entry.controller', [])
    .controller('EntryAttachmentController', function ($scope, $window, $cookieStore, $stateParams, FileUploader, Attachment) {

        // create a uploader with options
        var sid = $cookieStore.get("sessionId");
        var attachment = Attachment(sid);

        var desc = "";
        $scope.$watch('attachmentDescription', function () {
            desc = $scope.attachmentDescription;
        });

        var uploader = $scope.uploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "/rest/file/attachment",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": sid
            }
        });

        uploader.onSuccessItem = function (item, response, status, headers) {
            response.description = desc;
            attachment.create({
                    partId: $stateParams.id
                }, response,
                function (result) {
                    $scope.attachments.push(result);
                    $scope.cancel();
                });
        };

        $scope.cancel = function () {
            $scope.uploader.cancelAll();
            $scope.uploader.clearQueue();
            $scope.showAttachmentInput = false;
            $scope.attachmentDescription = undefined;
        };

        attachment.get({
            partId: $stateParams.id
        }, function (result) {
            $scope.attachments = result;
        });

        $scope.downloadAttachment = function (attachment) {
            $window.open("/rest/file/attachment/" + attachment.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };

        $scope.deleteAttachment = function (index, att) {
            attachment.delete({
                partId: $stateParams.id,
                attachmentId: att.id
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
            partId: entryId
        }, function (result) {
            $scope.entryComments = result;
        });

        entry.samples({
            partId: entryId
        }, function (result) {
            $scope.entrySamples = result;
        });

        $scope.createComment = function () {
            entry.createComment({
                partId: entryId
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
                partId: entryId,
                commentId: comment.id
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
    .controller('EntrySampleController', function ($location, $rootScope, $scope, $modal, $cookieStore, $stateParams, Entry, Samples) {
        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        var samples = Samples(sessionId);
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

        $scope.openAddToCart = function (entryId) {
            var modalInstance = $modal.open({
                templateUrl: '/views/modal/sample-request.html',
                controller: function ($scope) {
                    $scope.tempRange = [{value: 30}, {value: 37}];
                    $scope.sampleTemp = $scope.tempRange[0];

                    $scope.addSampleToCart = function (type, tmp) {
                        var sampleSelection = {
                            requestType: type,
                            growthTemperature: tmp.value,
                            partData: {
                                id: entryId
                            }
                        };

                        // add selection to shopping cart
                        samples.addRequestToCart({}, sampleSelection, function (result) {
                            $rootScope.$emit("SamplesInCart", result);
                            setInCart(result);
                            modalInstance.close('');
                        });
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

            $scope.newSample.open = {};
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
    })
    .controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, FileUploader, Entry) {
        var entryId = $stateParams.id;
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);
        $scope.traceUploadError = undefined;

        entry.traceSequences({
            partId: entryId
        }, function (result) {
            $scope.traceSequences = result;
        });

        var uploader = $scope.traceSequenceUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "/rest/parts/" + entryId + "/traces",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": sid
            },
            autoUpload: true,
            queueLimit: 1, // can only upload 1 file
            formData: [
                {
                    entryId: entryId
                }
            ]
        });

        uploader.onSuccessItem = function (item, response, status, headers) {
            console.log("response", response);
            entry.traceSequences({
                partId: entryId
            }, function (result) {
                $scope.traceSequences = result;
                $scope.showUploadOptions = false;
            });
        };

        uploader.onSuccessItem = function (item, response, status, headers) {
            $scope.traceUploadError = true;
        };

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
                    partId: entryId,
                    traceId: foundTrace.id
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
            partId: entryId
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
                partId: entryId
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
            partId: entryId
        }, function (result) {
            $scope.history = result;
        });

        $scope.deleteHistory = function (history) {
            entry.deleteHistory({partId: entryId, historyId: history.id}, function (result) {
                var idx = $scope.history.indexOf(history);
                if (idx == -1)
                    return;

                $scope.history.splice(idx, 1);
            });
        }
    })
    .controller('EditEntryController',
    function ($scope, $http, $location, $cookieStore, $rootScope, $stateParams, Entry, EntryService) {

        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);
        var partLinks;
        $scope.entry = undefined;

        entry.query({partId: $stateParams.id}, function (result) {
            $scope.entry = EntryService.convertToUIForm(result);
            partLinks = angular.copy($scope.entry.linkedParts);
            $scope.entry.linkedParts = [];

            // todo : this is used in other places and should be in a service
            // convert selection markers from array of strings to array of objects for the ui
            var arrayLength = result.selectionMarkers.length;
            if (arrayLength) {
                var tmp = [];
                for (var i = 0; i < arrayLength; i++) {
                    tmp.push({value: result.selectionMarkers[i]});
                }
                angular.copy(tmp, $scope.entry.selectionMarkers);
            } else {
                $scope.entry.selectionMarkers = [
                    {}
                ];
            }

            // convert links from array of strings to array of objects for the ui
            var linkLength = result.links.length;

            if (linkLength) {
                var tmpLinkObjectArray = [];
                for (var j = 0; j < linkLength; j++) {
                    tmpLinkObjectArray.push({value: result.links[j]});
                }
                angular.copy(tmpLinkObjectArray, $scope.entry.links);
            } else {
                $scope.entry.links = [
                    {}
                ];
            }

            if (result.bioSafetyLevel)
                $scope.entry.bioSafetyLevel = "Level " + result.bioSafetyLevel;
            $scope.linkOptions = EntryService.linkOptions(result.type);
            $scope.selectedFields = EntryService.getFieldsForType(result.type);
            $scope.activePart = $scope.entry;
            console.log($scope.activePart);
        });

        $scope.cancelEdit = function () {
            $location.path("/entry/" + $stateParams.id);
        };

        $scope.getLocation = function (inputField, val) {   // todo : move to service
            return $http.get('/rest/parts/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    val: val,
                    field: inputField
                }
            }).then(function (res) {
                return res.data;
            });
        };

        // difference between this and getLocation() is getLocation() returns a list of strings
        // and this returns a list of objects
        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('/rest/parts/autocomplete/partid', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    token: val
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.addExistingPartLink = function ($item, $model, $label) {
            entry.query({partId: $model.id}, function (result) {
                $scope.activePart = result;

                // convert selection markers from array of strings to array of objects for the ui
                var arrayLength = result.selectionMarkers.length;
                if (arrayLength) {
                    var tmp = [];
                    for (var i = 0; i < arrayLength; i++) {
                        tmp.push({value: result.selectionMarkers[i]});
                    }
                    angular.copy(tmp, $scope.activePart.selectionMarkers);
                } else {
                    $scope.activePart.selectionMarkers = [
                        {}
                    ];
                }

                // convert links from array of strings to array of objects for the ui
                var linkLength = result.links.length;

                if (linkLength) {
                    var tmpLinkObjectArray = [];
                    for (var j = 0; j < linkLength; j++) {
                        tmpLinkObjectArray.push({value: result.links[j]});
                    }
                    angular.copy(tmpLinkObjectArray, $scope.activePart.links);
                } else {
                    $scope.activePart.links = [
                        {}
                    ];
                }

                $scope.activePart.isExistingPart = true;
                $scope.activePart.fields = EntryService.getFieldsForType($scope.activePart.type);
                $scope.addExisting = false;
                $scope.entry.linkedParts.push($scope.activePart);

                $scope.colLength = 11 - $scope.entry.linkedParts.length;
                $scope.active = $scope.entry.linkedParts.length - 1;
            });
        };

        // todo : this is pretty much a copy of submitPart in CreateEntryController
        $scope.editEntry = function () {
            var canSubmit = EntryService.validateFields($scope.entry, $scope.selectedFields);
            $scope.entry.type = $scope.entry.type.toUpperCase();


            // validate contained parts, if any
            if ($scope.entry.linkedParts && $scope.entry.linkedParts.length) {
                for (var idx = 0; idx < $scope.entry.linkedParts.length; idx += 1) {
                    var canSubmitLinked = EntryService.validateFields($scope.entry.linkedParts[idx], $scope.selectedFields);
                    if (!canSubmitLinked) {
                        // show icon in tab
                        console.log("linked entry at idx " + idx + " is not valid", $scope.entry);
                        canSubmit = canSubmitLinked;
                    }
                }
            }

            if (!canSubmit) {
                $("body").animate({scrollTop: 130}, "slow");
                return;
            }

            if ($scope.entry.bioSafetyLevel === 'Level 1')
                $scope.entry.bioSafetyLevel = 1;
            else
                $scope.entry.bioSafetyLevel = 2;

            // convert arrays of objects to array strings
            $scope.entry.links = EntryService.toStringArray($scope.entry.links);
            $scope.entry.selectionMarkers = EntryService.toStringArray($scope.entry.selectionMarkers);

            //for (var i = 0; i < $scope.entry.linkedParts.length; i += 1) {
            //    $scope.entry.linkedParts[i].links = EntryService.toStringArray($scope.entry.linkedParts[i].links);
            //    $scope.entry.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.entry.linkedParts[i].selectionMarkers);
            //}

            // convert the part to a form the server can work with
            $scope.entry = EntryService.getTypeData($scope.entry);
            $scope.entry.linkedParts = partLinks;

            entry.update({partId: $scope.entry.id}, $scope.entry, function (result) {
                $location.path("/entry/" + result.id);
            });
        };

        $scope.setActive = function (index) {
            if (!isNaN(index) && $scope.entry.linkedParts.length <= index)
                return;

            $scope.active = index;
            if (isNaN(index))
                $scope.activePart = $scope.entry;
            else
                $scope.activePart = $scope.entry.linkedParts[index];
            $scope.activePart.fields = EntryService.getFieldsForType($scope.activePart.type);
        };
    })
    .controller('CreateEntryController', function ($http, $scope, $modal, $rootScope, FileUploader, $location,
                                                   $stateParams, $cookieStore, Entry, EntryService) {
        $scope.createType = $stateParams.type;
        $scope.showMain = true;

        // generate the various link options for selected option
        $scope.linkOptions = EntryService.linkOptions($scope.createType.toLowerCase());
        var sid = $cookieStore.get("sessionId");
        var entry = Entry(sid);

        // retrieves the defaults for the specified type. Note that $scope.part is the main part
        var getPartDefaults = function (type, isMain) {
            entry.query({partId: type}, function (result) {
                if (isMain) { // or if !$scope.part
                    $scope.part = result;
                    $scope.part = EntryService.setNewEntryFields($scope.part);
                    $scope.part.linkedParts = [];
                    $scope.activePart = $scope.part;
                    $scope.part.fields = EntryService.getFieldsForType($scope.createType);
                } else {
                    var newPart = result;
                    newPart = EntryService.setNewEntryFields(newPart);
                    newPart.fields = EntryService.getFieldsForType(type);
                    $scope.part.linkedParts.push(newPart);

                    $scope.colLength = 11 - $scope.part.linkedParts.length;
                    $scope.active = $scope.part.linkedParts.length - 1;
                    $scope.activePart = $scope.part.linkedParts[$scope.active];
                }
            }, function (error) {
                console.log("Error: " + error);
            });
        };

        // init : get defaults for main entry
        getPartDefaults($scope.createType, true);

        $scope.addLink = function (schema) {
            $scope.part[schema].push({value: ''});
        };

        $scope.removeLink = function (schema, index) {
            $scope.part[schema].splice(index, 1);
        };

        $scope.addNewPartLink = function (type) {
            getPartDefaults(type, false);
        };

        $scope.addExistingPartLink = function ($item, $model, $label) {
            entry.query({partId: $model.id}, function (result) {
                $scope.activePart = result;
                $scope.activePart.isExistingPart = true;
                if (!$scope.activePart.parameters)
                    $scope.activePart.parameters = [];
                $scope.addExisting = false;
                $scope.activePart.fields = EntryService.getFieldsForType($scope.activePart.type);
                $scope.part.linkedParts.push($scope.activePart);

                $scope.colLength = 11 - $scope.part.linkedParts.length;
                $scope.active = $scope.part.linkedParts.length - 1;
            });
        };

        $scope.deleteNewPartLink = function (linkedPart) {
            var indexOf = $scope.part.linkedParts.indexOf(linkedPart);
            if (indexOf < 0 || indexOf >= $scope.part.linkedParts.length)
                return;

            console.log("delete", linkedPart, "at", indexOf);
            $scope.part.linkedParts.splice(indexOf, 1);

            // todo: will need to actually delete it if has an id (linkedPart.id)

            // remove from array of linked parts
            if ($scope.active === indexOf) {
                var newActive;
                // set new active
                console.log("set new active", $scope.part.linkedParts.length);
                if (indexOf + 1 < $scope.part.linkedParts.length)
                    newActive = indexOf + 1;
                else {
                    if ($scope.part.linkedParts.length === 0)
                    // not really needed since when main will not be shown if no other tabs present
                        newActive = 'main';
                    else
                        newActive = indexOf - 1;
                }
                $scope.setActive(newActive);
            }
        };

        $scope.active = 'main';
        $scope.setActive = function (index) {
            if (!isNaN(index) && $scope.part.linkedParts.length <= index)
                return;

            $scope.active = index;
            if (isNaN(index))
                $scope.activePart = $scope.part;
            else
                $scope.activePart = $scope.part.linkedParts[index];
            //$scope.selectedFields = EntryService.getFieldsForType($scope.activePart.type);
        };

        $scope.getLocation = function (inputField, val) {
            return $http.get('/rest/parts/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    val: val,
                    field: inputField
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.submitPart = function () {
            var errorTabActive = false;

            // validate main part
            var canSubmit = EntryService.validateFields($scope.part, $scope.part.fields);
            if (!canSubmit) {
                $scope.setActive('main');
                errorTabActive = true;
            }
            $scope.part.type = $scope.part.type.toUpperCase();

            // validate contained parts, if any
            if ($scope.part.linkedParts && $scope.part.linkedParts.length) {
                for (var idx = 0; idx < $scope.part.linkedParts.length; idx += 1) {
                    var linkedPart = $scope.part.linkedParts[idx];

                    // do not attempt to validate existing part
                    if (linkedPart.isExistingPart)
                        continue;

                    //$scope.selectedFields = EntryService.getFieldsForType(linkedPart.type);

                    var canSubmitLinked = EntryService.validateFields(linkedPart, linkedPart.fields);
                    if (!canSubmitLinked) {
                        if (!errorTabActive) {
                            $scope.setActive(idx);
                            errorTabActive = true;
                        }
                        canSubmit = canSubmitLinked;
                    }
                }
            }

            if (!canSubmit) {
                $("body").animate({scrollTop: 130}, "slow");
                return;
            }

            // convert arrays of objects to array strings
            $scope.part.links = EntryService.toStringArray($scope.part.links);
            $scope.part.selectionMarkers = EntryService.toStringArray($scope.part.selectionMarkers);

            for (var i = 0; i < $scope.part.linkedParts.length; i += 1) {
                $scope.part.linkedParts[i].links = EntryService.toStringArray($scope.part.linkedParts[i].links);
                $scope.part.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.part.linkedParts[i].selectionMarkers);
            }

            // convert the part to a form the server can work with
            $scope.part = EntryService.getTypeData($scope.part);

            // create or update the part depending on whether there is a current part id
            if ($scope.part.id) {
                entry.update({partId: $scope.part.id}, $scope.part, function (result) {
                    $location.path('/entry/' + result.id);
                });
            } else {
                entry.create($scope.part, function (result) {
                    $scope.$emit("UpdateCollectionCounts");
                    $location.path('/entry/' + result.id);
                    $scope.showSBOL = false;
                }, function (error) {
                    console.error(error);
                });
            }
        };

        $scope.format = 'MMM d, yyyy h:mm:ss a';

        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('/rest/parts/autocomplete/partid', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    token: val
                }
            }).then(function (res) {
                return res.data;
            });
        };

        // for the date picker TODO : make it a directive ???
        $scope.today = function () {
            $scope.dt = new Date();
        };
        $scope.today();

        $scope.clear = function () {
            $scope.dt = null;
        };

        $scope.addCustomParameter = function () {
            $scope.activePart.parameters.push({key: '', value: ''});
        };

        $scope.removeCustomParameter = function (index) {
            $scope.activePart.parameters.splice(index, 1);
        };

        $scope.dateOptions = {
            'year-format': "'yy'",
            'starting-day': 1
        };

        $scope.cancelEntryCreate = function () {
            $location.path("/folders/personal");
        };

        // file upload
        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "/rest/file/sequence",
            method: 'POST',
//        formData:[{entryType:type}],
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": sid},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        $scope.uploadFile = function () {
            if (!$scope.isPaste) {
                uploader.queue[0].upload();
            } else {
                console.log($scope.pastedSequence);
            }
        };

        // REGISTER HANDLERS
        uploader.onAfterAddingAll = function (item) {
            $scope.serverError = false;
        };

        uploader.onBeforeUploadItem = function (item) {
            var entryTypeForm;
            if ($scope.active === 'main')
                entryTypeForm = {entryType: $scope.part.type.toUpperCase()};
            else
                entryTypeForm = {entryType: $scope.part.linkedParts[$scope.active].type};
            item.formData.push(entryTypeForm);
        };

        uploader.onProgressItem = function (item, progress) {
            if (progress != "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        };

        uploader.onSuccessItem = function (item, response, status, headers) {
            if ($scope.active === undefined || isNaN($scope.active)) {
                // set main entry id
                $scope.part.id = response.entryId;
                $scope.part.hasSequence = true;
            } else {
                // set linked parts id
                $scope.part.linkedParts[$scope.active].id = response.entryId;
                $scope.part.linkedParts[$scope.active].hasSequence = true;
            }

            $scope.activePart.hasSequence = true;
        };

        uploader.onErrorItem = function (item, response, status, headers) {
            item.remove();
            $scope.serverError = true;
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
        };
    })

    .
    controller('SequenceFileUploadController', function ($scope, $cookieStore, $modal, $modalInstance, FileUploader, type, paste) {
        console.log("SequenceFileUploadController");
        var sid = $cookieStore.get("sessionId");
        $scope.isPaste = paste;
        $scope.headerText = paste ? "Paste Sequence" : "Upload Sequence file";

        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "/rest/file/sequence",
            method: 'POST',
            formData: [
                {
                    entryType: type
                }
            ],
//        removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": sid},
//        autoUpload: true
            queueLimit: 1 // can only upload 1 file
        });

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.uploadFile = function () {
            if (!$scope.isPaste) {
                uploader.queue[0].upload();
            } else {
                console.log($scope.pastedSequence);
            }
        };

        // REGISTER HANDLERS
        uploader.onAfterAddingFile = function (item) {
            $scope.$emit("FileAdd", item);
        };

        uploader.onErrorItem = function (item, response, status, headers) {
            item.remove();
            $scope.serverError = true;
        };
    }).controller('EntryPermissionController', function ($rootScope, $scope, $cookieStore, User, Entry, Group, filterFilter, Permission) {
        var sessionId = $cookieStore.get("sessionId");
        var entry = Entry(sessionId);
        var panes = $scope.panes = [];
        $scope.userFilterInput = undefined;

        $scope.activateTab = function (pane) {
            angular.forEach(panes, function (pane) {
                pane.selected = false;
            });
            pane.selected = true;
            if (pane.title === 'Read')
                $scope.activePermissions = $scope.readPermissions;
            else
                $scope.activePermissions = $scope.writePermissions;
        };

        // retrieve permissions
        entry.permissions({partId: $scope.entry.id}, function (result) {
            $scope.readPermissions = [];
            $scope.writePermissions = [];

            angular.forEach(result, function (item) {
                if (item.type === 'WRITE_ENTRY')
                    $scope.writePermissions.push(item);
                else
                    $scope.readPermissions.push(item);
            });

            $scope.panes.push({title: 'Read', count: $scope.readPermissions.length, selected: true});
            $scope.panes.push({title: 'Write', count: $scope.writePermissions.length});

            $scope.activePermissions = $scope.readPermissions;
        });

        $scope.filter = function () {
            var val = $scope.userFilterInput;
            if (!val) {
                $scope.accessPermissions = undefined;
                return;
            }

            $scope.filtering = true;
            Permission().filterUsersAndGroups({limit: 10, val: val},
                function (result) {
                    $scope.accessPermissions = result;
                    $scope.filtering = false;
                }, function (error) {
                    $scope.filtering = false;
                    $scope.accessPermissions = undefined;
                });
        };

        $scope.showAddPermissionOptionsClick = function () {
            $scope.showPermissionInput = true;
        };

        $scope.closePermissionOptions = function () {
            $scope.showPermissionInput = false;
        };

        var removePermission = function (permissionId) {
            entry.removePermission({partId: $scope.entry.id, permissionId: permissionId},
                function (result) {
                    if (!result)
                        return;

                    // check which pane is selected
                    var pane;
                    if ($scope.panes[0].selected)
                        pane = $scope.panes[0];
                    else
                        pane = $scope.panes[1];

                    var i = -1;

                    for (var idx = 0; idx < $scope.activePermissions.length; idx += 1) {
                        if (permissionId == $scope.activePermissions[idx].id) {
                            i = idx;
                            break;
                        }
                    }

                    if (i == -1) {
                        console.log("not found");
                        return;
                    }

                    $scope.activePermissions.splice(i, 1);
                    pane.count = $scope.activePermissions.length;
                });
        };

        //
        // when user clicks on the check box, removes permission if exists or adds if not
        //
        $scope.addRemovePermission = function (permission) {
            permission.selected = !permission.selected;
            if (!permission.selected) {
                removePermission(permission.id);
                return;
            }

            // add permission
            for (var i = 0; i < panes.length; i += 1) {
                if (panes[i].selected) {
                    permission.type = panes[i].title.toUpperCase() + "_ENTRY";
                    break;
                }
            }

            permission.typeId = $scope.entry.id;

            entry.addPermission({partId: $scope.entry.id}, permission, function (result) {
                // result is the permission object
                $scope.entry.id = result.typeId;
                if (result.type == 'READ_ENTRY') {
                    $scope.readPermissions.push(result);
                    $scope.activePermissions = $scope.readPermissions;
                }
                else {
                    $scope.writePermissions.push(result);
                    $scope.activePermissions = $scope.writePermissions;
                }

                permission.id = result.id;
            });
        };

        $scope.enablePublicRead = function (e) {
            entry.enablePublicRead(e, function (result) {
                $scope.entry.publicRead = true;
            })
        };

        $scope.disablePublicRead = function (e) {
            entry.disablePublicRead({partId: e.id}, function (result) {
                $scope.entry.publicRead = false;
            })
        };

        $scope.deletePermission = function (index, permission) {
            removePermission(permission.id);
        };
    })

    .controller('EntryDetailsController', function ($scope) {
        console.log("EntryDetailsController");
        var entryPanes = $scope.entryPanes = [];

        $scope.showPane = function (pane) {
            console.log("activate details", entryPanes.length);
            angular.forEach(entryPanes, function (pane) {
                pane.selected = false;
            });
            pane.selected = true;
        };

        this.addEntryPane = function (pane) {
            // activate the first pane that is added
            if (entryPanes.length == 0)
                $scope.activateTab(pane);
            entryPanes.push(pane);
        };
    })

    .controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $modal, $rootScope,
                                             FileUploader, Entry, Folders, EntryService, EntryContextUtil, Selection) {
        $scope.partIdEditMode = false;
        $scope.showSBOL = true;
        $scope.context = EntryContextUtil.getContext();

        $scope.isFileUpload = false;

        var sessionId = $cookieStore.get("sessionId");
        $scope.sessionId = sessionId;

        $scope.open = function () {
            window.open('/static/swf/ve/VectorEditor?entryId=' + $scope.entry.id + '&sessionId=' + sessionId);
        };

        $scope.sequenceUpload = function (type) {
            if (type === 'file') {
                $scope.isFileUpload = true;
                $scope.isPaste = false;
            }
            else {
                $scope.isPaste = true;
                $scope.isFileUpload = false;
            }
        };

        $scope.processPastedSequence = function (event, part) {
            var sequenceString = event.originalEvent.clipboardData.getData('text/plain');
            entry.addSequenceAsString({partId: part.id}, {sequence: sequenceString}, function (result) {
                part.hasSequence = true;
            }, function (error) {
                console.log("error", error);
            });
        };

        $scope.deleteSequence = function (part) {
            var modalInstance = $modal.open({
                templateUrl: '/views/modal/delete-sequence-confirmation.html',
                controller: function ($scope, $modalInstance) {
                    $scope.toDelete = part;
                    $scope.processingDelete = undefined;
                    $scope.delete = function () {
                        $scope.processingDelete = true;
                        entry.deleteSequence({partId: part.id}, function (result) {
                            $scope.processingDelete = false;
                            $modalInstance.close(part);
                        }, function (error) {
                            console.error(error);
                        })
                    }
                },
                backdrop: "static"
            });

            modalInstance.result.then(function (part) {
                if (part)
                    part.hasSequence = false;
            }, function () {
            });
        };

        var entry = Entry(sessionId);

        $scope.addLink = function (part) {

            var modalInstance = $modal.open({
                templateUrl: '/views/modal/add-link-modal.html',
                controller: function ($scope, $http, $modalInstance, $cookieStore) {
                    $scope.mainEntry = part;
                    var sessionId = $cookieStore.get("sessionId");
                    var originalLinks = angular.copy($scope.mainEntry.linkedParts);
                    $scope.getEntriesByPartNumber = function (val) {
                        return $http.get('/rest/parts/autocomplete/partid', {
                            headers: {'X-ICE-Authentication-SessionId': sessionId},
                            params: {
                                token: val
                            }
                        }).then(function (res) {
                            return res.data;
                        });
                    };

                    $scope.addExistingPartLink = function ($item, $model, $label) {
                        if ($item.id == $scope.mainEntry.id)
                            return;

                        var found = false;
                        angular.forEach($scope.mainEntry.linkedParts, function (t) {
                            if (t.id === $item.id) {
                                found = true;
                            }
                        });

                        if (found)
                            return;
                        $scope.mainEntry.linkedParts.push($item);
                        $scope.addExistingPartNumber = undefined;
                    };

                    $scope.removeExistingPartLink = function (link) {
                        var i = $scope.mainEntry.linkedParts.indexOf(link);
                        if (i < 0)
                            return;

                        $scope.mainEntry.linkedParts.splice(i, 1);
                    };

                    $scope.processLinkAdd = function () {
                        entry.update($scope.mainEntry, function (result) {
                            entry.query({partId: result.id}, function (result) {
                                $scope.mainEntry.linkedParts = result.linkedParts;
                                $modalInstance.close(result);
                            }, function (error) {
                                console.error(error);
                            })
                        }, function (error) {
                            console.error(error);
                        })
                    };

                    $scope.cancelAddLink = function () {
                        $scope.mainEntry.linkedParts = originalLinks;
                        $modalInstance.close();
                    }
                },
                backdrop: "static"
            });

            modalInstance.result.then(function (entry) {
                if (entry) {
                    part = entry;
                }
            }, function () {
            });
        };

        var partDefaults = {
            type: $scope.createType,
            links: [
                {}
            ],
            selectionMarkers: [
                {}
            ],
            bioSafetyLevel: '1',
            status: 'Complete',
            creator: $scope.user.firstName + ' ' + $scope.user.lastName,
            creatorEmail: $scope.user.email
        };

        $scope.part = angular.copy(partDefaults);

        $scope.sbolShowHide = function () {
            $scope.showSBOL = !$scope.showSBOL;
        };

        $scope.entryFields = undefined;
        $scope.entry = undefined;
        $scope.notFound = undefined;
        $scope.noAccess = undefined;

        entry.query({partId: $stateParams.id},
            function (result) {
                Selection.reset();
                Selection.selectEntry(result);

                $scope.entry = EntryService.convertToUIForm(result);
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());

                entry.statistics({partId: $stateParams.id}, function (stats) {
                    $scope.entryStatistics = stats;
                });
            }, function (error) {
                if (error.status === 404)
                    $scope.notFound = true;
                else if (error.status === 403)
                    $scope.noAccess = true;
            });

        var menuSubDetails = $scope.subDetails = [
            {
                url: '/scripts/entry/general-information.html',
                display: 'General Information',
                isPrivileged: false,
                icon: 'fa-exclamation-circle'
            },
            {
                id: 'sequences',
                url: '/scripts/entry/sequence-analysis.html',
                display: 'Sequence Analysis',
                isPrivileged: false,
                countName: 'traceSequenceCount',
                icon: 'fa-search-plus'
            },
            {
                id: 'comments',
                url: '/scripts/entry/comments.html',
                display: 'Comments',
                isPrivileged: false,
                countName: 'commentCount',
                icon: 'fa-comments-o'
            },
            {
                id: 'samples',
                url: '/scripts/entry/samples.html',
                display: 'Samples',
                isPrivileged: false,
                countName: 'sampleCount',
                icon: 'fa-flask'
            },
            {
                id: 'history',
                url: '/scripts/entry/history.html',
                display: 'History',
                isPrivileged: true,
                countName: 'historyCount',
                icon: 'fa-history'
            },
            {
                id: 'experiments',
                url: '/scripts/entry/experiments.html',
                display: 'Experimental Data',
                isPrivileged: false,
                countName: 'experimentalDataCount',
                icon: 'fa-magic'
            }
        ];

        $scope.showSelection = function (index) {
            angular.forEach(menuSubDetails, function (details) {
                details.selected = false;
            });
            menuSubDetails[index].selected = true;
            $scope.selection = menuSubDetails[index].url;
            if (menuSubDetails[index].id) {
                $location.path("/entry/" + $stateParams.id + "/" + menuSubDetails[index].id);
            } else {
                $location.path("/entry/" + $stateParams.id);
            }
        };

        // check if a selection has been made
        var menuOption = $stateParams.option;
        if (menuOption === undefined) {
            $scope.selection = menuSubDetails[0].url;
            menuSubDetails[0].selected = true;
        } else {
            menuSubDetails[0].selected = false;
            for (var i = 1; i < menuSubDetails.length; i += 1) {
                if (menuSubDetails[i].id === menuOption) {
                    $scope.selection = menuSubDetails[i].url;
                    menuSubDetails[i].selected = true;
                    break;
                }
            }

            if ($scope.selection === undefined) {
                $scope.selection = menuSubDetails[0].url;
                menuSubDetails[0].selected = true;
            }
        }

        $scope.edit = function (type, val) {
            $scope[type] = val;
        };

        $scope.quickEdit = {};

        $scope.quickEditEntry = function (field) {
            // dirty is used to flag that the field's value has been modified to
            // prevent saving unchanged values on blur

            field.errorUpdating = false;
            if (!field.dirty) {
                return;
            }

            field.updating = true;

            // update the main entry with quickEdit (which is the model)
            $scope.entry[field.schema] = $scope.quickEdit[field.schema];
            if (field.inputType === 'withEmail') {
                $scope.entry[field.schema + 'Email'] = $scope.quickEdit[field.schema + 'Email'];
            }

            $scope.entry = EntryService.getTypeData($scope.entry);

            entry.update($scope.entry, function (result) {
                field.edit = false;

                if (result)
                    $scope.entry = EntryService.convertToUIForm(result);

                field.dirty = false;
                field.updating = false;
            }, function (error) {
                field.updating = false;
                field.errorUpdating = true;
            });
        };

        $scope.nextEntryInContext = function () {
            $scope.context.offset += 1;
            $scope.context.callback($scope.context.offset, function (result) {
                $location.path("/entry/" + result);
            });
        };

        $scope.prevEntryInContext = function () {
            $scope.context.offset -= 1;
            $scope.context.callback($scope.context.offset, function (result) {
                $location.path("/entry/" + result);
            });
        };

        $scope.backTo = function () {
            Selection.reset();
            $location.path($scope.context.back);
        };

        $scope.removeLink = function (mainEntry, linkedEntry) {
            entry.removeLink({partId: mainEntry.id, linkId: linkedEntry.id}, function (result) {
                var idx = mainEntry.linkedParts.indexOf(linkedEntry);
                if (idx != -1) {
                    mainEntry.linkedParts.splice(idx, 1);
                }
            }, function (error) {
                console.error(error);
            });
        };

        // file upload
        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "/rest/file/sequence",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": sessionId},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        uploader.onProgressItem = function (event, item, progress) {
            $scope.serverError = undefined;

            if (progress != "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        };

        uploader.onSuccessItem = function (item, response, status, header) {
            $scope.entry.hasSequence = true;
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
        };

        uploader.onBeforeUploadItem = function (item) {
            item.formData.push({entryType: $scope.entry.type});
            item.formData.push({entryRecordId: $scope.entry.recordId});
        };

        uploader.onErrorItem = function (item, response, status, headers) {
            $scope.serverError = true;
        };
    });

