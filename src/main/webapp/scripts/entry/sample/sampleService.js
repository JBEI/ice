'use strict';

angular.module('ice.entry.sample.service', [])
    .factory('SampleService', function () {
        var plate96Rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        var plate96Cols = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'];

        var streakOnAgarPlateOptions = [
            {value: "LB Apr50"},
            {value: "LB Spect100"},
            {value: "LB Carb100"},
            {value: "LB Kan50"},
            {value: "LB Chlor25"},
            {value: "LB Gent30"},
            {value: "LB Gent30 Kan50 Rif100"},
            {value: "LB"},
            {value: "CSM -LEU"},
            {value: "CSM -HIS"},
            {value: "CSM -HIS -LEU -URA"},
            {value: "YPD"},
            {value: "I will deliver my own media"}
        ];

        var liquidCultureOptions = [{value: "LB Kan50"},
            {value: "LB Carb100"},
            {value: "LB Chlor25"},
            {value: "LB"},
            {value: "YPD"},
            {value: "I will deliver my own media"}
        ];

        return {
            getPlate96Rows: function () {
                return plate96Rows;
            },

            getPlate96Cols: function () {
                return plate96Cols;
            },

            getStreakOnAgarPlateOptions: function () {
                return streakOnAgarPlateOptions;
            },

            getLiquidCultureOptions: function () {
                return liquidCultureOptions;
            }
        }
    });
