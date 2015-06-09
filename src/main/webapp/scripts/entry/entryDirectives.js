'use strict';

angular.module('ice.entry.directives', [])
    .directive("icePlate96", function () {
        return {
            scope:{
                sample:"=",
                delete:"&onDelete",
                remote:"="
            },

            restrict:"E",
            templateUrl:"scripts/entry/sample/plate96.html",
            controller:"DisplaySampleController"
        }
    })
    .directive("iceShelf", function () {
        return {
            scope:{
                sample:"=",
                delete:"&onDelete",
                remote:"="
            },

            restrict:"E",
            templateUrl: "scripts/entry/sample/shelf.html",
            controller:"DisplaySampleController"
        }
    })
    .directive("iceGeneric", function () {
        return {
            scope:{
                sample:"=",
                delete:"&onDelete",
                remote:"="
            },

            restrict:"E",
            templateUrl:"scripts/entry/sample/generic.html",
            controller:"DisplaySampleController"
        }
    })
    .directive("iceAddgene", function () {
        return {
            scope:{
                sample:"=",
                delete:"&onDelete",
                remote:"="
            },

            restrict:"E",
            templateUrl:"scripts/entry/sample/addgene.html",
            controller:"DisplaySampleController"
        }
    });