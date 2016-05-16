'use strict';

var iceFilters = angular.module('iceApp.filters', []);

iceFilters.filter('capitalize', function () {
    return function (input) {
        if (input === undefined)
            return '';

        var res = '';
        input = input.replace(new RegExp("_", 'g'), " ");
        var inputArr = input.split(" ");
        for (var i = 0; i < inputArr.length; i += 1) {
            if (i > 0)
                res += ' ';
            res += inputArr[i].substring(0, 1).toUpperCase() + inputArr[i].substring(1).toLowerCase();
        }
        return res;
    }
});

iceFilters.filter('truncate', function () {
    return function (text, length, end) {
        if (!text)
            return "";

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

// check is an object is an array and return a comma separated display
iceFilters.filter('arrayDisplay', function () {
    return function (arr) {
        if (angular.isArray(arr))
            return arr.join(', ');
        return arr;
    }
});

iceFilters.filter("externalLink", function () {
    return function (link) {
        if (link.indexOf("http") == 0)
            return link;
        else
            return "http://" + link;
    }
});

iceFilters.filter('startFrom', function () {
    return function (input, start) {
        if (input) {
            start = +start;
            return input.slice(start);
        }
        return [];
    }
});


