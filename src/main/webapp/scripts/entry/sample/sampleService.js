'use strict';

angular.module('ice.entry.sample.service', [])
    .factory('SampleService', function () {
        var plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        var plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        return {
            getPlate96Rows: function () {
                return plate96Rows;
            },

            getPlate96Cols: function () {
                return plate96Cols;
            }
        }
    });
