'use strict';

angular.module('ice.wor.directives', [])
    .directive("ice.menu.wor", function () {
        return {
            restrict: "E",
            templateUrl: "scripts/wor/web-of-registries-menu.html",
            controller: "WebOfRegistriesMenuController"
        }
    })
    .directive("ice.menu.wor.details", function () {
        return {
            restrict: "E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
            templateUrl: "scripts/wor/web-of-registries-menu-details.html",
            controller: "WebOfRegistriesDetailController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
        };
    });
