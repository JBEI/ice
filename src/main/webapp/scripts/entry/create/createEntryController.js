'use strict';

angular.module('ice.entry.create.controller', [])
    .controller('CreateEntryController', function ($http, $scope, $uibModal, $rootScope, FileUploader, $location,
                                                   $stateParams, Authentication, EntryService, Util, $anchorScroll) {
        $scope.createType = $stateParams.type;
        $scope.showMain = true;

        // generate the various link options for selected option
        $scope.linkOptions = EntryService.linkOptions($scope.createType.toLowerCase());

        // retrieves the defaults for the specified type. Note that $scope.part is the main part
        const getPartDefaults = function (type, isMain) {
            Util.get("rest/parts/defaults/" + type, function (result) {
                if (isMain) { // or if !$scope.part
                    $scope.part = result;
                    $scope.part = EntryService.setNewEntryFields($scope.part);
                    $scope.part.linkedParts = [];
                    $scope.activePart = $scope.part;

                    $scope.activePart.fields.forEach(function (field) {
                        field.invalid = false;
                    })
                } else {
                    let newPart = result;
                    newPart = EntryService.setNewEntryFields(newPart);
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
            $scope.part[schema].push({ value: '' });
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
            let indexOf = $scope.part.linkedParts.indexOf(linkedPart);
            if (indexOf < 0 || indexOf >= $scope.part.linkedParts.length)
                return;

            console.log("delete", linkedPart, "at", indexOf);
            $scope.part.linkedParts.splice(indexOf, 1);

            // todo: will need to actually delete it if has an id (linkedPart.id)

            // remove from array of linked parts
            if ($scope.active === indexOf) {
                let newActive;
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
                headers: { 'X-ICE-Authentication-SessionId': Authentication.getSessionId() },
                params: {
                    token: val,
                    field: inputField
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.submitPart = function () {
            let errorTabActive = false;

            // validate main part
            let canSubmit = EntryService.validateFields($scope.part, $scope.part.fields);
            if (!canSubmit) {
                $scope.setActive('main');
                errorTabActive = true;
            }
            $scope.part.type = $scope.part.type.toUpperCase();

            // validate contained parts, if any
            if ($scope.part.linkedParts && $scope.part.linkedParts.length) {
                for (let idx = 0; idx < $scope.part.linkedParts.length; idx += 1) {
                    let linkedPart = $scope.part.linkedParts[idx];

                    // do not attempt to validate existing part
                    if (linkedPart.isExistingPart)
                        continue;

                    //$scope.selectedFields = EntryService.getFieldsForType(linkedPart.type);

                    let canSubmitLinked = EntryService.validateFields(linkedPart, linkedPart.fields);
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

            for (let i = 0; i < $scope.part.linkedParts.length; i += 1) {
                $scope.part.linkedParts[i].links = EntryService.toStringArray($scope.part.linkedParts[i].links);
                $scope.part.linkedParts[i].selectionMarkers = EntryService.toStringArray($scope.part.linkedParts[i].selectionMarkers);
            }

            // convert the part to a form the server can work with (including custom fields)
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
                        Util.update("rest/parts/" + result.id + "/sequence", { sequence: $scope.part.pastedSequence }, {}, function () {
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
                headers: { 'X-ICE-Authentication-SessionId': Authentication.getSessionId() },
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
            $scope.activePart.parameters.push({ key: '', value: '' });
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
        let uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/sequence",
            method: 'POST',
            removeAfterUpload: true,
            headers: { "X-ICE-Authentication-SessionId": Authentication.getSessionId() },
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        $scope.uploadFile = function () {
            if (!$scope.isPaste) {
                uploader.queue[0].upload();
            }
        };

        // REGISTER HANDLERS
        uploader.onAfterAddingAll = function () {
            $scope.serverError = false;
        };

        uploader.onBeforeUploadItem = function (item) {
            let entryTypeForm;
            if ($scope.active === 'main')
                entryTypeForm = { entryType: $scope.part.type.toUpperCase() };
            else
                entryTypeForm = { entryType: $scope.part.linkedParts[$scope.active].type };
            item.formData.push(entryTypeForm);
        };

        uploader.onProgressItem = function (item, progress) {
            if (progress !== "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        };

        uploader.onSuccessItem = function (item, response) {
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
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
            $scope.serverError = false;
        };
    });
