'use strict';

angular.module('ice.entry.directives', [])
    .directive("icePlate96", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "=",
                entry: "=",
                plate: "=",
                selected: "="
            },

            restrict: "E",
            templateUrl: "scripts/entry/sample/plate96.html",
            controller: "DisplaySampleController"
        }
    })
    .directive("iceShelf", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "="
            },

            restrict: "E",
            templateUrl: "scripts/entry/sample/shelf.html",
            controller: "DisplaySampleController"
        }
    })
    .directive("iceGeneric", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "="
            },

            restrict: "E",
            templateUrl: "scripts/entry/sample/generic.html",
            controller: "DisplaySampleController"
        }
    })
    .directive("iceAddgene", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "="
            },

            restrict: "E",
            templateUrl: "scripts/entry/sample/addgene.html",
            controller: "DisplaySampleController"
        }
    })
    .directive("iceVectorViewer", function () {
        return {
            scope: {
                entry: "=",
                remote: "=",
                reloadInfo: "="
            },

            restrict: "AE",

            link: function (scope, element, attrs) {
                scope.$watch("reloadInfo", function () {
                    if (!scope.reloadInfo || !scope.entry || !scope.reloadInfo.reload)
                        return;

                    scope.fetchEntrySequence(scope.entry.id);
                });
            },

            template: '<div id="ve-Root"><br><img src="img/loader-mini.gif"> {{$scope.loadMessage || "Loading"}} sequence&hellip;</div>',

            controller: function ($scope, Util, $window) {
                $scope.loadMessage = undefined;
                if (!$scope.entry && !$scope.entry.id) // todo : error message?
                    return;

                $scope.loadVectorEditor = function (data) {
                    $scope.loadMessage = "Rendering";
                    $scope.editor = $window.createVectorEditor(document.getElementById("ve-Root"), {
                        onCopy: function (event, sequenceData, editorState) {
                            const clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            clipboardData.setData('text/plain', sequenceData.sequence);
                            data.selection = editorState.selectionLayer;
                            clipboardData.setData('application/json', JSON.stringify(data));
                            event.preventDefault();
                        },
                        onPaste: function(event, editorState) {
                            //the onPaste here must return sequenceData in the teselagen data format
                            const clipboardData = event.clipboardData;
                            let jsonData = clipboardData.getData("application/json")
                            if (jsonData) {
                                jsonData = JSON.parse(jsonData)
                                if (jsonData && jsonData.isJbeiSeq) {
                                    //HECTOR you'll need to implement/add the convertJbeiToTeselagen function here
                                    jsonData = convertJbeiToTeselagen(jsonData)
                                }
                            }
                            const sequenceData = jsonData || {sequence: clipboardData.getData("text/plain")}
                            return sequenceData
                        },
                        PropertiesProps: {
                            propertiesList: [
                                "features",
                                "translations",
                                "cutsites",
                                "orfs"
                            ]
                        },
                        ToolBarProps: {
                            toolList: [
                                "cutsiteTool",
                                "featureTool",
                                "orfTool",
                                "findTool",
                                "visibilityTool"
                            ]
                        }
                    });

                    $scope.editor.updateEditor({
                        sequenceData: data.sequenceData,
                        annotationVisibility: {
                            parts: false,
                            orfs: false,
                            cutsites: false
                        },
                        annotationsToSupport: {
                            features: true,
                            translations: true,
                            parts: false,
                            orfs: true,
                            cutsites: true,
                            primers: false
                        },
                        panelsShown: [
                            [
                              {
                                id: "circular",
                                name: "Plasmid",
                                active: true
                              },
                              {
                                id: "sequence",
                                name: "Sequence Map",
                                active: false
                              }
                              {
                                id: "rail",
                                name: "Linear Map",
                                active: false
                              },
                              {
                                id: "properties",
                                name: "Properties",
                                active: false
                              }
                            ]
                          ]
                    });
                };

                $scope.fetchEntrySequence = function (entryId) {
                    console.log("loading sequence for", entryId);
                    var url;
                    if ($scope.remote && $scope.remote.partner) {
                        url = "rest/web/" + $scope.remote.partner + "/entries/" + entryId + "/sequence";
                        console.log("loading remote sequence");
                    } else {
                        url = "rest/parts/" + entryId + "/sequence";
                        console.log("loading local sequence");
                    }

                    Util.get(url, function (result) {
                        var data = {
                            sequenceData: {
                                sequence: result.sequence, features: [], name: $scope.entry.name
                            },
                            registryData: {
                                uri: result.uri,
                                identifier: result.identifier,
                                name: result.name,
                                circular: result.circular
                            }
                        };

                        for (var i = 0; i < result.features.length; i += 1) {
                            var feature = result.features[i];
                            if (!feature.locations.length)
                                continue;

                            var notes = feature.notes.length ? feature.notes[0].value : "";

                            for (var j = 0; j < feature.locations.length; j += 1) {
                                var location = feature.locations[j];

                                data.sequenceData.features.push({
                                    start: location.genbankStart - 1,
                                    end: location.end - 1,
                                    fid: feature.id,
                                    forward: feature.strand == 1,
                                    type: feature.type,
                                    name: feature.name,
                                    notes: notes,
                                    annotationType: feature.type,
                                    locations: feature.locations
                                });
                            }
                        }

                        $scope.loadVectorEditor(data);
                    });
                };

                //
                // init
                //
                $scope.fetchEntrySequence($scope.entry.id);
            }
        };
    });
