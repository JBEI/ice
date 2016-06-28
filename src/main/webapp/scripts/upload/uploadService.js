'use strict';

angular.module('ice.upload.service', [])
    .factory('UploadUtil', function () {

        //
        // headers
        //
        var partHeaders = ["Principal Investigator <span class='required'>*</span>"
            , "PI Email"
            , "Funding Source"
            , "Intellectual Property"
            , "BioSafety Level <span class='required'>*</span>"
            , "Name <span class='required'>*</span>"
            , "Alias"
            , "Keywords"
            , "Summary <span class='required'>*</span>"
            , "Notes"
            , "References"
            , "External URL"
            , "Status <span class='required'>*</span>"
            , "Creator <span class='required'>*</span>"
            , "Creator Email <span class='required'>*</span>"
            // other headers are inserted here
            , "Sequence Trace File(s)"
            , "Sequence File"
            , "Attachment File"];

        var strainHeaders = angular.copy(partHeaders);
        strainHeaders.splice.apply(strainHeaders, [15, 0].concat(["Host", "Genotype or Phenotype",
            "Selection Markers <span class='required'>*</span>"]));

        var plasmidHeaders = angular.copy(partHeaders);
        plasmidHeaders.splice.apply(plasmidHeaders, [15, 0].concat(["Circular", "Backbone", "Promoters", "Replicates In",
            "Origin of Replication", "Selection Markers <span class='required'>*</span>"]));

        var seedHeaders = angular.copy(partHeaders);
        seedHeaders.splice.apply(seedHeaders, [15, 0].concat(["Homozygosity", "Ecotype", "Harvest Date", "Parents",
            "Plant Type", "Generation", "Sent to ABRC?", "Selection Markers <span class='required'>*</span>"]));

        //
        // data schema (should map exactly to headers)
        //
        var dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource',
            'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription',
            'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail',
            // other schema entered here
            'sequenceTrace', 'sequenceFileName', 'attachments'];

        var strainSchema = angular.copy(dataSchema);
        strainSchema.splice.apply(strainSchema, [15, 0].concat('host', 'genotypePhenotype', 'selectionMarkers'));

        var plasmidSchema = angular.copy(dataSchema);
        plasmidSchema.splice.apply(plasmidSchema, [15, 0].concat('circular', 'backbone', 'promoters', 'replicatesIn',
            'originOfReplication', 'selectionMarkers'));

        var seedSchema = angular.copy(dataSchema);
        seedSchema.splice.apply(seedSchema, [15, 0].concat('homozygosity', 'ecotype', 'harvestDate', 'parents',
            'plantType', 'generation', 'sentToAbrc', 'selectionMarkers'));

        return {
            getDataSchema: function (type) {
                switch (type.toLowerCase()) {
                    case "strain":
                        return strainSchema;

                    case "plasmid":
                        return plasmidSchema;

                    case "arabidopsis":
                        return seedSchema;

                    case "part":
                    default:
                        return dataSchema;
                }
            },

            // returns field for the specified type at specified index
            getTypeField: function (type, index) {
                return this.getDataSchema(type)[index];
            },

            // returns array of headers for specified type
            getSheetHeaders: function (type) {
                switch (type.toLowerCase()) {
                    case "strain":
                        return strainHeaders;

                    case "plasmid":
                        return plasmidHeaders;

                    case "part":
                        return partHeaders;

                    case "arabidopsis":
                        return seedHeaders;
                }
            },

            // converts the index (which depends on type) of the schema to the specific rest resource name
            indexToRestResource: function (type, index) {
                var schema = this.getDataSchema(type);
                if (index == schema.indexOf('sequenceFileName'))
                    return 'sequence';

                if (index == schema.indexOf('attachments'))
                    return 'attachment';

                return 'attachment';
            },

            setDataValue: function (type, index, object, value) {
                var dataSchema = this.getDataSchema(type);
                // links is an array
                if (dataSchema[index] == "links") {
                    object[dataSchema[index]] = [value];
                    return object;
                }

                if (index < 15) {
                    object[dataSchema[index]] = value;
                    return object;
                }

                // selection marker is an array
                if (dataSchema[index] == "selectionMarkers") {
                    object[dataSchema[index]] = [value];
                    return object;
                }

                // index is greater than 15 so it is one of the specialized types (strain, plasmid, seed)
                switch (type.toLowerCase()) {
                    case "strain":
                        object.strainData[dataSchema[index]] = value;
                        return object;

                    default:
                        return object;

                    case "plasmid":
                        object.plasmidData[dataSchema[index]] = value;
                        return object;

                    case "arabidopsis":
                        object.arabidopsisSeedData[dataSchema[index]] = value;
                        return object;
                }
            },

            // retrieves the value to be displayed in the spreadsheet from the entry object retrieved from the
            // server side. sort of acts as a mapping to handle the case of "strainData" etc
            // with selection markers being the exception
            getEntryValue: function (type, entry, index) {
                var dataSchema = this.getDataSchema(type);

                if (index < 15 || dataSchema[index] == "selectionMarkers") {
                    var val = entry[dataSchema[index]];
                    if (dataSchema[index] == "bioSafetyLevel" && val == 0)
                        return '';

                    if (dataSchema[index] == "selectionMarkers")
                        return val.toString();

                    return val;
                }

                switch (type.toLowerCase()) {
                    case "strain":
                        // 3 custom fields
                        if (index >= 18)
                            return entry[this.getDataSchema("part")[index - 3]];
                        return entry.strainData[dataSchema[index]];

                    case "plasmid":
                        //console.log(entry);

                        // 6 custom fields
                        if (index >= 21)
                            return entry[this.getDataSchema("part")[index - 6]];
                        return entry.plasmidData[dataSchema[index]];

                    case "arabidopsis":
                        // 7 custom fields
                        if (index >= 22)
                            return entry[this.getDataSchema("part")[index - 7]];
                        return entry.arabidopsisSeedData[dataSchema[index]];

                    case "part":
                        return entry[dataSchema[index]];
                }
                return undefined;
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

                    case 'arabidopsis':
                        return [
                            {type: 'part', display: 'Part'},
                            {type: 'arabidopsis', display: 'Arabidopsis Seed'}
                        ];
                }
            }
        }
    });