'use strict';

angular.module('ice.entry.export.service', [])
    .factory('ExportFields', function () {
            const entryFields = {
                'PI': {label: "Principal Investigator"},
                'PI_EMAIL': {label: "Principal Investigator Email"},
                'PART_NUMBER': {label: "Part Number"},
                'FUNDING_SOURCE': {label: "Funding Source"},
                'IP': {label: "Intellectual Property"},
                'BIO_SAFETY_LEVEL': {label: "BioSafety Level"},
                'NAME': {label: "Name"},
                'ALIAS': {label: "Alias"},
                'KEYWORDS': {label: "Keywords"},
                'SUMMARY': {label: "Summary"},
                'NOTES': {label: "Notes"},
                'REFERENCES': {label: "References"},
                'LINKS': {label: "Links"},
                'STATUS': {label: "Status"},
                'CREATOR': {label: "Creator"},
                'CREATOR_EMAIL': {label: "Creator Email"},

                // todo : files
                // 'SEQ_FILENAME': {label: "Sequence File"},
                // 'ATT_FILENAME': {label: "Attachment File"},
                // 'SEQ_TRACE_FILES': {label: "Sequence Trace File(s)"},
            };

            const strainFields = {
                'HOST': {label: "Host"},
                'GENOTYPE_OR_PHENOTYPE': {label: "Genotype or Phenotype"},
                'SELECTION_MARKERS': {label: "Selection Markers"}
            };

            const plasmidFields = {
                'CIRCULAR': {label: "Circular"},
                'BACKBONE': {label: "Backbone"},
                'PROMOTERS': {label: "Promoters"},
                'REPLICATES_IN': {label: "Replicates In"},
                'ORIGIN_OF_REPLICATION': {label: "Origin of Replication"},
                'SELECTION_MARKERS': {label: "Selection Markers"}
            };

            const proteinFields = {
                'ORGANISM': {label: "Organism"},
                'FULL_NAME': {label: "Full Name"},
                'GENE_NAME': {label: "Gene Name"},
                'UPLOADED_FROM': {label: "Uploaded From"}
            };

            const seedFields = {
                'HOMOZYGOSITY': {label: "Homozygosity"},
                'ECOTYPE': {label: "Ecotype"},
                'HARVEST_DATE': {label: "Harvest Date"},
                'GENERATION': {label: "Generation"},
                'SENT_TO_ABRC': {label: "Sent to ABRC?"},
                'PLANT_TYPE': {label: "Plant Type"},
                'PARENTS': {label: "Parents"},
                'SELECTION_MARKERS': {label: "Selection Markers"}
            };

            const fields = {
                general: entryFields,
                plasmid: plasmidFields,
                strain: strainFields,
                seed: seedFields,
                protein: proteinFields
            };

            return {
                fields: function () {
                    return fields;
                },
            }
        }
    );