'use strict';

angular.module('ice.entry.service', [])
    .factory('Selection', function ($rootScope, $cookieStore) {
        var selectedEntries = {};
        var selectedSearchResultsCount = 0;
        var selectedSearchNotificationSent = false;  // send notification when at least one is selected and then none
        var canEdit = false;
        var allSelection = {};
        var canDelete = false;
        var selectedTypes = {};
        var searchQuery = undefined;
        var userId = $cookieStore.get('userId');

        return {
            selectEntry: function (entry) {
                if (this.isSelected(entry)) {
                    return;
                }

                // todo : this may be a problem when a user selects 3 entries (can edit 2) and deselects the 3rd
                // todo : that use cannot edit

                canEdit = entry.canEdit;
                canDelete = entry.ownerEmail === userId;

                if (selectedEntries[entry.id]) {
                    // remove entry id
                    selectedEntries[entry.id] = undefined;
                    selectedSearchResultsCount -= 1;

                    // remove type
                    if (selectedTypes[entry.type] && selectedTypes[entry.type].length) {
                        var idx = selectedTypes[entry.type].indexOf(entry.id);
                        selectedTypes[entry.type].splice(idx, 1);
                        if (selectedTypes[entry.type].length === 0)
                            delete selectedTypes[entry.type];
                    }
                } else {

                    // add entry id
                    selectedEntries[entry.id] = entry;
                    selectedSearchResultsCount += 1;

                    // add type
                    if (!selectedTypes[entry.type])
                        selectedTypes[entry.type] = [];
                    selectedTypes[entry.type].push(entry.id);
                }

                // determine when to send a notification
                if (selectedSearchResultsCount == 0) {
                    if (selectedSearchNotificationSent) {
                        // notify again that count is not 0
                        $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
                        selectedSearchNotificationSent = false;
                    } else {
                        // no need
                    }
                } else {
                    // if count > 0
                    if (selectedSearchNotificationSent) {
                        // no need. count is at least one
                    } else {
                        $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
                        selectedSearchNotificationSent = true;
                    }
                }
            },

            hasSelection: function () {
                return (allSelection.type && allSelection.type != 'NONE') || selectedSearchResultsCount > 0;
            },

            setTypeSelection: function (type) {
                if (!type) {
                    this.reset();
                    return;
                }

                // selects a specific type of entry from the list e.g. all plasmids
                allSelection.type = type.toUpperCase();
                if (allSelection.type != 'NONE')
                    $rootScope.$emit("EntrySelected", 1);
                else
                    this.reset();
            },

            searchEntrySelected: function (entry) {
                return selectedEntries[entry.id] != undefined;
            },

            getSelectedEntries: function () {
                var selected = [];
                for (var k in selectedEntries) {
                    if (selectedEntries.hasOwnProperty(k) && selectedEntries[k]) {
                        selected.push({id: k, visible: selectedEntries[k].visible});
                    }
                }
                return selected;
            },

            getSelectedTypes: function () {
                return selectedTypes;
            },

            getSelection: function () {
                return allSelection;
            },

            canEdit: function () {
                var count = 0;
                // selectedTypes is the type of entries selected
                for (var k in selectedTypes) if (selectedTypes.hasOwnProperty(k)) ++count;
                return !this.allSelected() && canEdit && selectedSearchResultsCount > 0 && count == 1 && !this.canRestore();
            },

            canDelete: function () {
                return !this.allSelected() && (($rootScope.user && $rootScope.user.isAdmin) || canDelete) && selectedSearchResultsCount > 0;
            },

            canRestore: function () {
                if (allSelection.type && allSelection.type == 'ALL')
                    return false;
                return this.hasSelection() && this.getSelectedEntries()[0].visible == 'DELETED';
            },

            isAdmin: function () {
                return $rootScope.user && $rootScope.user.isAdmin;
            },

            // determines if an entry has been selected
            isSelected: function (entry) {
                if (!entry)
                    return false;

                if (this.allSelected())
                    return true;

                return allSelection.type == entry.type.toUpperCase();
            },

            // have all entries been marked for selection?
            allSelected: function () {
                return allSelection.type == 'ALL';
            },

            setSearch: function (query) {
                searchQuery = query;
            },

            getSearch: function () {
                return searchQuery;
            },

            // resets all selected and send notifications
            reset: function () {
                selectedEntries = {};
                selectedTypes = {};
                selectedSearchResultsCount = 0;
                selectedSearchNotificationSent = false;
                allSelection = {};
                canEdit = false;
                canDelete = false;
                searchQuery = undefined;
                $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
            }
        }
    })
    .factory('EntryService', function () {
        var toStringArray = function (objArray) {
            var result = [];
            angular.forEach(objArray, function (object) {
                if (!object || !object.value || object.value === "")
                    return;
                result.push(object.value);
            });
            return result;
        };

        //
        // commons fields to all the different types of parts supported by the system
        // inputType of "withEmail" uses attribute "bothRequired" to indicate that the email portion is required
        //
        var partFields = [
            {label: "Name", required: true, schema: 'name', inputType: 'short'},
            {label: "Alias", schema: 'alias', inputType: 'short'},
            {
                label: "Principal Investigator",
                required: true,
                schema: 'principalInvestigator',
                inputType: 'withEmail',
                bothRequired: false
            },
            {label: "Funding Source", schema: 'fundingSource', inputType: 'short'},
            {
                label: "Status", schema: 'status', options: [
                {value: "Complete", text: "Complete"},
                {value: "In Progress", text: "In Progress"},
                {value: "Abandoned", text: "Abandoned"},
                {value: "Planned", text: "Planned"}
            ]
            },
            {
                label: "Bio Safety Level", schema: 'bioSafetyLevel', options: [
                {value: "1", text: "Level 1"},
                {value: "2", text: "Level 2"}
            ]
            },
            {label: "Creator", required: true, schema: 'creator', inputType: 'withEmail', bothRequired: true},
            {label: "Keywords", schema: 'keywords', inputType: 'medium'},
            {label: "External URL", schema: 'links', inputType: 'add'},
            {label: "Summary", required: true, schema: 'shortDescription', inputType: 'long'},
            {label: "References", schema: 'references', inputType: 'long'},
            {label: "Intellectual Property", schema: 'intellectualProperty', inputType: 'long'}
        ];

        // fields peculiar to plasmids
        var plasmidFields = [
            {label: "Backbone", schema: 'backbone', subSchema: 'plasmidData', inputType: 'medium'},
            {label: "Circular", schema: 'circular', inputType: 'bool', subSchema: 'plasmidData'},
            {
                label: "Origin of Replication", schema: 'originOfReplication', inputType: 'autoComplete',
                autoCompleteField: 'ORIGIN_OF_REPLICATION', subSchema: 'plasmidData'
            },
            {
                label: "Selection Markers", required: true, schema: 'selectionMarkers', inputType: 'autoCompleteAdd',
                autoCompleteField: 'SELECTION_MARKERS'
            },
            {
                label: "Promoters",
                schema: 'promoters',
                subSchema: 'plasmidData',
                inputType: 'autoComplete',
                autoCompleteField: 'PROMOTERS'
            },
            {
                label: "Replicates In",
                schema: 'replicatesIn',
                subSchema: 'plasmidData',
                inputType: 'autoComplete',
                autoCompleteField: 'REPLICATES_IN'
            }
        ];

        // fields peculiar to arabidopsis seeds
        var seedFields = [
            {
                label: "Sent To ABRC",
                schema: 'sentToABRC',
                help: "Help Text",
                inputType: 'bool',
                subSchema: 'arabidopsisSeedData'
            },
            {
                label: "Plant Type", schema: 'plantType', subSchema: 'arabidopsisSeedData', options: [
                {value: "EMS", text: "EMS"},
                {value: "OVER_EXPRESSION", text: "OVER_EXPRESSION"},
                {value: "RNAI", text: "RNAi"},
                {value: "REPORTER", text: "Reporter"},
                {value: "T_DNA", text: "T-DNA"},
                {value: "OTHER", text: "Other"}
            ]
            },
            {
                label: "Generation", schema: 'generation', subSchema: 'arabidopsisSeedData', options: [
                {value: "UNKNOWN", text: "UNKNOWN"},
                {value: "F1", text: "F1"},
                {value: "F2", text: "F2"},
                {value: "F3", text: "F3"},
                {value: "M0", text: "M0"},
                {value: "M1", text: "M1"},
                {value: "M2", text: "M2"},
                {value: "T0", text: "T0"},
                {value: "T1", text: "T1"},
                {value: "T2", text: "T2"},
                {value: "T3", text: "T3"},
                {value: "T4", text: "T4"},
                {value: "T5", text: "T5"}
            ]
            },
            {label: "Harvest Date", schema: 'harvestDate', subSchema: 'arabidopsisSeedData', inputType: 'date'},
            {label: "Homozygosity", schema: 'homozygosity', subSchema: 'arabidopsisSeedData', inputType: 'medium'},
            {label: "Ecotype", schema: 'ecotype', subSchema: 'arabidopsisSeedData', inputType: 'medium'},
            {
                label: "Selection Markers", required: true, schema: 'selectionMarkers', inputType: 'autoCompleteAdd',
                autoCompleteField: 'SELECTION_MARKERS'
            }
        ];

        // fields peculiar to seeds
        var strainFields = [
            {
                label: "Selection Markers", required: true, schema: 'selectionMarkers',
                inputType: 'autoCompleteAdd', autoCompleteField: 'SELECTION_MARKERS'
            },
            {label: "Genotype/Phenotype", schema: 'genotypePhenotype', inputType: 'long', subSchema: 'strainData'},
            {label: "Host", schema: 'host', inputType: 'short', subSchema: 'strainData'}
        ];

        var generateLinkOptions = function (type) {
            switch (type.toLowerCase()) {
                case 'plasmid':
                    return [
                        {type: 'part', display: 'Part'},
                        {type: 'plasmid', display: 'Plasmid'}
                    ];

                case 'part':
                    return [
                        {type: 'part', display: 'Part'}
                    ];

                case 'strain':
                    return [
                        {type: 'part', display: 'Part'},
                        {type: 'plasmid', display: 'Plasmid'},
                        {type: 'strain', display: 'Strain'}
                    ];

                case 'arabidopsis':
                    return [
                        {type: 'part', display: 'Part'},
                        {type: 'arabidopsis', display: 'Arabidopsis Seed'}
                    ];
            }
        };

        var validateFields = function (part, fields) {
            var canSubmit = true;

            // for each field in the part, check if it validates (only fields that are required)
            angular.forEach(fields, function (field) {
                if (!field.required)
                    return;

                if (field.inputType === 'add' || field.inputType === 'autoCompleteAdd') {
                    if (part[field.schema].length == 0) {
                        field.invalid = true;
                    }
                    else {
                        for (var i = 0; i < part[field.schema].length; i += 1) {
                            var fieldValue = part[field.schema][i].value;
                            field.invalid = !fieldValue || fieldValue === '';
                        }
                    }
                } else {
                    if (field.bothRequired) {
                        // check email portion
                        field.withEmailInvalid = (part[field.schema + 'Email'] === undefined || part[field.schema + 'Email'] === '');
                    }
                    field.invalid = (part[field.schema] === undefined || part[field.schema] === '');
                }

                if (canSubmit) {
                    canSubmit = !field.invalid;
                }
            });

            if (!canSubmit) {
                part.fields = fields;
            }
            return canSubmit;
        };

        var getFieldsForType = function (type) {
            var fields = angular.copy(partFields);
            type = type.toLowerCase();
            switch (type) {
                case 'strain':
                    fields.splice.apply(fields, [7, 0].concat(strainFields));
                    return fields;

                case 'arabidopsis':
                    fields.splice.apply(fields, [7, 0].concat(seedFields));
                    return fields;

                case 'plasmid':
                    fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                    return fields;

                case 'part':
                default:
                    return fields;
            }
        };

        return {
            toStringArray: function (obj) {
                return toStringArray(obj);
            },

            linkOptions: function (type) {
                return generateLinkOptions(type);
            },

            getFieldsForType: function (type) {
                return getFieldsForType(type);
            },

            // converts to a form that the backend can work with
            getTypeData: function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);
                angular.forEach(fields, function (field) {
                    if (field.subSchema) {
                        if (entry[field.subSchema] === undefined)
                            entry[field.subSchema] = {};
                        entry[field.subSchema][field.schema] = entry[field.schema];
                    }
                });

                return entry;
            },

            // inverse of the above. converts to form ui can work with
            convertToUIForm: function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);

                angular.forEach(fields, function (field) {
                    if (field.subSchema && entry[field.subSchema]) {
                        entry[field.schema] = entry[field.subSchema][field.schema];
                    }
                });

                return entry;
            },

            validateFields: function (part, fields) {
                return validateFields(part, fields);
            },

            // converts autocomplete fields from an array string to an array of objects in order to be
            // able to use ng-model on the ui
            // also converts entry to form that UI can work with
            setNewEntryFields: function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);

                angular.forEach(fields, function (field) {
                    if (field.inputType === 'autoCompleteAdd' || field.inputType === 'add') {
                        entry[field.schema] = [
                            {value: ''}
                        ];
                        return;
                    }

                    if (field.subSchema && entry[field.subSchema]) {
                        entry[field.schema] = entry[field.subSchema][field.schema];
                        return;
                    }

                    if (!entry.hasOwnProperty([field.schema]))
                        entry[field.schema] = undefined;
                });

                // new entry field defaults
                entry.bioSafetyLevel = '1';
                entry.status = 'Complete';
                entry.parameters = [];
                return entry;
            },

            getMenuSubDetails: function () {
                return [
                    {
                        url: 'scripts/entry/general-information.html',
                        display: 'General Information',
                        isPrivileged: false,
                        icon: 'fa-exclamation-circle'
                    },
                    {
                        id: 'sequences',
                        url: 'scripts/entry/sequence-analysis.html',
                        display: 'Sequence Analysis',
                        isPrivileged: false,
                        countName: 'sequenceCount',
                        icon: 'fa-search-plus'
                    },
                    {
                        id: 'comments',
                        url: 'scripts/entry/comments.html',
                        display: 'Comments',
                        isPrivileged: false,
                        countName: 'commentCount',
                        icon: 'fa-comments-o'
                    },
                    {
                        id: 'samples',
                        url: 'scripts/entry/samples.html',
                        display: 'Samples',
                        isPrivileged: false,
                        countName: 'sampleCount',
                        icon: 'fa-flask'
                    },
                    {
                        id: 'history',
                        url: 'scripts/entry/history.html',
                        display: 'History',
                        isPrivileged: true,
                        countName: 'historyCount',
                        icon: 'fa-history'
                    },
                    {
                        id: 'experiments',
                        url: 'scripts/entry/experiments.html',
                        display: 'Experimental Data',
                        isPrivileged: false,
                        countName: 'experimentalDataCount',
                        icon: 'fa-database'
                    }
                ];
            }
        }
    })
;
