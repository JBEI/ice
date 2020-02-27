'use strict';

angular.module('ice.admin.sample.directives', [])
    .directive("samplePlate96", function () {
        return {
            scope: {
                locationBarcodes: "=",
                entries: "=",
                plateName: "="
            },

            restrict: "E",
            templateUrl: "scripts/admin/sample/samplePlate96.html",
            controller: function ($scope) {
                $scope.Plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
                $scope.Plate96Cols = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12'];

                $scope.showBarcode = function (location) {
                    // console.log(location);
                };
            }
        }
    });