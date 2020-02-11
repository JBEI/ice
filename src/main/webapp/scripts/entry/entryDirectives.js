'use strict';

angular.module('ice.entry.directives', [])
    .directive("icePlate96", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "=",
                entry: "=",
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
    .directive("iceGenscript", function () {
        return {
            scope: {
                sample: "=",
                delete: "&onDelete",
                remote: "="
            },

            restrict: "E",
            templateUrl: "scripts/entry/sample/genscript.html",
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
            },

            template: '<div id="ve-Root" style="height: 550px; width: 900px"><br><img src="img/loader-mini.gif"> {{$scope.loadMessage || "Loading"}} sequence&hellip;</div>',

            controller: function ($rootScope, $scope, Util, $window) {
                $rootScope.$on("ReloadVectorViewData", function (event, data) {
                    if (!data)
                        return;

                    console.log("refreshing vector editor");
                    $scope.loadVectorEditor(convertToVEModel(data));
                });

                $scope.loadMessage = undefined;
                if (!$scope.entry && !$scope.entry.id) {// todo : error message?
                    console.log("no entry details to retrieve");
                    return;
                }

                $scope.loadVectorEditor = function (data) {
                    $scope.loadMessage = "Rendering";
                    $scope.editor = $window.createVectorEditor(document.getElementById("ve-Root"), {
                        onCopy: function (event, sequenceData, editorState) {
                            const clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            clipboardData.setData('text/plain', sequenceData.sequence);
                            data.selection = editorState.selectionLayer;
                            data.openVECopied = sequenceData;
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
                                "findTool",
                                "visibilityTool"
                            ]
                        }
                    });

                    let plasmidActive = data && data.sequenceData && data.sequenceData.circular === true;

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
                        panelsShown: [[
                            {
                                id: "circular",
                                name: "Plasmid",
                                active: plasmidActive
                            },
                            {
                                id: "sequence",
                                name: "Sequence Map",
                                active: false
                            },

                            {
                                id: "rail",
                                name: "Linear Map",
                                active: !plasmidActive
                            },
                            {
                                id: "properties",
                                name: "Properties",
                                active: false
                            }
                        ]]
                    });
                };

                function compareLocations(l1, l2) {
                    if (l1.genbankStart - l2.genbankStart === 0)
                        return l1.end - l2.end;
                    return l1.genbankStart - l2.genbankStart;
                }

                let convertToVEModel = function (result) {
                    let data;
                    if ($scope.entry.type === 'PROTEIN') {
                        data = {
                            sequenceData: {
                                isProtein: true,
                                proteinSequence: result.sequence,
                                name: $scope.entry.name,
                            }
                        }
                    } else {
                        data = {
                            sequenceData: {
                                sequence: result.sequence,
                                features: {},
                                name: $scope.entry.name,
                                circular: result.isCircular
                            },
                            registryData: {
                                sid: result.id,
                                uri: result.uri,
                                identifier: result.identifier,
                                name: result.name,
                                circular: result.isCircular
                            }
                        };
                    }

                    for (let i = 0; i < result.features.length; i += 1) {
                        let feature = result.features[i];
                        if (!feature.locations.length)
                            continue;

                        let notes = feature.notes.length ? feature.notes[0].value : "";
                        feature.locations.sort(compareLocations);

                        for (let j = 0; j < feature.locations.length; j += 1) {
                            let location = feature.locations[j];
                            let featureObject = data.sequenceData.features[feature.id];
                            if (featureObject) {
                                // update locations
                                let locations = featureObject.locations;
                                if (!locations) {
                                    locations = [];
                                    locations.push({start: featureObject.start, end: featureObject.end});
                                }
                                locations.push({start: location.genbankStart - 1, end: location.end - 1});
                                featureObject.end = location.end - 1;
                                featureObject.locations = locations;
                            } else {
                                featureObject = {
                                    start: location.genbankStart - 1,
                                    end: location.end - 1,
                                    fid: feature.id,
                                    forward: feature.strand === 1,
                                    type: feature.type,
                                    name: feature.name,
                                    notes: notes,
                                    annotationType: feature.type
                                };
                            }

                            data.sequenceData.features[featureObject.fid] = featureObject;
                        }
                    }
                    return data;
                };

                $scope.fetchEntrySequence = function (entryId) {
                    let url;

                    if ($scope.remote && $scope.remote.folderId) {
                        url = "rest/parts/" + entryId + "/sequence?remote=true&folderId=" + $scope.remote.folderId;
                        console.log("loading shared sequence for " + entryId + " remote: " + $scope.remote);
                    } else if ($scope.remote && $scope.remote.partner) {
                        url = "rest/web/" + $scope.remote.partner + "/entries/" + entryId + "/sequence";
                        console.log("loading remote sequence: " + $scope.remote);
                    } else {
                        url = "rest/parts/" + entryId + "/sequence";
                        console.log("loading local sequence for entry " + entryId);
                    }

                    Util.get(url, function (result) {
                        if (!result.sequence)
                            return;

                        const data = convertToVEModel(result);
                        $rootScope.$emit("VectorEditorSequenceModel", data);
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
