'use strict';

/* Directives */
var iceDirectives = angular.module('iceApp.directives', []);


iceDirectives.directive("iceSearchInput", function () {
    return {
        restrict:"E",
        templateUrl:"/views/search-input.html",
        controller:"SearchController"
    }
});

iceDirectives.directive("ice.menu.collections", function () {
    return {
        restrict:"E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl:"/views/collections-menu.html",
        controller:"FolderController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

iceDirectives.directive("ice.menu.collections.details", function () {
    return {
        restrict:"E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl:"/views/collections-menu-details.html"
//        controller:"CollectionDetailController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

// web of registries menu directive
iceDirectives.directive("ice.menu.wor", function () {
    return {
        restrict:"E",
        templateUrl:"/views/web-of-registries-menu.html"
    }
});

// tags menu directive
iceDirectives.directive("ice.menu.tags", function () {
    return {
        restrict:"E",
        templateUrl:"/views/tags-menu.html"
    }
});

iceDirectives.directive("ice.scroll", function () {
    return function (scope, elm, attr) {
        var raw = elm[0];

        var funCheckBounds = function (evt) {
            console.log("event fired: " + evt.type);
//            var rectObject = raw.getBoundingClientRect();
//            if (rectObject.bottom === window.innerHeight) {
//                scope.$apply(attr.whenScrolled);
//            }
        };

        angular.element(window).bind('scroll load', funCheckBounds);
    };

//    return {
//        restrict:"E",
//        templateUrl:"/"
//    }
});

iceDirectives.directive("iceInfiniteScroll", function () {
    return function (scope, elm, attr) {
        var raw = elm[0];

        elm.bind('scroll', function () {
            scope.scrollTop = (raw.scrollTop);

            if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight) {
//                console.log(raw.scrollTop + raw.offsetHeight, raw.scrollHeight);
                scope.$apply(attr.iceInfiniteScroll);
            }
        });
    };
});



