'use strict';

angular.module('ice.upload.file.controller', ['ngFileUpload'])
    .controller('BulkUploadModalController', function ($window, $scope, $location, $routeParams, uploadId,
                                                       $uibModalInstance, FileUploader, addType, linkedAddType, Util,
                                                       Authentication) {
        $scope.addType = addType;
        $scope.processing = false;

        //
        // reset the current bulk upload. involves deleting all entries and showing user new upload form
        //
        $scope.resetBulkUpload = function () {
            // expected folders that can be deleted have type "PRIVATE" and "UPLOAD"
            Util.remove("rest/folders/" + uploadId, {folderId: uploadId, type: "UPLOAD"}, function () {
                $location.path("/upload/" + addType);
                $uibModalInstance.dismiss('cancel');
            }, function (error) {
                console.error(error);
            });
        };

        $scope.retryUpload = function () {
            $scope.uploadError = undefined;
            createUploader();
        };

        let createUploader = function () {

            if ($scope.importUploader) {
                $scope.importUploader.cancelAll();
                $scope.importUploader.clearQueue();
                $scope.importUploader.destroy();
            }

            $scope.importUploader = new FileUploader({
                url: "rest/uploads/" + uploadId + "/file",
                method: 'POST',
                removeAfterUpload: true,
                headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
                formData: [
                    {type: addType}
                ]
            });
        };

        createUploader();

        $scope.importUploader.onSuccessItem = function (item, response) {
            $scope.modalClose = "Close";
            $scope.processing = false;
            if (response.success && response.uploadInfo.id) {
                $uibModalInstance.close();
                $location.path("upload/" + response.uploadInfo.id);
            } else {
                $scope.uploadError = {};
                if (response.userMessage)
                    $scope.uploadError.message = response.userMessage;
            }
        };

        $scope.importUploader.onErrorItem = function (item, response, status) {
            $scope.processing = false;
            $scope.uploadError = {};
            if (response.userMessage)
                $scope.uploadError.message = response.userMessage;
            else
                $scope.uploadError.message = "Unknown server error";

            if (status === 400) {
                $scope.uploadError.message = "Validation error processing file \'" + item.file.name + "\'";
                if (response.userMessage)
                    $scope.uploadError.details = response.userMessage;
                $scope.uploadError.headers = response.headers;
            }
        };

        $scope.importUploader.onBeforeUploadItem = function () {
            $scope.processing = true;
        };

        $scope.importUploader.onCompleteItem = function () {
            $scope.processing = false;
        };

        $scope.close = function () {
            $uibModalInstance.close($scope.modalClose);
        };

        $scope.downloadCSVTemplate = function () {
            let url = "rest/file/upload/" + $scope.addType;
            if (linkedAddType)
                url += "?link=" + linkedAddType;
            $window.open(url, "_self");
        }
    });