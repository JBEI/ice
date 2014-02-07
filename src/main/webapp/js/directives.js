'use strict';

/* Directives */
var iceDirectives = angular.module('iceApp.directives', []);

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
        templateUrl:"/views/collections-menu-details.html",
        controller:"CollectionDetailController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

// web of registries menu directive
var menuWor = function () {
    return {
        restrict:"E",
        templateUrl:"/views/web-of-registries-menu.html"
    };
};
iceDirectives.directive("ice.menu.wor", menuWor);

// tags menu directive
var tagsMenu = function () {
    return {
        restrict:"E",
        templateUrl:"/views/tags-menu.html"
    };
};
iceDirectives.directive("ice.menu.tags", tagsMenu);


