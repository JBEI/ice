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

iceFilters.filter('fileTruncate', function () {
    // TODO : if to truncate, show the extension e.g. "j5__kre0934....zip" instead of "j5__kre0934912..."
    return function (input, chars, breakOnWord) {
        if (isNaN(chars)) return input;
        if (chars <= 0) return '';
        if (input && input.length > chars) {
            input = input.substring(0, chars);

            if (!breakOnWord) {
                var lastspace = input.lastIndexOf(' ');
                //get last space
                if (lastspace !== -1) {
                    input = input.substr(0, lastspace);
                }
            } else {
                while (input.charAt(input.length - 1) === ' ') {
                    input = input.substr(0, input.length - 1);
                }
            }
            return input + '\u2026';
        }
        return input;
    };
});

