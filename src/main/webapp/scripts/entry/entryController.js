'use strict';

angular.module('ice.entry.controller', [])
    .controller('EntryAttachmentController', function ($scope, $window, $stateParams, FileUploader, Util, Authentication) {

        // init. get remote
        Util.list("rest/parts/" + $stateParams.id + "/attachments", function (result) {
            $scope.attachments = result;
        }, $scope.remote);

        let desc = "";
        $scope.uploadError = undefined;

        $scope.$watch('attachmentDescription', function () {
            desc = $scope.attachmentDescription;
        });

        // create a uploader with options
        let uploader = $scope.uploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/file/attachment",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": Authentication.getSessionId()
            }
        });

        uploader.onAfterAddingFile = function () {
            $scope.uploadError = undefined;
        };

        uploader.onSuccessItem = function (item, response) {
            response.description = desc;
            $scope.uploadError = undefined;
            Util.post("rest/parts/" + $stateParams.id + "/attachments", response, function (result) {
                result.canEdit = true;
                $scope.attachments.push(result);
                $scope.cancel();
            });
        };

        uploader.onErrorItem = function (item, response, status, headers) {
            console.log(item, response, status, headers);
            $scope.uploadError = true;
        };

        $scope.cancel = function () {
            $scope.uploader.cancelAll();
            $scope.uploader.clearQueue();
            $scope.showAttachmentInput = false;
            $scope.attachmentDescription = undefined;
        };

        $scope.downloadAttachment = function (attachment) {
            $window.open("rest/file/attachment/" + attachment.fileId + "?sid=" + Authentication.getSessionId(), "_self");
        };

        const confirmObject = {};

        $scope.deleteAttachment = function (index, att) {
            Util.remove('rest/parts/' + $stateParams.id + '/attachments/' + att.id, {}, function (result) {
                confirmObject[index] = false;
                $scope.attachments.splice(index, 1);
            });
        };

        $scope.confirmDelete = function (idx) {
            return confirmObject[idx];
        };

        $scope.setConfirmDelete = function (idx, value) {
            confirmObject[idx] = value;
        }
    })
    .controller('EntryCommentController', function ($scope, $stateParams, Util) {
        let entryId = $stateParams.id;
        $scope.newComment = {samples: []};

        Util.list('rest/parts/' + entryId + '/comments', function (result) {
            $scope.entryComments = result;
        });

        Util.list('rest/parts/' + entryId + '/samples', function (result) {
            $scope.entrySamples = result;
        });

        $scope.createComment = function () {
            Util.post('rest/parts/' + entryId + '/comments', $scope.newComment, function (result) {
                $scope.entryComments.splice(0, 0, result);
                $scope.addComment = false;
                $scope.entryStatistics.commentCount = $scope.entryComments.length;
            });
        };

        $scope.updateComment = function (comment) {
            Util.update('rest/parts/' + entryId + '/comments/' + comment.id, comment, {}, function (result) {
                if (result) {
                    comment.edit = false;
                    comment.modified = result.modified;
                }
            });
        };

        /**
         * Add or remove sample to comment. If sample is already a part of the comment, it is removed,
         * if not, it is added
         * @param sample sample to add or remove
         */
        $scope.addRemoveSample = function (sample) {
            let idx = $scope.newComment.samples.indexOf(sample);
            if (idx === -1)
                $scope.newComment.samples.push(sample);
            else
                $scope.newComment.samples.splice(idx, 1);
        };
    })
    .controller('ShotgunSequenceController', function ($scope, $window, $stateParams, FileUploader, $uibModal, Util, Authentication) {
        let entryId = $stateParams.id;
        $scope.shotgunUploadError = undefined;
        $scope.maxSize = 5;
        $scope.shotgunParams = {limit: 5, currentPage: 1, start: 0};

        Util.list('rest/parts/' + entryId + '/shotgunsequences', function (result) {
            $scope.shotgunSequences = result;
        }, $scope.shotgunParams);

        $scope.shotgunPageChanged = function () {
            $scope.shotgunParams.start = ($scope.shotgunParams.currentPage - 1) * $scope.shotgunParams.limit;
            Util.list("/rest/parts/" + entryId + "/shotgunsequences", function (result) {
                $scope.shotgunSequences = result;
            }, $scope.shotgunParams);
        };

        $scope.showAddShotgunSequenceModal = function () {
            let modalInstance = $uibModal.open({
                templateUrl: "scripts/entry/modal/add-shotgun-sequence.html",
                controller: 'ShotgunSequenceUploadModalController',
                backdrop: 'static',
                resolve: {
                    entryId: function () {
                        return $stateParams.id;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.shotgunParams.start = 0;

                Util.list("/rest/parts/" + entryId + "/shotgunsequences", function (result) {
                    Util.setFeedback("", "success");
                    $scope.shotgunSequences = result;
                    $scope.showUploadOptions = false;
                    $scope.shotgunUploadError = false;
                }, $scope.shotgunParams);
            });
        };

        $scope.deleteShotgunSequenceFile = function (fileId) {
            let foundSequence;
            let foundIndex;

            for (let i = 0; i < $scope.shotgunSequences.length; i++) {
                let shotgunSequence = $scope.shotgunSequences[i];
                if (shotgunSequence.fileId === fileId && shotgunSequence.fileId !== undefined) {
                    foundSequence = shotgunSequence;
                    foundIndex = i;
                    break;
                }
            }

            if (foundSequence !== undefined) {
                Util.remove("rest/parts/" + entryId + "/shotgunsequences/" + foundSequence.id, {}, function () {
                    $scope.shotgunSequences.splice(foundIndex, 1);
                    $scope.entryStatistics.sequenceCount = $scope.shotgunSequences.length;
                });
            }
        };

        $scope.downloadShotgunFile = function (sequence) {
            $window.open("rest/file/shotgunsequence/" + sequence.fileId + "?sid=" + Authentication.getSessionId(), "_self");
        };
    })
    .controller('ShotgunSequenceUploadModalController', function ($scope, FileUploader, $uibModalInstance, entryId,
                                                                  Authentication) {
        $scope.cancelAddShotgunSequence = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.shotgunSequenceUploader = new FileUploader({
            scope: $scope, // to automatically update the html. Default: $rootScope
            url: "rest/parts/" + entryId + "/shotgunsequences",
            method: 'POST',
            removeAfterUpload: true,
            headers: {
                "X-ICE-Authentication-SessionId": Authentication.getSessionId()
            },
            autoUpload: true,
            queueLimit: 1, // can only upload 1 file
            formData: [
                {
                    entryId: entryId
                }
            ]
        });

        $scope.shotgunSequenceUploader.onSuccessItem = function (item, response, status) {
            if (status !== "200") {
                $scope.shotgunUploadError = true;
                return;
            }

            $uibModalInstance.close();
        };

        $scope.shotgunSequenceUploader.onErrorItem = function () {
            $scope.shotgunUploadError = true;
        };
    })
    .controller('EntryExperimentController', function ($scope, $stateParams, Util) {
        let entryId = $stateParams.id;
        $scope.experiment = {};
        $scope.addExperiment = false;

        Util.list("/rest/parts/" + entryId + "/experiments", function (result) {
            $scope.entryExperiments = result;
        });

        $scope.createExperiment = function () {
            if ($scope.experiment === undefined ||
                $scope.experiment.url === undefined ||
                $scope.experiment.url === '' ||
                $scope.experiment.url.lastIndexOf('http', 0) !== 0) {
                $scope.urlMissing = true;
                return;
            }

            Util.post("/rest/parts/" + entryId + "/experiments", $scope.experiment, function (result) {
                $scope.entryExperiments.splice(0, 0, result);
                $scope.addExperiment = false;
                $scope.entryStatistics.experimentalDataCount = $scope.entryExperiments.length;
            });
        };

        $scope.deleteStudy = function (study) {
            Util.remove("/rest/parts/" + entryId + "/experiments/" + study.id, {}, function () {
                let idx = $scope.entryExperiments.indexOf(study);
                if (idx >= 0) {
                    $scope.entryExperiments.splice(idx, 1);
                }
            });
        }
    })
    .controller('PartHistoryController', function ($scope, $window, $stateParams, Util) {
        let entryId = $stateParams.id;
        $scope.historyParams = {offset: 0, limit: 10, currentPage: 1, maxSize: 5};

        $scope.historyPageChanged = function () {
            $scope.historyParams.offset = ($scope.historyParams.currentPage - 1) * $scope.historyParams.limit;
            Util.get("rest/parts/" + entryId + "/history", function (result) {
                if (history)
                    $scope.history = result;
                //$scope.history = result;
            }, $scope.historyParams);
        };
        $scope.historyPageChanged(); // init

        $scope.deleteHistory = function (history) {
            Util.remove('rest/parts/' + entryId + '/history/' + history.id, {}, function () {
                let idx = $scope.history.data.indexOf(history);
                if (idx === -1)
                    return;

                $scope.history.data.splice(idx, 1);
                $scope.history.resultCount -= 1;
            });
        }
    })

    .controller('EntryPermissionController', function ($rootScope, $scope, filterFilter, Util) {
        let panes = $scope.panes = [];
        $scope.userFilterInput = undefined;
        $scope.canSetPublicPermission = undefined;
        $scope.selectedArticle = {type: 'ACCOUNT', placeHolder: "Enter name or email"};

        if (!$rootScope.settings || !$rootScope.settings['RESTRICT_PUBLIC_ENABLE']) {
            Util.get("rest/config/RESTRICT_PUBLIC_ENABLE", function (result) {
                if (!result)
                    return;
                if (!$rootScope.settings)
                    $rootScope.settings = {};
                $rootScope.settings['RESTRICT_PUBLIC_ENABLE'] = result.value;
                $scope.canSetPublicPermission = (result.value === "no") || $rootScope.user.isAdmin;
            });
        } else {
            $scope.canSetPublicPermission = ($rootScope.settings['RESTRICT_PUBLIC_ENABLE'].value === "no") || $rootScope.user.isAdmin;
        }

        $scope.setPermissionArticle = function (type) {
            $scope.selectedArticle.type = type;
            $scope.autoCompleteUsersOrGroups = undefined;
            $scope.userFilterInput = undefined;
        };

        $scope.activateTab = function (pane) {
            angular.forEach(panes, function (pane) {
                pane.selected = false;
            });
            pane.selected = true;
            if (pane.title === 'Read')
                $scope.activePermissions = $scope.readPermissions;
            else
                $scope.activePermissions = $scope.writePermissions;
        };

        // retrieve permissions
        Util.list('rest/parts/' + $scope.entry.id + '/permissions', function (result) {
            $scope.readPermissions = [];
            $scope.writePermissions = [];

            angular.forEach(result, function (item) {
                item.canEdit = $rootScope.user.isAdmin || (item.group && !item.group.autoJoin);

                if (item.type === 'WRITE_ENTRY')
                    $scope.writePermissions.push(item);
                else
                    $scope.readPermissions.push(item);
            });

            $scope.panes.push({title: 'Read', count: $scope.readPermissions.length, selected: true});
            $scope.panes.push({title: 'Write', count: $scope.writePermissions.length});
            $scope.activePermissions = $scope.readPermissions;
        });

        $scope.filter = function () {
            let val = $scope.userFilterInput;
            if (!val) {
                $scope.autoCompleteUsersOrGroups = undefined;
                return;
            }

            $scope.filtering = true;
            let resource;
            let queryParams;

            if ($scope.selectedArticle.type === 'ACCOUNT') {
                resource = "users";
                queryParams = {limit: 8, val: val};
            } else {
                resource = "groups";
                queryParams = {limit: 8, token: val};
            }

            Util.list("rest/" + resource + "/autocomplete", function (result) {
                if ($scope.selectedArticle.type === "ACCOUNT") {
                    angular.forEach(result, function (item) {
                        item.label = item.firstName + " " + item.lastName;
                    });
                }

                $scope.autoCompleteUsersOrGroups = result;
                $scope.filtering = false;

            }, queryParams, function (error) {
                console.error(error);
                $scope.filtering = false;
                $scope.autoCompleteUsersOrGroups = undefined;
            });
        };

        $scope.showAddPermissionOptionsClick = function () {
            $scope.showPermissionInput = true;
        };

        $scope.closePermissionOptions = function () {
            $scope.showPermissionInput = false;
        };

        let removePermission = function (permissionId) {
            Util.remove("rest/parts/" + $scope.entry.id + "/permissions/" + permissionId, {}, function (result) {
                if (!result)
                    return;

                // check which pane is selected
                let pane;
                if ($scope.panes[0].selected)
                    pane = $scope.panes[0];
                else
                    pane = $scope.panes[1];

                let i = -1;

                for (let idx = 0; idx < $scope.activePermissions.length; idx += 1) {
                    if (permissionId === $scope.activePermissions[idx].id) {
                        i = idx;
                        break;
                    }
                }

                if (i === -1) {
                    return;
                }

                $scope.activePermissions.splice(i, 1);
                pane.count = $scope.activePermissions.length;
            });
        };

        //
        // when user clicks on the check box, removes permission if exists or adds if not
        //
        $scope.addRemovePermission = function (userOrGroup) {
            if (userOrGroup.selected) {
                removePermission(userOrGroup.permissionId);
                userOrGroup.selected = false;
                return;
            }

            let permission = {};
            permission.article = $scope.selectedArticle.type;
            permission.articleId = userOrGroup.id;

            // add permission
            for (let i = 0; i < panes.length; i += 1) {
                if (panes[i].selected) {
                    permission.type = panes[i].title.toUpperCase() + "_ENTRY";
                    break;
                }
            }

            permission.typeId = $scope.entry.id;

            Util.post('rest/parts/' + $scope.entry.id + '/permissions', permission, function (result) {
                // result is the permission object
                $scope.entry.id = result.typeId;
                result.canEdit = $rootScope.user.isAdmin || (result.group && !result.group.autoJoin);

                if (result.type === 'READ_ENTRY') {
                    $scope.readPermissions.push(result);
                    $scope.activePermissions = $scope.readPermissions;
                } else {
                    $scope.writePermissions.push(result);
                    $scope.activePermissions = $scope.writePermissions;
                }

                userOrGroup.permissionId = result.id;
                userOrGroup.selected = true;
            });
        };

        $scope.enablePublicRead = function (e) {
            Util.update('rest/parts/' + e.id + '/permissions/public', {}, {}, function () {
                $scope.entry.publicRead = true;
            });
        };

        $scope.disablePublicRead = function (e) {
            Util.remove('rest/parts/' + e.id + '/permissions/public', {}, function () {
                $scope.entry.publicRead = false;
            });
        };

        $scope.deletePermission = function (index, permission) {
            removePermission(permission.id);
        };
    })
    .controller('EntryFoldersController', function ($rootScope, $scope, Util) {
        $scope.containedFolders = undefined;
        Util.list("rest/parts/" + $scope.entry.recordId + "/folders", function (result) {
            $scope.containedFolders = result;
        });

        $scope.removeEntryFromFolder = function (folder) {
            Util.post("rest/folders/" + folder.id + "/entries",
                {entries: [$scope.entry.id], folderId: folder.id}, function (result) {
                    if (result) {
                        $rootScope.$broadcast("RefreshAfterDeletion");
                        $scope.$broadcast("UpdateCollectionCounts");
                        Util.setFeedback('1 entry removed from folder', 'success');
                        let i = $scope.containedFolders.indexOf(folder);
                        if (i >= 0)
                            $scope.containedFolders.splice(i, 1);
                    }
                }, {move: false});
        };
    })
    .controller('EntryController', function ($scope, $stateParams, $location, $uibModal, $rootScope,
                                             $route, $window, $document, FileUploader, EntryService, EntryContextUtil,
                                             Selection, Util, Authentication) {
            $scope.partIdEditMode = false;
            $scope.showSBOL = true;
            $scope.context = EntryContextUtil.getContext();

            $scope.isFileUpload = false;
            $scope.existingVectorEditorSequenceModel = undefined;
            $scope.sessionId = Authentication.getSessionId();

            $rootScope.$on("VectorEditorSequenceModel", function (event, data) {
                $scope.existingVectorEditorSequenceModel = data;
            });

            // determines if the specified field has a value that allows is to be displayed
            $scope.fieldHasValue = function (field) {
                if (field.isCustom) {
                    return field.value;
                }

                return $scope.entry[field.schema] != null && $scope.entry[field.schema].toString().length !== 0;
            };

            // open vector editor modal
            $scope.openSequenceInFullVectorEditor = function () {
                let sequence;
                $scope.updatedSequence = undefined;

                let entry = $scope.entry;
                let remote = $scope.remoteParams;

                // converts FeaturedDNASequence (jbei format) to genbank
                let convertFeaturedDNASequence = function (result) {
                    let features = [];

                    if (result.features) {
                        for (let i = 0; i < result.features.length; i += 1) {
                            let feature = result.features[i];
                            if (!feature.locations.length)
                                continue;

                            let notes = feature.notes.length ? feature.notes[0].value : "";

                            for (let j = 0; j < feature.locations.length; j += 1) {
                                let location = feature.locations[j];

                                let featureObject = {
                                    start: location.genbankStart - 1,
                                    end: location.end - 1,
                                    fid: feature.id,
                                    forward: feature.strand === 1,
                                    type: feature.type,
                                    name: feature.name,
                                    notes: notes,
                                    annotationType: feature.type
                                };

                                features.push(featureObject);
                            }
                        }
                    }

                    return features;
                };

                // init
                const createVectorEditorNode = function (openVEData) {
                    $scope.vEeditor = $window.createVectorEditor("createDomNodeForMe", {
                        editorName: "vector-editor",
                        doNotUseAbsolutePosition: true,
                        isFullscreen: true,
                        shouldAutosave: true,
                        disableSetReadOnly: true,
                        handleFullscreenClose: function () { // this will make the editor fullscreen by default, and will allow you to handle the close request
                            $scope.vEeditor.close();         // handle vector editor root removal and clean up
                        },

                        getSequenceAtVersion: function (versionId) {
                            return openVEData.sequenceData;
                            // teselagenSequenceData
                        },

                        // getVersionList: function () {
                        //     Util.get('rest/sequences/' + openVEData.registryData.identifier + '/history', function (result) {
                        //         return [
                        //             {
                        //                 versionId: "51241",
                        //                 dateChanged: "01/11/2019",
                        //                 editedBy: "Hector Plahar",
                        //                 revisionType: "Feature Add"
                        //             },
                        //             {
                        //                 versionId: "51241",
                        //                 dateChanged: "01/11/2019",
                        //                 editedBy: "Hector Plahar",
                        //                 revisionType: "Feature Remove"
                        //             }]
                        //     })
                        // },

                        getVersionList: function () {
                            //fake talking to some api
                            return new Promise(resolve => {
                                setTimeout(() => {
                                    resolve([
                                        // {
                                        //     dateChanged: "12/30/2211",
                                        //     editedBy: "Nara",
                                        //     revisionType: "Sequence Deletion",
                                        //     versionId: 2
                                        // },
                                        // {
                                        //     dateChanged: "8/30/2211",
                                        //     editedBy: "Ralph",
                                        //     revisionType: "Feature Edit",
                                        //     versionId: 3
                                        // }
                                    ]);
                                }, 100);
                            });
                        },

                        onSave: function (event, sequenceData, editorState, onSuccessCallback) {
                            if (remote.remote || !entry.canEdit)
                                return;

                            // convert to featuredDNASequence
                            sequence = {
                                features: [],
                                sequence: sequenceData.sequence
                            };

                            let featureMap = {};

                            for (const prop in sequenceData.features) {
                                if (!sequenceData.features.hasOwnProperty(prop))
                                    continue;

                                let feature = sequenceData.features[prop];
                                let existingFeature = featureMap[feature.id];
                                if (existingFeature) {
                                    existingFeature.locations.push({
                                        genbankStart: feature.start + 1,
                                        end: feature.end + 1
                                    })
                                } else {
                                    featureMap[feature.id] = {
                                        id: feature.fid,
                                        type: feature.type,
                                        name: feature.name,
                                        strand: feature.forward ? 1 : -1,
                                        notes: [{name: "note", value: feature.notes}],
                                        locations: [{
                                            genbankStart: feature.start + 1,
                                            end: feature.end + 1
                                        }]
                                    };
                                }
                            }

                            for (const property in featureMap) {
                                if (!featureMap.hasOwnProperty(property))
                                    continue;

                                sequence.features.push(featureMap[property]);
                            }

                            Util.update("rest/parts/" + entry.id + "/sequence", sequence, {},
                                function (result) {
                                    console.log("save completed for", entry.id);
                                    $rootScope.$emit("ReloadVectorViewData", result);
                                    const sequenceModel = {
                                        sequenceData: {
                                            sequence: result.sequence,
                                            features: convertFeaturedDNASequence(result),
                                            name: result.name,
                                            circular: result.isCircular
                                        },
                                    };
                                    $rootScope.$emit("VectorEditorSequenceModel", sequenceModel);
                                    $scope.updatedSequence = result;
                                    onSuccessCallback();
                                })
                        },

                        onCopy: function (event, copiedSequenceData, editorState) {
                            let clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            clipboardData.setData('text/plain', copiedSequenceData.sequence);
                            openVEData.selection = editorState.selectionLayer;
                            openVEData.openVECopied = copiedSequenceData;
                            clipboardData.setData('application/json', JSON.stringify(openVEData));
                            event.preventDefault();
                        },

                        onPaste: function (event, editorState) {
                            let clipboardData = event.clipboardData || window.clipboardData || event.originalEvent.clipboardData;
                            let jsonData = clipboardData.getData('application/json');
                            if (jsonData) {
                                jsonData = JSON.parse(jsonData);
                                jsonData = jsonData.openVECopied;
                            }
                            return jsonData || {sequence: clipboardData.getData("text/plain")}
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
                            //name the tools you want to see in the toolbar in the order you want to see them
                            toolList: [
                                "versionHistoryTool",
                                // "saveTool",
                                "undoTool",
                                "redoTool",
                                "cutsiteTool",
                                "featureTool",
                                "orfTool",
                                "findTool",
                                "visibilityTool"
                            ]
                        }
                    });

                    $scope.vEeditor.updateEditor({
                        readOnly: remote.remote || !entry.canEdit,
                        sequenceData: openVEData.sequenceData,
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
                                    id: "rail",
                                    name: "Linear Map",
                                    active: false
                                },
                                {
                                    id: "properties",
                                    name: "Properties",
                                    active: false
                                }
                            ],
                            [
                                {
                                    id: "sequence",
                                    name: "Sequence Map",
                                    active: true
                                }
                            ]
                        ]
                    })
                };

                if ($scope.existingVectorEditorSequenceModel && $scope.existingVectorEditorSequenceModel.sequenceData) {
                    createVectorEditorNode($scope.existingVectorEditorSequenceModel);
                } else {
                    Util.get("rest/parts/" + entry.id + "/sequence", function (result) {
                        console.log(result);

                        $scope.sequenceName = result.name;
                        sequence = result;

                        let data = {
                            sequenceData: {
                                sequence: result.sequence,
                                features: [],
                                name: result.name,
                                circular: result.isCircular
                            },
                            registryData: {
                                uri: result.uri,
                                identifier: result.identifier,
                                name: result.name,
                                circular: result.isCircular
                            }
                        };

                        data.sequenceData.features = convertFeaturedDNASequence(result);
                        createVectorEditorNode(data);
                    }, remote);
                }
            };

            $scope.sequenceUpload = function (type) {
                if (type === 'file') {
                    $scope.isFileUpload = true;
                    $scope.isPaste = false;
                } else {
                    $scope.isPaste = true;
                    $scope.isFileUpload = false;
                }
            };

            $scope.processPastedSequence = function (event, part) {
                let sequenceString = event.originalEvent.clipboardData.getData('text/plain');
                Util.update("rest/parts/" + part.id + "/sequence", {sequence: sequenceString}, {isPaste: true},
                    function (result) {
                        if (!result)
                            return;

                        part.hasSequence = true;
                        part.basePairCount = result.sequence.length;
                        $rootScope.$emit("ReloadVectorViewData", result);
                    })
            };

            $scope.deleteSequence = function (part) {
                let modalInstance = $uibModal.open({
                    templateUrl: 'scripts/entry/sequence/modal-delete-sequence-confirmation.html',
                    controller: function ($scope, $uibModalInstance) {
                        $scope.toDelete = part;
                        $scope.processingDelete = undefined;
                        $scope.errorDeleting = undefined;

                        $scope.deleteSequence = function () {
                            $scope.processingDelete = true;
                            $scope.errorDeleting = false;

                            Util.remove('rest/parts/' + part.id + '/sequence', {}, function (result) {
                                $scope.processingDelete = false;
                                $uibModalInstance.close(part);
                            }, function () {
                                $scope.processingDelete = false;
                                $scope.errorDeleting = true;
                            });
                        }
                    },
                    backdrop: "static"
                });

                modalInstance.result.then(function (part) {
                    if (part)
                        part.hasSequence = false;
                });
            };

            $scope.addLink = function (part, role) {
                let modalInstance = $uibModal.open({
                    templateUrl: 'scripts/entry/modal/add-link-modal.html',
                    controller: function ($scope, $http, $uibModalInstance, Authentication) {
                        $scope.mainEntry = part;
                        $scope.role = role;
                        $scope.loadingAddExistingData = undefined;

                        if (role === 'PARENT') {
                            $scope.links = part.parents;
                        } else {
                            $scope.links = part.linkedParts;
                        }

                        $scope.getEntriesByPartNumber = function (val) {
                            return $http.get('rest/search/filter', {
                                headers: {'X-ICE-Authentication-SessionId': Authentication.getSessionId()},
                                params: {
                                    token: val,
                                    field: 'PART_NUMBER'
                                }
                            }).then(function (res) {
                                return res.data;
                            });
                        };

                        let linkPartToMainEntry = function (item) {
                            Util.post('rest/parts/' + $scope.mainEntry.id + '/links', item, function () {
                                $scope.links.push(item);   // todo
                                $scope.addExistingPartNumber = undefined;
                                $scope.mainEntrySequence = undefined;
                            }, {linkType: $scope.role}, function () {
                                $scope.errorMessage = "Error linking this entry to " + item.partId;
                            });
                        };

                        // todo : todo
                        $scope.addExistingPartLink = function ($item) {
                            $scope.errorMessage = undefined;

                            // prevent selecting current entry
                            if ($item === $scope.mainEntry.partId)
                                return;

                            // or already added entry
                            let found = false;
                            angular.forEach($scope.links, function (t) {
                                if (t.partId === $item) {
                                    found = true;
                                }
                            });
                            if (found)
                                return;

                            // fetch entry being added from server
                            Util.get("rest/parts/" + $item, function (result) {
                                $scope.selectedLink = result;
                                if ($scope.role === 'CHILD') {

                                    // if item being added as a child is of type part then
                                    if (result.type.toLowerCase() === 'part') {

                                        // check if it has a sequence
                                        if (!result.hasSequence) {
                                            $scope.addExistingPartNumber = result;

                                            // if not, retrieve sequence annotations for parent entry
                                            // to allow user to select one annotation as the sequence for the entry being
                                            // added
                                            $scope.getEntrySequence($scope.mainEntry.id);
                                        } else {

                                            // has sequence so just add the link
                                            linkPartToMainEntry(result);
                                        }
                                    } else {
                                        // just add the link
                                        linkPartToMainEntry(result);
                                    }
                                } else {
                                    // parent of main entry being added
                                    if ($scope.mainEntry.type.toLowerCase() === 'part') {

                                        // if child (main) does not have a attached sequence
                                        if (!$scope.mainEntry.hasSequence) {

                                            // retrieve sequence feature options for parent
                                            $scope.addExistingPartNumber = result;
                                            $scope.getEntrySequence($scope.addExistingPartNumber.id);
                                        } else {
                                            linkPartToMainEntry(result);
                                        }
                                    } else {
                                        linkPartToMainEntry(result);
                                    }
                                }
                            });
                        };

                        $scope.removeExistingPartLink = function (link) {
                            let i = $scope.links.indexOf(link);
                            if (i < 0)
                                return;

                            Util.remove('rest/parts/' + $scope.mainEntry.id + '/links/' + link.id,
                                {linkType: $scope.role}, function () {
                                    $scope.links.splice(i, 1);
                                });
                        };

                        $scope.close = function () {
                            $uibModalInstance.close();
                        };

                        $scope.getEntrySequence = function (id) {
                            $scope.retrievingSequenceFeatureList = true;
                            $scope.mainEntrySequence = undefined;

                            Util.get("rest/parts/" + id + "/sequence", function (result) {
                                $scope.mainEntrySequence = result;
                                $scope.retrievingSequenceFeatureList = false;
                            }, function (error) {
                                console.error(error);
                                $scope.retrievingSequenceFeatureList = false;
                            });
                        };

                        $scope.addSequenceToLinkAndLink = function (feature) {
                            // update sequence information on entry
                            // POST rest/parts/{id}/sequence featuredDNA sequence
                            //console.log($scope.mainEntrySequence, feature, $scope.addExistingPartNumber);

                            // todo : backend should probably handle this; quick fix for the milestone
                            let start = feature.locations[0].genbankStart;
                            let end = feature.locations[0].end;
                            let sequence = $scope.mainEntrySequence.sequence.substring(start - 1, end);
                            feature.locations[0].genbankStart = 1;
                            feature.locations[0].end = sequence.length;

                            let linkSequence = {
                                identifier: $scope.addExistingPartNumber.partId,
                                sequence: sequence,
                                genbankStart: 0,
                                end: sequence.length,
                                features: [feature]
                            };

                            let sequencePartId;
                            if ($scope.role === 'CHILD') {
                                sequencePartId = $scope.selectedLink.id;
                            } else {
                                sequencePartId = $scope.mainEntry.id;
                            }

                            // add sequence to entry
                            Util.update("rest/parts/" + sequencePartId + "/sequence", linkSequence, {}, function () {
                                linkPartToMainEntry($scope.addExistingPartNumber);
                            });
                        };
                    },
                    backdrop: "static"
                });

                modalInstance.result.then(function (entry) {
                    if (entry) {
                        part = entry;
                    }
                }, function () {
                });
            };

            $scope.sbolShowHide = function () {
                $scope.showSBOL = !$scope.showSBOL;
            };

            $scope.getSequenceSectionHeader = function () {
                if ($scope.entry.hasSequence && !$scope.entry.basePairCount)
                    return "SBOL INFORMATION";
                return "SEQUENCE";
            };

            $scope.entryFields = undefined;
            $scope.entry = undefined;
            $scope.notFound = undefined;
            $scope.noAccess = undefined;

            // init : fetch entry; see if folder id and remote params is set
            let params = $location.search();
            $scope.remoteParams = params;

            Util.get("rest/parts/" + $stateParams.id, function (result) {
                Selection.reset();
                Selection.selectEntry(result);

                $scope.entry = EntryService.convertToUIForm(result);
                if ($scope.entry.canEdit)
                    $scope.newParameter = {edit: false};

                $scope.entry.remote = params.remote;

                // get sample count, comment count etc
                Util.get("rest/parts/" + $stateParams.id + "/statistics", function (stats) {
                    $scope.entryStatistics = stats;
                }, params);

            }, params, function (error) {
                if (error.status === 404)
                    $scope.notFound = true;
                else if (error.status === 403)
                    $scope.noAccess = true;
            });

            let menuSubDetails = $scope.subDetails = EntryService.getMenuSubDetails(params.remote);

            $scope.enableQuickEdit = function (field) {

                //
                // converts autoCompleteAdd array of strings to object
                //
                if (field.inputType === 'autoCompleteAdd' && angular.isArray($scope.entry[field.schema])) {
                    $scope.convertedAutoCompleteAdd = [];

                    for (let i = 0; i < $scope.entry[field.schema].length; i += 1) {
                        $scope.convertedAutoCompleteAdd[i] = {value: $scope.entry[field.schema][i]};
                    }
                }
                //
                // end
                //

                $scope.quickEdit = {};
                field.edit = true; //

                // for custom fields
                if (field.isCustom) {
                    $scope.quickEdit[field.label] = field.value;
                }

                // special treatment for biosafety
                if (field.schema !== "bioSafetyLevel") {
                    $scope.quickEdit[field.schema] = $scope.entry[field.schema];
                } else {
                    switch ($scope.entry[field.schema]) {
                        case "Level 2":
                            $scope.quickEdit[field.schema] = '2';
                            break;

                        case "Restricted":
                            $scope.quickEdit[field.schema] = '-1';
                            break;

                        default:
                            $scope.quickEdit[field.schema] = '1';
                    }
                }

                if (field.inputType === 'withEmail') {
                    $scope.quickEdit[field.schema + 'Email'] = $scope.entry[field.schema + 'Email']
                }
            };

            $scope.showSelection = function (index) {
                angular.forEach(menuSubDetails, function (details) {
                    details.selected = false;
                });
                menuSubDetails[index].selected = true;
                $scope.selection = menuSubDetails[index].url;
                if (menuSubDetails[index].id) {
                    $location.path("entry/" + $stateParams.id + "/" + menuSubDetails[index].id);
                } else {
                    $location.path("entry/" + $stateParams.id);
                }
            };

            $scope.createCopyOfEntry = function () {
                Util.post("rest/parts", {}, function (result) {
                    $scope.$emit("UpdateCollectionCounts");
                    $scope.showSBOL = false;
                    $location.path('entry/' + result.id);
                }, {source: $scope.entry.recordId});
            };

// check if a selection has been made
            let menuOption = $stateParams.option;
            if (menuOption === undefined) {
                $scope.selection = menuSubDetails[0].url;
                menuSubDetails[0].selected = true;
            } else {
                menuSubDetails[0].selected = false;
                for (let i = 1; i < menuSubDetails.length; i += 1) {
                    if (menuSubDetails[i].id === menuOption) {
                        $scope.selection = menuSubDetails[i].url;
                        menuSubDetails[i].selected = true;
                        break;
                    }
                }

                if ($scope.selection === undefined) {
                    $scope.selection = menuSubDetails[0].url;
                    menuSubDetails[0].selected = true;
                }
            }

            $scope.edit = function (type, val) {
                $scope[type] = val;
            };

            $scope.quickEditEntry = function (field) {
                field.errorUpdating = false;
                field.updating = true;
                field.invalid = false;

                const values = [];

                if (field.inputType === "autoCompleteAdd") {
                    $scope.quickEdit[field.schema] = $scope.convertedAutoCompleteAdd;
                    angular.forEach($scope.convertedAutoCompleteAdd, function (obj) {
                        values.push(obj.value);
                    })
                } else {
                    for (const property in $scope.quickEdit) {
                        if (!$scope.quickEdit.hasOwnProperty(property))
                            continue;

                        values.push($scope.quickEdit[property]);
                    }
                }

                // validate
                let canSubmit = EntryService.validateFields($scope.quickEdit, [field]);
                if (!canSubmit) {
                    field.updating = false;
                    return;
                }

                // update on server with field.label
                Util.update("rest/parts/" + $scope.entry.id + "/" + field.label, values, {}, function (result) {
                    $scope.entry = EntryService.convertToUIForm(result);
                    // switch (field.inputType) {
                    //     default:
                    //         $scope.entry[field.schema] = $scope.quickEdit[field.schema];
                    //         break;
                    //
                    //     case "withEmail":
                    //         $scope.entry[field.schema + 'Email'] = $scope.quickEdit[field.schema + 'Email'];
                    //         break;
                    //
                    //     case "autoCompleteAdd":
                    //         console.log(field, values);
                    //         $scope.entry[field.schema] = values;
                    //         break;
                    // }

                    field.edit = false;
                    field.dirty = false;
                    field.updating = false;
                }, function (error) {
                    console.error(error);
                    field.updating = false;
                    field.errorUpdating = true;
                    Util.setFeedback("Error updating entry", "danger")
                });
            };

            $scope.displayForBLSValue = function (bslValue) {
                if (bslValue === -1)
                    return "Restricted";
                return bslValue;
            };

            $scope.deleteCustomField = function (parameter) {
                let index = $scope.entry.parameters.indexOf(parameter);
                if (index >= 0) {
                    let currentParam = $scope.entry.parameters[index];
                    if (currentParam.id === parameter.id) {
                        Util.remove("rest/custom-fields/" + parameter.id, {}, function () {
                            $scope.entry.parameters.splice(index, 1);
                        })
                    }
                }
            };

            $scope.nextEntryInContext = function () {
                $scope.context.offset += 1;
                $scope.context.callback($scope.context.offset, function (result) {
                    $location.path("entry/" + result);
                });
            };

            $scope.prevEntryInContext = function () {
                $scope.context.offset -= 1;
                $scope.context.callback($scope.context.offset, function (result) {
                    $location.path("entry/" + result);
                });
            };

            $scope.backTo = function () {
                Selection.reset();
                $location.path($scope.context.back);
            };

// removes linked parts
            $scope.removeLink = function (mainEntry, linkedEntry) {
                Util.remove('rest/parts/' + mainEntry.id + '/links/' + linkedEntry.id, {}, function () {
                    let idx = mainEntry.linkedParts.indexOf(linkedEntry);
                    if (idx !== -1) {
                        mainEntry.linkedParts.splice(idx, 1);
                    }
                });
            };

// removes a value from an autoCompleteAdd field at the specified index
            $scope.removeAutoCompleteAdd = function (index) {
                $scope.convertedAutoCompleteAdd.splice(index, 1);
            };

// add a new autoComplete add value at the specified index
            $scope.addAutoCompleteAdd = function (index) {
                $scope.convertedAutoCompleteAdd.splice(index + 1, 0, {value: ""});
            };

// file upload
            let uploader = $scope.sequenceFileUpload = new FileUploader({
                scope: $scope, // to automatically update the html. Default: $rootScope
                url: "rest/file/sequence",
                method: 'POST',
                removeAfterUpload: true,
                headers: {"X-ICE-Authentication-SessionId": Authentication.getSessionId()},
                autoUpload: true,
                queueLimit: 1 // can only upload 1 file
            });

            uploader.onProgressItem = function (item, progress) {
                $scope.serverError = undefined;

                if (progress !== "100")  // isUploading is always true until it returns
                    return;

                // upload complete. have processing
                $scope.processingFile = item.file.name;
            };

            uploader.onSuccessItem = function (item, response, status, header) {
                if (!response)
                    return;

                if (response.sequence) {
                    $scope.entry.basePairCount = response.sequence.sequence.length;
                }

                if (response.format && response.format.indexOf("SBOL") > -1) {
                    Util.list("rest/parts/" + $scope.entry.id + "/links", function (result) {
                        if (!result)
                            return;
                        $scope.entry.linkedParts = result;
                    });
                }

                $scope.entry.hasSequence = true;
            };

            uploader.onCompleteAll = function () {
                $scope.processingFile = undefined;
            };

            uploader.onBeforeUploadItem = function (item) {
                item.formData.push({entryType: $scope.entry.type});
                item.formData.push({entryRecordId: $scope.entry.recordId});
            };

            uploader.onErrorItem = function (item, response, status, headers) {
                $scope.serverError = response.message;
            };

// customer parameter add for entry view
            $scope.addNewCustomField = function () {
                $scope.newParameter.nameInvalid = $scope.newParameter.name == undefined || $scope.newParameter.name == '';
                $scope.newParameter.valueInvalid = $scope.newParameter.value == undefined || $scope.newParameter.value == '';
                if ($scope.newParameter.nameInvalid || $scope.newParameter.valueInvalid)
                    return;

                $scope.newParameter.partId = $scope.entry.id;
                Util.post("rest/custom-fields", $scope.newParameter, function (result) {
                    if (!result)
                        return;

                    $scope.entry.parameters.push(result);
                    $scope.newParameter.edit = false;
                })
            };

            $scope.showAutoAnnotationPopup = function () {
                let modalInstance = $uibModal.open({
                    templateUrl: 'scripts/entry/sequence/modal-auto-annotate-sequence.html',
                    controller: function ($scope, $uibModalInstance, part, Util) {
                        $scope.selectedFeatures = [];
                        $scope.allSelected = false;
                        $scope.part = part;
                        $scope.pagingParams = {
                            currentPage: 0,
                            pageSize: 8,
                            sort: "locations[0].genbankStart",
                            asc: true
                        };
                        let displayOptions = [{display: "All features", key: "all"}, {
                            display: "My features",
                            key: "mine"
                        }];
                        $scope.options = {values: displayOptions, selection: displayOptions[0]};

                        // retrieves "suggested" annotations for current entry
                        $scope.fetchAnnotations = function () {
                            $scope.annotations = undefined;
                            Util.get("rest/parts/" + part.id + "/annotations/auto", function (result) {
                                angular.forEach(result.features, function (feature) {
                                        //console.log(feature);
                                        //if (feature.strand == 1)
                                        feature.length = (feature.locations[0].end - feature.locations[0].genbankStart) + 1;
                                        //else
                                        //feature.length = (feature.locations[0].genbankStart - feature.locations[0].end) + 1;
                                    }
                                );
                                $scope.annotations = result;
                                $scope.pagingParams.resultCount = result.features.length;
                                $scope.pagingParams.numberOfPages = Math.ceil(result.features.length / $scope.pagingParams.pageSize);
                            }, {ownerFeatures: $scope.options.selection.key == "mine"});
                        };
                        $scope.fetchAnnotations();

                        /**
                         * Support for sorting
                         * @param field field to sort on
                         */
                        $scope.sort = function (field) {
                            if ($scope.pagingParams.sort == field) {
                                $scope.pagingParams.asc = !$scope.pagingParams.asc;
                            } else {
                                $scope.pagingParams.sort = field;
                                $scope.pagingParams.asc = true;
                            }
                            $scope.pagingParams.currentPage = 0;
                        };

                        /**
                         * Select all features on the UI
                         */
                        $scope.selectAll = function () {
                            $scope.allSelected = !$scope.allSelected;
                            if ($scope.allSelected) {
                                $scope.selectedFeatures = $scope.annotations.features;
                            } else {
                                $scope.selectedFeatures = [];
                            }
                        };

                        /**
                         * Check or un-check (on UI) specific feature
                         * @param feature
                         */
                        $scope.checkFeature = function (feature) {
                            feature.selected = !feature.selected;
                            let i = $scope.selectedFeatures.indexOf(feature);
                            if (i == -1) {
                                $scope.selectedFeatures.push(feature);
                            } else {
                                $scope.selectedFeatures.splice(i, 1);
                            }

                            $scope.allSelected = ($scope.selectedFeatures.length == $scope.annotations.features.length);
                        };

                        $scope.setClassName = function (feature) {
                            let classPrefix = feature.strand == -1 ? "reverse-strand-" : "forward-strand-";
                            feature.className = classPrefix + feature.type.toLowerCase();
                        };

                        /**
                         *  Determine background color based on feature type
                         * @param feature
                         * @returns {{background-color: string}}
                         */
                        $scope.getBgStyle = function (feature) {
                            let bgColor = "#CCC";

                            switch (feature.type.toLowerCase()) {
                                case 'cds':
                                    bgColor = "#EF6500";
                                    break;

                                case "misc_feature":
                                    bgColor = "#006FEF";
                                    break;

                                case "promoter":
                                    bgColor = "#31B440";
                                    break;

                                case "terminator":
                                    bgColor = "red";
                                    break;

                                case "rep_origin":
                                    bgColor = "#878787";
                                    break;

                                case "misc_marker":
                                    bgColor = "#8DCEB1";
                                    break;
                            }
                            return {'background-color': bgColor};
                        };

                        $scope.getFirstStyle = function (selectedFeature) {
                            let width = (selectedFeature.locations[0].genbankStart / $scope.annotations.length) * 100;
                            return {"width": (Math.floor(width)) + '%'};
                        };

                        $scope.getSecondStyle = function (selectedFeature) {
                            let width = ((selectedFeature.locations[0].end - selectedFeature.locations[0].genbankStart) / $scope.annotations.length) * 100;
                            let style = $scope.getBgStyle(selectedFeature);
                            style.width = (Math.ceil(width)) + '%';
                            return style;
                        };

                        $scope.getThirdStyle = function (selectedFeature) {
                            let w = (($scope.annotations.length - selectedFeature.locations[0].end) / $scope.annotations.length) * 100;
                            return {"width": (Math.floor(w)) + '%'};
                        };

                        $scope.saveAnnotations = function () {
                            $scope.errorSavingAnnotations = false;
                            $scope.savingAnnotations = true;

                            //url, obj, successHandler, params, errHandler
                            Util.update("rest/parts/" + part.id + "/sequence", {features: $scope.selectedFeatures}, {add: true}, function () {
                                $uibModalInstance.close(true);
                            }, function (error) {
                                $scope.savingAnnotations = false;
                                $scope.errorSavingAnnotations = true;
                            })
                        };

                        // used to show, in table of features, the selected feature
                        $scope.showAnnotationInTable = function (selectedFeature) {
                            let index = $scope.annotations.features.indexOf(selectedFeature);
                            $scope.pagingParams.currentPage = parseInt(index / $scope.pagingParams.pageSize);
                        }
                    },
                    size: 'lg',
                    resolve: {
                        part: function () {
                            return $scope.entry;
                        }
                    }
                    ,
                    backdrop: "static"
                });

                modalInstance.result.then(function (reload) {
                    if (reload) {
                        $window.location.reload();
                    }
                });
            };
        }
    )
;
