'use strict';

angular.module('ice.entry.controller', [])
    .controller('EntryAttachmentController', function ($scope, $window, $cookieStore, $stateParams, FileUploader, Util) {

        // create a uploader with options
        var sid = $cookieStore.get("sessionId");
        var desc = "";
        $scope.$watch('attachmentDescription', function () {
            desc = $scope.attachmentDescription;
        });

        var uploader = $scope.uploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/attachment",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": sid
            }
        });

        uploader.onSuccessItem = function (item, response, status, headers) {
            response.description = desc;
            Util.post("rest/parts/" + $stateParams.id + "/attachments", response, function (result) {
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

        Util.list("rest/parts/" + $stateParams.id + "/attachments", function (result) {
            $scope.attachments = result;
        });

        $scope.downloadAttachment = function (attachment) {
            $window.open("rest/file/attachment/" + attachment.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };

        $scope.deleteAttachment = function (index, att) {
            Util.remove('rest/parts/' + $stateParams.id + '/attachments/' + att.id, function (result) {
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
    .controller('EntryCommentController', function ($scope, $cookieStore, $stateParams, Util) {
        var entryId = $stateParams.id;
        $scope.newComment = {samples: []};

        Util.list('rest/parts/' + entryId + '/comments', function (result) {
            $scope.entryComments = result;
        });

        Util.list('rest/parts/' + entryId + '/samples', function (result) {
            $scope.entrySamples = result;
        });

        $scope.createComment = function () {
            Util.post('rest/parts/' + entryId + '/comments', $scope.newComment, function (result) {
                $scope.entryComments.splice(0, 0, result);
                $scope.addComment = false;
                $scope.entryStatistics.commentCount = $scope.entryComments.length;
            });
        };

        $scope.updateComment = function (comment) {
            Util.update('rest/parts/' + entryId + '/comments/' + comment.id, comment, {}, function (result) {
                if (result) {
                    comment.edit = false;
                    comment.modified = result.modified;
                }
            });
        };

        /**
         * Add or remove sample to comment. If sample is already a part of the comment, it is removed,
         * if not, it is added
         * @param sample sample to add or remove
         */
        $scope.addRemoveSample = function (sample) {
            var idx = $scope.newComment.samples.indexOf(sample);
            if (idx == -1)
                $scope.newComment.samples.push(sample);
            else
                $scope.newComment.samples.splice(idx, 1);
        }
    })
    .controller('ShotgunSequenceController', function ($scope, $window, $cookieStore, $stateParams, Util) {
        var entryId = $stateParams.id;
        $scope.shotgunUploadError = undefined;
        Util.list('rest/parts/' + entryId + '/shotgunsequences', function (result) {
            $scope.shotgunSequences = result;
        });

        $scope.downloadShotgunFile = function (sequence) {
            $window.open("rest/file/shotgunsequence/" + sequence.fileId + "?sid=" + $cookieStore.get("sessionId"), "_self");
        };
    })
    .controller('TraceSequenceController', function ($scope, $window, $cookieStore, $stateParams, FileUploader, $uibModal, Util) {
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
                }, $scope.tracesParams);
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
    })
    .controller('TraceSequenceUploadModalController', function ($scope, FileUploader, $uibModalInstance, entryId,
                                                                $cookieStore) {
        $scope.cancelAddSangerTrace = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.traceSequenceUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/parts/" + entryId + "/traces",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": $cookieStore.get("sessionId")
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
    })
    .controller('EntryExperimentController', function ($scope, $cookieStore, $stateParams, Util) {
        var entryId = $stateParams.id;
        $scope.experiment = {};
        $scope.addExperiment = false;

        Util.list("/rest/parts/" + entryId + "/experiments", function (result) {
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

            Util.post("/rest/parts/" + entryId + "/experiments", $scope.experiment, function (result) {
                $scope.entryExperiments.splice(0, 0, result);
                $scope.addExperiment = false;
                $scope.entryStatistics.experimentalDataCount = $scope.entryExperiments.length;
            });
        };

        $scope.deleteStudy = function (study) {
            Util.remove("/rest/parts/" + entryId + "/experiments/" + study.id, {}, function (result) {
                var idx = $scope.entryExperiments.indexOf(study);
                if (idx >= 0) {
                    $scope.entryExperiments.splice(idx, 1);
                }
            });
        }
    })
    .controller('PartHistoryController', function ($scope, $window, $cookieStore, $stateParams, Util) {
        var entryId = $stateParams.id;
        $scope.historyParams = {offset: 0, limit: 10, currentPage: 1, maxSize: 5};

        $scope.historyPageChanged = function () {
            $scope.historyParams.offset = ($scope.historyParams.currentPage - 1) * $scope.historyParams.limit;
            Util.get("rest/parts/" + entryId + "/history", function (result) {
                if (history)
                    $scope.history = result;
                //$scope.history = result;
            }, $scope.historyParams);
        };
        $scope.historyPageChanged(); // init

        $scope.deleteHistory = function (history) {
            Util.remove('rest/parts/' + entryId + '/history/' + history.id, {}, function (result) {
                var idx = $scope.history.data.indexOf(history);
                if (idx == -1)
                    return;

                $scope.history.data.splice(idx, 1);
                $scope.history.resultCount -= 1;
            });
        }
    })
    .controller('EditEntryController', function ($scope, $http, $location, $cookieStore, $rootScope, FileUploader,
                                                 $stateParams, EntryService, Util, $anchorScroll) {
        var sid = $cookieStore.get("sessionId");
        var partLinks;
        $scope.entry = undefined;

        Util.get("rest/parts/" + $stateParams.id, function (result) {
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
        });

        // file upload
        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/sequence",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": sid},
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

        $scope.addLink = function (schema, index) {
            $scope.activePart[schema].splice(index + 1, 0, {value: ''});
        };

        // todo : make name more generic
        $scope.removeLink = function (schema, index) {
            $scope.activePart[schema].splice(index, 1);
        };

        $scope.cancelEdit = function () {
            $location.path("entry/" + $stateParams.id);
        };

        $scope.getLocation = function (inputField, val) {
            return $http.get('rest/search/filter', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    token: val,
                    field: inputField
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.editEntry = function () {
            var canSubmit = EntryService.validateFields($scope.entry, $scope.selectedFields);
            $scope.entry.type = $scope.entry.type.toUpperCase();

            if (!canSubmit) {
                Util.setFeedback('Missing required fields', 'danger');
                $anchorScroll();
                return;
            }

            if ($scope.entry.bioSafetyLevel === 'Level 1')
                $scope.entry.bioSafetyLevel = 1;
            else
                $scope.entry.bioSafetyLevel = 2;

            // convert arrays of objects to array strings
            $scope.entry.links = EntryService.toStringArray($scope.entry.links);
            $scope.entry.selectionMarkers = EntryService.toStringArray($scope.entry.selectionMarkers);

            // convert the part to a form the server can work with
            $scope.entry = EntryService.getTypeData($scope.entry);
            $scope.entry.linkedParts = partLinks;

            Util.update("rest/parts/" + $scope.entry.id, $scope.entry, {}, function (result) {
                $location.path("entry/" + result.id);
                Util.setFeedback('Part successfully updated', 'success');
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
    .controller('CreateEntryController', function ($http, $scope, $uibModal, $rootScope, FileUploader, $location,
                                                   $stateParams, $cookieStore, EntryService, Util, $anchorScroll) {
        $scope.createType = $stateParams.type;
        $scope.showMain = true;

        // generate the various link options for selected option
        $scope.linkOptions = EntryService.linkOptions($scope.createType.toLowerCase());
        var sid = $cookieStore.get("sessionId");

        // retrieves the defaults for the specified type. Note that $scope.part is the main part
        var getPartDefaults = function (type, isMain) {
            //entry.query({partId: type}, function (result) {
            Util.get("rest/parts/defaults/" + type, function (result) {
                if (isMain) { // or if !$scope.part
                    $scope.part = result;
                    $scope.part = EntryService.setNewEntryFields($scope.part);
                    $scope.part.linkedParts = [];
                    $scope.activePart = $scope.part;
                    $scope.part.fields = EntryService.getFieldsForType($scope.createType);
                    angular.forEach($scope.activePart.fields, function (field) {
                        field.invalid = false;
                    })
                } else {
                    var newPart = result;
                    newPart = EntryService.setNewEntryFields(newPart);
                    newPart.fields = EntryService.getFieldsForType(type);
                    $scope.part.linkedParts.push(newPart);

                    $scope.colLength = 11 - $scope.part.linkedParts.length;
                    $scope.active = $scope.part.linkedParts.length - 1;
                    $scope.activePart = $scope.part.linkedParts[$scope.active];
                }
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

        $scope.addExistingPartLink = function ($item, $model) {
            Util.get("rest/parts/" + $model, function (result) {
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
        };

        $scope.getLocation = function (inputField, val) {
            return $http.get('rest/search/filter', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    token: val,
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
                Util.setFeedback("Missing required fields", "danger");
                $anchorScroll();
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
            // which might be the case if a sequence is uploaded first
            if ($scope.part.id) {
                Util.update("rest/parts/" + $scope.part.id, $scope.part, {}, function (result) {
                    $location.path('/entry/' + result.id);
                });
            } else {
                Util.post("rest/parts", $scope.part, function (result) {
                    $scope.$emit("UpdateCollectionCounts");
                    if ($scope.part.pastedSequence) {
                        // todo : also handle linked parts
                        Util.post("rest/parts/" + result.id + "/sequence", {sequence: $scope.part.pastedSequence}, function () {
                            $location.path('/entry/' + result.id);
                            $scope.showSBOL = false;
                        })
                    } else {
                        $location.path('/entry/' + result.id);
                        $scope.showSBOL = false;
                    }
                });
            }
        };

        $scope.format = 'MMM d, yyyy h:mm:ss a';

        $scope.getEntriesByPartNumber = function (val) {
            return $http.get('rest/search/filter', {
                headers: {'X-ICE-Authentication-SessionId': sid},
                params: {
                    token: val,
                    field: 'PART_NUMBER'
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
            $location.path("folders/personal");
        };

        // file upload
        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/sequence",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": sid},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        $scope.uploadFile = function () {
            if (!$scope.isPaste) {
                uploader.queue[0].upload();
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
            console.log(item, response, status, headers);
            $scope.serverError = true;
            $scope.processingFile = undefined;
            uploader.resetAll();
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
            $scope.serverError = false;
        };
    })
    .controller('EntryPermissionController', function ($rootScope, $scope, $cookieStore, filterFilter, Util) {
        var sessionId = $cookieStore.get("sessionId");
        var panes = $scope.panes = [];
        $scope.userFilterInput = undefined;
        $scope.canSetPublicPermission = undefined;
        $scope.selectedArticle = {type: 'ACCOUNT', placeHolder: "Enter name or email"};

        if (!$rootScope.settings || !$rootScope.settings['RESTRICT_PUBLIC_ENABLE']) {
            Util.get("rest/config/RESTRICT_PUBLIC_ENABLE", function (result) {
                if (!result)
                    return;
                if (!$rootScope.settings)
                    $rootScope.settings = {};
                $rootScope.settings['RESTRICT_PUBLIC_ENABLE'] = result.value;
                $scope.canSetPublicPermission = (result.value == "no") || $rootScope.user.isAdmin;
            });
        } else {
            $scope.canSetPublicPermission = ($rootScope.settings['RESTRICT_PUBLIC_ENABLE'].value == "no") || $rootScope.user.isAdmin;
        }

        $scope.setPermissionArticle = function (type) {
            $scope.selectedArticle.type = type;
            $scope.autoCompleteUsersOrGroups = undefined;
            $scope.userFilterInput = undefined;
        };

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
        Util.list('rest/parts/' + $scope.entry.id + '/permissions', function (result) {
            $scope.readPermissions = [];
            $scope.writePermissions = [];

            angular.forEach(result, function (item) {
                item.canEdit = $rootScope.user.isAdmin || (item.group && !item.group.autoJoin);

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
                $scope.autoCompleteUsersOrGroups = undefined;
                return;
            }

            $scope.filtering = true;
            var resource;
            var queryParams;

            if ($scope.selectedArticle.type == 'ACCOUNT') {
                resource = "users";
                queryParams = {limit: 8, val: val};
            } else {
                resource = "groups";
                queryParams = {limit: 8, token: val};
            }

            Util.list("rest/" + resource + "/autocomplete", function (result) {
                if ($scope.selectedArticle.type == "ACCOUNT") {
                    angular.forEach(result, function (item) {
                        item.label = item.firstName + " " + item.lastName;
                    });
                }

                $scope.autoCompleteUsersOrGroups = result;
                $scope.filtering = false;

            }, queryParams, function (error) {
                $scope.filtering = false;
                $scope.autoCompleteUsersOrGroups = undefined;
            });
        };

        $scope.showAddPermissionOptionsClick = function () {
            $scope.showPermissionInput = true;
        };

        $scope.closePermissionOptions = function () {
            $scope.showPermissionInput = false;
        };

        var removePermission = function (permissionId) {
            Util.remove("rest/parts/" + $scope.entry.id + "/permissions/" + permissionId, {}, function (result) {
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
                    return;
                }

                $scope.activePermissions.splice(i, 1);
                pane.count = $scope.activePermissions.length;
            });
        };

        //
        // when user clicks on the check box, removes permission if exists or adds if not
        //
        $scope.addRemovePermission = function (userOrGroup) {
            if (userOrGroup.selected) {
                removePermission(userOrGroup.permissionId);
                userOrGroup.selected = false;
                return;
            }

            var permission = {};
            permission.article = $scope.selectedArticle.type;
            permission.articleId = userOrGroup.id;

            // add permission
            for (var i = 0; i < panes.length; i += 1) {
                if (panes[i].selected) {
                    permission.type = panes[i].title.toUpperCase() + "_ENTRY";
                    break;
                }
            }

            permission.typeId = $scope.entry.id;

            Util.post('rest/parts/' + $scope.entry.id + '/permissions', permission, function (result) {
                // result is the permission object
                $scope.entry.id = result.typeId;
                result.canEdit = $rootScope.user.isAdmin || (result.group && !result.group.autoJoin);

                if (result.type == 'READ_ENTRY') {
                    $scope.readPermissions.push(result);
                    $scope.activePermissions = $scope.readPermissions;
                }
                else {
                    $scope.writePermissions.push(result);
                    $scope.activePermissions = $scope.writePermissions;
                }

                userOrGroup.permissionId = result.id;
                userOrGroup.selected = true;
            });
        };

        $scope.enablePublicRead = function (e) {
            Util.update('rest/parts/' + e.id + '/permissions/public', {}, {}, function () {
                $scope.entry.publicRead = true;
            });
        };

        $scope.disablePublicRead = function (e) {
            Util.remove('rest/parts/' + e.id + '/permissions/public', {}, function () {
                $scope.entry.publicRead = false;
            });
        };

        $scope.deletePermission = function (index, permission) {
            removePermission(permission.id);
        };
    })
    .controller('EntryFoldersController', function ($scope, Util) {
        $scope.containedFolders = undefined;
        Util.list("rest/parts/" + $scope.entry.id + "/folders", function (result) {
            $scope.containedFolders = result;
        });
    })
    .controller('EntryDetailsController', function ($scope) {
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

    .controller('EntryController', function ($scope, $stateParams, $cookieStore, $location, $uibModal, $rootScope,
                                             $route, $window, FileUploader, EntryService, EntryContextUtil, Selection,
                                             Util, Authentication) {
        $scope.partIdEditMode = false;
        $scope.showSBOL = true;
        $scope.context = EntryContextUtil.getContext();

        $scope.isFileUpload = false;

        var sessionId = $cookieStore.get("sessionId");
        $scope.sessionId = sessionId;

        $scope.open = function () {
            window.open('static/swf/ve/VectorEditor?entryId=' + $scope.entry.id + '&sessionId=' + sessionId);
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
            Util.post("rest/parts/" + part.id + "/sequence", {sequence: sequenceString}, function (result) {
                part.hasSequence = true;
            })
        };

        $scope.deleteSequence = function (part) {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/entry/sequence/modal-delete-sequence-confirmation.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.toDelete = part;
                    $scope.processingDelete = undefined;
                    $scope.errorDeleting = undefined;

                    $scope.deleteSequence = function () {
                        $scope.processingDelete = true;
                        $scope.errorDeleting = false;

                        Util.remove('rest/parts/' + part.id + '/sequence', {}, function (result) {
                            $scope.processingDelete = false;
                            $uibModalInstance.close(part);
                        }, function () {
                            $scope.processingDelete = false;
                            $scope.errorDeleting = true;
                        });
                    }
                },
                backdrop: "static"
            });

            modalInstance.result.then(function (part) {
                if (part)
                    part.hasSequence = false;
            });
        };

        $scope.addLink = function (part, role) {

            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/entry/modal/add-link-modal.html',
                controller: function ($scope, $http, $uibModalInstance, $cookieStore) {
                    $scope.mainEntry = part;
                    $scope.role = role;
                    $scope.loadingAddExistingData = undefined;

                    if (role === 'PARENT') {
                        $scope.links = part.parents;
                    } else {
                        $scope.links = part.linkedParts;
                    }

                    var sessionId = $cookieStore.get("sessionId");
                    $scope.getEntriesByPartNumber = function (val) {
                        return $http.get('rest/search/filter', {
                            headers: {'X-ICE-Authentication-SessionId': sessionId},
                            params: {
                                token: val,
                                field: 'PART_NUMBER'
                            }
                        }).then(function (res) {
                            return res.data;
                        });
                    };

                    var linkPartToMainEntry = function (item) {
                        Util.post('rest/parts/' + $scope.mainEntry.id + '/links', item, function () {
                            $scope.links.push(item);   // todo
                            $scope.addExistingPartNumber = undefined;
                            $scope.mainEntrySequence = undefined;
                        }, {linkType: $scope.role}, function () {
                            $scope.errorMessage = "Error linking this entry to " + item.partId;
                        });
                    };

                    // todo : todo
                    $scope.addExistingPartLink = function ($item, $model, $label) {
                        $scope.errorMessage = undefined;

                        // prevent selecting current entry
                        if ($item == $scope.mainEntry.partId)
                            return;

                        // or already added entry
                        var found = false;
                        angular.forEach($scope.links, function (t) {
                            if (t.partId === $item) {
                                found = true;
                            }
                        });
                        if (found)
                            return;

                        // fetch entry being added from server
                        Util.get("rest/parts/" + $item, function (result) {
                            $scope.selectedLink = result;
                            if ($scope.role == 'CHILD') {

                                // if item being added as a child is of type part then
                                if (result.type.toLowerCase() == 'part') {

                                    // check if it has a sequence
                                    if (!result.hasSequence) {
                                        $scope.addExistingPartNumber = result;

                                        // if not, retrieve sequence annotations for parent entry
                                        // to allow user to select one annotation as the sequence for the entry being
                                        // added
                                        $scope.getEntrySequence($scope.mainEntry.id);
                                    } else {

                                        // has sequence so just add the link
                                        linkPartToMainEntry(result);
                                    }
                                } else {
                                    // just add the link
                                    linkPartToMainEntry(result);
                                }
                            } else {
                                // parent of main entry being added
                                if ($scope.mainEntry.type.toLowerCase() == 'part') {

                                    // if child (main) does not have a attached sequence
                                    if (!$scope.mainEntry.hasSequence) {

                                        // retrieve sequence feature options for parent
                                        $scope.addExistingPartNumber = result;
                                        $scope.getEntrySequence($scope.addExistingPartNumber.id);
                                    } else {
                                        linkPartToMainEntry(result);
                                    }
                                } else {
                                    linkPartToMainEntry(result);
                                }
                            }
                        });
                    };

                    $scope.removeExistingPartLink = function (link) {
                        var i = $scope.links.indexOf(link);
                        if (i < 0)
                            return;

                        Util.remove('rest/parts/' + $scope.mainEntry.id + '/links/' + link.id,
                            {linkType: $scope.role}, function (result) {
                                $scope.links.splice(i, 1);
                            });
                    };

                    $scope.close = function () {
                        $uibModalInstance.close();
                    };

                    $scope.getEntrySequence = function (id) {
                        $scope.retrievingSequenceFeatureList = true;
                        $scope.mainEntrySequence = undefined;

                        Util.get("rest/parts/" + id + "/sequence", function (result) {
                            $scope.mainEntrySequence = result;
                            $scope.retrievingSequenceFeatureList = false;
                        }, function (error) {
                            console.error(error);
                            $scope.retrievingSequenceFeatureList = false;
                        });
                    };

                    $scope.addSequenceToLinkAndLink = function (feature) {
                        // update sequence information on entry
                        // POST rest/parts/{id}/sequence featuredDNA sequence
                        //console.log($scope.mainEntrySequence, feature, $scope.addExistingPartNumber);

                        // todo : backend should probably handle this; quick fix for the milestone
                        var start = feature.locations[0].genbankStart;
                        var end = feature.locations[0].end;
                        var sequence = $scope.mainEntrySequence.sequence.substring(start - 1, end);
                        feature.locations[0].genbankStart = 1;
                        feature.locations[0].end = sequence.length;

                        var linkSequence = {
                            identifier: $scope.addExistingPartNumber.partId,
                            sequence: sequence,
                            genbankStart: 0,
                            end: sequence.length,
                            features: [feature]
                        };

                        var sequencePartId;
                        if ($scope.role == 'CHILD') {
                            sequencePartId = $scope.selectedLink.id;
                        } else {
                            sequencePartId = $scope.mainEntry.id;
                        }

                        // add sequence to entry
                        Util.post("rest/parts/" + sequencePartId + "/sequence", linkSequence, function (result) {
                            linkPartToMainEntry($scope.addExistingPartNumber);
                        });
                    };
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

// todo :

        var setPartDefaults = function (user) {
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
                creator: user.firstName + ' ' + user.lastName,
                creatorEmail: user.email
            };

            $scope.part = angular.copy(partDefaults);
        };

        if (!$rootScope.user) {
            Authentication.getLoggedInUser().then(function (result) {
                var user = result.data;
                setPartDefaults(user);
            });
        } else {
            setPartDefaults($rootScope.user);
        }

        $scope.sbolShowHide = function () {
            $scope.showSBOL = !$scope.showSBOL;
        };

        $scope.getSequenceSectionHeader = function () {
            if ($scope.entry.hasSequence && !$scope.entry.basePairCount)
                return "SBOL INFORMATION";
            return "SEQUENCE";
        };

        $scope.entryFields = undefined;
        $scope.entry = undefined;
        $scope.notFound = undefined;
        $scope.noAccess = undefined;

        var params = $location.search();

        Util.get("rest/parts/" + $stateParams.id,
            function (result) {
                Selection.reset();
                Selection.selectEntry(result);

                $scope.entry = EntryService.convertToUIForm(result);
                if ($scope.entry.canEdit)
                    $scope.newParameter = {edit: false};
                $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());
                $scope.entry.remote = params.remote;

                // get sample count, comment count etc
                Util.get("rest/parts/" + $stateParams.id + "/statistics", function (stats) {
                    $scope.entryStatistics = stats;
                }, params);

            }, params, function (error) {
                if (error.status === 404)
                    $scope.notFound = true;
                else if (error.status === 403)
                    $scope.noAccess = true;
            });

        var menuSubDetails = $scope.subDetails = EntryService.getMenuSubDetails();

        $scope.showSelection = function (index) {
            angular.forEach(menuSubDetails, function (details) {
                details.selected = false;
            });
            menuSubDetails[index].selected = true;
            $scope.selection = menuSubDetails[index].url;
            if (menuSubDetails[index].id) {
                $location.path("entry/" + $stateParams.id + "/" + menuSubDetails[index].id);
            } else {
                $location.path("entry/" + $stateParams.id);
            }
        };

        $scope.createCopyOfEntry = function () {
            Util.post("rest/parts", {}, function (result) {
                $scope.$emit("UpdateCollectionCounts");
                $scope.showSBOL = false;
                $location.path('entry/' + result.id);
            }, {source: $scope.entry.recordId});
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
            field.errorUpdating = false;
            field.updating = true;
            field.invalid = false;

            if (field.inputType === "autoCompleteAdd") {
                $scope.quickEdit[field.schema] = $scope.convertedAutoCompleteAdd;
            }

            // validate
            var canSubmit = EntryService.validateFields($scope.quickEdit, [field]);
            if (!canSubmit) {
                field.updating = false;
                return;
            }
            // getTypeData is not converting selection markers for some reason
            if (field.inputType === "autoCompleteAdd") {
                $scope.entry[field.schema] = [];
                angular.forEach($scope.convertedAutoCompleteAdd, function (val) {
                    if (val.value.trim() == "")
                        return;

                    $scope.entry[field.schema].push(val.value);
                });
            } else {
                // update the main entry with quickEdit (which is the model)
                $scope.entry[field.schema] = $scope.quickEdit[field.schema];
                if (field.inputType === 'withEmail') {
                    $scope.entry[field.schema + 'Email'] = $scope.quickEdit[field.schema + 'Email'];
                }
            }

            $scope.entry = EntryService.getTypeData($scope.entry);

            Util.update("rest/parts/" + $scope.entry.id, $scope.entry, {}, function (result) {
                field.edit = false;

                if (result)
                    $scope.entry = EntryService.convertToUIForm(result);

                field.dirty = false;
                field.updating = false;
            }, function (error) {
                field.updating = false;
                field.errorUpdating = true;
                Util.setFeedback("Error updating entry", "danger")
            });
        };

// converts an array of string (currently only for autoCompleteAdd) to object so it can be edited
        $scope.checkConvertFieldToObject = function (field) {
            $scope.convertedAutoCompleteAdd = [];
            if (!angular.isArray($scope.entry[field.schema]))
                return;

            if (field.inputType !== 'autoCompleteAdd')
                return;

            for (var i = 0; i < $scope.entry[field.schema].length; i += 1) {
                $scope.convertedAutoCompleteAdd[i] = {value: $scope.entry[field.schema][i]};
            }
        };

        $scope.deleteCustomField = function (parameter) {
            var index = $scope.entry.parameters.indexOf(parameter);
            if (index >= 0) {
                var currentParam = $scope.entry.parameters[index];
                if (currentParam.id == parameter.id) {
                    Util.remove("rest/custom-fields/" + parameter.id, {}, function (result) {
                        $scope.entry.parameters.splice(index, 1);
                    })
                }
            }
        };

        $scope.nextEntryInContext = function () {
            $scope.context.offset += 1;
            $scope.context.callback($scope.context.offset, function (result) {
                $location.path("entry/" + result);
            });
        };

        $scope.prevEntryInContext = function () {
            $scope.context.offset -= 1;
            $scope.context.callback($scope.context.offset, function (result) {
                $location.path("entry/" + result);
            });
        };

        $scope.backTo = function () {
            Selection.reset();
            $location.path($scope.context.back);
        };

// removes linked parts
        $scope.removeLink = function (mainEntry, linkedEntry) {
            Util.remove('rest/parts/' + mainEntry.id + '/links/' + linkedEntry.id, {}, function () {
                var idx = mainEntry.linkedParts.indexOf(linkedEntry);
                if (idx != -1) {
                    mainEntry.linkedParts.splice(idx, 1);
                }
            });
        };

// removes a value from an autoCompleteAdd field at the specified index
        $scope.removeAutoCompleteAdd = function (index) {
            $scope.convertedAutoCompleteAdd.splice(index, 1);
        };

// add a new autoComplete add value at the specified index
        $scope.addAutoCompleteAdd = function (index) {
            $scope.convertedAutoCompleteAdd.splice(index + 1, 0, {value: ""});
        };

// file upload
        var uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/sequence",
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
            if (!response)
                return;

            if (response.sequence) {
                $scope.entry.basePairCount = response.sequence.sequence.length;
            }

            if (response.format && response.format.indexOf("SBOL") > -1) {
                Util.list("rest/parts/" + $scope.entry.id + "/links", function (result) {
                    if (!result)
                        return;
                    $scope.entry.linkedParts = result;
                });
            }

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
            $scope.serverError = response.message;
        };

// customer parameter add for entry view
        $scope.addNewCustomField = function () {
            $scope.newParameter.nameInvalid = $scope.newParameter.name == undefined || $scope.newParameter.name == '';
            $scope.newParameter.valueInvalid = $scope.newParameter.value == undefined || $scope.newParameter.value == '';
            if ($scope.newParameter.nameInvalid || $scope.newParameter.valueInvalid)
                return;

            $scope.newParameter.partId = $scope.entry.id;
            Util.post("rest/custom-fields", $scope.newParameter, function (result) {
                if (!result)
                    return;

                $scope.entry.parameters.push(result);
                $scope.newParameter.edit = false;
            })
        };

        $scope.showAutoAnnotationPopup = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'scripts/entry/sequence/modal-auto-annotate-sequence.html',
                controller: function ($scope, $uibModalInstance, part, Util) {
                    $scope.selectedFeatures = [];
                    $scope.allSelected = false;
                    $scope.part = part;
                    $scope.pagingParams = {
                        currentPage: 0,
                        pageSize: 8,
                        sort: "locations[0].genbankStart",
                        asc: true
                    };
                    var displayOptions = [{display: "All features", key: "all"}, {
                        display: "My features",
                        key: "mine"
                    }];
                    $scope.options = {values: displayOptions, selection: displayOptions[0]};

                    // retrieves "suggested" annotations for current entry
                    $scope.fetchAnnotations = function () {
                        $scope.annotations = undefined;
                        Util.get("rest/parts/" + part.id + "/annotations/auto", function (result) {
                            angular.forEach(result.features, function (feature) {
                                    //console.log(feature);
                                    //if (feature.strand == 1)
                                        feature.length = (feature.locations[0].end - feature.locations[0].genbankStart) + 1;
                                    //else
                                    //feature.length = (feature.locations[0].genbankStart - feature.locations[0].end) + 1;
                                }
                            );
                            $scope.annotations = result;
                            $scope.pagingParams.resultCount = result.features.length;
                            $scope.pagingParams.numberOfPages = Math.ceil(result.features.length / $scope.pagingParams.pageSize);
                        }, {ownerFeatures: $scope.options.selection.key == "mine"});
                    };
                    $scope.fetchAnnotations();

                    /**
                     * Support for sorting
                     * @param field field to sort on
                     */
                    $scope.sort = function (field) {
                        if ($scope.pagingParams.sort == field) {
                            $scope.pagingParams.asc = !$scope.pagingParams.asc;
                        } else {
                            $scope.pagingParams.sort = field;
                            $scope.pagingParams.asc = true;
                        }
                        $scope.pagingParams.currentPage = 0;
                    };

                    /**
                     * Select all features on the UI
                     */
                    $scope.selectAll = function () {
                        $scope.allSelected = !$scope.allSelected;
                        if ($scope.allSelected) {
                            $scope.selectedFeatures = $scope.annotations.features;
                        } else {
                            $scope.selectedFeatures = [];
                        }
                    };

                    /**
                     * Check or un-check (on UI) specific feature
                     * @param feature
                     */
                    $scope.checkFeature = function (feature) {
                        feature.selected = !feature.selected;
                        var i = $scope.selectedFeatures.indexOf(feature);
                        if (i == -1) {
                            $scope.selectedFeatures.push(feature);
                        }
                        else {
                            $scope.selectedFeatures.splice(i, 1);
                        }

                        $scope.allSelected = ($scope.selectedFeatures.length == $scope.annotations.features.length);
                    };

                    $scope.setClassName = function (feature) {
                        var classPrefix = feature.strand == -1 ? "reverse-strand-" : "forward-strand-";
                        feature.className = classPrefix + feature.type.toLowerCase();
                    };

                    /**
                     *  Determine background color based on feature type
                     * @param feature
                     * @returns {{background-color: string}}
                     */
                    $scope.getBgStyle = function (feature) {
                        var bgColor = "#CCC";

                        switch (feature.type.toLowerCase()) {
                            case 'cds':
                                bgColor = "#EF6500";
                                break;

                            case "misc_feature":
                                bgColor = "#006FEF";
                                break;

                            case "promoter":
                                bgColor = "#31B440";
                                break;

                            case "terminator":
                                bgColor = "red";
                                break;

                            case "rep_origin":
                                bgColor = "#878787";
                                break;

                            case "misc_marker":
                                bgColor = "#8DCEB1";
                                break;
                        }
                        return {'background-color': bgColor};
                    };

                    $scope.getFirstStyle = function (selectedFeature) {
                        var width = (selectedFeature.locations[0].genbankStart / $scope.annotations.length) * 100;
                        return {"width": (Math.floor(width)) + '%'};
                    };

                    $scope.getSecondStyle = function (selectedFeature) {
                        var width = ((selectedFeature.locations[0].end - selectedFeature.locations[0].genbankStart) / $scope.annotations.length) * 100;
                        var style = $scope.getBgStyle(selectedFeature);
                        style.width = (Math.ceil(width)) + '%';
                        return style;
                    };

                    $scope.getThirdStyle = function (selectedFeature) {
                        var w = (($scope.annotations.length - selectedFeature.locations[0].end) / $scope.annotations.length) * 100;
                        return {"width": (Math.floor(w)) + '%'};
                    };

                    $scope.saveAnnotations = function () {
                        $scope.errorSavingAnnotations = false;
                        $scope.savingAnnotations = true;

                        //url, obj, successHandler, params, errHandler
                        Util.post("rest/parts/" + part.id + "/sequence", {features: $scope.selectedFeatures}, function () {
                            $uibModalInstance.close(true);
                        }, {add: true}, function (error) {
                            $scope.savingAnnotations = false;
                            $scope.errorSavingAnnotations = true;
                        })
                    };

                    // used to show, in table of features, the selected feature
                    $scope.showAnnotationInTable = function (selectedFeature) {
                        var index = $scope.annotations.features.indexOf(selectedFeature);
                        $scope.pagingParams.currentPage = parseInt(index / $scope.pagingParams.pageSize);
                    }
                },
                size: 'lg',
                resolve: {
                    part: function () {
                        return $scope.entry;
                    }
                }
                ,
                backdrop: "static"
            });

            modalInstance.result.then(function (reload) {
                if (reload) {
                    $window.location.reload();
                }
            });
        };
    }
)
;

