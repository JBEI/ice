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
                reloadInfo: "=",
                loaded: "&onLoaded"
            },

            restrict: "AE",

            link: function (scope, element, attrs) {
            },

            template: '<div ng-if="fetchingAnnotations"><img src="img/loader-mini.gif"> &nbsp;Fetching annotations...</div>' +
                '<div id="ve-Root" style="height: 550px; width: 900px"><br><img src="img/loader-mini.gif"> Loading sequence&hellip;</div>',

            controller: function ($rootScope, $scope, Util, $window) {
                $scope.fetchingAnnotations = false;
                $rootScope.$on("ReloadVectorViewData", function (event, data) {
                    if (!data)
                        return;

                    $scope.loadVectorEditor(convertToVEModel(data));
                });

                if (!$scope.entry && !$scope.entry.id) {// todo : error message?
                    console.log("no entry details to retrieve");
                    return;
                }

                $scope.loadVectorEditor = function (data) {
                    $scope.editor = $window.createVectorEditor(document.getElementById("ve-Root"), {
                        onCopy: function (event, sequenceData, editorState) {
                            const clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            // clipboardData.setData('text/plain', sequenceData.sequence);
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

                let convertNotes = function (feature, featureObject) {
                    if (feature.notes.length) {
                        for (let k = 0; k < feature.notes.length; k += 1) {
                            const note = feature.notes[k];
                            if (!featureObject.notes[note.name])
                                featureObject.notes[note.name] = [];
                            featureObject.notes[note.name].push(note.value);
                        }
                    }
                    return featureObject;
                }

                // converts the features array (which is what is returned by ICE) to a class (which is what
                // openVE uses)
                const convertFeaturesToVEModel = function (features, openVE = null) {
                    if (!features) {
                        return {};
                    }

                    // OVE features object
                    if (!openVE)
                        openVE = {};

                    for (let i = 0; i < features.length; i += 1) {
                        let feature = features[i];
                        if (openVE[feature.id]) // feature is keyed by feature database id
                            continue;

                        // skip any features that do not have any locations specified
                        if (!feature.locations.length)
                            continue;

                        if (feature.locations.length > 1)
                            feature.locations.sort(compareLocations);   // todo: there is a bug here if spanning origin

                        // deal with locations
                        for (let j = 0; j < feature.locations.length; j += 1) {
                            let location = feature.locations[j];
                            let featureObject = openVE[feature.id];
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

                                // deal with feature notes
                                featureObject = convertNotes(feature, featureObject);
                            } else {
                                featureObject = {
                                    start: location.genbankStart - 1,
                                    end: location.end - 1,
                                    fid: feature.id,
                                    forward: feature.strand === 1,
                                    type: feature.type,
                                    name: feature.name,
                                    notes: {},
                                    annotationType: feature.type
                                };

                                // deal with feature notes
                                featureObject = convertNotes(feature, featureObject);
                            }

                            openVE[featureObject.fid] = featureObject;
                        }
                    }
                    return openVE;
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

                    data.sequenceData.features = convertFeaturesToVEModel(result.features, data.sequenceData.features);
                    return data;
                };

                const fetchLocalSequenceFeatures = function (sequence, start = undefined) {
                    if (!start)
                        start = 20;     // default number of features returned on initial call is 20

                    $scope.fetchingAnnotations = true;
                    Util.get("rest/sequences/" + sequence.identifier + "/annotations", function (result) {
                        const seqData = $scope.editor.getState().sequenceData;
                        start += result.data.length;
                        seqData.features = convertFeaturesToVEModel(result.data, seqData.features); // result.resultCount;
                        $scope.editor.updateEditor({sequenceData: seqData});

                        if (start < result.resultCount) {
                            console.log("loaded", start);
                            fetchLocalSequenceFeatures(sequence, start);
                        } else {
                            $scope.fetchingAnnotations = false;
                            $scope.loaded(seqData);
                            $rootScope.$emit("VectorEditorSequenceModel", {sequenceData: seqData});
                        }
                    }, {start: start, limit: 10}, function (error) {
                        $scope.fetchingAnnotations = false;
                    });
                }

                const getSequence = function (url, pageAnnotations = false) {
                    Util.get(url, function (result) {
                        if (!result.sequence)
                            return;

                        const data = convertToVEModel(result);
                        $rootScope.$emit("VectorEditorSequenceModel", data);
                        $scope.loadVectorEditor(data);

                        if (pageAnnotations)
                            fetchLocalSequenceFeatures(result)
                    });
                }

                $scope.fetchEntrySequence = function (entryId) {
                    if ($scope.remote && $scope.remote.folderId) {
                        console.log("loading shared sequence for " + entryId + " remote: " + $scope.remote);
                        getSequence("rest/parts/" + entryId + "/sequence?remote=true&folderId=" + $scope.remote.folderId);
                    } else if ($scope.remote && $scope.remote.partner) {
                        console.log("loading remote sequence: " + $scope.remote);
                        getSequence("rest/web/" + $scope.remote.partner + "/entries/" + entryId + "/sequence");
                    } else {
                        console.log("loading local sequence for entry " + entryId);
                        getSequence("rest/parts/" + entryId + "/sequence?annotations=false", true);
                    }
                };

                //
                // init
                //
                $scope.fetchEntrySequence($scope.entry.id);
            }
        };
    });
