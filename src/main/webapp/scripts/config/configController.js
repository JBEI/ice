'use strict';

angular.module('ice.config.controller', [])
    .controller('ConfigurationController', function (Util, Configuration, $scope) {

        $scope.config = undefined;
        $scope.options = Configuration.getOptions();

        // get the initial configuration
        Util.get("rest/config/init", function (result) {
            console.log(result);
            $scope.config = result;
        })
    });