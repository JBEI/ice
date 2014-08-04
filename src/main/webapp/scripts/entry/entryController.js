'use strict';

angular.module('ice.entry.controller', [])
    .controller('EntryAttachmentController', function ($scope, $window, $cookieStore, $stateParams, $fileUploader, Attachment) {
        console.log("EntryAttachmentController");

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
    });