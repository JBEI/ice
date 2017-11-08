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
            scope: false,
            restrict: "AE",
            transclude: true,

            link: function (scope, element, attrs) {
                var entryId;
                scope.$watch('entry', function (value) {
                    if (!value) {
                        if (attrs.partid) {
                            entryId = attrs.partid;
                        }
                    } else {
                        entryId = value.id;
                    }

                    if (entryId) {
                        scope.fetchEntrySequence(entryId);
                    }
                });
            },

            template: '<div id="ve-Root"><br><img src="img/loader-mini.gif"> Loading sequence&hellip;</div>',

            controller: function ($scope, Util, $window) {
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
                                //"parts",
                                //"primers",
                                "translations",
                                "cutsites",
                                "orfs"
                                //"genbank"
                            ]
                        },
                        ToolBarProps: {
                            //name the tools you want to see in the toolbar in the order you want to see them
                            toolList: [
                                //"saveTool",
                                //"downloadTool",
                                //"undoTool",
                                //"redoTool",
                                "cutsiteTool",
                                "featureTool",
                                "oligoTool",
                                "orfTool",
                                "viewTool",
                                //"editTool",
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
                    Util.get("rest/parts/" + entryId + "/sequence", function (result) {
                        console.log(result);

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

                            data.sequenceData.features.push({
                                start: location.genbankStart,
                                end: location.end,
                                id: feature.id,
                                forward: feature.strand == 1,
                                type: feature.type,
                                name: feature.name,
                                notes: feature.notes,
                                annotationType: feature.type
                            });
                        }

                        $scope.loadVectorEditor(data);
                    });
                };
            }
        };
    });
