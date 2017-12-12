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
                remote: "="
            },
            restrict: "AE",

            link: function (scope, element, attrs) {
            },

            template: '<div id="ve-Root"><br><img src="img/loader-mini.gif"> Loading sequence&hellip;</div>',

            controller: function ($scope, Util, $window) {
                if (!$scope.entry && !$scope.entry.id) // todo : error message?
                    return;

                $scope.loadVectorEditor = function (data) {
                    $scope.editor = $window.createVectorEditor(document.getElementById("ve-Root"), {
                        onCopy: function (event, sequenceData, editorState) {
                            const clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            clipboardData.setData('text/plain', sequenceData.sequence);
                            data.selection = editorState.selectionLayer;
                            clipboardData.setData('application/json', JSON.stringify(data));
                            event.preventDefault();
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
                                "viewTool",
                                "findTool",
                                "visibilityTool",
                                "propertiesTool"
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
                        panelsShown: {
                            sequence: false,
                            circular: true,
                            rail: false
                        }
                    });
                };

                $scope.fetchEntrySequence = function (entryId) {
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
                                sequence: result.sequence, features: [] //, name: $scope.entry.name
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

                            var location = feature.locations[0];
                            var notes = feature.notes.length ? feature.notes[0].value : "";

                            data.sequenceData.features.push({
                                start: location.genbankStart - 1,
                                end: location.end - 1,
                                id: feature.id,
                                forward: feature.strand == 1,
                                type: feature.type,
                                name: feature.name,
                                notes: notes,
                                annotationType: feature.type,
                                locations: feature.locations
                            });
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
