'use strict';

angular.module('ice.profile.message.controller', [])

    .controller('MessageController', function ($scope, $uibModal, $stateParams, $location, Util) {
        $scope.selectedMessage = undefined;
        $scope.messageReply = undefined;
        $scope.messageParams = {currentPage: 1, limit: 15};

        $scope.selectMessage = function (message) {
            Util.get("rest/messages/" + message.id, function (result) {
                message.selected = true;
                //result.message = result.message.replace(/(?:\r\n|\r|\n)/g, '<br />');
                $scope.selectedMessage = result;
            });
        };

        $scope.getMessages = function () {
            // get all messages
            Util.get("rest/messages", function (result) {
                $scope.messages = result;
            }, $scope.messageParams);
        };

        //
        // init
        //
        $scope.getMessages();
        //
        // end init
        //

        $scope.replyMessage = function () {
            Util.post("rest/messages/" + $scope.selectedMessage.id + "/response", {message: $scope.messageReply},
                function (response) {
                    console.log(response);
                }, {}, function (err) {
                    console.error(err);
                }
            );
        };

        $scope.openCreateMessageModal = function () {
            $uibModal.open({
                templateUrl: 'scripts/profile/modal/create-message.html',
                backdrop: "static",
                keyboard: false,
                controller: 'CreateMessageController'
            })
        };

        $scope.backToMessages = function () {
            $scope.selectedMessage = undefined;
        }
    })
    .
    controller('CreateMessageController', function ($scope, $uibModalInstance, $http, $cookies, Util) {
        $scope.newMessage = {accounts: [], userGroups: []};

        $scope.createNewMessage = function () {
            Util.post("rest/messages", $scope.newMessage, function (result) {
                $uibModalInstance.close();
            })
        };

        $scope.closeGroupModal = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.setMessageRecipient = function (a, b, c) {
            $scope.newMessage.accounts.push(a);
            $scope.addedUser = undefined;
        };

        $scope.removeMessageRecipient = function (account, group) {
            if (account) {
                var accountIdx = $scope.newMessage.accounts.indexOf(account);
                if (accountIdx != -1)
                    $scope.newMessage.accounts.splice(accountIdx, 1);
            }

            if (group) {
                var groupIdx = $scope.newMessage.userGroups.indexOf(gr);
                if (groupIdx != -1)
                    $scope.newMessage.userGroups.splice(groupIdx, 1);
            }
        };

        $scope.filter = function (val) {
            return $http.get('rest/users/autocomplete', {
                headers: {'X-ICE-Authentication-SessionId': $cookies.get("sessionId")},
                params: {
                    val: val
                }
            }).then(function (res) {
                return res.data;
            });
        };
    })
;