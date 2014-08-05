'use strict';

/* Directives */
var iceDirectives = angular.module('iceApp.directives', []);


iceDirectives.directive("iceSearchInput", function () {
    return {
//        scope: {
//            filters:"=",
//            runUserSearch:"&"
//        },
        restrict:"E",
        templateUrl:"scripts/search/search-input.html",
        controller:"SearchInputController"
    }
});

iceDirectives.directive('focus', function ($timeout, $rootScope) {
    return {
        restrict:'A',
        link:function ($scope, $element, attrs) {
            $element[0].focus();
        }
    }
});

iceDirectives.directive("addSequence", function () {
    return {
        restrict:"AE",
        templateUrl:"/views/entry/sequence/add-sequence.html"
//        controller:"AddSequenceController"
    }
});

iceDirectives.directive("folderActions", function () {
    return {
        restrict:"AE",
        templateUrl:"/views/folder/folder-actions.html"
    }
});

iceDirectives.directive("iceActionMenu", function () {
    return {
        restrict:"E",
        templateUrl:"/views/action-menu.html",
        controller:"ActionMenuController"
    }
});

iceDirectives.directive("iceEntryAttachment", function () {
    return {
        restrict:"E",
        templateUrl:"/views/entry-attachment.html"
    }
});

iceDirectives.directive("iceEntryPermission", function () {
    return {
        restrict:"E",
        scope:{
            entry:'='
        },
        templateUrl:"/views/entry-permission.html",
        controller:'EntryPermissionController'
    }
});

iceDirectives.directive("tabs", function () {
    return {
        restrict:'E',
        transclude:true,
        controller:"GenericTabsController",
        templateUrl:"/views/tabs.html"
    }
});

iceDirectives.directive("pane", function () {
    return {
        require:"^tabs",
        restrict:"E",
        transclude:true,
        scope:{
            title:"@",
            count:"@"
        },
        link:function (scope, element, attrs, permCtrl) {
            permCtrl.addPane(scope);
        },
        templateUrl:"/views/generic-pane.html"
    }
});

iceDirectives.directive('stopEvent', function () {
    return {
        restrict:'A',
        link:function (scope, element, attr) {
            element.bind(attr.stopEvent, function (e) {
                e.stopPropagation();
            });
        }
    };
});

