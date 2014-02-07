'use strict';

angular.module('iceApp.filters', []).
    filter('interpolate', ['version', function (version) {
    return function (text) {
        return String(text).replace(/\%VERSION\%/mg, version);
    }
}]);
