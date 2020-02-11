'use strict';

angular.module('ice.entry.service', [])
    .factory('Selection', function ($rootScope, Authentication) {
        let selectedEntries = {};
        let selectedSearchResultsCount = 0;
        let selectedSearchNotificationSent = false;  // send notification when at least one is selected and then none
        let allSelection = {};
        let canDelete = false;
        let selectedTypes = {};
        let searchQuery = undefined;

        return {
            selectEntry: function (entry) {
                if (this.isSelected(entry)) {
                    return;
                }

                canDelete = entry.ownerEmail === Authentication.getUserId();

                if (selectedEntries[entry.id]) {
                    // remove entry id
                    selectedEntries[entry.id] = undefined;
                    selectedSearchResultsCount -= 1;

                    // remove type
                    if (selectedTypes[entry.type] && selectedTypes[entry.type].length) {
                        let idx = selectedTypes[entry.type].indexOf(entry.id);
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
                if (selectedSearchResultsCount === 0) {
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
                return (allSelection.type && allSelection.type !== 'NONE') || selectedSearchResultsCount > 0;
            },

            setTypeSelection: function (type) {
                if (!type) {
                    this.reset();
                    return;
                }

                // selects a specific type of entry from the list e.g. all plasmids
                allSelection.type = type.toUpperCase();
                if (allSelection.type !== 'NONE')
                    $rootScope.$emit("EntrySelected", 1);
                else
                    this.reset();
            },

            searchEntrySelected: function (entry) {
                return selectedEntries[entry.id] !== undefined;
            },

            getSelectedEntries: function () {
                let selected = [];
                for (let k in selectedEntries) {
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
                let count = 0;
                // selectedTypes is the type of entries selected
                for (let k in selectedTypes) {
                    if (selectedTypes.hasOwnProperty(k))
                        ++count;
                }

                return !this.allSelected() && selectedSearchResultsCount && count && !this.canRestore();
            },

            canDelete: function () {
                return !this.allSelected() && (($rootScope.user && $rootScope.user.isAdmin) || canDelete) && selectedSearchResultsCount > 0;
            },

            canRestore: function () {
                if (allSelection.type && allSelection.type === 'ALL')
                    return false;
                return this.hasSelection() && this.getSelectedEntries()[0].visible === 'DELETED';
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

                return allSelection.type === entry.type.toUpperCase();
            },

            // have all entries been marked for selection?
            allSelected: function () {
                return allSelection.type === 'ALL';
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
                canDelete = false;
                searchQuery = undefined;
                $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
            }
        }
    })
    .factory('EntryService', function () {
        let toStringArray = function (objArray) {
            let result = [];
            if (objArray && objArray.length) {
                objArray.forEach(function (object) {
                    if (!object || !object.value || object.value === "")
                        return;

                    result.push(object.value);
                });
            }
            return result;
        };

        //
        // commons fields to all the different types of parts supported by the system
        // inputType of "withEmail" uses attribute "bothRequired" to indicate that the email portion is required
        //
        const partFields = [
            {label: "Name", required: true, schema: 'name', inputType: 'short'},
            {label: "Alias", schema: 'alias', inputType: 'short'},
            {
                label: "Principal Investigator",
                required: true,
                schema: 'principalInvestigator',
                inputType: 'withEmail',
                bothRequired: false
            },
            {label: "Funding Source", schema: 'fundingSource', inputType: 'medium'},
            {
                label: "Status", schema: 'status', required: true, options: [
                    {value: "Complete", text: "Complete"},
                    {value: "In Progress", text: "In Progress"},
                    {value: "Abandoned", text: "Abandoned"},
                    {value: "Planned", text: "Planned"}
                ]
            },
            {
                label: "BioSafety Level", schema: 'bioSafetyLevel', required: true, options: [
                    {value: "1", text: "Level 1"},
                    {value: "2", text: "Level 2"},
                    {value: "-1", text: "Restricted"}
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
        const plasmidFields = [
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
        const seedFields = [
            {
                label: "Sent To ABRC",
                schema: 'sentToABRC',
                help: "Help Text",
                inputType: 'bool',
                subSchema: 'seedData'
            },
            {
                label: "Plant Type", schema: 'plantType', subSchema: 'seedData', options: [
                    {value: "EMS", text: "EMS"},
                    {value: "OVER_EXPRESSION", text: "OVER_EXPRESSION"},
                    {value: "RNAI", text: "RNAi"},
                    {value: "REPORTER", text: "Reporter"},
                    {value: "T_DNA", text: "T-DNA"},
                    {value: "OTHER", text: "Other"}
                ]
            },
            {
                label: "Generation", schema: 'generation', subSchema: 'seedData', options: [
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
            {label: "Harvest Date", schema: 'harvestDate', subSchema: 'seedData', inputType: 'date'},
            {label: "Homozygosity", schema: 'homozygosity', subSchema: 'seedData', inputType: 'medium'},
            {label: "Ecotype", schema: 'ecotype', subSchema: 'seedData', inputType: 'medium'},
            {
                label: "Selection Markers", required: true, schema: 'selectionMarkers', inputType: 'autoCompleteAdd',
                autoCompleteField: 'SELECTION_MARKERS'
            }
        ];

        // fields peculiar to strains
        const strainFields = [
            {
                label: "Selection Markers", required: true, schema: 'selectionMarkers',
                inputType: 'autoCompleteAdd', autoCompleteField: 'SELECTION_MARKERS'
            },
            {label: "Genotype/Phenotype", schema: 'genotypePhenotype', inputType: 'long', subSchema: 'strainData'},
            {label: "Host", schema: 'host', inputType: 'short', subSchema: 'strainData'}
        ];

        // fields peculiar to proteins
        const proteinFields = [
            {label: "Organism", schema: 'organism', inputType: 'medium', subSchema: 'proteinData'},
            {label: "Full Name", schema: 'fullName', inputType: 'medium', subSchema: 'proteinData'},
            {label: "Gene Name", schema: 'geneName', inputType: 'medium', subSchema: 'proteinData'},
            {label: "Uploaded From", schema: 'uploadedFrom', inputType: 'medium', subSchema: 'proteinData'},
        ];

        const generateLinkOptions = function (type) {
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
                        {type: 'arabidopsis', display: 'Seed'}
                    ];

                case 'protein':
                    return [
                        {type: 'part', display: 'Part'},
                        {type: 'protein', display: 'Protein'}
                    ];
            }
        };

        const validateFields = function (part, fields) {
            let canSubmit = true;

            // for each field in the part, check if it validates (only fields that are required)
            fields.forEach(function (field) {
                if (!field.required)
                    return;

                if (field.inputType === 'add' || field.inputType === 'autoCompleteAdd') {
                    if (part[field.schema].length === 0) {
                        field.invalid = true;
                    } else {
                        for (let i = 0; i < part[field.schema].length; i += 1) {
                            let fieldValue = part[field.schema][i].value;
                            field.invalid = !fieldValue || fieldValue === '';
                        }
                    }
                } else {
                    // validate custom field
                    if (field.isCustom) { // or field.schema is undefined
                        // transmitted via part.customFields
                        field.invalid = (part[field.label] === undefined || part[field.label].trim === '');
                        if (!field.invalid) {
                            if (part[field.label] === 'Other' && !part[field.label + '_plus']) {
                                field.invalid = true;
                            }
                        }

                        // console.log(part[field.label], part[field.label + '_plus']);
                        // part.customFields.forEach(function (customField) {
                        //     if (field.label !== customField.label)
                        //         return;
                        //
                        //     field.invalid = (customField.value === undefined);
                        // });

                    } else {
                        if (field.bothRequired) {
                            // check email portion
                            field.withEmailInvalid = (part[field.schema + 'Email'] === undefined || part[field.schema + 'Email'] === '');
                        }

                        // validate regular fields
                        field.invalid = (part[field.schema] === undefined || part[field.schema] === '');
                    }
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

        const getFieldsForType = function (type) {
            let fields = angular.copy(partFields);
            type = type.toLowerCase();

            switch (type.toLowerCase()) {
                case 'strain':
                    fields.splice.apply(fields, [7, 0].concat(strainFields));
                    break;

                case 'arabidopsis':
                case 'seed':
                    fields.splice.apply(fields, [7, 0].concat(seedFields));
                    break;

                case 'plasmid':
                    fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                    break;

                case 'protein':
                    fields.splice.apply(fields, [7, 0].concat(proteinFields));
                    break;
            }

            return fields;
        };

        const sortEntryFields = function (fields) {

            // ghetto sorting
            const sortFields = [
                "short",
                "withEmail",
                "date",
                "bool",
                "options",
                "autoComplete",
                "medium",
                "autoCompleteAdd",
                "add",
                "long",
            ];

            const sortMap = {};
            sortFields.forEach(function (value) {
                sortMap[value] = [];
            });

            for (let i = 0; i < fields.length; i += 1) {
                if (!fields[i].inputType)
                    fields[i].inputType = "options";
                const type = fields[i].inputType;
                sortMap[type].push(fields[i]);
            }

            const sortedFields = [];
            sortFields.forEach(function (e) {
                sortMap[e].forEach(function (el) {
                    sortedFields.push(el);
                })
            });

            // sort
            return sortedFields;
        };

        // add custom fields to set of regular fields and sorts the fields
        const addEntryCustomFields = function (entry, fields) {
            entry.customFields.forEach(function (custom) {
                if (!custom.label)
                    return;
                //
                // entry[custom.label] = custom.value;
                // fields.push({schema: custom.label, label: custom.label});
                const customField = {
                    label: custom.label,
                    required: custom.required,
                    isCustom: true,
                    value: custom.value
                };

                switch (custom.fieldType) {
                    case "MULTI_CHOICE":
                    case "MULTI_CHOICE_PLUS":
                        customField.options = [];
                        customField.inputType = "options";
                        custom.options.forEach(function (each) {
                            customField.options.push({value: each.name, text: each.name})
                        });

                        if (custom.fieldType === "MULTI_CHOICE_PLUS")
                            customField.options.push({value: "Other", text: "Other"});

                        fields.push(customField);
                        break;

                    case "EXISTING":
                        if (!custom.options || !custom.options.length)
                            return;

                        // schema is contained in options. assuming only one
                        // todo : it is set as {name: 'schema', value: schema_value}
                        const schema = custom.options[0].name;
                        for (let i = 0; i < fields.length; i += 1) {
                            if (fields[i].schema === schema) {
                                fields[i].required = custom.required;
                                fields[i].label = custom.label;
                                break;
                            }
                        }
                }
            });
            entry.fields = sortEntryFields(fields);
            return entry;
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
                // let type = entry.type.toLowerCase();
                // let fields = getFieldsForType(type);

                entry.fields.forEach(function (field) {
                    if (field.subSchema) {
                        if (entry[field.subSchema] === undefined)
                            entry[field.subSchema] = {};
                        entry[field.subSchema][field.schema] = entry[field.schema];
                    }

                    if (field.isCustom) {
                        entry.customFields.forEach(function (custom) {
                            if (custom.fieldType === "EXISTING") {
                                const idx = entry.customFields.indexOf(custom);
                                entry.customFields.splice(idx, 1);
                                return;
                            }

                            custom.value = entry[custom.label];
                            if (custom.fieldType === 'MULTI_CHOICE_PLUS' && custom.value === 'Other') {
                                custom.value = entry[custom.label + '_plus'];
                                delete entry[custom.label + '_plus'];
                            } else {
                                delete entry[custom.label];
                            }
                        })
                    }
                });

                // check biosafety
                if (entry.bioSafetyLevel === "Level 2")
                    entry.bioSafetyLevel = 2;
                else if (entry.bioSafetyLevel === "Restricted")
                    entry.bioSafetyLevel = -1;
                else
                    entry.bioSafetyLevel = 1;

                return entry;
            },

            // inverse of the above. converts to form ui can work with
            convertToUIForm: function (entry) {
                let type = entry.type.toLowerCase();

                // note that this is for display; when in edit mode this will not work for options bSL field
                if (entry.bioSafetyLevel === 2)
                    entry.bioSafetyLevel = "Level 2";
                else if (entry.bioSafetyLevel === -1)
                    entry.bioSafetyLevel = "Restricted";
                else
                    entry.bioSafetyLevel = "Level 1";

                let fields = getFieldsForType(type);

                fields.forEach(function (field) {
                    if (field.subSchema && entry[field.subSchema]) {
                        entry[field.schema] = entry[field.subSchema][field.schema];
                    }
                });

                if (!entry.customFields || !entry.customFields.length) {
                    entry.fields = sortEntryFields(fields);
                    return entry;
                }

                return addEntryCustomFields(entry, fields);
            },

            validateFields: function (part, fields) {
                return validateFields(part, fields);
            },

            // converts autocomplete fields from an array string to an array of objects in order to be
            // able to use ng-model on the ui
            // also converts entry to form that UI can work with
            // also sets "fields" parameter
            setNewEntryFields: function (entry) {
                let type = entry.type.toLowerCase();
                let fields = getFieldsForType(type);

                fields.forEach(function (field) {
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

                    if (entry.hasOwnProperty(field.schema))
                        return;

                    entry[field.schema] = undefined;
                });

                // new entry field defaults
                entry.bioSafetyLevel = '1';
                entry.status = 'Complete';
                entry.parameters = [];

                // deal with custom fields
                if (!entry.customFields || !entry.customFields.length) {
                    entry.fields = sortEntryFields(fields);
                    return entry;
                }

                return addEntryCustomFields(entry, fields);
            },

            // retrieves the submenu options for entry (if param set to true then it is for a remote entry)
            getMenuSubDetails: function (forRemoteEntry) {
                const details = [
                    {
                        url: 'scripts/entry/general-information.html',
                        display: 'General Information',
                        isPrivileged: false,
                        icon: 'fa-exclamation-circle'
                    },
                    {
                        id: 'sequences',
                        url: 'scripts/entry/traces/sequence-analysis.html',
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
                    }];

                if (!forRemoteEntry) {
                    details.push({
                        id: 'samples',
                        url: 'scripts/entry/samples.html',
                        display: 'Samples',
                        isPrivileged: false,
                        countName: 'sampleCount',
                        icon: 'fa-flask'
                    });

                    details.push({
                        id: 'history',
                        url: 'scripts/entry/history.html',
                        display: 'History',
                        isPrivileged: true,
                        countName: 'historyCount',
                        icon: 'fa-history'
                    });

                    details.push({
                        id: 'experiments',
                        url: 'scripts/entry/experiments.html',
                        display: 'Experimental Data',
                        isPrivileged: false,
                        countName: 'experimentalDataCount',
                        icon: 'fa-database'
                    });
                }

                return details;
            },

            getEntryItems: function () {
                return [
                    {name: "Plasmid", type: "plasmid"},
                    {name: "Strain", type: "strain"},
                    {name: "Part", type: "part"},
                    {name: "Seed", type: "seed"},
                    {name: "Protein", type: "protein"}
                ]
            }
        }
    })
;