iceDirectives.directive('myTabs', function () {
    return {
        restrict:'E',
        transclude:true,
        scope:{},
        controller:function ($scope) {
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
        templateUrl:'/views/entry/tabs.html'
    };
})
    .directive('myPane', function () {
        return {
            require:'^myTabs',
            restrict:'E',
            transclude:true,
            scope:{
                title:'@'
            },
            link:function (scope, element, attrs, tabsCtrl) {
                tabsCtrl.addPane(scope);
            },
            templateUrl:'/views/entry/pane.html'
        };
    });

iceDirectives.directive("iceCollectionContents", function () {
    return {
        restrict:"AE",
        templateUrl:"/views/folder/folder-contents.html",
        controller:"CollectionFolderController"
    }
});

iceDirectives.directive("ice-wor-contents", function () {
    return {
        restrict:"AE",
        templateUrl:"/views/wor/wor-contents.html",
        controller:"WorContentController"
    }
});

iceDirectives.directive("iceBulkUploadContents", function () {
    return {
        scope:{
            contents:"="
        },
        restrict:"AE",
        templateUrl:"/views/bulk-upload-contents.html"
    }
});

iceDirectives.directive("ice.menu.collections", function () {
    return {
        restrict:"E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl:"/views/collections-menu.html",
        controller:"CollectionMenuController"
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
iceDirectives.directive("ice.menu.wor", function () {
    return {
        restrict:"E",
        templateUrl:"/views/web-of-registries-menu.html",
        controller:"WebOfRegistriesMenuController"
    }
});

iceDirectives.directive("ice.menu.wor.details", function () {
    return {
        restrict:"E", // match element name ("A" for attribute - e.g. <div ice.menu.collections></div>)
        templateUrl:"/views/web-of-registries-menu-details.html",
        controller:"WebOfRegistriesDetailController"
//        link: function ( scope, element, attributes ){
//            element.bind( "click", function)
//        }
    };
});

// tags menu directive
iceDirectives.directive("ice.menu.tags", function () {
    return {
        restrict:"E",
        templateUrl:"/views/tags-menu.html"
    }
});

iceDirectives.directive("iceFlash", function ($cookieStore) {
    function link(scope, element, attrs) {
        var sid = $cookieStore.get("sessionId");

        scope.$watch('entry', function (value) {
            if (value) {
                element.html('<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540002" id="VectorEditor" width="100%" height="100%" codebase="https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab"> \
          <param name="movie" value="VectorViewer.swf"> \
              <param name="quality" value="high">  \
                  <param name="bgcolor" value="#869ca7"> \
                      <param name="wmode" value="opaque">  \
                          <param name="allowScriptAccess" value="sameDomain"> \
                              <embed src="/swf/vv/VectorViewer.swf?entryId=' + value.id + '&amp;sessionId=' + sid + '" \
                              quality="high" bgcolor="#869ca7" width="100%" wmode="opaque" height="100%" \
                              name="VectorEditor" align="middle" play="true" loop="false"  \
                              type="application/x-shockwave-flash" \
                              pluginspage="http://www.adobe.com/go/getflashplayer"> \
                              </object>');
//                element.html('<b>' + value.recordId + '</b>')
            } else {
                element.html('<b>No entry data loaded</b>')
            }
        });
    }

    return {
        restrict:'AE',
        link:link
    };
});

iceDirectives.directive("iceVectorViewer", function ($cookieStore) {
    function link(scope, element, attrs) {

        var id, sid = $cookieStore.get("sessionId");

        function generateObject() {
            if (!id) {
                element.html("<b>Cannot render vector viewer.</b>")
            } else {
                element.html('<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540002" id="VectorEditor" width="100%" height="100%" codebase="https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab"> \
          <param name="movie" value="VectorViewer.swf"> \
              <param name="quality" value="high">  \
                  <param name="bgcolor" value="#869ca7"> \
                      <param name="wmode" value="opaque">  \
                          <param name="allowScriptAccess" value="sameDomain"> \
                              <embed src="/swf/vv/VectorViewer.swf?entryId=' + id + '&amp;sessionId=' + sid + '" \
                              quality="high" bgcolor="#869ca7" width="100%" wmode="opaque" height="100%" \
                              name="VectorEditor" align="middle" play="true" loop="false"  \
                              type="application/x-shockwave-flash" \
                              pluginspage="http://www.adobe.com/go/getflashplayer"> \
                              </object>');
            }
        }

        scope.$watch("active", function (value) {
            id = attrs.entryid;
            generateObject();
        });
    }

    return {
        restrict:'AE',
        link:link
    };
});

iceDirectives.directive("iceSequenceChecker", function ($cookieStore) {
    function link(scope, element, attrs) {

        var id, sid = $cookieStore.get("sessionId");

        function generateObject() {
            if (!id) {
                element.html("<b>Cannot render sequence checker.</b>")
            } else {
                element.html('<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540002" id="SequenceChecker" width="100%" height="100%" codebase="https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab"> \
          <param name="movie" value="SequenceChecker.swf"> \
              <param name="quality" value="high">  \
                  <param name="bgcolor" value="#869ca7"> \
                      <param name="wmode" value="opaque">  \
                          <param name="allowScriptAccess" value="sameDomain"> \
                              <embed src="/swf/sc/SequenceChecker.swf?entryId=' + id + '&amp;sessionId=' + sid + '" \
                              quality="high" bgcolor="#869ca7" width="100%" wmode="opaque" height="100%" \
                              name="SequenceChecker" align="middle" play="true" loop="false"  \
                              type="application/x-shockwave-flash" \
                              pluginspage="http://www.adobe.com/go/getflashplayer"> \
                              </object>');
            }
        }

        scope.$watch("active", function (value) {
            id = attrs.entryid;
            generateObject();
        });
    }

    return {
        restrict:'AE',
        link:link
    };
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
        link:link
    };
});



