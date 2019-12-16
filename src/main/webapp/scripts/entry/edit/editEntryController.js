'use strict';

angular.module('ice.entry.edit.controller', [])

    .controller('EditEntryController', function ($scope, $http, $location, Authentication, $rootScope, FileUploader,
                                                 $stateParams, EntryService, StringArrayToObjectArray, Util, $anchorScroll) {
        let partLinks;
        $scope.entry = {id: $stateParams.id};

        // retrieve the part from the server
        Util.get("rest/parts/" + $stateParams.id, function (result) {
            console.log(result);

            $scope.entry = EntryService.convertToUIForm(result);
            partLinks = angular.copy($scope.entry.linkedParts);
            $scope.entry.linkedParts = [];

            // convert selection markers from array of strings to array of objects for the ui
            StringArrayToObjectArray.convert($scope.entry, result.selectionMarkers, 'selectionMarkers');

            // convert links from array of strings to array of objects for the ui
            StringArrayToObjectArray.convert($scope.entry, result.links, 'links');

            $scope.linkOptions = EntryService.linkOptions(result.type);
            $scope.selectedFields = $scope.entry.fields;
            for (let i = 0; i < $scope.entry.customFields.length; i += 1) {
                const customField = $scope.entry.customFields[i];
                if (customField.fieldType === 'EXISTING')
                    continue;

                $scope.entry[customField.label] = customField.value;
            }

            $scope.activePart = $scope.entry;
        });

        // file upload
        const uploader = $scope.sequenceFileUpload = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/sequence",
            method: 'POST',
            removeAfterUpload: true,
            headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
            autoUpload: true,
            queueLimit: 1 // can only upload 1 file
        });

        uploader.onProgressItem = function (item, progress) {
            $scope.serverError = undefined;

            if (progress !== "100")  // isUploading is always true until it returns
                return;

            // upload complete. have processing
            $scope.processingFile = item.file.name;
        };

        uploader.onSuccessItem = function () {
            $scope.entry.hasSequence = true;
        };

        uploader.onCompleteAll = function () {
            $scope.processingFile = undefined;
        };

        uploader.onBeforeUploadItem = function (item) {
            item.formData.push({entryType: $scope.entry.type});
            item.formData.push({entryRecordId: $scope.entry.recordId});
        };

        uploader.onErrorItem = function () {
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
                headers: {'X-ICE-Authentication-SessionId': Authentication.getSessionId()},
                params: {
                    token: val,
                    field: inputField
                }
            }).then(function (res) {
                return res.data;
            });
        };

        $scope.editEntry = function () {
            let canSubmit = EntryService.validateFields($scope.entry, $scope.selectedFields);
            $scope.entry.type = $scope.entry.type.toUpperCase();

            if (!canSubmit) {
                Util.setFeedback('Missing required fields', 'danger');
                $anchorScroll();
                return;
            }

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
    });