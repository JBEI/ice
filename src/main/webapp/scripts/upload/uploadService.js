'use strict';

angular.module('ice.upload.service', [])
    .factory('UploadUtil', function () {

        //
        // headers
        //
        var partHeaders = ["Principal Investigator <span class='required'>*</span>"
            , "PI Email <i class='opacity_hover fa fa-question-circle' title='tooltip' style='margin-left: 20px'></i>"
            , "Funding Source"
            , "Intellectual Property"
            , "BioSafety Level <span class='required'>*</span>"
            , "Name <span class='required'>*</span>"
            , "Alias"
            , "Keywords"
            , "Summary <span class='required'>*</span>"
            , "Notes"
            , "References"
            , "Links"
            , "Status <span class='required'>*</span>"
            , "Creator <span class='required'>*</span>"
            , "Creator Email <span class='required'>*</span>"
            // other headers are inserted here
            , "Sequence Trace File(s)"
            , "Sequence File"
            , "Attachment File"];

        var strainHeaders = angular.copy(partHeaders);
        strainHeaders.splice.apply(strainHeaders, [15, 0].concat(["Parental Strain", "Genotype or Phenotype", "Plasmids",
            "Selection Markers"]));

        var plasmidHeaders = angular.copy(partHeaders);
        plasmidHeaders.splice.apply(plasmidHeaders, [15, 0].concat(["Circular", "Backbone", "Promoters", "Replicates In",
            "Origin of Replication", "Selection Markers"]));

        var seedHeaders = angular.copy(partHeaders);
        seedHeaders.splice.apply(seedHeaders, [15, 0].concat(["Homozygosity", "Ecotype", "Harvest Date", "Parents",
            "Plant Type", "Generation", "Sent to ABRC?"]));

        //
        // data schema (should map exactly to headers)
        //
        var dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource',
            'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription',
            'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail',
            // other schema entered here
            'sequenceTrace', 'sequenceFileName', 'attachments'];

        var strainSchema = angular.copy(dataSchema);
        strainSchema.splice.apply(strainSchema, [15, 0].concat('parentStrain', 'genotypePhenotype', 'plasmids',
            'selectionMarkers'));

        var plasmidSchema = angular.copy(dataSchema);
        plasmidSchema.splice.apply(plasmidSchema, [15, 0].concat('circular', 'backbone', 'promoters', 'replicatesIn',
            'originOfReplication', 'selectionMarkers'));

        var seedSchema = angular.copy(dataSchema);
        seedSchema.splice.apply(seedSchema, [15, 0].concat('homozygosity', 'ecotype', 'harvestDate', 'parents',
            'plantType', 'generation', 'sentToAbrc'));

        return {
            getDataSchema:function (type) {
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
            getTypeField:function (type, index) {
                return this.getDataSchema(type)[index];
            },

            // returns array of headers for specified type
            getSheetHeaders:function (type) {
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
            // todo : if index > length (or linkedType is valid)
            indexToRestResource:function (type, index) {
                var schema = this.getDataSchema(type);
                if (index == schema.indexOf('sequenceFileName'))
                    return 'sequence';

                if (index == schema.indexOf('attachments'))
                    return 'attachment';

                return 'attachment';
            }
        }
    });