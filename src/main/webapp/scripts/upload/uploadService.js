'use strict';

angular.module('ice.upload.service', [])
    .factory('UploadUtil', function () {

            return {
                // converts the index (which depends on type) of the schema to the specific rest resource name
                indexToRestResource: function (index) {
                    switch (index) {
                        case 0:
                            return "trace";

                        case 1:
                            return "sequence";

                        default:
                        case 2:
                            return "attachment";
                    }
                },

                setDataValue: function (type, index, object, value, partTypeDefault) {
                    const field = partTypeDefault.fields[index];
                    if (!field)
                        return;

                    if (field.isCustom) {
                        if (!object.customFields)
                            object.customFields = [];

                        field.value = value;
                        field.entryType = type;
                        object.customFields.push(field);
                    } else {
                        if (field.subSchema) {
                            object[field.subSchema][field.schema] = value;
                        } else {
                            // todo : find a way to avoid this explicit callout to specific fields
                            if (field.schema === "bioSafetyLevel") {
                                if (object.bioSafetyLevel === "Level 2")
                                    object.bioSafetyLevel = 2;
                                else if (object.bioSafetyLevel === "Restricted")
                                    object.bioSafetyLevel = "-1";
                                else
                                    object.bioSafetyLevel = 1;
                            }

                            if (field.schema === "selectionMarkers") {
                                // todo : allow comma separation
                                object[field.schema] = [value];
                            } else {
                                object[field.schema] = value;
                            }
                        }
                    }
                },

                generateLinkOptions: function (type) {
                    switch (type) {
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

                        case 'seed':
                            return [
                                {type: 'part', display: 'Part'},
                                {type: 'seed', display: 'Seed'}
                            ];

                        case 'protein':
                            return [
                                {type: 'part', display: 'Part'},
                                {type: 'protein', display: 'Protein'}
                            ];
                    }
                },

                isFileColumn: function (partTypeDefault, linkedPartTypeDefault, col) {
                    const FILE_FIELDS_COUNT = 3;
                    let fields;
                    if (col < partTypeDefault.fields.length + FILE_FIELDS_COUNT) {
                        fields = partTypeDefault.fields;
                    } else {
                        fields = linkedPartTypeDefault.fields;
                        col = col - (partTypeDefault.fields.length + FILE_FIELDS_COUNT);
                    }
                    return (col >= fields.length && col < fields.length + 3);
                },

                // type can also be "EXISTING"
                createPartObject: function (id, type) {
                    let object = {id: id, type: type.toUpperCase()};

                    switch (type.toLowerCase()) {
                        case "strain":
                            object.strainData = {};
                            break;

                        case "plasmid":
                            object.plasmidData = {};
                            break;

                        case "seed":
                            object.arabidopsisSeedData = {};
                            break;

                        case "protein":
                            object.proteinData = {};
                            break;
                    }

                    return object;
                },


                getPartValue: function (field, entry) {
                    if (field.isCustom)
                        return field.value;

                    if (field.subSchema)
                        return entry[field.subSchema][field.schema];

                    return entry[field.schema];
                },

                getCellProperties: function (field, autoComplete, uploadedFiles) {
                    const cellProperties = {};

                    if (!field)
                        return cellProperties;

                    switch (field.inputType) {
                        case 'bool':
                            cellProperties.type = 'checkbox';
                            break;

                        case 'options':
                            cellProperties.type = 'autocomplete';
                            cellProperties.source = [''];
                            for (let i = 0; i < field.options.length; i += 1) {
                                cellProperties.source.push(field.options[i].text);
                            }
                            cellProperties.allowInvalid = false;
                            cellProperties.validator = function (value, callback) {
                                callback(cellProperties.source.indexOf(value) !== -1);
                            };
                            break;

                        case "autoComplete":
                        case "autoCompleteAdd":
                            cellProperties.type = 'autocomplete';
                            cellProperties.strict = false;
                            cellProperties.source = function (query, process) {
                                autoComplete(field.autoCompleteField, query, process);
                            };
                            break;

                        case "date":
                            cellProperties.type = "date";
                            cellProperties.dateFormat = "MM/DD/YYYY";
                            cellProperties.correctFormat = true;
                            break;

                        case "file":
                            cellProperties.type = 'autocomplete';
                            cellProperties.strict = true;
                            cellProperties.copyable = false; // file cells cannot be copied
                            cellProperties.source = function (query, process) {
                                if (uploadedFiles.arr.length > 1)
                                    return process(uploadedFiles.arr);
                                else
                                    alert("No files available. Drag and drop files to be able to select them")
                            };
                            cellProperties.validator = function (value, callback) {
                                if (!value || value.trim() === "")
                                    callback(true);
                                else
                                    callback(uploadedFiles.map.get(value) !== undefined);
                            };
                            break;
                    }

                    return cellProperties;
                },

                getHeaderForIndex: function (fields, index) {
                    let field = fields[index];
                    if (!field) {
                        index = index - fields.length;
                        // files
                        switch (index) {
                            case 0:
                                return "Sequence Trace File";

                            case 1:
                                return "Sequence File";

                            case 2:
                                return "Attachment File";

                            default:
                                return undefined;
                        }
                    } else {
                        let sheetHeaderString = field.label;
                        if (field.required)
                            sheetHeaderString += "<span class='required'>*</span>";
                        return sheetHeaderString;
                    }
                },

                getColumnWidth: function (fields, index) {
                    let field = fields[index];
                    if (!field) {
                        index = index - fields.length;
                        if (index < 3) {
                            switch (index) {
                                case 0:
                                    return 170;

                                case 1:
                                    return 160;

                                case 2:
                                    return 150;
                            }
                        }
                    } else {
                        switch (field.inputType) {
                            case 'bool':
                                return 80;

                            case 'long':
                                return 200;

                            default:
                                return 150;
                        }
                    }
                },

                cleanBSL: function (part) {
                    if (!part.bioSafetyLevel)
                        return part;

                    switch (part.bioSafetyLevel) {

                        case "Level 1":
                            part.bioSafetyLevel = 1;
                            break;

                        case "Level 2":
                            part.bioSafetyLevel = 2;
                            break;

                        case "Restricted":
                            part.bioSafetyLevel = -1;
                            break;
                    }

                    return part;
                },

                // checks if, based on index, columning being edited is a main entry column
                // isMainEntryCol: function (index, mainFields) {
                //     if (index < mainFields.length)
                //         return true;
                //
                //     return false;
                // }
            }
        }
    );
