'use strict';

angular.module('ice.upload.service', [])
    .factory('UploadUtil', function () {
        var plasmidHeaders, strainHeaders, seedHeaders;

        // headers todo : these need to be moved to the server (similar to what is being done for parts)
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
            , "Sequence File"
            , "Sequence Trace File(s)"
            , "Attachment File"];

        strainHeaders = angular.copy(partHeaders);
        strainHeaders.splice.apply(strainHeaders, [15, 0].concat(["Parental Strain", "Genotype or Phenotype", "Plasmids",
            "Selection Markers"]));

        plasmidHeaders = angular.copy(partHeaders);
        plasmidHeaders.splice.apply(plasmidHeaders, [15, 0].concat(["Circular", "Backbone", "Promoters", "Replicates In",
            "Origin of Replication", "Selection Markers"]));

        seedHeaders = angular.copy(partHeaders);
        seedHeaders.splice.apply(seedHeaders, [15, 0].concat(["Homozygosity", "Ecotype", "Harvest Date", "Parents",
            "Plant Type", "Generation", "Sent to ABRC?"]));

        var getTypeSchema = function (type) {
            var dataSchema;
            switch (type.toLowerCase()) {
                case "strain":
                    dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource', 'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription', 'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail', 'parentStrain', 'genotypePhenotype', 'plasmids', 'selectionMarkers', 'sequenceFilename', 'attachmentFilename'];
                    break;

                case "plasmid":
                    dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource', 'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription', 'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail', 'circular', 'backbone', 'promoters', 'replicatesIn', 'originOfReplication', 'selectionMarkers', 'sequenceFilename', 'attachmentFilename'];
                    break;

                case "arabidopsis":
                    dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource', 'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription', 'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail', 'homozygosity', 'ecotype', 'harvestDate', 'parents', 'plantType', 'generation', 'sentToAbrc', 'sequenceFilename', 'attachmentFilename'];
                    break;

                case "part":
                    dataSchema = ['principalInvestigator', 'principalInvestigatorEmail', 'fundingSource', 'intellectualProperty', 'bioSafetyLevel', 'name', 'alias', 'keywords', 'shortDescription', 'longDescription', 'references', 'links', 'status', 'creator', 'creatorEmail', 'sequenceFilename', 'attachmentFilename'];
                    break;
            }
            return dataSchema;
        };

        return {
            getDataSchema:function (type) {
                return getTypeSchema(type);
            },

            getTypeField:function (type, index) {  // returns field for the specified type at specified index
                return getTypeSchema(type)[index];
            },

            getSheetHeaders:function (type) { // returns array of headers for specified type
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
            }

        }
    });