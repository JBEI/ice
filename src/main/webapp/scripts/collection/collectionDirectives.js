'use strict';

angular.module('ice.collection.directives', [])
    .directive('entryList', function () {
        return {
            scope: {},

            restrict: "E",
            templateUrl: "/scripts/collection/entryList.html",
            controller: "EntryListController"
        }
    });
