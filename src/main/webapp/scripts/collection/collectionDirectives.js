'use strict';

angular.module('ice.collection.directives', [])
    .directive('iceEntryList', function () {
        return {
            scope: {
                folder: '=',
                sort: '&'
            },

            restrict: "AE",
            templateUrl: "scripts/collection/entryList.html",
            controller: "CollectionEntryListController"
        }
    });
