'use strict';

angular.module('ice.entry.sample.controller', [])
    .controller('DisplaySampleController', function ($rootScope, $scope) {
        $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        $scope.Plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        $scope.canDelete = function () {
            return $rootScope.user && $rootScope.user.isAdmin;
        }
    });


