'use strict';

var iceFilters = angular.module('iceApp.filters', []);

iceFilters.filter('capitalize', function () {
    return function (input) {
        if (input != null)
            return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
});

iceFilters.filter('truncate', function () {
    return function (text, length, end) {
        if (isNaN(length))
            length = 10;

        if (end === undefined)
            end = "...";

        if (text.length <= length || text.length - end.length <= length) {
            return text;
        }
        else {
            return String(text).substring(0, length - end.length) + end;
        }
    };
});

