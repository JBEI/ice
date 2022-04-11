'use strict';

/* Directives */
var iceDirectives = angular.module('iceApp.directives', []);

iceDirectives.directive("iceSearchInput", function () {
    return {
//        scope: {
//            filters:"=",
//            runUserSearch:"&"
//        },
        restrict: "E",
        templateUrl: "scripts/search/search-input.html",
        controller: "SearchInputController"
    }
});

iceDirectives.directive('focus', function ($timeout, $rootScope) {
    return {
        restrict: 'A',
        link: function ($scope, $element, attrs) {
            $element[0].focus();
        }
    }
});

iceDirectives.directive("folderActions", function () {
    return {
        restrict: "AE",
        templateUrl: "scripts/folder/folder-actions.html"
    }
});

iceDirectives.directive("iceActionMenu", function () {
    return {
        restrict: "E",
        templateUrl: "views/action-menu.html",
        controller: "ActionMenuController"
    }
});

iceDirectives.directive("iceEntryAttachment", function () {
    return {
        restrict: "E",
        scope: {
            remote: '=',
            canEdit: '='
        },
        templateUrl: "scripts/entry/entry-attachment.html"
    }
});

iceDirectives.directive("iceRemoteEntryAttachment", function () {
    return {
        restrict: "E",
        templateUrl: "scripts/wor/entry/attachment.html"
    }
});

iceDirectives.directive("iceEntryPermission", function () {
    return {
        restrict: "E",
        scope: {
            entry: '='
        },
        templateUrl: "scripts/entry/entry-permission.html",
        controller: 'EntryPermissionController'
    }
});

iceDirectives.directive("iceEntryFolders", function () {
    return {
        restrict: "E",
        scope: {
            entry: '='
        },
        templateUrl: "scripts/entry/entry-folders.html",
        controller: 'EntryFoldersController'
    }
});

iceDirectives.directive("iceUserFolders", function () {
    return {
        restrict: "E",
        scope: {
            entry: '='
        },
        templateUrl: "scripts/profile/user-folders.html",
        controller: 'UserFoldersController'
    }
});

iceDirectives.directive("tabs", function () {
    return {
        restrict: 'E',
        transclude: true,
        controller: "GenericTabsController",
        templateUrl: "views/tabs.html"
    }
});

iceDirectives.directive("pane", function () {
    return {
        require: "^tabs",
        restrict: "E",
        transclude: true,
        scope: {
            title: "@",
            count: "@"
        },
        link: function (scope, element, attrs, permCtrl) {
            permCtrl.addPane(scope);
        },
        templateUrl: "views/generic-pane.html"
    }
});

iceDirectives.directive('stopEvent', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attr) {
            element.bind(attr.stopEvent, function (e) {
                e.stopPropagation();
            });
        }
    };
});

iceDirectives.directive('myTabs', function () {
    return {
        restrict: 'E',
        transclude: true,
        scope: {},
        controller: function ($scope) {
            var panes = $scope.panes = [];

            $scope.select = function (pane) {
                angular.forEach(panes, function (pane) {
                    pane.selected = false;
                });
                pane.selected = true;
            };

            this.addPane = function (pane) {
                if (panes.length == 0) {
                    $scope.select(pane);
                }
                panes.push(pane);
            };
        },
        templateUrl: 'scripts/entry/tabs.html'
    };
});

iceDirectives.directive('myPane', function () {
    return {
        require: '^myTabs',
        restrict: 'E',
        transclude: true,
        scope: {
            title: '@'
        },
        link: function (scope, element, attrs, tabsCtrl) {
            tabsCtrl.addPane(scope);
        },
        templateUrl: 'scripts/entry/pane.html'
    };
});

iceDirectives.directive("iceCollectionContents", function () {
    return {
        restrict: "AE",
        templateUrl: "scripts/folder/folder-contents.html",
        controller: "CollectionFolderController"
    }
});

iceDirectives.directive("ice-wor-contents", function () {
    return {
        restrict: "AE",
        templateUrl: "scripts/wor/wor-contents.html",
        controller: "WorContentController"
    }
});

iceDirectives.directive("iceBulkUploadContents", function () {
    return {
        scope: {
            contents: "="
        },
        restrict: "AE",
        templateUrl: "views/bulk-upload-contents.html"
    }
});

iceDirectives.directive("ice.menu.collections", function () {
    return {
        restrict: "E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl: "scripts/collection/collections-menu.html",
        controller: "CollectionMenuController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

iceDirectives.directive("ice.menu.collections.details", function () {
    return {
        restrict: "E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl: "scripts/collection/collections-menu-details.html",
        controller: "CollectionDetailController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

// tags menu directive
iceDirectives.directive("ice.menu.tags", function () {
    return {
        restrict: "E",
        templateUrl: "views/tags-menu.html"
    }
});

iceDirectives.directive('myCurrentTime', function ($interval, dateFilter) {

    function link(scope, element, attrs) {
        var format, timeoutId;

        function updateTime() {
            element.text(dateFilter(new Date(), format));
        }

        scope.$watch(attrs.myCurrentTime, function (value) {
            format = value;
            updateTime();
        });

        element.on('$destroy', function () {
            $interval.cancel(timeoutId);
        });

        timeoutId = $interval(function () {
            updateTime(); // update DOM
        }, 1000);
    }

    return {
        link: link
    };
});



